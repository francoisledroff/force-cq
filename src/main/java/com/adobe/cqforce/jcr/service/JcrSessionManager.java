package com.adobe.cqforce.jcr.service;

import com.adobe.cqforce.jcr.exception.JcrRepositoryWrappingException;

import javax.jcr.Session;


public interface JcrSessionManager {

    Session getSession() throws JcrRepositoryWrappingException;

    void closeAndSaveSession(Session session) throws JcrRepositoryWrappingException;
}
