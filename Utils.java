package com.mokylin.sink.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Random;
import java.util.concurrent.ConcurrentMap;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;

import jersey.repackaged.jsr166e.ConcurrentHashMapV8;

import org.apache.commons.lang.StringUtils;
import org.iq80.snappy.Snappy;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.google.common.io.Files;
import com.mokylin.collection.ByteArrayBuilder;
import com.mokylin.collection.IntArrayList;
import com.twmacinta.util.MD5;

public class Utils{

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    private Utils(){
    }

    public static final int CORE_NUM = Runtime.getRuntime()
            .availableProcessors();

    private static final Random rand = new Random(System.currentTimeMillis());

    public static <K, V> ConcurrentMap<K, V> newConcurrentMap(){
        return new ConcurrentHashMapV8<>();
    }

    public static void printStackTrace(PrintStream ps){
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        for (int i = 0; i < trace.length; i++)
            ps.println("\tat  " + trace[i]);
    }

    public static void printStackTraceToError(Logger logger){
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        for (int i = 0; i < trace.length; i++)
            logger.error("\tat ->" + trace[i]);
    }

    public static String getSelfIP() throws SocketException{
        Enumeration<NetworkInterface> interfaces = NetworkInterface
                .getNetworkInterfaces();
        String result = null;
        while (interfaces.hasMoreElements()){
            NetworkInterface current = interfaces.nextElement();
            if (!current.isUp() || current.isLoopback() || current.isVirtual())
                continue;
            Enumeration<InetAddress> addresses = current.getInetAddresses();
            while (addresses.hasMoreElements()){
                InetAddress current_addr = addresses.nextElement();
                if (current_addr.isLoopbackAddress())
                    continue;
                if (current_addr instanceof Inet4Address){
                    result = current_addr.getHostAddress();
                    if (result.startsWith("10.")){
                        continue;
                    }

                    return result;
                }
            }
        }
        return result;
    }

    public static String getStackTrace(){
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();

        StringBuilder sb = new StringBuilder(128);
        for (StackTraceElement e : trace){
            sb.append("\n\tat ");
            sb.append(e);
        }
        return sb.toString();
    }

    public static int getPointWithRange(int low, int high, int value){
        if (value >= high){
            return high;
        }
        if (value <= low){
            return low;
        }
        return value;
    }

    public static byte[] randomByteArray(int len){
        byte[] result = new byte[len];
        rand.nextBytes(result);
        return result;
    }

    public static int min(int a, int b, int c){
        int min = a;
        if (b < min){
            min = b;
        }
        if (c < min){
            min = c;
        }
        return min;
    }

    public static boolean isLongFitsInInt(long x){
        return (int) x == x;
    }

    public static boolean isInEasyRange(int x1, int y1, int x2, int y2,
            int range){
        return Math.abs(x1 - x2) <= range && Math.abs(y1 - y2) <= range;
    }

    public static int getEasyRange(int x1, int y1, int x2, int y2){
        return Math.max(Math.abs(x1 - x2), Math.abs(y1 - y2));
    }

    public static int getHighShort(int i){
        return i >>> 16;
    }

    public static int getLowShort(int i){
        return i & 0xffff;
    }

    public static int short2Int(int high, int low){
        return (high << 16) | low;
    }

    public static long int2Long(int high, int low){
        return (((long) high) << 32) | low;
    }

    public static int getLowInt(long l){
        return (int) l;
    }

    public static int getHighInt(long l){
        return (int) (l >>> 32);
    }

    /**
     * 获得第一个比x大的2的倍数. 如果x是0, 返回也是0
     * 
     * @param x
     * @return
     */
    public static int getClosestPowerOf2(int x){
        x--;
        x |= x >> 1;
        x |= x >> 2;
        x |= x >> 4;
        x |= x >> 8;
        x |= x >> 16;
        x++;
        return x;
    }

    public static boolean isPowerOf2(int x){
        return (x != 0) && ((x & (x - 1)) == 0);
    }

    /**
     * 在ai中找key，如果存在，返回该idx，如果不存在，返回第一个大于key的idx，如果key大于ai中最大值，返回ai.length -
     * 1（ai升序排列）
     */
    public static int binarySearchForCeilingKey(int[] ai, int key){
        int low = 0;
        int high = ai.length - 1;

        while (low <= high){
            int mid = (low + high) >>> 1;
            int midVal = ai[mid];

            if (midVal < key)
                low = mid + 1;
            else if (midVal > key)
                high = mid - 1;
            else
                return mid;
        }

        return low == ai.length ? low - 1 : low;
    }

    /**
     * 在ai中找key，如果存在，返回该idx，如果不存在，返回第一个小于key的idx，如果key小于ai中的最小值，返回0（ai升序排列）
     */
    public static int binarySearchForFloorKey(int[] ai, int key){
        int low = 0;
        int high = ai.length - 1;

        while (low <= high){
            int mid = (low + high) >>> 1;
            int midVal = ai[mid];

            if (midVal < key)
                low = mid + 1;
            else if (midVal > key)
                high = mid - 1;
            else
                return mid;
        }

        return low == 0 ? 0 : low - 1;
    }

    public static byte[] readFile(String s) throws IOException{
        return readFile(new File(s));
    }

    public static byte[] readFile(File file) throws IOException{
        if (!file.exists() || file.isDirectory())
            return null;
        return Files.toByteArray(file);
    }

    /**
     * 将当前流中能读入的数据全部读入
     * @param in
     * @return
     */
    public static byte[] readFile(InputStream in){
        if (in == null){
            return null;
        }
        try{
            //只读能读到的字节,如果要读全部数据,要循环读
            byte abyte0[] = new byte[in.available()];
            in.read(abyte0);
            in.close();
            return abyte0;
        } catch (IOException ioexception){
            ioexception.printStackTrace();
        }
        return null;
    }

    public static byte[] readGzipFile(File file){
        if (!file.exists() || file.isDirectory())
            return null;
        try (InputStream in = new FileInputStream(file);
             GZIPInputStream is = new GZIPInputStream(in);){
            ByteArrayBuilder builder = new ByteArrayBuilder(8192 * 1024);

            byte abyte0[] = new byte[2048 * 1024];
            int length = 0;
            while ((length = is.read(abyte0)) != -1){
                builder.append(Arrays.copyOf(abyte0, length));
            }
            return builder.toByteArray();
        } catch (IOException ioexception){
            ioexception.printStackTrace();
        }
        return null;
    }

    /**
     * 
     * @param inputStream
     * @return
     */
    public static byte[] readGzipFile(InputStream inputStream){
        try (GZIPInputStream is = new GZIPInputStream(inputStream)){
            ByteArrayBuilder builder = new ByteArrayBuilder(8192 * 1024);

            byte abyte0[] = new byte[2048 * 1024];
            int length = 0;
            while ((length = is.read(abyte0)) != -1){
                builder.append(Arrays.copyOf(abyte0, length));
            }
            return builder.toByteArray();
        } catch (IOException ioexception){
            throw new RuntimeException(ioexception);
        }
    }

    /**
     * 从ClassLoader查找指定资源文件
     * @param sourcePath  资源路径
     * @return byte[]
     * @throws IOException 
     */
    public static InputStream getInputStreamFromClassPath(String sourcePath)
            throws IOException{
        assert sourcePath != null && sourcePath.length() > 0;
        return ClassLoader.getSystemResourceAsStream(sourcePath);
    }

    public static String stripSuffix(String input){
        int pos = input.lastIndexOf(".");
        if (pos < 0){
            return input;
        }
        if (pos == 0){
            return "";
        }
        return input.substring(0, pos);
    }

    public static String[] split(String s, String s1){
        return StringUtils.splitByWholeSeparatorPreserveAllTokens(s, s1);
    }

    public static String md5(String s){
        MD5 md5 = new MD5();
        md5.Update(s);
        return md5.asHex();
    }

    public static String json(Object object){
        return JSON.toJSONString(object);
    }

    public static byte[] zlibCompress(byte[] input){
        int maxSize = ((int) Math.ceil(input.length * 1.001)) + 44;
        byte[] output = new byte[maxSize];
        Deflater de = new Deflater(Deflater.BEST_COMPRESSION);
        de.setInput(input);
        de.finish();
        int size = de.deflate(output);
        return Arrays.copyOf(output, size);
    }

    public static byte[] zlibUncompress(byte[] data){
        ByteArrayBuilder result = new ByteArrayBuilder(8192);

        byte[] buf = new byte[1024];
        Inflater ift = new Inflater();
        ift.setInput(data);
        try{
            while (!ift.finished()){
                int size = ift.inflate(buf);
                if (size == 0){
                    throw new IllegalArgumentException("inflate return size 0");
                }
                result.append(Arrays.copyOf(buf, size));
            }
        } catch (DataFormatException ex){
            throw new IllegalArgumentException("not a zlib format");
        } finally{
            ift.end();
        }

        return result.toByteArray();
    }

    public static long calculateMillisToMidnight(long ctime){
        DateTime now = new DateTime(ctime);
        long lastMidnight = new DateTime(now.getYear(), now.getMonthOfYear(),
                now.getDayOfMonth(), 0, 0).getMillis();
        return lastMidnight + DateTimeConstants.MILLIS_PER_DAY
                - now.getMillis();
    }

    public static long calculateDelay(int initialHour, int initialMinute,
            long ctime){
        DateTime cdt = new DateTime(ctime);
        DateTime todayTarget = cdt.withTime(initialHour, initialMinute, 0, 0);
        if (todayTarget.isBefore(ctime)){
            todayTarget = todayTarget.plusDays(1);
        }

        return todayTarget.getMillis() - ctime;
    }

    public static byte[] snappyCompress(byte[] data){
        int maxCompressedSize = Snappy.maxCompressedLength(data.length);
        byte[] compressed = new byte[maxCompressedSize];
        int compressedSize = Snappy.compress(data, 0, data.length, compressed,
                0);

        if (compressedSize == maxCompressedSize){
            return compressed;
        }
        byte[] result = new byte[compressedSize];
        System.arraycopy(compressed, 0, result, 0, compressedSize);
        return result;
    }

    public static byte[] snappyUncompress(byte[] data){
        return Snappy.uncompress(data, 0, data.length);
    }

    /**
     * 返回数字最小的index, 有相同数字的话, 优先返回前面的
     * 
     * @param array
     * @return
     */
    public static int getMinIndex(int[] array){
        assert array.length > 0;
        int min = array[0];
        int result = 0;
        for (int i = 1; i < array.length; i++){
            if (array[i] < min){
                min = array[i];
                result = i;
            }
        }
        return result;
    }

    public static int divide(int count, int maxCount){
        return (count + maxCount - 1) / maxCount;
    }

    public static int divide(long count, long maxCount){
        return (int) ((count + maxCount - 1) / maxCount);
    }

    public static int square(int i){
        return i * i;
    }

    public static boolean isValidName(String s){
        for (int j = s.length(); --j >= 0;){
            char c = s.charAt(j);

            if (c <= ' '){
                return false;
            }

            if (c == '\ue779'){ // windows下显示为空格
                return false;
            }

            if (c == '[' || c == ']'){ // 区服信息放在[]中间
                return false;
            }
        }

        return true;
    }

    public static int getStringLength(byte[] b_name){
        int len = 0; //定义返回的字符串长度
        int j = 0;
        int limit = b_name.length - 1;
        while (j <= limit){
            short tmpst = (short) (b_name[j] & 0xF0);
            if (tmpst >= 0xB0){
                if (tmpst < 0xC0){
                    j += 2;
                    len += 2;
                } else if ((tmpst == 0xC0) || (tmpst == 0xD0)){
                    j += 2;
                    len += 2;
                } else if (tmpst == 0xE0){
                    j += 3;
                    len += 2;
                } else if (tmpst == 0xF0){
                    short tmpst0 = (short) (((short) b_name[j]) & 0x0F);
                    if (tmpst0 == 0){
                        j += 4;
                        len += 2;
                    } else if ((tmpst0 > 0) && (tmpst0 < 12)){
                        j += 5;
                        len += 2;
                    } else if (tmpst0 > 11){
                        j += 6;
                        len += 2;
                    }
                }
            } else{
                j += 1;
                len += 1;
            }
        }
        return len;
    }

    public static byte[] getServerIDBytes(int serverID){
        String s = String.valueOf(serverID);
        return StringEncoder.encode(s);
    }

    public static boolean hasDuplicate(String[] array){
        int len = array.length;
        for (int i = 0; i < len; i++){
            String s = array[i];
            for (int j = i + 1; j < len; j++){
                if (array[j].equals(s)){
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasDuplicate(int[] array){
        int len = array.length;
        for (int i = 0; i < len; i++){
            int s = array[i];
            for (int j = i + 1; j < len; j++){
                if (array[j] == s){
                    return true;
                }
            }
        }
        return false;
    }

    // ---- 随机字符串 ----

    private static final char[] symbols = new char[36];

    static{
        for (int idx = 0; idx < 10; ++idx)
            symbols[idx] = (char) ('0' + idx);
        for (int idx = 10; idx < 36; ++idx)
            symbols[idx] = (char) ('a' + idx - 10);
    }

    public static String getRandomString(int length){
        char[] buf = new char[length];
        for (int i = 0; i < buf.length; ++i){
            buf[i] = symbols[RandomNumber.getRate(symbols.length)];
        }
        return new String(buf);
    }

    public static <V> V getValidObject(V[] array, int idx){
        if (idx >= 0 && idx < array.length){
            return array[idx];
        }

        if (idx < 0){
            return array[0];
        }

        return array[array.length - 1];
    }

    public static int getValidInteger(int[] array, int idx){
        if (idx >= 0 && idx < array.length){
            return array[idx];
        }

        if (idx < 0){
            return array[0];
        }

        return array[array.length - 1];
    }

    public static int safeParseInt(String str, int defaultValue){
        try{
            return Integer.parseInt(str);
        } catch (Exception e){
        }

        return defaultValue;
    }

    public static int parseInt(Object master, String str){
        try{
            return Integer.parseInt(str);
        } catch (NumberFormatException e){
            throw new IllegalArgumentException(master + " 解析数值时出错, " + str);
        }
    }

    public static String decodeURL(String data){
        try{
            return URLDecoder.decode(data, StringEncoder.UTF_8_NAME);
        } catch (UnsupportedEncodingException e){
            throw new RuntimeException(e);
        }
    }

    public static String decodeURL(byte[] data){
        return decodeURL(StringEncoder.encode(data));
    }

    public static long calculateTPS(int count, long nanos){
        return (count * 10_0000_0000L) / nanos;
    }

    public static int getAngleBetween(int d1, int d2){
        return getInferiorAngle(d1 - d2);
    }

    /**
     * 劣角（小于180度的角）
     * @param degree
     * @return
     */
    private static int getInferiorAngle(int degree){
        int t = Math.abs(degree);
        if (t > 180){
            return 360 - t;
        } else{
            return t;
        }
    }

    public static int getDirection(int baseX, int baseY, int targetX,
            int targetY, int lastDirection){

        if (baseX == targetX && baseY == targetY){
            return lastDirection;
        }

        int degree = (int) Math.toDegrees(Math.atan2(baseY - targetY, targetX
                - baseX));

        if (degree < 0)
            degree = 360 - ((-degree) % 360); // 转成正角度

        // 角度转成8方向

        int direction = ((degree + 23) / 45) & 7;

        return direction;  // flash 的坐标的y是反的
    }

//    public static String randomUUID(){
//        return UUID.randomUUID().toString();
//    }

    /**
     * 传说中的彩票随机器
     */
    public static void randomRoundIndex(IntArrayList indexList, int maxCount,
            int count){
        if (maxCount < count)
            throw new IllegalArgumentException(
                    "randomRoundIndex error. maxCount < count");

        indexList.clear();
        for (int i = 0; i < maxCount; i++){
            indexList.add(i);
        }

        int maxIdx = maxCount;
        for (int i = 0; i < count; i++){
            int idx = RandomNumber.getRate(maxIdx--, true);
            if (idx > 0){
                indexList.set(i, indexList.set(idx + i, indexList.get(i)));
            }
        }
    }

    public static void closeQuietly(Closeable closeable){
        try{
            closeable.close();
        } catch (Throwable e){
            logger.error("Error closing closable", e);
        }
    }
}
