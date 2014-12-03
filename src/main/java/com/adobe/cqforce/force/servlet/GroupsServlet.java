package com.adobe.cqforce.force.servlet;


import com.adobe.cqforce.force.service.SalesForceService;
import com.adobe.cqforce.force.service.ApiAuthService;
import com.adobe.cqforce.util.GsonUtil;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;

import javax.servlet.ServletException;
import java.io.IOException;

@SlingServlet(methods = {"GET"}, resourceTypes = "com/adobe/cqforce/force/groups", extensions = {"json"})
public class GroupsServlet extends DatServlet
{
    @Reference
    private ApiAuthService apiAuthService;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException,
            IOException
    {
        try
        {
            SalesForceService sfService = this.apiAuthService.getSalesForceService(request);
            response.getWriter().println(GsonUtil.toJson(sfService.getGroups()));

        } catch (Exception e)
        {
            throw new ServletException("Error while fetching groups", e);
        }
    }
}
