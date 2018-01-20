package common.wechat;

import com.alibaba.fastjson.JSONObject;
import common.tools.StringUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author LongerHsiao
 * @date 2018-1-15
 */
public class WeChatJsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String url = req.getParameter("url");
        if (StringUtil.isEmpty(url) ||
                !(url.startsWith("http://") || (url.startsWith("https://")))) {
            return;
        }
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.getWriter().write(JSONObject.toJSONString(WeChatHelper.getSignature(req.getParameter("url"))));
    }
}
