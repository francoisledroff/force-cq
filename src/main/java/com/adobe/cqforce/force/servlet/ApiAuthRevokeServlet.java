package com.adobe.cqforce.force.servlet;

import com.adobe.granite.xss.XSSFilter;

import com.adobe.cqforce.util.Constants;
import com.adobe.cqforce.force.service.ApiAuthService;
import com.adobe.cqforce.force.service.SalesForceService;
import com.adobe.cqforce.security.service.HubUser;
import com.adobe.cqforce.security.service.SecurityService;
import com.adobe.cqforce.util.CommonUtils;
import com.force.api.ApiSession;
import com.force.api.Auth;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * To revoke all token information for an user the following actions must be performed:
 * -- revoke authorisation from sales force (using SF API). Both access token (the short-lived session token)
 * and refresh token (the long-lived persistent token) must be removed
 * -- delete tokens stored in JCR for the user
 * <p/>
 * Additionally, the SF session cookies stored on client when user authenticated to SF must be removed to avoid
 * the need to restart a browser session. Otherwise, even if we remove the tokens from SF and JCR, the session id will
 * be reused and new tokens will be recreated for the same user without the login window being prompted to the user.
 * <p/>
 * To remove the SF session cookies, a GET request should be done by the client for https://<sf_instance>/secur/logout.jsp
 * (to also include the cookies). The request can be done using a hidden iframe or a direct call from the client. The second
 * choice will be used when we will have revoke buttons in UI.
 */
@SlingServlet(methods = {"GET"}, resourceTypes = "com/adobe/cqforce/force/authRevoke", extensions = {"json"})
public class ApiAuthRevokeServlet extends SlingAllMethodsServlet {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Reference
    private ApiAuthService apiAuthService;

    @Reference
    private SecurityService securityService;

    @Reference
    private XSSFilter xssFilter;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        //this content type is temporary
        response.setContentType("text/html");

        String status = Constants.ACTION_RESULT_ERROR;
        String message = null;

        String userId = null;
        try {
            //get user information
            userId = DatUtils.getUserIdFromRequest(request, response, xssFilter);
            HubUser user = this.securityService.getUser(userId);
            SalesForceService sfService = this.apiAuthService.getSalesForceService(user);
            ApiSession apiSession = sfService.getApiSession();

            if (apiSession != null) {
                //remove the tokens
                removeSalesForceTokens(userId, apiSession.getAccessToken(), apiSession.getRefreshToken());

                //remove SF information from JCR
                sfService.removeSalesForceTokens();
                status = Constants.ACTION_RESULT_SUCCESS;

            } else {
                //set a "user friendly" message - without words like oauth, token, etc. We don't want to confuse the user :).
                message = "There is no session information available for user";
            }

        } catch (RepositoryException e) {
            message = "Error while processing request";
            //at the moment just log the exception, do not surface it to the user
            CommonUtils.logError(message, e, logger, userId);
        }


        response.getWriter().print(buildResponse());
    }


    /**
     * Build the response for the client. Currently we include an inframe in the response.
     * When we will have UI buttons we will send only json response and the call to SF logout will
     * be done on the client.
     */
    private String buildResponse() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");
        sb.append("<iframe src='" + getLogoutUrl() + "' width='0' height='0' style='display:none;'></iframe>");
        sb.append("</body></html>");

        return sb.toString();
    }

    private String getLogoutUrl() {
        String loginEndPoint = apiAuthService.getApiConfig().getLoginEndpoint();
        if (loginEndPoint != null) {
            return getNormalizedUrl(loginEndPoint) + "/secur/logout.jsp";
        }
        throw new RuntimeException("SF endpoint not configured");
    }

    private String getNormalizedUrl(String url) {
        if (url != null) {
            return StringUtils.removeEnd(url, "/");
        }
        return url;
    }

    /**
     * Revoke tokens using force-api.
     */
    private void removeSalesForceTokens(String userId, String... tokens) {
        if (tokens != null) {
            for (String token : tokens) {
                try {
                    //process individually the tokens
                    Auth.revokeToken(apiAuthService.getApiConfig(), token);
                } catch (Exception e) {
                    CommonUtils.logError("Error while removing token", e, logger, userId);
                }

            }
        }
    }
}