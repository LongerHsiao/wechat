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
 * @api WeChatMessageServlet 1、微信消息处理服务
 * @apiGroup Servlet
 * @apiName WeChatMessageServlet
 * @apiVersion 1.0.0
 */
public class WeChatMessageServlet extends HttpServlet {
    private static Logger logger = LoggerFactory.getLogger(WeChatMessageServlet.class);

    private String weChatAppId;
    private String encodingAesKey;
    private String token;
    private JSONObject rewardPatterns;

    @Override
    public void init(ServletConfig config) throws ServletException {
        Properties prop = ConfigBuilder.getInstance().init(WeChatPropertiesFile.filename);
        token = prop.getProperty("token");
        weChatAppId = prop.getProperty("weChatAppId");
        if (StringUtil.isEmpty(weChatAppId)) {
            throw new IllegalArgumentException("<<======weChatAppId could not be null");
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
            WXBizMsgCrypt wxBizMsgCrypt = new WXBizMsgCrypt(token, encodingAesKey, weChatAppId);
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
            requestMap.putAll(XmlUtils.xmlToMap(inputStream));
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
