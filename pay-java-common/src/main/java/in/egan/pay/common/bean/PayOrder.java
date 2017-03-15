package in.egan.pay.common.bean;

import java.math.BigDecimal;

/**
 * 支付订单信息
 *
 * @author egan
 * @email egzosn@gmail.com
 * @date 2016/10/19 22:34
 */
public class PayOrder {
    //商品名称
    private String subject;
    //商品描述
    private String body;
    //价格
    private BigDecimal price;
    //商户单号
    private String tradeNo;
    //银行卡类型
    private String bankType;
    //设备号
    private String deviceInfo;
    //交易类型
    private TransactionType transactionType;
    //支付币种
    private CurType curType;


    public CurType getCurType() {
        return curType;
    }

    public void setCurType(CurType curType) {
        this.curType = curType;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public String getBankType() {
        return bankType;
    }

    public void setBankType(String bankType) {
        this.bankType = bankType;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public PayOrder() {
    }


    public PayOrder(String subject, String body, BigDecimal price, String tradeNo, TransactionType transactionType) {

        this.subject = subject;
        this.body = body;
        this.price = price;
        this.tradeNo = tradeNo;
        this.transactionType = transactionType;
    }



}
