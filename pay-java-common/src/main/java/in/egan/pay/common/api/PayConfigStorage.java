package in.egan.pay.common.api;

import in.egan.pay.common.bean.MsgType;

import java.util.concurrent.locks.Lock;

/**
 * 支付客户端配置存储
 * @author  egan
 * @email egzosn@gmail.com
 * @date 2016-5-18 14:09:01
 * @source chanjarster/weixin-java-tools
 */
 public interface PayConfigStorage {

    /*
     *  应用id
     */
     String getAppid();
    /**
     * 合作商唯一标识
     */
     String getPartner();
    /**
     * 获取收款账号
     */
     String getSeller();

    /**
     * 授权令牌
     */
     String getToken();


    /**
     * 服务端异步回调Url
     */
     String getNotifyUrl();
    /**
     * 服务端同步回调Url
     */
     String getReturnUrl();
    /**
    * 签名方式
     */
     String getSignType();
    // 字符编码格式 目前支持 gbk 或 utf-8
     String getInputCharset();
    /**
     *  获取密钥 与 #getKeyPrivate 类似
     */
     String getSecretKey();

    /**
     * 公钥
     * @return
     */
     String getKeyPublic();

    /**
     * 私钥
     * @return
     */
     String getKeyPrivate();

    /**
     * 支付类型 自定义
     * 这里暂定 aliPay 支付宝， wxPay微信支付
     * @return
     */
     String getPayType();

    /**
     * 消息类型
     *  @see #getMsgType
     *  @see MsgType
     * @return "text" 或者 "xml"
     * @see MsgType#text
     * @see MsgType#xml
     * @see MsgType#json
     */
     MsgType getMsgType();


    /**
     * 获取访问令牌
     * @return
     */
    String getAccessToken();

    /**
     * 访问令牌是否过期
     * @return
     */
    boolean isAccessTokenExpired();
    /**
     * 获取access token锁
     * @return
     */
    Lock getAccessTokenLock();

    /**
     * 强制将access token过期掉
     */
    void expireAccessToken();
    /**
     * 强制将access token过期掉
     */
    long getExpiresTime();

    /**
     * 应该是线程安全的
     * @param accessToken 新的accessToken值
     * @param expiresInSeconds 过期时间，以秒为单位 多少秒
     */
    void updateAccessToken(String accessToken, int expiresInSeconds);

    /**
     * 应该是线程安全的
     * @param accessToken 新的accessToken值
     * @param expiresTime 过期时间，时间戳
     */
    void updateAccessToken(String accessToken, long expiresTime);


    /**
     * http代理地址
     * @return
     */
     String getHttpProxyHost();

    /**
     *   代理端口
     * @return
     */
    int getHttpProxyPort();

    /**
     * 代理用户名
     * @return
     */
     String getHttpProxyUsername();

    /**
     *  代理密码
     * @return
     */
     String getHttpProxyPassword();

}
