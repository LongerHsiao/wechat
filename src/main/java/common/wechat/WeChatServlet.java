package common.wechat;

import com.alibaba.fastjson.JSONObject;
import common.tools.StringUtil;
import common.wechat.aes.AesException;
import common.wechat.aes.WXBizMsgCrypt;
import common.wechat.util.ConfigBuilder;
import common.wechat.util.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author LongerHsiao
 * @date 2017-12-5
 */
/**
 * @api WeChatServlet 微信消息处理服务
 * @apiGroup WeChat_Component
 * @apiName WeChatServlet
 * @apiVersion 1.0.0
 * @apiDescription 在需要使用网页授权的应用中接入此拦截器, 需要配置state和redirectHost参数。<br/>
 * 其中,state参数可对回调地址进行校验,也可置空;<br/>
 * redirectHost参数为授权回调域名,关于网页授权回调域名的说明：<br/>
 * 1、在微信公众号请求用户网页授权之前，开发者需要先到公众平台官网中的“开发 - 接口权限 - 网页服务 - 网页帐号 - 网页授权获取用户基本信息”的配置选项中，修改授权回调域名。请注意，这里填写的是域名（是一个字符串），而不是URL，因此请勿加 http:// 等协议头；<br/>
 * 2、授权回调域名配置规范为全域名，比如需要网页授权的域名为：www.qq.com，配置以后此域名下面的页面http://www.qq.com/music.html 、 http://www.qq.com/login.html 都可以进行OAuth2.0鉴权。但http://pay.qq.com 、 http://music.qq.com 、 http://qq.com无法进行OAuth2.0鉴权<br/>
 * 3、如果公众号登录授权给了第三方开发者来进行管理，则不必做任何设置，由第三方代替公众号实现网页授权即可<br/>
 */
public class WeChatServlet extends HttpServlet {
    private static Logger logger = LoggerFactory.getLogger(WeChatServlet.class);

    private String wechatAppid;
    private String encodingAesKey;
    private String token;
    private JSONObject rewardPatterns;

    @Override
    public void init(ServletConfig config) throws ServletException {
        Properties prop = ConfigBuilder.getInstance().init(WeChatPropertiesFile.filename);
        token = prop.getProperty("token");
        wechatAppid = prop.getProperty("wechatAppid");
        if (StringUtil.isEmpty(wechatAppid)) {
            throw new IllegalArgumentException("<<======wechatAppid could not be null");
        }
        encodingAesKey = prop.getProperty("encodingAesKey");
        String patterns = prop.getProperty("rewardPatterns");
        if (!StringUtil.isEmpty(patterns)) {
            rewardPatterns = JSONObject.parseObject(patterns);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String echoStr = req.getParameter("echostr");
        String timestamp = req.getParameter("timestamp");
        String nonce = req.getParameter("nonce");
        String signature = req.getParameter("signature");
        if (StringUtil.isEmpty(signature) || StringUtil.isEmpty(timestamp) || StringUtil.isEmpty(nonce)) {
            return;
        }
        try {
            WXBizMsgCrypt wxBizMsgCrypt = new WXBizMsgCrypt(token, encodingAesKey, wechatAppid);
            if (wxBizMsgCrypt.verifyUrl(signature, timestamp, nonce)) {
                resp.getWriter().write(echoStr);
            }
        } catch (AesException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=utf-8");
        resp.addHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.addHeader("P3P", "CP=CAO PSA OUR");

        Map<String, Object> requestMap = new HashMap<>();

        try {
            ServletInputStream inputStream = req.getInputStream();
            requestMap.putAll(XmlUtils.XMLToMap(inputStream));
        } catch (Exception e) {
            logger.error("parse xml", e);
        }

        String msgType = (String) requestMap.get("MsgType");
        String event = (String) requestMap.get("Event");
        String eventKey = (String) requestMap.get("EventKey");

        String pattern;
        switch (msgType) {
            case "event":
                pattern = getRewardPatternByEventType(event, eventKey);
                break;
            case "text":
                logger.info("<<======fire text reply");
                pattern = getRewardPattern("textReply");
                break;
            default:
                logger.info("<<======fire default");
                pattern = getRewardPattern("default");
        }

        if (!StringUtil.isEmpty(pattern)) {
            req.setAttribute("weChatParams", requestMap);
            req.getRequestDispatcher(pattern).forward(req, resp);
        }
    }

    private String getRewardPatternByEventType(String event, String eventKey) {
        String pattern;
        switch (event) {
            case "subscribe":
                logger.info("<<======fire subscribe");
                pattern = getRewardPattern("subscribe");
                break;
            case "unsubscribe":
                logger.info("<<======fire unSubscribe");
                pattern = getRewardPattern("unSubscribe");
                break;
            case "scancode_waitmsg":
                logger.info("<<======fire scanCodeWaitMsg");
                pattern = getRewardPattern("scanCodeWaitMsg");
                break;
            case "CLICK":
                logger.info("<<======fire CLICK, eventKey:" + eventKey);
                pattern = getRewardPattern(eventKey);
                break;
            case "SCAN":
                logger.info("<<======fire SCAN");
                pattern = getRewardPattern("scan");
                break;
            case "VIEW":
                logger.info("<<======fire VIEW");
                pattern = getRewardPattern("view");
                break;
            default:
                logger.info("<<======fire event default");
                pattern = getRewardPattern("eventDefault");
        }
        return pattern;
    }

    private String getRewardPattern(String key) {
        return rewardPatterns.getString(key);
    }
}
