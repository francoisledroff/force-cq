package com.adobe.cqforce.security.service.impl;

import java.util.Dictionary;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import com.adobe.cqforce.security.service.HubUser;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.jcr.resource.JcrResourceResolverFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.crypto.CryptoSupport;
import com.adobe.cqforce.security.service.SecurityService;
import com.adobe.cqforce.security.service.vo.Credentials;
import com.day.cq.security.User;

@SuppressWarnings("deprecation")
@Component(immediate = true, metatype = true, label = SecurityServiceImpl.COMPONENT_LABEL, description = SecurityServiceImpl.COMPONENT_DESC)
@Service(SecurityService.class)
public class SecurityServiceImpl implements SecurityService {
    static final String COMPONENT_LABEL = "Adobe Hub Security Service";
    static final String COMPONENT_DESC = "This service supports Hub Security concerns.";

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Reference
    private CryptoSupport cryptoSupport;

    @Reference
    private EventAdmin eventAdmin;

    @Reference
    private SlingRepository repo;

    @Reference
    private JcrResourceResolverFactory resolverFactory;


    @Override
    public boolean validateSession(SlingHttpServletRequest request) {
        ResourceResolver resolver = request.getResourceResolver();
        User user = resolver.adaptTo(User.class);
        return (null != user && !user.getID().equals("anonymous"));
    }

    @Override
    public synchronized Credentials getCredentials(SlingHttpServletRequest request) {
        if (!validateSession(request)) {
            throw new SecurityException("Missing Authentication");
        }

        ResourceResolver resolver = request.getResourceResolver();
        User user = resolver.adaptTo(User.class);
        Credentials credentials = new Credentials(user.getID());
        log.debug(credentials + "retrieved");
        return credentials;
    }

    @Override
    public HubUser getUser(SlingHttpServletRequest request) {
        ResourceResolver resolver = request.getResourceResolver();

        /* UserManager userManager = resolver.adaptTo(UserManager.class);*/

        User user = resolver.adaptTo(User.class);
        return new HubUser(user, this.cryptoSupport, this.eventAdmin);
    }

    @Override
    public HubUser getUser(String userId){
        Session adminSession = null;
        Session userSession = null;
        try {
            adminSession = repo.loginAdministrative(null);
            userSession = adminSession.impersonate(new SimpleCredentials(userId, "".toCharArray()));
            adminSession.logout();
            adminSession = null;
            ResourceResolver userResourceResolver = resolverFactory.getResourceResolver(userSession);
            User user = userResourceResolver.adaptTo(User.class);
            return new HubUser(user, this.cryptoSupport, this.eventAdmin);

        } catch (RepositoryException e) {
            throw new RuntimeException("User "+userId+ " was not found",e);
        } finally {
            if (adminSession != null) {
                adminSession.logout();
            }
            //TODO polish that and do the finally somewhere through the anonymous api auth servlet
            /*if (userSession != null) {
                userSession.logout();
            }*/
        }
    }

    @Activate
    protected void activate(ComponentContext context) {

        Bundle thisBun = context.getBundleContext().getBundle();
        Bundle[] bundles = context.getBundleContext().getBundles();

        for (int i = 0; i < bundles.length; i++) {
            Bundle bun = bundles[i];

            // Skip this bundle.
            if (bun.equals(thisBun))
                continue;

            // Skip fragments.
            Dictionary hdrs = bun.getHeaders();
            if (hdrs.get("Fragment-Host") != null)
                continue;

            if (bun.getState() == Bundle.INSTALLED || bun.getState() == Bundle.RESOLVED) {
                if (bun.getSymbolicName().startsWith("com.adobe")) {
                    log.info("Activating: " + bun.getSymbolicName());
                    try {
                        bun.start();
                    } catch (BundleException e) {
                        log.warn("Activation exception on: " + bun.getSymbolicName());
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
