package common.wechat;

/**
 * @author LongerHsiao
 * @date 2018-01-13
 */
public class JsTicket {
    private JsTicket() {

    }

    private static class JsTicketHolder {
        public static final JsTicket INSTANCE = new JsTicket();
    }

    public static JsTicket getInstance() {
        return JsTicketHolder.INSTANCE;
    }

    public boolean isExpired() {
        return (System.currentTimeMillis() - this.timestamp) >= expiresSecond * 1000;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
        refreshTimeStamp();
    }

    public void setExpiresSecond(int expiresSecond) {
        this.expiresSecond = expiresSecond;
    }

    private void refreshTimeStamp() {
        this.timestamp = System.currentTimeMillis();
    }

    private String ticket;
    private long timestamp;
    private int expiresSecond;
}
