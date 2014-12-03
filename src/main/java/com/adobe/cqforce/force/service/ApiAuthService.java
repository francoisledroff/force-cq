package com.adobe.cqforce.force.service;

import com.adobe.cqforce.security.service.HubUser;
import com.force.api.ApiConfig;
import org.apache.sling.api.SlingHttpServletRequest;

import javax.jcr.RepositoryException;

public interface ApiAuthService
{
    ApiConfig getApiConfig();

    SalesForceService getSalesForceService(SlingHttpServletRequest request) throws RepositoryException;

    public SalesForceService getSalesForceService(HubUser user);

    String getSoapServiceEndpoint();
}
