package com.adobe.cqforce.jcr.service.impl;

import com.adobe.cqforce.jcr.exception.JcrRepositoryWrappingException;
import com.adobe.cqforce.jcr.service.JcrSessionManager;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.jcr.api.SlingRepository;

import javax.jcr.RepositoryException;
import javax.jcr.Session;


@Component(immediate = true, label = JcrRepositoryServiceImpl.COMPONENT_LABEL, description = JcrRepositoryServiceImpl.COMPONENT_DESC)
@Service(JcrSessionManager.class)
public class JcrSessionManagerImpl implements JcrSessionManager {

    @Reference
    private SlingRepository slingRepository;

    public Session getSession() {
        try {
            return slingRepository.loginAdministrative(null);
        } catch (RepositoryException e) {
            throw new JcrRepositoryWrappingException("Error while retrieving session", e);
        }
    }

    public void closeAndSaveSession(Session session) {
        try {
            if (session != null && session.isLive()) {
                session.save();
                session.logout();
            }
        } catch (RepositoryException e) {
            throw new JcrRepositoryWrappingException("Error while closing session", e);
        }
    }
}
