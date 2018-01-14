package common.wechat.message;

import common.wechat.IWeChatMessage;
import common.wechat.util.XmlUtils;

public class ReplyMessage implements IWeChatMessage {
	protected String openid;

	public String toXML() {
		return XmlUtils.ObjectToXML(this);
	}

	@Override
	public String getOpenid() {
		return openid;
	}
}