package com.adobe.cqforce.jcr.service;

import com.adobe.cqforce.jcr.domain.RepositoryItem;
import com.adobe.cqforce.jcr.exception.JcrRepositoryWrappingException;

import java.util.List;
import java.util.Map;

/**
 * Simple repository definition. It can store any type of data specified as byte array. Data to be stored
 * is specified in an "independent format" (byte array) to avoid class-loading issues.
 * <p/>
 * E.g. We can store serialized objects in JCR. But if we try to de-serialize the objects to classes loaded by another
 * module's class-loader, the operation will fail.
 * <p/>
 * Each class using the service is responsible for transforming the byte array into the desired output.
 * <p/>
 * All objects also have an expiration period.
 * <p/>
 */
public interface RepositoryService {

    /**
     * Get content from path specified as parameter. Path is always relative to cache root node.
     */
    byte[] getObject(String... path) throws JcrRepositoryWrappingException;

    /**
     * Get data by ID using the expiration period specified as parameter.
     *
     * @param id               ID of the object
     * @param expirationPeriod Expiration period (in milliseconds)
     * @return
     * @throws JcrRepositoryWrappingException
     */
    byte[] getObject(String id, long expirationPeriod) throws JcrRepositoryWrappingException;

    /**
     * Save multiple objects as children nodes in the parent node specified as parameter.
     * The children nodes will be created from all (key, value) map pairs.
     */
    void saveObjects(Map<String, byte[]> data, String... path) throws JcrRepositoryWrappingException;

    void saveObjects(List<RepositoryItem> itemList, String... path) throws JcrRepositoryWrappingException;

    boolean exists(String id) throws JcrRepositoryWrappingException;

    boolean exists(String id, long expirationPeriod) throws JcrRepositoryWrappingException;

    List<String> getChildrenNames(String... path) throws JcrRepositoryWrappingException;

    void deleteNode(String... path) throws JcrRepositoryWrappingException;

    RepositoryItem getRepositoryItem(String nodePath);

    void removeUser(String ldapId) throws JcrRepositoryWrappingException;
}
