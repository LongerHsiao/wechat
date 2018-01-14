package common.wechat;

import java.io.Serializable;
import java.util.Date;

/**
 * @author LongerHsiao
 * @date 2017-11-8
 */
public class OAuthUserInfo implements Serializable {
    private static final long serialVersionUID = -252836877741734103L;
    private String openid;
    private String accessToken;
    private int expiredSecond;
    private String refreshToken;
    private Date timestamp;

    public boolean isExpired() {
        return System.currentTimeMillis() - timestamp.getTime() >= expiredSecond * 1000;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        setTimestamp();
    }

    public void setExpiredSecond(int expiredSecond) {
        this.expiredSecond = expiredSecond;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    private void setTimestamp() {
        this.timestamp = new Date();
    }

    @Override
    public String toString() {
        return "OAuthUserInfo{" +
                "openid='" + openid + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", expiredSecond=" + expiredSecond +
                ", refreshToken='" + refreshToken + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
