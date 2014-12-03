package com.adobe.cqforce.force.servlet;

import com.adobe.cqforce.force.event.DatEventConstants;
import com.adobe.cqforce.force.service.ApiAuthService;
import com.adobe.cqforce.force.service.SalesForceService;
import com.adobe.cqforce.security.service.SecurityService;
import com.adobe.cqforce.util.GsonUtil;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

@SlingServlet(methods = {"GET"}, resourceTypes = "com/adobe/cqforce/force/identity", extensions = {"json"})
public class IdentityServlet extends DatServlet {
    @Reference
    private ApiAuthService apiAuthService;
    @Reference
    private SecurityService securityService;
    @Reference
    private EventAdmin eventAdmin;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        try {
            String userId = securityService.getUser(request).getUserProfile().getUserId();
            SalesForceService sfService = this.apiAuthService.getSalesForceService(request);
            this.postDatIdentityEvent(userId);
            response.getWriter().println(GsonUtil.toJson(sfService.getIdentity(true)));

        } catch (Exception e) {
            log.error(e.getMessage(),e);
            throw new ServletException("Error while fetching SF identity", e);
        }
    }

    private void postDatIdentityEvent(String userId) {
        final Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put(DatEventConstants.USER_ID, userId);
        Event event = new Event(DatEventConstants.DAT_IDENTITY_VALID_TOPIC, properties);
        this.eventAdmin.postEvent(event);   //postEvent is asynchronous cf. http://experiencedelivers.adobe.com/cemblog/en/experiencedelivers/2012/04/event_handling_incq.html
    }

}
