package common.wechat.message;

import common.wechat.IWeChatMessage;
import common.wechat.util.XmlUtils;

/**
 * @author LongerHsiao
 * @date 2017-12-5
 */
public class WeChatReplyMessage implements IWeChatMessage {
    protected String openid;

    public String toXML() {
        return XmlUtils.ObjectToXML(this);
    }

    @Override
    public String getOpenid() {
        return openid;
    }
}
