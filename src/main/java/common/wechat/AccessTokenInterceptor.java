package common.wechat;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author LongerHsiao
 * @date 2017-12-14
 */
public class AccessTokenInterceptor implements HandlerInterceptor {
    /**
     * @api {get} /accessToken 2、获取微信access_token
     * @apiGroup Interceptor
     * @apiName GetAccessToken
     * @apiDescription <p>
     * 1、需在servlet.xml中配置此拦截器<code>common.wechat.AccessTokenInterceptor</code>方可使用 <br/>
     * 2、获取的access_token是在缓存中进行管理的值，而非重新向微信服务器请求得来
     * <br/>
     * @apiVersion 1.0.0
     * @apiSuccess {String} none 直接返回access_token
     * @apiSuccessExample {String} none
     * "6_-DTFOy1-3ylQb4G7vGJB45znynsb-cQXwT1XCuDcTypfKx-80En7_AEK7CfYRWeAmtyNXaH4gxsX4tsmnSn3zQrtSUZumGXLcQbmyRRTzXboIRDk0HhM_1mnz96jVJloYYvd5-HxeAR53aFmDYRhADAZUN"
     */
    /**
     * @api {get} /jsTicket 3、获取微信jsTicket
     * @apiGroup Interceptor
     * @apiName GetJsApiTicket
     * @apiDescription <p>
     * 1、需在servlet.xml中配置此拦截器<code>common.wechat.AccessTokenInterceptor</code>方可使用 <br/>
     * 2、获取的jsTicket是在缓存中进行管理的值，而非重新向微信服务器请求得来
     * <br/>
     * @apiVersion 1.0.0
     * @apiSuccess {String} none 直接返回jsTicket
     * @apiSuccessExample {String} none
     * "kgt8ON7yVITDhtdwci0qeUD52kOc5cW_ofyvbHQPdf_vusEEurTj7wP1bj05HZrtokqoYYpLYuF85m5xweepyg"
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (request.getRequestURI().contains("/accessToken")) {
            response.getWriter().write(WeChatHelper.getAccessToken());
            return false;
        }
        if (request.getRequestURI().contains("/jsTicket")) {
            response.getWriter().write(WeChatHelper.getJsApiTicket());
            return false;
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}