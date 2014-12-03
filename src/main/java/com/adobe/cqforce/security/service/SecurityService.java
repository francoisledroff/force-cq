package com.adobe.cqforce.security.service;

import com.adobe.cqforce.security.service.vo.Credentials;
import org.apache.sling.api.SlingHttpServletRequest;

public interface SecurityService
{
    boolean validateSession(SlingHttpServletRequest request);

    /**
     *
     * @param request
     * @return the current user Credentials (will not return null, it will throw an exception instead)
     * @throws SecurityException if the session was not valid, or if the Credentials could not be retrieved
     */
    Credentials getCredentials(SlingHttpServletRequest request);

    HubUser getUser(SlingHttpServletRequest request);

    HubUser getUser(String userId);
}
