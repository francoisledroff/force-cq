package com.adobe.cqforce.force.service.impl;

import com.adobe.cqforce.force.service.ApiAuthService;
import com.adobe.cqforce.force.service.SalesForceService;
import com.adobe.cqforce.force.service.UserService;
import com.adobe.cqforce.jcr.service.RepositoryService;
import com.adobe.cqforce.jcr.service.index.IndexImpl;
import com.adobe.cqforce.jcr.service.index.IndexService;
import com.adobe.cqforce.jcr.service.index.IndexServiceImpl;
import com.adobe.cqforce.force.domain.User;
import com.adobe.cqforce.security.service.HubUser;
import com.adobe.cqforce.security.service.SecurityService;
import com.adobe.cqforce.util.GsonUtil;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.*;


@Component(immediate = true, label = UserServiceImpl.COMPONENT_LABEL, description = UserServiceImpl.COMPONENT_DESC)
@Service(UserService.class)
public class UserServiceImpl implements UserService {

    static final String COMPONENT_LABEL = "Adobe Hub SF Users service";
    static final String COMPONENT_DESC = "Adobe Hub SF Users service";
    /**
     * Cache root for DAT. All entries will be stored under the entry with this name.
     */
    private final static String DAT_CACHE_ROOT = "dat";
    /**
     * Prefix for all DAT index names.
     */
    private final static String DAT_CACHE_IDX_PREFIX = "idx_";
    /**
     * Used for proper de-serialisation into the desired data type.
     */
    private final static Type USER_LIST_DATA_TYPE = new TypeToken<List<User>>() {
    }.getType();
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * Used for indexing content for user lists. If necessary make this an OSGi service.
     */
    private IndexService idxService = new IndexServiceImpl(new IndexImpl());
    @Reference
    private ApiAuthService apiAuthService;
    @Reference
    private RepositoryService repositoryService;
    @Reference
    private SecurityService securityService;

    @Override
    public List<User> getUserList(String currentUserId, String filter) {
        long t1 = System.currentTimeMillis();

        List<User> userList;
        try {

            if (filter == null || filter.isEmpty()) {
                userList = getUserListAll(currentUserId);

            } else {
                userList = getUserListByFilter(currentUserId, filter);
            }

        } catch (RepositoryException e) {
            throw new RuntimeException("Error while fetching user list", e);

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Error while fetching user list", e);
        }

        logger.debug("DAT - user search by filter took [" + (System.currentTimeMillis() - t1) + "]ms");
        return userList;
    }

    @Override
    public void loadUserList(String currentUserId) {
        try {
            if (isEmptyCache()) {
                reloadCache(currentUserId);
            }

        } catch (RepositoryException e) {
            throw new RuntimeException("Error while loading user list", e);
        }
    }

    @Override
    public User getUserByUsername(String currentUserId, String username) {
        long t1 = System.currentTimeMillis();
        User user = null;

        try {
            if (username != null) {
                String idxKey = idxService.getIndexKey(username);
                String idxContent = getIndexedContent(currentUserId, User.PROPERTY_USERNAME, idxKey);

                if (idxContent != null) {
                    user = getUserByProperty(idxContent, User.PROPERTY_USERNAME, username.trim());
                }
            }

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Error while fetching user details", e);

        } catch (RepositoryException e) {
            throw new RuntimeException("Error while fetching user details", e);
        }

        logger.debug("DAT - search took [" + (System.currentTimeMillis() - t1) + "]ms");
        return user;
    }

    private User getUserByProperty(String deserializedUserList, String propertyName, String propertyValue) {
        List<User> userList = deserializeUserList(deserializedUserList);

        for (User user : userList) {
            if (propertyValue.equalsIgnoreCase(user.getPropertyValue(propertyName))) {
                return user;
            }
        }
        return null;
    }

    /**
     * Used for retrieving users' list. First it tries to fetch data from local cache. If data not available in cache
     * is retrieves the list of users from SF and then stores it in the cache.
     * <p/>
     * Get data from cache using default expiration period (one day).
     */
    private List<User> getUserListByFilter(String currentUserId, String filter)
            throws RepositoryException, UnsupportedEncodingException {

        if (isEmptyCache()) {
            reloadCache(currentUserId);
        }

        //using set to filter duplicates
        Set<User> userSet = new HashSet<User>();
        for (String property : User.INDEXABLE_PROPERTIES) {
            userSet.addAll(getUserListFromCacheByFilter(property, filter));
        }

        return new ArrayList<User>(userSet);
    }

    private List<User> getUserListAll(String currentUserId)
            throws RepositoryException, UnsupportedEncodingException {

        if (isEmptyCache()) {
            reloadCache(currentUserId);
        }

        String content = getContentFromCachePath(DAT_CACHE_ROOT, getIdxNameForProperty(User.PROPERTY_ALL));
        if (content != null) {
            return deserializeUserList(content);
        }

        return null;
    }

    private List<User> getUserListFromCacheByFilter(String propertyName, String filter)
            throws UnsupportedEncodingException {

        String content = getContentFromCachePath(DAT_CACHE_ROOT, getIdxNameForProperty(propertyName), idxService.getIndexKey(filter));

        if (content != null) {
            List<User> userList = deserializeUserList(content);
            return getFilteredUserList(userList, propertyName, filter);
        }

        return new ArrayList<User>();
    }

    private String getIndexedContent(String currentUserId, String propertyName, String key)
            throws RepositoryException, UnsupportedEncodingException {

        if (isEmptyCache()) {
            reloadCache(currentUserId);
        }

        return getContentFromCachePath(DAT_CACHE_ROOT, getIdxNameForProperty(propertyName), key);
    }

    private String getContentFromCachePath(String... path) throws UnsupportedEncodingException {
        byte[] data = repositoryService.getObject(path);

        if (data != null) {
            return new String(data, "UTF-8");
        }
        return null;
    }

    private boolean isEmptyCache() {
        return !repositoryService.exists(DAT_CACHE_ROOT);
    }

    private synchronized void reloadCache(String currentUserId) throws RepositoryException {
        Long t1 = System.currentTimeMillis();

        // re-validating cache content to avoid multiple threads (waiting for this method's lock to be released)
        // saving the same content.
        if (isEmptyCache()) {
            HubUser currentUser = this.securityService.getUser(currentUserId);
            SalesForceService sfService = this.apiAuthService.getSalesForceService(currentUser);
            List<User> usersList = sfService.getUsers();

            //create and store indexes in JCR
            for (String property : User.INDEXABLE_PROPERTIES) {

                Map<String, byte[]> indexedContent = idxService.getIndexedContent(usersList, property);
                repositoryService.saveObjects(indexedContent, DAT_CACHE_ROOT, getIdxNameForProperty(property));
            }

            //also store all objects into one entry
            Map<String, byte[]> all = idxService.getIndexedContentAll(usersList, getIdxNameForProperty(User.PROPERTY_ALL));
            repositoryService.saveObjects(all, DAT_CACHE_ROOT);

            logger.debug("DAT - cache reload took [" + (System.currentTimeMillis() - t1) + "]ms");
        }
    }

    private List<User> deserializeUserList(String serialized) {
        return GsonUtil.fromJson(serialized, USER_LIST_DATA_TYPE);
    }

    private List<User> getFilteredUserList(List<User> userList, final String propertyName, final String filter) {

        CollectionUtils.filter(userList, new Predicate() {
            @Override
            public boolean evaluate(Object object) {
                User user = (User) object;
                String value = user.getPropertyValue(propertyName);

                return value.toLowerCase().startsWith(filter.toLowerCase());
            }
        });

        return userList;
    }

    private String getIdxNameForProperty(String propertyName) {
        return DAT_CACHE_IDX_PREFIX + propertyName;
    }

    public void setIdxService(IndexService idxService) {
        this.idxService = idxService;
    }
}
