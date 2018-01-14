package common.wechat;

import com.alibaba.fastjson.JSONObject;
import common.tools.AppException;
import common.tools.JsonUtil;
import common.tools.StringUtil;
import common.webtools.MyHttpClient;
import common.wechat.cache.CacheManager;
import common.wechat.util.ConfigBuilder;
import org.apache.http.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * @author LongerHsiao
 * @date 2017-10-20
 */

/**
 * @api WeChatHelper 微信基础接口服务
 * @apiGroup WeChat_Component
 * @apiName WeChatHelper
 * @apiVersion 1.0.0
 * @apiParam {String} weChatAppId 微信公众号唯一凭证<code>必须</code>
 * @apiParam {String} weChatSecret 微信公众号凭证密钥<code>必须</code>
 * @apiParam {String} rootKey key值前缀[若启用缓存,为避免多应用配置同一缓存框架而造成设值冲突]
 * @apiDescription 所有需要调用微信功能的应用中都需要依赖本服务。<br/>
 * 其中,state参数可对回调地址进行校验,也可置空;<br/>
 * redirectHost参数为授权回调域名,关于网页授权回调域名的说明：<br/>
 * 1、在微信公众号请求用户网页授权之前，开发者需要先到公众平台官网中的“开发 - 接口权限 - 网页服务 - 网页帐号 - 网页授权获取用户基本信息”的配置选项中，修改授权回调域名。请注意，这里填写的是域名（是一个字符串），而不是URL，因此请勿加 http:// 等协议头；<br/>
 * 2、授权回调域名配置规范为全域名，比如需要网页授权的域名为：www.qq.com，配置以后此域名下面的页面http://www.qq.com/music.html 、 http://www.qq.com/login.html 都可以进行OAuth2.0鉴权。但http://pay.qq.com 、 http://music.qq.com 、 http://qq.com无法进行OAuth2.0鉴权<br/>
 * 3、如果公众号登录授权给了第三方开发者来进行管理，则不必做任何设置，由第三方代替公众号实现网页授权即可<br/>
 */
public class WeChatHelper {
    private static Logger logger = LoggerFactory.getLogger(WeChatHelper.class);

    private WeChatHelper() {
        init();
    }

    private static class WeChatHelperHolder {
        private static final WeChatHelper INSTANCE = new WeChatHelper();
    }

    public static WeChatHelper getInstance() {
        return WeChatHelperHolder.INSTANCE;
    }

    private void init() {
        Properties prop = ConfigBuilder.getInstance().init(WeChatPropertiesFile.filename);
        if (prop == null) {
            throw new NullPointerException("<<======the wechat.properties file not found");
        }

        weChatAppId = prop.getProperty("weChatAppId");
        weChatSecret = prop.getProperty("weChatSecret");

        if (StringUtil.isEmpty(weChatAppId)) {
            throw new IllegalArgumentException("<<======weChatAppId could not be null");
        }
        if (StringUtil.isEmpty(weChatSecret)) {
            throw new IllegalArgumentException("<<======weChatSecret could not be null");
        }
        if (!StringUtil.isEmpty(prop.getProperty("expiredSecondOffset"))) {
            expiredSecondOffset = Integer.valueOf(prop.getProperty("expiredSecondOffset"));
        }

        cacheManager = CacheManager.getInstance();
        if (cacheManager.cacheOn) {
            rootKey = prop.getProperty("rootKey");
            if (StringUtil.isEmpty(rootKey)) {
                throw new IllegalArgumentException("<<======rootKey could not be null");
            }
            if (!rootKey.endsWith(".")) {
                rootKey += ".";
            }
            cacheKeyForAccessToken = rootKey + "ACCESS_TOKEN";
            cacheKeyForJsTicket = rootKey + "JS_TICKET";
        } else {
            accessToken = AccessToken.getInstance();
            jsTicket = JsTicket.getInstance();
        }
    }

    public OAuthUserInfo getOauthInfo(String code) {
        OAuthUserInfo oAuthUserInfo = null;
        try {
            if (StringUtil.isEmpty(code)) {
                throw new AppException("code could not be null");
            }
            String url = WeChatUrlFormat.GET_OAUTH_TOKEN.format(weChatAppId, weChatSecret, code);
            String result = httpGet(url);
            JSONObject jsonObject = JSONObject.parseObject(result);
            String openid = jsonObject.getString("openid");
            if (openid == null) {
                logger.error("<<======WeChatHelper.getOpenid:code=" + code + "======" + JsonUtil.toJson(result));
                throw new AppException("Error message from WeChat:" + result);
            }
            oAuthUserInfo = new OAuthUserInfo();
            oAuthUserInfo.setOpenid(openid);
            oAuthUserInfo.setAccessToken(jsonObject.getString("access_token"));
            oAuthUserInfo.setRefreshToken(jsonObject.getString("refresh_token"));
            oAuthUserInfo.setExpiredSecond(jsonObject.getInteger("expires_in") - expiredSecondOffset);
        } catch (AppException ex) {
            throw ex;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("<<======WeChatHelper.getOpenid:code=" + code);
        }
        return oAuthUserInfo;
    }

    public OAuthUserInfo refreshAccessToken(OAuthUserInfo oAuthUserInfo) {
        if (oAuthUserInfo == null || StringUtil.isEmpty(oAuthUserInfo.getRefreshToken())) {
            throw new AppException("refreshToken could not be null");
        }
        try {
            String url = WeChatUrlFormat.REFRESH_OAUTH_TOKEN.format(weChatAppId, oAuthUserInfo.getRefreshToken());
            String result = httpGet(url);
            JSONObject jsonObject = JSONObject.parseObject(result);
            String token = jsonObject.getString("access_token");
            if (StringUtil.isEmpty(token)) {
                logger.error("<<======WeChatHelper.refreshSpecialAccessToken:" + JsonUtil.toJson(result));
                throw new AppException("the server is not answer, please try again later");
            }
            oAuthUserInfo.setAccessToken(token);
            oAuthUserInfo.setRefreshToken(jsonObject.getString("refresh_token"));
            oAuthUserInfo.setExpiredSecond(jsonObject.getInteger("expires_in") - expiredSecondOffset);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return oAuthUserInfo;
    }

    public WeChatUserInfo getUserInfo(String openid) {
        WeChatUserInfo weChatUserInfo = null;
        try {
            String url = WeChatUrlFormat.GET_USER_INFO.format(getAccessToken(), openid);
            String result = httpGet(url);
            JSONObject jsonObject = JSONObject.parseObject(result);
            if (jsonObject.getInteger("subscribe") == 0) {
                logger.info("<<======WeChatHelper.getUserInfo:the user is not subscribed, openid =" + openid);
                return null;
            }
            weChatUserInfo = new WeChatUserInfo();
            weChatUserInfo.setOpenid(openid);
            weChatUserInfo.setNickname(jsonObject.getString("nickname"));
            weChatUserInfo.setSex(jsonObject.getString("sex"));
            weChatUserInfo.setProvince(jsonObject.getString("province"));
            weChatUserInfo.setCity(jsonObject.getString("city"));
            weChatUserInfo.setCountry(jsonObject.getString("country"));
            weChatUserInfo.setHeadImgUrl(jsonObject.getString("headimgurl"));
            weChatUserInfo.setPrivilege((String[]) jsonObject.get("privilege"));
            weChatUserInfo.setUnionid(jsonObject.containsKey("unionid") ? jsonObject.getString("unionid") : null);
            weChatUserInfo.setSubscribeTime(jsonObject.getDate("subscribe_time"));
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("<<======WeChatHelper.getUserInfo:openid=" + openid);
        }
        return weChatUserInfo;
    }

    public WeChatUserInfo getUserInfoByOAuth(OAuthUserInfo oAuthUserInfo) {
        if (oAuthUserInfo == null || StringUtil.isEmpty(oAuthUserInfo.getOpenid())) {
            throw new AppException("openid could not be null");
        }
        if (oAuthUserInfo.isExpired()) {
            throw new AppException("the access_token is expired");
        }
        WeChatUserInfo weChatUserInfo = null;
        String openid = oAuthUserInfo.getOpenid();
        try {
            String url = WeChatUrlFormat.GET_USER_INFO_BY_OAUTH.format(oAuthUserInfo.getAccessToken(), openid);
            String result = httpGet(url);
            JSONObject jsonObject = JSONObject.parseObject(result);
            weChatUserInfo = new WeChatUserInfo();
            weChatUserInfo.setOpenid(openid);
            weChatUserInfo.setNickname(jsonObject.getString("nickname"));
            weChatUserInfo.setSex(jsonObject.getString("sex"));
            weChatUserInfo.setProvince(jsonObject.getString("province"));
            weChatUserInfo.setCity(jsonObject.getString("city"));
            weChatUserInfo.setCountry(jsonObject.getString("country"));
            weChatUserInfo.setHeadImgUrl(jsonObject.getString("headimgurl"));
            weChatUserInfo.setPrivilege((String[]) jsonObject.get("privilege"));
            weChatUserInfo.setUnionid(jsonObject.containsKey("unionid") ? jsonObject.getString("unionid") : null);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("<<======WeChatHelper.getUserInfoFromOAuth:openid=" + openid);
        }
        return weChatUserInfo;
    }

    public String getAccessToken() {
        if (cacheManager.cacheOn) {
            String accessToken = getAccessTokenFromCache();
            if (StringUtil.isEmpty(accessToken)) {
                queryAccessToken();
            }
            return getAccessTokenFromCache();
        }

        if (StringUtil.isEmpty(accessToken.getAccessToken()) || accessToken.isExpired()) {
            queryAccessToken();
        }
        return accessToken.getAccessToken();
    }

    public String getJsTicket() {
        if (cacheManager.cacheOn) {
            String jsTicket = getJsTicketFromCache();
            if (StringUtil.isEmpty(jsTicket)) {
                queryJsTicket();
            }
            return getJsTicketFromCache();
        }

        if (StringUtil.isEmpty(jsTicket.getTicket()) || jsTicket.isExpired()) {
            queryJsTicket();
        }
        return jsTicket.getTicket();
    }

    protected String getOAuthUrl(String redirectURI, String state) {
        state = StringUtil.isEmpty(state) ? "" : state;
        return WeChatUrlFormat.OAUTH_URL.format(weChatAppId, redirectURI, state);
    }

    protected String queryOpenid(String code) {
        return getOauthInfo(code).getOpenid();
    }

    private void queryAccessToken() {
        try {
            String url = WeChatUrlFormat.GET_ACCESS_TOKEN.format(weChatAppId, weChatSecret);
            JSONObject jsonObject = JSONObject.parseObject(httpGet(url));
            if (StringUtil.isEmpty(jsonObject.getString("access_token"))) {
                logger.error("<<======WeChatHelper.queryAccessToken:" + jsonObject.toJSONString());
                throw new AppException("get wechat access_token fail:" + jsonObject.toJSONString());
            }
            setAccessToken(jsonObject.getString("access_token"), jsonObject.getInteger("expires_in") - expiredSecondOffset);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 存储access_token
     * 若开启了外部缓存,则不再于内存中维护access_token
     */
    private void setAccessToken(String accessToken, int expiredSecond) {
        if (cacheManager.cacheOn) {
            setAccessToken2Cache(accessToken, expiredSecond);
            return;
        }
        this.accessToken.setAccessToken(accessToken);
        this.accessToken.setExpiredSecond(expiredSecond);
    }

    private void setAccessToken2Cache(String token, Integer expiresIn) {
        cacheManager.setValue(cacheKeyForAccessToken, token, expiresIn);
    }

    private String getAccessTokenFromCache() {
        return cacheManager.getValue(cacheKeyForAccessToken);
    }

    private void queryJsTicket() {
        try {
            String url = WeChatUrlFormat.GET_JS_API_TICKET.format(getAccessToken());
            JSONObject jsonObject = JSONObject.parseObject(httpGet(url));
            if (StringUtil.isEmpty(jsonObject.getString("ticket"))) {
                logger.error("<<======WeChatHelper.queryJsTicket:" + jsonObject.toJSONString());
                throw new AppException("get wechat js_api_ticket fail:" + jsonObject.toJSONString());
            }
            setJsTicket(jsonObject.getString("ticket"), jsonObject.getInteger("expires_in") - expiredSecondOffset);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 存储js_ticket
     * 若开启了外部缓存,则不再于内存中维护js_ticket
     */
    private void setJsTicket(String ticket, int expiresIn) {
        if (cacheManager.cacheOn) {
            setJsTicket2Cache(ticket, expiresIn);
            return;
        }
        this.jsTicket.setTicket(ticket);
        this.jsTicket.setExpiresSecond(expiresIn);
    }

    private void setJsTicket2Cache(String ticket, int expiresIn) {
        cacheManager.setValue(cacheKeyForJsTicket, ticket, expiresIn);
    }

    private String getJsTicketFromCache() {
        return cacheManager.getValue(cacheKeyForJsTicket);
    }

    private String httpGet(String url) throws IOException, HttpException {
        MyHttpClient myHttpClient = new MyHttpClient();
        return myHttpClient.get(url, null);
    }

    private static String weChatAppId;
    private static String weChatSecret;
    private static String rootKey;
    private static String cacheKeyForAccessToken;
    private static String cacheKeyForJsTicket;
    // 失效秒数偏移量默认为900秒
    private static int expiredSecondOffset = 900;

    private AccessToken accessToken;
    private JsTicket jsTicket;
    private CacheManager cacheManager;
}
