package com.adobe.cqforce.force.servlet;

import com.adobe.granite.xss.XSSFilter;
import com.adobe.cqforce.force.Constant;
import com.adobe.cqforce.force.event.DatEventConstants;
import com.adobe.cqforce.force.service.ApiAuthService;
import com.adobe.cqforce.force.service.SalesForceService;
import com.adobe.cqforce.security.service.HubUser;
import com.adobe.cqforce.security.service.SecurityService;
import com.adobe.cqforce.util.CommonUtils;
import com.force.api.ApiSession;
import com.force.api.Auth;
import com.force.api.AuthorizationRequest;
import com.force.api.AuthorizationResponse;
import org.apache.commons.codec.binary.Base64;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;


@SlingServlet(methods = {"GET"}, resourceTypes = "com/adobe/cqforce/force/aapiauth", extensions = {"json"})
public class AnonymousApiAuthServlet extends SlingAllMethodsServlet {
    private static final String UTF_8 = "UTF-8";
    private Logger log = LoggerFactory.getLogger(this.getClass());
    @Reference
    private ApiAuthService apiAuthService;
    @Reference
    private EventAdmin eventAdmin;
    @Reference
    private SecurityService securityService;
    @Reference
    private XSSFilter xssFilter;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException,
            IOException {
        try {

            String userId = DatUtils.getUserIdFromRequest(request, response, xssFilter);
            HubUser user = this.securityService.getUser(userId);
            SalesForceService sfService = this.apiAuthService.getSalesForceService(user);
            ApiSession apiSession = sfService.getApiSession();
            if (apiSession == null) {
                AuthCallBack callback = getAuthCallBack(request);
                if (callback.state == null || callback.state.equals(AuthCallBack.NO_CALLBACK_YET)) {
                    String accessAuthorizationRedirectUrl = Auth.startOAuthWebServerFlow(new AuthorizationRequest()
                            .apiConfig(this.apiAuthService.getApiConfig())
                            .state(AuthCallBack.OBTAINING_ACCESS_AUTHORIZATION));
                    response.sendRedirect(accessAuthorizationRedirectUrl);
                } else if (callback.state.equals(AuthCallBack.OBTAINING_ACCESS_AUTHORIZATION) && callback.error == null
                        && callback.code != null) {
                    ApiSession apiSessionReceived = Auth.completeOAuthWebServerFlow(new AuthorizationResponse()
                            .apiConfig(this.apiAuthService.getApiConfig()).code(callback.code));
                    if (apiSessionReceived != null && apiSessionReceived.getRefreshToken() != null) {
                        sfService.setApiSession(apiSessionReceived);
                        this.postDatAuthEvent(userId);
                        response.sendRedirect(Constant.DAT_AUTH_SUCCESS_PAGE);
                    } else if (apiSessionReceived != null && apiSessionReceived.getRefreshToken() == null) {
                        throw new RuntimeException("The salesforce.com authorization callback processing failed for user " + userId +
                                ". We did not receive any refresh token from Salesforce.com. Please contact the service desk.");
                    } else {
                        throw new RuntimeException("The salesforce.com authorization callback processing failed for user " + userId +
                                ". We could not parse the Salesforce.com user information. Please contact the service desk.");
                    }
                } else {
                    throw new RuntimeException(AuthCallBack.UNEXPECTED_CALLBACK_STATE_OR_ERROR + callback.toString());
                }
            } else if (apiSession != null && apiSession.getRefreshToken() != null) {
                log.warn("the user " + userId + " has a salesforce oauth token already, wondering why it went back here...");
                response.sendRedirect(Constant.DAT_AUTH_SUCCESS_PAGE);
            } else {
                log.warn("the user " + userId + " has corrupted salesforce entries, we will delete them, and redirect the user back here...");
                sfService.removeSalesForceTokens();
                response.sendRedirect(Constant.DAT_AUTH_SERVICE);

            }
        } catch (Exception e) {
            String errorMessage = Constant.DAT_AUTH_ERROR_FLOW + CommonUtils.logAndIdError(log, e.getMessage());
            log.error(errorMessage, e);
            String encMessage = (errorMessage != null) ? Base64.encodeBase64String(errorMessage.getBytes()) : "";
            // Add encoded error message as URL parameter and send it to the browser
            String redirectUrl = Constant.DAT_AUTH_ERROR_PAGE + "?msg=" + encMessage;
            response.sendRedirect(redirectUrl);
        }
    }

    public AuthCallBack getAuthCallBack(SlingHttpServletRequest request) {
        AuthCallBack acb = new AuthCallBack();
        acb.code = filter(request.getParameter("code"));
        acb.error = filter(request.getParameter("error"));
        acb.error_description = filter(request.getParameter("error_description"));
        acb.state = filter(request.getParameter("state"));
        return acb;
    }

    /**
     * The XSS Filter throws an exception when it's passed a null string. We don't need to filter null strings
     * so only pass the string through the filter when the string is not null.
     * @param value
     * @return A filtered string, or null.
     */
    private String filter(String value) {
        if (value != null) {
            return xssFilter.filter(value);
        }
        else {
            return value;
        }
    }

    private void postDatAuthEvent(String userId) {
        final Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put(DatEventConstants.USER_ID, userId);
        Event event = new Event(DatEventConstants.DAT_AUTH_SUCCESS_EVENT_TOPIC, properties);
        this.eventAdmin.postEvent(event);   //postEvent is asynchronous cf. http://experiencedelivers.adobe.com/cemblog/en/experiencedelivers/2012/04/event_handling_incq.html
    }

    class AuthCallBack {
        static final String NO_CALLBACK_YET = "no callback yet";
        static final String OBTAINING_ACCESS_AUTHORIZATION = "ObtainingAccessAuthorization";
        static final String UNEXPECTED_CALLBACK_STATE_OR_ERROR = "unexpected callback state or error ";
        String code;
        String state = NO_CALLBACK_YET;
        String error;
        String error_description;
    }
}