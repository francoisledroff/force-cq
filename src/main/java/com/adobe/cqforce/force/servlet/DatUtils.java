package com.adobe.cqforce.force.servlet;

import com.adobe.granite.xss.XSSFilter;
import com.adobe.cqforce.force.Constant;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;

public class DatUtils {

    private static final Logger logger = LoggerFactory.getLogger(DatUtils.class);


    /**
     * Get the  user id from request parameter.
     */
    public static String getUserIdFromRequest(SlingHttpServletRequest request, SlingHttpServletResponse response, XSSFilter xssFilter) {
        String userId = null;
        if (request.getParameter(Constant.REQUEST_PARAM_USER_ID) != null) {
            userId = xssFilter.filter(request.getParameter(Constant.REQUEST_PARAM_USER_ID));
            Cookie userIdCookie = new Cookie(Constant.REQUEST_PARAM_USER_ID, userId);
            response.addCookie(userIdCookie);
        } else {
            Cookie[] cookies = request.getCookies();

            for (int i = 0; i < cookies.length; i++) {
                String name = cookies[i].getName();
                if (name.equals(Constant.REQUEST_PARAM_USER_ID)) {
                    userId = cookies[i].getValue();
                    break;
                }
            }
        }
        if (userId == null) {
            throw new RuntimeException("This should not happen, we could not retrieve the Hub userid nor in your request, nor in your cookie");
        }
        return userId;
    }


}