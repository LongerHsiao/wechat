package common.wechat;

import common.tools.JsonUtil;
import common.tools.StringUtil;
import common.webtools.ResponseResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author LongerHsiao
 * @date 2017-11-2
 */

/**
 * @api OAuthInterceptor 网页授权拦截器
 * @apiGroup WeChat_Component
 * @apiName OAuthInterceptor
 * @apiVersion 1.0.0
 * @apiParam {String} state 校验位
 * @apiParam {String} redirectHost 授权回调域名<code>必须</code>
 * @apiDescription 在需要使用网页授权的应用中接入此拦截器, 需要配置state和redirectHost参数。<br/>
 * 其中,state参数可对回调地址进行校验,也可置空;<br/>
 * redirectHost参数为授权回调域名,关于网页授权回调域名的说明：<br/>
 * 1、在微信公众号请求用户网页授权之前，开发者需要先到公众平台官网中的“开发 - 接口权限 - 网页服务 - 网页帐号 - 网页授权获取用户基本信息”的配置选项中，修改授权回调域名。请注意，这里填写的是域名（是一个字符串），而不是URL，因此请勿加 http:// 等协议头；<br/>
 * 2、授权回调域名配置规范为全域名，比如需要网页授权的域名为：www.qq.com，配置以后此域名下面的页面http://www.qq.com/music.html 、 http://www.qq.com/login.html 都可以进行OAuth2.0鉴权。但http://pay.qq.com 、 http://music.qq.com 、 http://qq.com无法进行OAuth2.0鉴权<br/>
 * 3、如果公众号登录授权给了第三方开发者来进行管理，则不必做任何设置，由第三方代替公众号实现网页授权即可<br/>
 * @apiError Service Err
 * @apiErrorExample {json} 1、服务器开小差了,请稍后重试
 * {
 * "success": false,
 * "result": "服务器开小差了,请稍后重试"
 * }
 */
public class OAuthInterceptor implements HandlerInterceptor {
    @Value("${state}")
    private String state;

    @Value("${redirectHost}")
    private String redirectHost;

    private WeChatHelper weChatHelper = WeChatHelper.getInstance();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        response.setHeader("Content-type", "text/html;charset=UTF-8");
        response.setCharacterEncoding("utf8");

        if (request.getRequestURI().contains("/accessToken")) {
            response.getWriter().write(weChatHelper.getAccessToken());
            return false;
        }

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
//        response.sendRedirect(getURLWithoutCode(request));
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }

    private boolean runAtMicroMessenger(HttpServletRequest request) {
        return request.getHeader("user-agent").toLowerCase().contains("micromessenger");
    }

    private boolean isErrorPage(HttpServletRequest request) {
        return !StringUtil.isEmpty(request.getQueryString()) && request.getQueryString().contains("state=err");
    }

    private boolean validOpenid(String openid) {
        return !StringUtil.isEmpty(openid) && !(openid.length() < 28);
    }

    private String queryOpenid(String code) {
        return weChatHelper.queryOpenid(code);
    }

    private OAuthUserInfo queryOauthInfo(String code) {
        return weChatHelper.getOauthInfo(code);
    }

    private String takeTheOpenid(HttpServletRequest request) {
        OAuthUserInfo oAuthUser = (OAuthUserInfo)request.getSession().getAttribute("oAuthUser");
        return null == oAuthUser ? null : oAuthUser.getOpenid();
    }

    private String getDispatcherFromRequest(HttpServletRequest request) {
        return request.getRequestURI().substring(request.getContextPath().length());
    }

    private String getURLWithoutCode(HttpServletRequest request) {
        StringBuilder url = new StringBuilder(request.getRequestURI());
        String redirectURL = url.toString();
        if (!StringUtil.isEmpty(request.getQueryString())) {
            String queryString = request.getQueryString();
            url.append("?");
            for (String str : queryString.split("&")) {
                if (!str.toLowerCase().contains("code")) {
                    url.append(str).append("&");
                }
            }
            redirectURL = url.substring(0, url.length() - 1);
        }
        return redirectURL;
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
        return weChatHelper.getOAuthUrl(redirectURI, state);
    }

}
