package common.wechat;

import java.io.Serializable;

/**
 * @author LongerHsiao
 * @date 2018-01-14
 */
public class Signature implements Serializable {
    private static final long serialVersionUID = -743480065331906044L;
    private String appId;
    private String nonceStr;
    private String timestamp;
    private String signature;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getNonceStr() {
        return nonceStr;
    }

    public void setNonceStr(String nonceStr) {
        this.nonceStr = nonceStr;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
