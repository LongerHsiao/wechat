package common.wechat;

import java.text.MessageFormat;

/**
 * @author LongerHsiao
 * @date 2017-10-20
 */
enum WeChatUrlFormat {
    OAUTH_URL_USER_UNSUBSCRIBE("https://open.weixin.qq.com/connect/oauth2/authorize?appid={0}&redirect_uri={1}&response_type=code&scope=snsapi_base&state={2}#wechat_redirect"),

    OAUTH_URL("https://open.weixin.qq.com/connect/oauth2/authorize?appid={0}&redirect_uri={1}&response_type=code&scope=snsapi_userinfo&state={2}#wechat_redirect"),

    GET_OAUTH_TOKEN("https://api.weixin.qq.com/sns/oauth2/access_token?appid={0}&secret={1}&code={2}&grant_type=authorization_code"),

    GET_USER_INFO("https://api.weixin.qq.com/cgi-bin/user/info?access_token={0}&openid={1}&lang=zh_CN"),

    GET_USER_INFO_BY_OAUTH("https://api.weixin.qq.com/sns/userinfo?access_token={0}&openid={1}&lang=zh_CN"),

    REFRESH_OAUTH_TOKEN("https://api.weixin.qq.com/sns/oauth2/refresh_token?appid={0}&grant_type=refresh_token&refresh_token={1}"),

    GET_ACCESS_TOKEN("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={0}&secret={1}"),

    GET_JS_API_TICKET("https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token={0}&type=jsapi"),

    SEND_TEMPLATE_MSG("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token={0}"),

    SEND_CUSTOMER_MSG("https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token={0}");

    private MessageFormat format;

    WeChatUrlFormat(String pattern) {
        this.format = new MessageFormat(pattern);
    }

    public String format(String... params) {
        return format.format(params);
    }

    public MessageFormat getFormat() {
        return format;
    }
}
