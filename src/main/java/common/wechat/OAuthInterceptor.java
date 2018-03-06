package common.wechat;

import common.tools.JsonUtil;
import common.tools.StringUtil;
import common.webtools.ResponseResult;
import common.wechat.util.ConfigBuilder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Properties;

/**
 * @author LongerHsiao
 * @date 2017-11-2
 */
public class OAuthInterceptor implements HandlerInterceptor {
    private static String state;
    private static String redirectHost;

    static {
        Properties prop = ConfigBuilder.getInstance().init(WeChatPropertiesFile.filename);
        if (prop == null) {
            throw new NullPointerException("<<======the wechat.properties file not found");
        }
        state = prop.getProperty("state");
        redirectHost = prop.getProperty("redirectHost");
        if (StringUtil.isEmpty(redirectHost)) {
            throw new IllegalArgumentException("<<======redirectHost could not be null");
        }
    }

    /**
     * @api OAuthInterceptor 1、网页授权拦截器
     * @apiGroup Interceptor
     * @apiName OAuthInterceptor
     * @apiDescription <p>
     * 1、继承自spring-mvc中的HandlerInterceptor <br/>
     * 2、需在servlet.xml中配置此拦截器<code>common.wechat.OAuthInterceptor</code>才能使用 <br/>
     * 3、当授权成功时，会将授权信息放入HttpServletRequest的HttpSession中。
     * key值为<code>oAuthUser</code>，value类型为<code>OAuthUserInfo</code>
     * <br/>
     * @apiVersion 1.0.0
     * @apiParam {String} state 校验值，可对回调地址进行校验 <code>非必须,仅在wechat.properties中配置</code>
     * @apiParam {String} redirectHost 授权回调域名<code>必须,仅在wechat.properties中配置</code><br/>
     * 关于网页授权回调域名的说明：<br/>
     * 1、在微信公众号请求用户网页授权之前，开发者需要先到公众平台官网中的
     * “开发 - 接口权限 - 网页服务 - 网页帐号 - 网页授权获取用户基本信息”的配置选项中，修改授权回调域名。
     * 请注意，这里填写的是域名（是一个字符串），而不是URL，因此请勿加 http:// 等协议头；<br/>
     * 2、授权回调域名配置规范为全域名，比如需要网页授权的域名为：www.qq.com，
     * 配置以后此域名下面的页面http://www.qq.com/music.html 、 http://www.qq.com/login.html
     * 都可以进行OAuth2.0鉴权。但http://pay.qq.com 、 http://music.qq.com 、 http://qq.com  无法进行OAuth2.0鉴权<br/>
     * 3、如果公众号登录授权给了第三方开发者来进行管理，则不必做任何设置，由第三方代替公众号实现网页授权即可
     * <p>
     * @apiError (异常) {String} 1 服务器开小差了,请稍后重试 [当服务器请求微信api接口失败或者超时，会引发该异常]
     * @apiErrorExample {json} 服务器开小差了,请稍后重试
     * {
     * "success": false,
     * "result": "服务器开小差了,请稍后重试"
     * }
     * @apiError (异常) {String} 2 非法请求,请在微信客户端访问 [当授权请求被外部中断，或者授权参数被篡改时，会引发该异常]
     * @apiErrorExample {json} 非法请求,请在微信客户端访问
     * {
     * "success": false,
     * "result": "非法请求,请在微信客户端访问"
     * }
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        response.setHeader("Content-type", "text/html;charset=UTF-8");
        response.setCharacterEncoding("utf8");

        if (isErrorPage(request)) {
            response.getWriter().write(JsonUtil.toJson(new ResponseResult(false, "服务器开小差了,请稍后重试")));
            return false;
        }
        String openid = takeTheOpenid(request);
        if (validOpenid(openid)) {
            return true;
        }
        String code = request.getParameter("code");
        if (StringUtil.isEmpty(code)) {
            response.sendRedirect(getOauthUrl(request));
            return false;
        }
        if (!StringUtil.isEmpty(state)) {
            if (!state.equals(request.getParameter("state"))) {
                response.getWriter().write(JsonUtil.toJson(new ResponseResult(false, "非法请求,请在微信客户端访问")));
                return false;
            }
        }
        OAuthUserInfo oAuthUserInfo = queryOauthInfo(code);
        if (oAuthUserInfo == null || StringUtil.isEmpty(oAuthUserInfo.getOpenid())) {
            request.getRequestDispatcher(getDispatcherFromRequest(request) + "?state=err").forward(request, response);
            return false;
        }
        HttpSession httpSession = request.getSession();
        httpSession.setAttribute("oAuthUser", oAuthUserInfo);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }

    private boolean isErrorPage(HttpServletRequest request) {
        return !StringUtil.isEmpty(request.getQueryString()) && request.getQueryString().contains("state=err");
    }

    private boolean validOpenid(String openid) {
        return !StringUtil.isEmpty(openid) && !(openid.length() < 28);
    }

    private OAuthUserInfo queryOauthInfo(String code) {
        return WeChatHelper.getOauthInfo(code);
    }

    private String takeTheOpenid(HttpServletRequest request) {
        OAuthUserInfo oAuthUser = (OAuthUserInfo) request.getSession().getAttribute("oAuthUser");
        if (oAuthUser != null && oAuthUser.isExpired()) {
            if (WeChatHelper.refreshAccessToken(oAuthUser).isExpired()) {
                oAuthUser = null;
                request.getSession().removeAttribute("oAuthUser");
            }
        }
        return null == oAuthUser ? null : oAuthUser.getOpenid();
    }

    private String getDispatcherFromRequest(HttpServletRequest request) {
        return request.getRequestURI().substring(request.getContextPath().length());
    }

    private String getOauthUrl(HttpServletRequest request) {
        StringBuilder url = new StringBuilder(request.getRequestURI()).append("?");
        if (!StringUtil.isEmpty(request.getQueryString())) {
            String queryString = request.getQueryString();
            for (String str : queryString.split("&")) {
                if (!str.toLowerCase().contains("code")) {
                    url.append(str).append("&");
                }
            }
        }
        String redirectURI =
                redirectHost + (url.toString().endsWith("&") ? url.substring(0, url.length() - 1) : url.toString());
        return WeChatHelper.getOAuthUrl(redirectURI, state);
    }
}