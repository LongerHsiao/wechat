package common.wechat.message;

import common.wechat.util.XmlUtils;

public class ReplyMessage implements IWeChatMessage {
	protected String openid;

	public String toXML() {
		return XmlUtils.toXml(this);
	}

	@Override
	public String getOpenid() {
		return openid;
	}
}