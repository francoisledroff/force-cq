package com.adobe.cqforce.force.servlet;


import com.adobe.cqforce.util.Constants;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.IOException;


public abstract class DatServlet extends SlingAllMethodsServlet {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void service(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        try {
            response.setContentType(Constants.CONTENT_TYPE_JSON);

            super.service(request, response);

        } catch (SecurityException e) {
            response.setStatus(SlingHttpServletResponse.SC_FORBIDDEN);
            response.getWriter().println(e.getMessage());

        } catch (Exception e) {
            response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println(e.getMessage());
        }
    }


}
