package com.adobe.cqforce.jcr.service.impl;

import com.adobe.cqforce.jcr.exception.JcrRepositoryWrappingException;
import com.adobe.cqforce.jcr.service.JcrSessionManager;
import com.adobe.cqforce.jcr.domain.RepositoryItem;
import com.adobe.cqforce.jcr.service.RepositoryService;
import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

import javax.jcr.*;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeType;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


@Component(immediate = true, label = JcrRepositoryServiceImpl.COMPONENT_LABEL, description = JcrRepositoryServiceImpl.COMPONENT_DESC)
@Service(RepositoryService.class)
public class JcrRepositoryServiceImpl implements RepositoryService {

    static final String COMPONENT_LABEL = "Adobe Hub Simple JCR service";
    static final String COMPONENT_DESC = "Adobe Hub Simple JCR service";

    /**
     * Default expiration period (in milliseconds): 3 days
     */
    public static final long DEFAULT_EXPIRATION_PERIOD = 1000 * 60 * 60 * 24 * 3;

    /**
     * Root JCR node where data will be saved. This can be an OSGi property.
     */
    private static final String JCR_ROOT_HUB_NODE = "/tmp";

    @Reference
    private JcrSessionManager sessionManager;

    @Override
    public byte[] getObject(String... path) throws JcrRepositoryWrappingException {
        StringBuilder sb = new StringBuilder();

        for (String nodeName : path) {
            sb.append(nodeName + "/");
        }

        return getObject(sb.toString(), DEFAULT_EXPIRATION_PERIOD);
    }

    @Override
    public boolean exists(String id) throws JcrRepositoryWrappingException {
        return exists(id, DEFAULT_EXPIRATION_PERIOD);
    }

    @Override
    public boolean exists(String id, long expirationPeriod) {
        Session session = null;
        try {
            session = getSession();
            Node node = getNodeRelativeToCacheRoot(session, id);

            if (node != null) {

                if (!isExpired(node, expirationPeriod)) {
                    return true;
                }

                removeNode(session, id);
            }
            return false;

        } catch (RepositoryException e) {
            throw new JcrRepositoryWrappingException("Error while checking if node exists", e);

        } finally {
            closeAndSaveSession(session);
        }
    }

    @Override
    public byte[] getObject(String id, long expirationPeriod) {
        Session session = null;
        try {
            session = getSession();
            Node node = getNodeRelativeToCacheRoot(session, id);

            if (node != null) {

                if (!isExpired(node, expirationPeriod)) {
                    return getContentFromNode(node);
                }
                removeNode(session, id);
            }
            return null;

        } catch (RepositoryException e) {
            throw new JcrRepositoryWrappingException("Error while retrieving object", e);

        } catch (IOException e) {
            throw new JcrRepositoryWrappingException("Error while retrieving object", e);

        } finally {
            closeAndSaveSession(session);
        }
    }

    @Override
    public void saveObjects(List<RepositoryItem> itemList, String... path) throws JcrRepositoryWrappingException {
        Session session = null;
        try {
            session = getSession();
            Node node = getPathToNode(session, path);

            for (RepositoryItem item : itemList) {

                Node childNode = node.addNode(item.getId(), NodeType.NT_UNSTRUCTURED);
                setNodeProperties(childNode, item);
            }

        } catch (PathNotFoundException e) {
            throw new JcrRepositoryWrappingException("Error while saving object", e);

        } catch (ConstraintViolationException e) {
            throw new JcrRepositoryWrappingException("Error while saving object", e);

        } catch (RepositoryException e) {
            throw new JcrRepositoryWrappingException("Error while saving object", e);

        } finally {
            closeAndSaveSession(session);
        }
    }

    @Override
    public void saveObjects(Map<String, byte[]> dataMap, String... path) throws JcrRepositoryWrappingException {
        List<RepositoryItem> itemList = getRepositoryItemListFromMap(dataMap);

        saveObjects(itemList, path);
    }

    @Override
    public List<String> getChildrenNames(String... path) throws JcrRepositoryWrappingException {
        List<String> childrenList = new ArrayList<String>();

        Session session = null;
        try {
            session = getSession();
            Node rootNode = getRootNode(session);

            String relPathToNode = "";
            for (String nodeName : path) {
                relPathToNode += nodeName + "/";
            }

            if (!rootNode.hasNode(relPathToNode)) {
                return childrenList;
            }

            Node child = rootNode.getNode(relPathToNode);
            NodeIterator iterator = child.getNodes();

            while (iterator.hasNext()) {
                childrenList.add(iterator.nextNode().getName());
            }

        } catch (RepositoryException e) {
            throw new JcrRepositoryWrappingException("Error while saving object", e);

        } finally {
            closeAndSaveSession(session);
        }

        return childrenList;
    }


    @Override
    public void deleteNode(String... path) throws JcrRepositoryWrappingException {
        Session session = null;

        try {
            session = getSession();
            Node rootNode = getRootNode(session);

            String relPathToNode = "";
            for (String nodeName : path) {
                relPathToNode += nodeName + "/";
            }

            if (rootNode.hasNode(relPathToNode)) {
                Node child = rootNode.getNode(relPathToNode);
                child.remove();
            }
        } catch (RepositoryException e) {
            throw new JcrRepositoryWrappingException("Error while saving object", e);

        } finally {
            closeAndSaveSession(session);
        }
    }

    private Node getPathToNode(Session session, String... path) throws RepositoryException {
        Node node = getRootNode(session);

        for (String nodeName : path) {
            if (!node.hasNode(nodeName)) {
                node = node.addNode(nodeName, NodeType.NT_UNSTRUCTURED);
                setBaseNodeProperties(node, null);

            } else {
                node = node.getNode(nodeName);
            }
        }
        return node;
    }

    private Node getNodeRelativeToCacheRoot(Session session, String id) {
        try {
            Node rootNode = getRootNode(session);

            if (rootNode.hasNode(id)) {
                return rootNode.getNode(id);
            }

            return null;

        } catch (RepositoryException e) {
            throw new JcrRepositoryWrappingException("Error while getting node", e);
        }
    }

    @Override
    public RepositoryItem getRepositoryItem(String nodePath) {
        Session session = null;
        try {
            session = getSession();
            Node node = getRootNode(session);

            if (node.hasNode(nodePath)) {
                Node itemNode = node.getNode(nodePath);
                return getRepositoryItemFromNode(itemNode);
            }

        } catch (RepositoryException e) {
            throw new JcrRepositoryWrappingException("Error while retrieving object", e);

        } catch (IOException e) {
            throw new JcrRepositoryWrappingException("Error while retrieving object", e);

        } finally {
            closeAndSaveSession(session);
        }

        return null;
    }

    private RepositoryItem getRepositoryItemFromNode(Node node) throws RepositoryException, IOException {
        RepositoryItem item = new RepositoryItem();

        item.setData(getContentFromNode(node));
        item.setId(node.getName());

        String itemName = node.getProperty(RepositoryItem.PROP_FILENAME).getString();
        item.setName(itemName);

        return item;
    }

    /**
     * Is the expiration period (specified as parameter) lower than the time passed between
     * last modification and now ?
     */
    private boolean isExpired(Node node, long expirationPeriod) throws RepositoryException {
        Property property = node.getProperty(Property.JCR_LAST_MODIFIED);

        long value = property.getLong();
        long diff = System.currentTimeMillis() - value;

        return diff > expirationPeriod;
    }

    /**
     * Get binary data from "jcr:data" node property.
     */
    private byte[] getContentFromNode(Node node) throws RepositoryException, IOException {

        if (node.hasProperty(Property.JCR_DATA)) {

            InputStream is = node.getProperty(Property.JCR_DATA).getBinary().getStream();
            return IOUtils.toByteArray(is);
        }
        return null;
    }

    private Node getRootNode(Session session) throws RepositoryException {
        return session.getNode(JCR_ROOT_HUB_NODE);
    }

    private void setBaseNodeProperties(Node node, Value value) throws RepositoryException {
        node.setProperty(Property.JCR_ENCODING, "UTF-8");
        node.setProperty(Property.JCR_LAST_MODIFIED, System.currentTimeMillis());

        if (value != null) {
            node.setProperty(Property.JCR_DATA, value);
        }
    }

    private void setNodeProperties(Node node, RepositoryItem item) throws RepositoryException {
        setBaseNodeProperties(node, item.getBinaryValue());

        if (item.getProperties() != null) {
            Iterator<String> keys = item.getProperties().keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                String value = item.getProperties().get(key);
                node.setProperty(key, value);
            }
        }
    }

    private synchronized void removeNode(Session session, String id) throws RepositoryException {
        Node rootNode = getRootNode(session);

        if (rootNode.hasNode(id)) {
            NodeIterator iterator = rootNode.getNodes();

            while (iterator.hasNext()) {
                Node node = iterator.nextNode();

                if (node.getName().startsWith(id)) {
                    node.remove();
                }
            }
        }
    }

    private List<RepositoryItem> getRepositoryItemListFromMap(Map<String, byte[]> data) {
        List<RepositoryItem> itemList = new ArrayList<RepositoryItem>();
        Iterator<String> keys = data.keySet().iterator();

        while (keys.hasNext()) {
            String key = keys.next();
            itemList.add(new RepositoryItem(key, data.get(key)));
        }

        return itemList;
    }

    @Override
    public void removeUser(String ldapId) throws JcrRepositoryWrappingException {
        Session adminSession = sessionManager.getSession();

        try {
            String pathToHomeDir = "/home/users/" + ldapId;
            Node node = adminSession.getNode(pathToHomeDir);
            node.remove();

        } catch (RepositoryException e) {
            throw new JcrRepositoryWrappingException("Exception while removing user data from JCR", e);

        } finally {
            sessionManager.closeAndSaveSession(adminSession);
        }
    }

    private Session getSession() {
        return sessionManager.getSession();
    }

    private void closeAndSaveSession(Session session) {
        sessionManager.closeAndSaveSession(session);
    }
}
