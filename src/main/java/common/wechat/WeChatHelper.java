package common.wechat;

import com.alibaba.fastjson.JSONObject;
import common.tools.AppException;
import common.tools.StringUtil;
import common.webtools.MyHttpClient;
import common.wechat.cache.CacheManager;
import common.wechat.util.ConfigBuilder;
import common.wechat.util.SHA1;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Properties;

/**
 * @author LongerHsiao
 * @date 2017-10-20
 */

/**
 * @api WeChatHelper 1、微信基础接口服务
 * @apiGroup Static Util
 * @apiName WeChatHelper
 * @apiVersion 1.0.0
 * @apiParam {String} weChatAppId 微信公众号唯一凭证<code>必须</code>
 * @apiParam {String} weChatSecret 微信公众号凭证密钥<code>必须</code>
 * @apiParam {String} rootKey key值前缀[若启用缓存，则需配置该字段，为避免多应用配置同一缓存框架而造成设值冲突]
 * @apiDescription 所有需要调用微信功能的应用中都需要依赖本服务。<br/>
 */
public class WeChatHelper {
    private static Logger logger = LoggerFactory.getLogger(WeChatHelper.class);

    private static String weChatAppId;
    private static String weChatSecret;
    private static String cacheKeyForAccessToken;
    private static String cacheKeyForJsApiTicket;
    // 失效秒数偏移量默认为900秒
    private static int expiredSecondOffset = 900;

    private static AccessToken accessToken;
    private static JsApiTicket jsApiTicket;
    private static CacheManager cacheManager;

    private WeChatHelper() {
    }

    static {
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
            expiredSecondOffset = Integer.parseInt(prop.getProperty("expiredSecondOffset"));
        }

        cacheManager = CacheManager.getInstance();
        if (cacheManager.cacheOn) {
            String rootKey = prop.getProperty("rootKey");
            if (StringUtil.isEmpty(rootKey)) {
                throw new IllegalArgumentException("<<======rootKey could not be null");
            }
            if (!rootKey.endsWith(".")) {
                rootKey += ".";
            }
            cacheKeyForAccessToken = rootKey + "ACCESS_TOKEN";
            cacheKeyForJsApiTicket = rootKey + "JS_API_TICKET";
        } else {
            accessToken = AccessToken.getInstance();
            jsApiTicket = JsApiTicket.getInstance();
        }
    }

    public static OAuthUserInfo getOauthInfo(String code) {
        if (StringUtil.isEmpty(code)) {
            throw new AppException("code could not be null");
        }
        OAuthUserInfo oAuthUserInfo = null;
        try {
            String url = WeChatUrlFormat.GET_OAUTH_TOKEN.format(weChatAppId, weChatSecret, code);
            JSONObject jsonObject = JSONObject.parseObject(httpGet(url));
            if (StringUtil.isEmpty(jsonObject.getString("openid"))) {
                logger.error("<<======WeChatHelper.getOpenid:code=" + code + "======" + jsonObject.toJSONString());
                return null;
            }
            oAuthUserInfo = new OAuthUserInfo();
            oAuthUserInfo.setOpenid(jsonObject.getString("openid"));
            oAuthUserInfo.setAccessToken(jsonObject.getString("access_token"));
            oAuthUserInfo.setRefreshToken(jsonObject.getString("refresh_token"));
            oAuthUserInfo.setExpiredSecond(jsonObject.getInteger("expires_in") - expiredSecondOffset);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("<<======WeChatHelper.getOpenid:code=" + code);
        }
        return oAuthUserInfo;
    }

    public static WeChatUserInfo getUserInfo(String openid) {
        WeChatUserInfo weChatUserInfo = null;
        try {
            String url = WeChatUrlFormat.GET_USER_INFO.format(getAccessToken(), openid);
            JSONObject jsonObject = JSONObject.parseObject(httpGet(url));
            if (jsonObject.getInteger("subscribe") == 0) {
                logger.info("<<======WeChatHelper.getUserInfo:the user is not subscribed, openid =" + openid);
                return null;
            }
            weChatUserInfo = new WeChatUserInfo();
            weChatUserInfo.setOpenid(openid);
            weChatUserInfo.setNickname(jsonObject.getString("nickname"));
            int sex = StringUtil.isEmpty(jsonObject.getString("sex")) ?
                    0 : Integer.parseInt(jsonObject.getString("sex"));
            weChatUserInfo.setSex(sex);
            weChatUserInfo.setProvince(jsonObject.getString("province"));
            weChatUserInfo.setCity(jsonObject.getString("city"));
            weChatUserInfo.setCountry(jsonObject.getString("country"));
            weChatUserInfo.setHeadImgUrl(jsonObject.getString("headimgurl"));
            weChatUserInfo.setPrivilegeAsJson(jsonObject.getJSONArray("privilege"));
            weChatUserInfo.setUnionid(jsonObject.containsKey("unionid") ? jsonObject.getString("unionid") : null);
            weChatUserInfo.setSubscribeTime(jsonObject.getDate("subscribe_time"));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("<<======WeChatHelper.getUserInfo:openid=" + openid);
        }
        return weChatUserInfo;
    }

    public static WeChatUserInfo getUserInfoByOAuth(OAuthUserInfo oAuthUserInfo) {
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
            JSONObject jsonObject = JSONObject.parseObject(httpGet(url));
            weChatUserInfo = new WeChatUserInfo();
            weChatUserInfo.setOpenid(openid);
            weChatUserInfo.setNickname(jsonObject.getString("nickname"));
            int sex = StringUtil.isEmpty(jsonObject.getString("sex")) ?
                    0 : Integer.parseInt(jsonObject.getString("sex"));
            weChatUserInfo.setSex(sex);
            weChatUserInfo.setProvince(jsonObject.getString("province"));
            weChatUserInfo.setCity(jsonObject.getString("city"));
            weChatUserInfo.setCountry(jsonObject.getString("country"));
            weChatUserInfo.setHeadImgUrl(jsonObject.getString("headimgurl"));
            weChatUserInfo.setPrivilegeAsJson(jsonObject.getJSONArray("privilege"));
            weChatUserInfo.setUnionid(jsonObject.containsKey("unionid") ? jsonObject.getString("unionid") : null);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("<<======WeChatHelper.getUserInfoFromOAuth:openid=" + openid);
        }
        return weChatUserInfo;
    }

    /**
     * 刷新OAuth认证token
     * 出现以下情形时直接返回原oAuthUserInfo对象:
     * 1、refresh_token为空;
     * 2、刷新过程中出现异常;
     * 3、认证未过期.
     */
    public static OAuthUserInfo refreshAccessToken(OAuthUserInfo oAuthUserInfo) {
        if (oAuthUserInfo == null
                || StringUtil.isEmpty(oAuthUserInfo.getRefreshToken())
                || !oAuthUserInfo.isExpired()) {
            return oAuthUserInfo;
        }
        try {
            String url = WeChatUrlFormat.REFRESH_OAUTH_TOKEN.format(weChatAppId, oAuthUserInfo.getRefreshToken());
            JSONObject jsonObject = JSONObject.parseObject(httpGet(url));
            if (StringUtil.isEmpty(jsonObject.getString("access_token"))) {
                logger.error("<<======WeChatHelper.refreshSpecialAccessToken:" + jsonObject.toJSONString());
                return oAuthUserInfo;
            }
            oAuthUserInfo.setAccessToken(jsonObject.getString("access_token"));
            oAuthUserInfo.setRefreshToken(jsonObject.getString("refresh_token"));
            oAuthUserInfo.setExpiredSecond(jsonObject.getInteger("expires_in") - expiredSecondOffset);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return oAuthUserInfo;
    }

    /**
     * 获取access_token
     * 如果启用了外部缓存,则从缓存中获取
     */
    public static String getAccessToken() {
        if (cacheManager.cacheOn) {
            if (StringUtil.isEmpty(getAccessTokenFromCache())) {
                queryAccessToken();
            }
            return getAccessTokenFromCache();
        }
        if (StringUtil.isEmpty(accessToken.getAccessToken()) || accessToken.isExpired()) {
            queryAccessToken();
        }
        return accessToken.getAccessToken();
    }

    /**
     * 获取js_api_ticket
     * 如果启用了外部缓存,则从缓存中获取
     */
    public static String getJsApiTicket() {
        if (cacheManager.cacheOn) {
            if (StringUtil.isEmpty(getJsApiTicketFromCache())) {
                queryJsApiTicket();
            }
            return getJsApiTicketFromCache();
        }
        if (StringUtil.isEmpty(jsApiTicket.getTicket()) || jsApiTicket.isExpired()) {
            queryJsApiTicket();
        }
        return jsApiTicket.getTicket();
    }

    /**
     * 1、对 js_api_ticket、 timestamp 和 nonce 按字典排序
     * 对所有待签名参数按照字段名的 ASCII码从小到大排序（字典序）后,
     * 使用 URL 键值对的格式（即key1=value1&key2=value2…）拼接成字符串string1.
     * 这里需要注意的是所有参数名均为小写字符.
     * 2、接下来对 string1 作 sha1 加密,字段名和字段值都采用原始值,不进行URL 转义.
     * 即 signature=sha1(string1).
     * 如果没有按照生成的key1=value&key2=value拼接的话会报错
     */
    public static Signature getSignature(String url) {
        Signature signature = new Signature();
        signature.setAppId(weChatAppId);
        signature.setNonceStr(RandomStringUtils.random(16, true, true));
        signature.setTimestamp(String.valueOf(System.currentTimeMillis() / 1000));

        String[] params = new String[]{
                "jsapi_ticket=" + getJsApiTicket(),
                "timestamp=" + signature.getTimestamp(),
                "noncestr=" + signature.getNonceStr(),
                "url=" + url};
        Arrays.sort(params);
        String param = params[0].concat("&" + params[1]).concat("&" + params[2]).concat("&" + params[3]);
        try {
            signature.setSignature(SHA1.sign(param.getBytes("utf-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return signature;
    }

    protected static String getOAuthUrl(String redirectURI, String state) {
        state = StringUtil.isEmpty(state) ? "" : state;
        return WeChatUrlFormat.OAUTH_URL.format(weChatAppId, redirectURI, state);
    }

    private static void queryAccessToken() {
        try {
            String url = WeChatUrlFormat.GET_ACCESS_TOKEN.format(weChatAppId, weChatSecret);
            JSONObject jsonObject = JSONObject.parseObject(httpGet(url));
            if (StringUtil.isEmpty(jsonObject.getString("access_token"))) {
                logger.error("<<======WeChatHelper.queryAccessToken:" + jsonObject.toJSONString());
                return;
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
    private static void setAccessToken(String token, int expiredSecond) {
        if (cacheManager.cacheOn) {
            setAccessToken2Cache(token, expiredSecond);
            return;
        }
        accessToken.setAccessToken(token);
        accessToken.setExpiredSecond(expiredSecond);
    }

    private static void setAccessToken2Cache(String token, Integer expiresIn) {
        cacheManager.setValue(cacheKeyForAccessToken, token, expiresIn);
    }

    private static String getAccessTokenFromCache() {
        return cacheManager.getValue(cacheKeyForAccessToken);
    }

    private static void queryJsApiTicket() {
        try {
            String url = WeChatUrlFormat.GET_JS_API_TICKET.format(getAccessToken());
            JSONObject jsonObject = JSONObject.parseObject(httpGet(url));
            if (StringUtil.isEmpty(jsonObject.getString("ticket"))) {
                logger.error("<<======WeChatHelper.queryJsApiTicket:" + jsonObject.toJSONString());
                return;
            }
            setJsApiTicket(jsonObject.getString("ticket"), jsonObject.getInteger("expires_in") - expiredSecondOffset);
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
    private static void setJsApiTicket(String ticket, int expiresIn) {
        if (cacheManager.cacheOn) {
            setJsApiTicket2Cache(ticket, expiresIn);
            return;
        }
        jsApiTicket.setTicket(ticket);
        jsApiTicket.setExpiresSecond(expiresIn);
    }

    private static void setJsApiTicket2Cache(String ticket, int expiresIn) {
        cacheManager.setValue(cacheKeyForJsApiTicket, ticket, expiresIn);
    }

    private static String getJsApiTicketFromCache() {
        return cacheManager.getValue(cacheKeyForJsApiTicket);
    }

    private static String httpGet(String url) throws IOException, HttpException {
        MyHttpClient myHttpClient = new MyHttpClient();
        return myHttpClient.get(url, null);
    }
}
