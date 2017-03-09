package app.game.service;

import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Singleton;
import com.mokylin.sink.util.Utils;
import com.mokylin.sink.util.concurrent.DisruptorExecutor;
import com.mokylin.sink.util.concurrent.PaddedAtomicInteger;

@Singleton
public class ThreadServiceImpl implements IThreadService{

    @VisibleForTesting
    final DisruptorExecutor[] gameExec;

    @VisibleForTesting
    final ExecutorService dbExec;

    private final ScheduledThreadPoolExecutor scheduledExec;

    @VisibleForTesting
    final int GAME_THREAD_COUNT_TO_MOD;

    private final DisruptorExecutor[] gameExecArrayCopy;

    public ThreadServiceImpl(){
        // game exec
        int threadCount = Utils.getClosestPowerOf2(Utils.CORE_NUM) * 2;

        if (threadCount <= 0){
            throw new RuntimeException("core num * 2 <= 0 ?");
        }

        GAME_THREAD_COUNT_TO_MOD = threadCount - 1;

        gameExec = new DisruptorExecutor[threadCount];

        for (int i = 0; i < gameExec.length; i++){
            gameExec[i] = new DisruptorExecutor(i, 8192, "GAME_WORKER_" + i);
        }

        gameExecArrayCopy = Arrays.copyOf(gameExec, gameExec.length);

        // db exec
        int dbThreadCount = threadCount;

        dbExec = new ThreadPoolExecutor(dbThreadCount, dbThreadCount * 2, 60L,
                TimeUnit.SECONDS, new LinkedTransferQueue<Runnable>(),
                new ThreadFactory(){

                    private final PaddedAtomicInteger idCounter = new PaddedAtomicInteger();

                    @Override
                    public Thread newThread(Runnable r){
                        Thread result = new Thread(r, "DB_WORKER_"
                                + idCounter.incrementAndGet());
                        result.setPriority(Thread.NORM_PRIORITY);
                        return result;
                    }
                });

        // scheduled exec
        int scheduledThreadCount = 8;

        scheduledExec = new ScheduledThreadPoolExecutor(scheduledThreadCount,
                new ThreadFactory(){

                    private final PaddedAtomicInteger idCounter = new PaddedAtomicInteger();

                    @Override
                    public Thread newThread(Runnable r){
                        Thread result = new Thread(r, "UPDATE_WORKER_"
                                + idCounter.incrementAndGet());
                        result.setPriority(Thread.MAX_PRIORITY);
                        return result;
                    }
                });
        scheduledExec.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
    }

    public DisruptorExecutor[] getExecutorsArrayCopy(){
        return gameExecArrayCopy;
    }

    /*
     * (non-Javadoc)
     * 
     * @see app.game.service.ThreadService#getExecutor(int)
     */
    @Override
    public DisruptorExecutor getExecutor(int id){
        return gameExec[id & GAME_THREAD_COUNT_TO_MOD];
    }

    /*
     * (non-Javadoc)
     * 
     * @see app.game.service.ThreadService#getDbExecutor()
     */
    @Override
    public Executor getDbExecutor(){
        return dbExec;
    }

    @Override
    public ScheduledExecutorService getScheduledExecutorService(){
        return scheduledExec;
    }

    /*
     * (non-Javadoc)
     * 
     * @see app.game.service.ThreadService#shutDown()
     */
    @Override
    public void close(){
        // 关闭顺序必须是schedule, gameExec, dbExec
        // schedule可能会放任务到gameExec
        // gameExec可能会放任务到dbExec
        shutDownUpdateExec();
        shutDownGameExec();
        shutDownDbExec();
    }

    void shutDownUpdateExec(){
        scheduledExec.shutdown();

        try{
            scheduledExec.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e){
            System.err.println("关闭updateExec时出错");
            e.printStackTrace();
        }
    }

    void shutDownGameExec(){
        for (DisruptorExecutor exec : gameExec){
            exec.shutdown();
        }
    }

    void shutDownDbExec(){
        dbExec.shutdown();

        try{
            dbExec.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e){
            System.err.println("关闭dbExec时出错");
            e.printStackTrace();
        }
    }
}
