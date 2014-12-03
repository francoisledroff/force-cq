package com.adobe.cqforce.force.service;

import com.force.api.ApiSession;
import com.force.api.ForceApiCallback;

import javax.jcr.RepositoryException;

/**
 * Callback used in force-api. It's purpose is to update the oauth token stored in JCR, if force-api detects that
 * the token is old/expired.
 */
public class DatCallback implements ForceApiCallback {

    private SalesForceService sfService;

    public DatCallback(SalesForceService sfService) {
        this.sfService = sfService;
    }

    @Override
    public void execute(ApiSession apiSession) {
        try {
            if (isValidApiSession(apiSession) && !sfService.getApiSession().equals(apiSession)) {

                //update only the access token; leave the refresh token unchanged
                ApiSession storedSession = sfService.getApiSession();
                storedSession.setAccessToken(apiSession.getAccessToken());

                sfService.setApiSession(storedSession);
            }
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }


    private boolean isValidApiSession(ApiSession apiSession) {
        return apiSession != null && apiSession.getAccessToken() != null;
    }
}
