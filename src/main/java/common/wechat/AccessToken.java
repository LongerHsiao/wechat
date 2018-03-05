package common.wechat;

/**
 * @author LongerHsiao
 * @date 2017-11-8
 */
class AccessToken {
    private AccessToken() {

    }

    private static class AccessTokenHolder {
        private static final AccessToken INSTANCE = new AccessToken();
    }

    public static AccessToken getInstance() {
        return AccessTokenHolder.INSTANCE;
    }

    public boolean isExpired() {
        return (System.currentTimeMillis() - this.timestamp) >= expiredSecond * 1000;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        refreshTimestamp();
    }

    private void refreshTimestamp() {
        this.timestamp = System.currentTimeMillis();
    }

    public void setExpiredSecond(int expiredSecond) {
        this.expiredSecond = expiredSecond;
    }

    private String accessToken;
    private long timestamp;
    private int expiredSecond;
}
