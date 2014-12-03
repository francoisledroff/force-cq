package com.adobe.cqforce.force.servlet;

import com.adobe.granite.xss.XSSFilter;
import com.adobe.cqforce.force.domain.User;
import com.adobe.cqforce.force.service.UserService;
import com.adobe.cqforce.security.service.SecurityService;
import com.adobe.cqforce.util.GsonUtil;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;


@SlingServlet(methods = {"GET"}, resourceTypes = "com/adobe/cqforce/force/users", extensions = {"json"})
public class UsersServlet extends DatServlet {

    @Reference
    private UserService userService;

    @Reference
    private SecurityService securityService;

    @Reference
    private XSSFilter xssFilter;

    /**
     * Name of the parameter with the filter value.
     */
    private static final String filterParamName = "key";

    /**
     * Filter is taken into consideration only if trimmed value is greated than this value.
     */
    private static final int filterMinLength = 2;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        try {
        String filter = xssFilter.filter(request.getParameter(filterParamName));

            String userId = securityService.getUser(request).getUserProfile().getUserId();

        List<User> userList = null;
        if (isValidFilter(filter)) {
            userList = userService.getUserList(userId, filter);
        }

        response.getWriter().println(GsonUtil.toJson(userList));

    } catch (Exception e) {
        throw new ServletException("Error while fetching SFDC user lists", e);
    }
    }

    private boolean isValidFilter(String filter) {
        return filter == null || filter.isEmpty() || (filter != null && filter.trim().length() >= filterMinLength);
    }
}
