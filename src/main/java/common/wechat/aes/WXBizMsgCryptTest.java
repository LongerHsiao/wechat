package common.wechat.aes;

public class WXBizMsgCryptTest {
	String encodingAesKey = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFG";
	String token = "pamtest";
	String timestamp = "1409304348";
	String nonce = "xxxxxx";
	String appId = "wxb11529c136998cb6";
	String replyMsg = "我是中文abcd123";
	String xmlFormat = "<xml><ToUserName><![CDATA[toUser]]></ToUserName><Encrypt><![CDATA[%1$s]]></Encrypt></xml>";
	String afterAesEncrypt = "jn1L23DB+6ELqJ+6bruv21Y6MD7KeIfP82D6gU39rmkgczbWwt5+3bnyg5K55bgVtVzd832WzZGMhkP72vVOfg==";
	String randomStr = "aaaabbbbccccdddd";

	String replyMsg2 = "<xml><ToUserName><![CDATA[oia2Tj我是中文jewbmiOUlr6X-1crbLOvLw]]></ToUserName><FromUserName><![CDATA[gh_7f083739789a]]></FromUserName><CreateTime>1407743423</CreateTime><MsgType><![CDATA[video]]></MsgType><Video><MediaId><![CDATA[eYJ1MbwPRJtOvIEabaxHs7TX2D-HV71s79GUxqdUkjm6Gs2Ed1KF3ulAOA9H1xG0]]></MediaId><Title><![CDATA[testCallBackReplyVideo]]></Title><Description><![CDATA[testCallBackReplyVideo]]></Description></Video></xml>";
	String afterAesEncrypt2 = "jn1L23DB+6ELqJ+6bruv23M2GmYfkv0xBh2h+XTBOKVKcgDFHle6gqcZ1cZrk3e1qjPQ1F4RsLWzQRG9udbKWesxlkupqcEcW7ZQweImX9+wLMa0GaUzpkycA8+IamDBxn5loLgZpnS7fVAbExOkK5DYHBmv5tptA9tklE/fTIILHR8HLXa5nQvFb3tYPKAlHF3rtTeayNf0QuM+UW/wM9enGIDIJHF7CLHiDNAYxr+r+OrJCmPQyTy8cVWlu9iSvOHPT/77bZqJucQHQ04sq7KZI27OcqpQNSto2OdHCoTccjggX5Z9Mma0nMJBU+jLKJ38YB1fBIz+vBzsYjrTmFQ44YfeEuZ+xRTQwr92vhA9OxchWVINGC50qE/6lmkwWTwGX9wtQpsJKhP+oS7rvTY8+VdzETdfakjkwQ5/Xka042OlUb1/slTwo4RscuQ+RdxSGvDahxAJ6+EAjLt9d8igHngxIbf6YyqqROxuxqIeIch3CssH/LqRs+iAcILvApYZckqmA7FNERspKA5f8GoJ9sv8xmGvZ9Yrf57cExWtnX8aCMMaBropU/1k+hKP5LVdzbWCG0hGwx/dQudYR/eXp3P0XxjlFiy+9DMlaFExWUZQDajPkdPrEeOwofJb";


//	@Test
//	public void testNormal() throws ParserConfigurationException, SAXException, IOException {
//		try {
//			WXBizMsgCrypt pc = new WXBizMsgCrypt(token, encodingAesKey, appId);
//			String afterEncrpt = pc.encryptMsg(replyMsg, timestamp, nonce);
//
//			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//			DocumentBuilder db = dbf.newDocumentBuilder();
//			StringReader sr = new StringReader(afterEncrpt);
//			InputSource is = new InputSource(sr);
//			Document document = db.parse(is);
//
//			Element root = document.getDocumentElement();
//			NodeList nodelist1 = root.getElementsByTagName("Encrypt");
//			NodeList nodelist2 = root.getElementsByTagName("MsgSignature");
//
//			String encrypt = nodelist1.item(0).getTextContent();
//			String msgSignature = nodelist2.item(0).getTextContent();
//			String fromXML = String.format(xmlFormat, encrypt);
//
//			// 第三方收到公众号平台发送的消息
//			String afterDecrpt = pc.decryptMsg(msgSignature, timestamp, nonce, fromXML);
//			assertEquals(replyMsg, afterDecrpt);
//		} catch (AesException e) {
//				fail("正常流程，怎么就抛出异常了？？？？？？");
//		}
//	}
//
//	@Test
//	public void testAesEncrypt() {
//		try {
//			WXBizMsgCrypt pc = new WXBizMsgCrypt(token, encodingAesKey, appId);
//			assertEquals(afterAesEncrypt, pc.encrypt(randomStr, replyMsg));
//		} catch (AesException e) {
//			e.printStackTrace();
//			fail("no异常");
//		}
//	}
//
//	@Test
//	public void testAesEncrypt2() {
//		try {
//			WXBizMsgCrypt pc = new WXBizMsgCrypt(token, encodingAesKey, appId);
//			assertEquals(afterAesEncrypt2, pc.encrypt(randomStr, replyMsg2));
//
//		} catch (AesException e) {
//			e.printStackTrace();
//			fail("no异常");
//		}
//	}
//
//	@Test
//	public void testIllegalAesKey() {
//		try {
//			new WXBizMsgCrypt(token, "abcde", appId);
//		} catch (AesException e) {
//			assertEquals(AesException.IllegalAesKey, e.getCode());
//			return;
//		}
//		fail("错误流程不抛出异常？？？");
//	}
//
//	@Test
//	public void testValidateSignatureError() throws ParserConfigurationException, SAXException,
//			IOException {
//		try {
//			WXBizMsgCrypt pc = new WXBizMsgCrypt(token, encodingAesKey, appId);
//			String afterEncrpt = pc.encryptMsg(replyMsg, timestamp, nonce);
//			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//			DocumentBuilder db = dbf.newDocumentBuilder();
//			StringReader sr = new StringReader(afterEncrpt);
//			InputSource is = new InputSource(sr);
//			Document document = db.parse(is);
//
//			Element root = document.getDocumentElement();
//			NodeList nodelist1 = root.getElementsByTagName("Encrypt");
//
//			String encrypt = nodelist1.item(0).getTextContent();
//			String fromXML = String.format(xmlFormat, encrypt);
//			pc.decryptMsg("12345", timestamp, nonce, fromXML); // 这里签名错误
//		} catch (AesException e) {
//			assertEquals(AesException.ValidateSignatureError, e.getCode());
//			return;
//		}
//		fail("错误流程不抛出异常？？？");
//	}
//
//	@Test
//	public void testVerifyUrl() throws AesException {
//        String token = "12039475";
//        String encodingAesKey = "ziZH0Kzm4wvgR6dK6r3mMDA30VKX9LVKuhT4UcBWLVs";
//        String appId = "wx0c8e33f9b2ea2508";
//		WXBizMsgCrypt wxcpt = new WXBizMsgCrypt(token,
//				encodingAesKey, appId);
//		String verifyMsgSig = "3f5de45cb6ee46a01da0afbc83f01b1b261b1b55";
//		String timeStamp = "1512453034";
//		String nonce = "2579726205";
//		String echoStr = "7878091623650277291";
//		wxcpt.verifyUrl(verifyMsgSig, timeStamp, nonce, echoStr);
//		// 只要不抛出异常就好
//	}
}
