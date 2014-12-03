package com.adobe.cqforce.security.service;


import com.adobe.granite.crypto.CryptoException;
import com.adobe.granite.crypto.CryptoSupport;
import com.adobe.cqforce.security.service.vo.UserProfile;
import com.adobe.cqforce.util.GsonUtil;
import com.day.cq.security.Authorizable;
import com.day.cq.security.User;
import org.apache.commons.codec.binary.Base64;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.security.SecureRandom;
import java.util.Random;

public class HubUser {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private CryptoSupport cryptoSupport;
    private EventAdmin eventAdmin;

    private User user;     //TODO I tried, failed and wasted some time to get to user JacRabbitUser instead

    public HubUser(User user, CryptoSupport cryptoSupport, EventAdmin eventAdmin) {

        this.user = user;
        this.cryptoSupport = cryptoSupport;
        this.eventAdmin = eventAdmin;
    }

    public UserProfile getUserProfile() throws RepositoryException {

        UserProfile profile = new UserProfile();
        String userId = user.getID();
        profile.setUserId(userId);
        profile.setEmail(user.getProperty(Authorizable.PROPERTY_EMAIL));
        profile.setFirstName(user.getProperty(Authorizable.PROPERTY_FIRST_NAME));
        profile.setLastName(user.getProperty(Authorizable.PROPERTY_LAST_NAME));
        return profile;
    }

    public synchronized void removeProperties(String... propertyNames) throws RepositoryException {

        if (propertyNames != null) {
            for (String property : propertyNames) {
                user.removeProperty(property);
            }
            triggerUserReplicationEvent();
        }
    }


    /**
     * We first append some random bytes to the password.
     * They prevent dictionary attacks,
     * whereby an attacker pre-computes the keys from some common passwords and then tries those keys on the encrypted data.
     * Without salt bytes, the dictionary attack would be worthwhile attack because we use a deliberately slow function
     * to derive a key from a password.
     * With the salt bytes, the attacker is forced to run the slow key derivation function
     * for each password they want to try on each piece of data.
     *
     * @return salt to append to the password before crypting it
     */
    private String getSalt() {
        final Random r = new SecureRandom();
        byte[] salt = new byte[20];
        // according to http://www.javamex.com/tutorials/cryptography/pbe_salt.shtml 20 bytes is optimum
        r.nextBytes(salt);
        return Base64.encodeBase64String(salt);
        //will always returns a 28 char long random salt string
        //hence we use it in the decode function as well to get the length of the salt
    }

    public synchronized void setProperty(String propertyName, Object propertyValue, boolean crypted) throws RepositoryException {

        try {
            String propertyToStore = (propertyValue instanceof String) ? (String) propertyValue : GsonUtil.toJson(propertyValue);
            if (crypted) {
                // adding salt twice
                // a salt before we crypt it.
                // a salt as a prefix in jcr after we crypt it and then a salt
                user.setProperty(propertyName, cryptoSupport.protect(this.getSalt() + propertyToStore));
            } else {
                user.setProperty(propertyName, propertyToStore);
            }
            triggerUserReplicationEvent();
            log.debug(propertyName + " stored in " + user.getID() + " node (encrypted:" + crypted + ")");
        } catch (CryptoException e) {
            throw new RepositoryException(e);
        }
    }

    private void triggerUserReplicationEvent()  throws RepositoryException {

        /* enable this if you have a cluster
        final Dictionary<String,Object> properties = new Hashtable<String, Object>();
        properties.put(HubUserEventConstants.USER_HOME_PATH, user.getHomePath());
        Event event = new Event(HubUserEventConstants.HUB_USER_MODIFIED_EVENT_TOPIC, properties);
        this.eventAdmin.postEvent(event);   //postEvent is asynchronous cf. http://experiencedelivers.adobe.com/cemblog/en/experiencedelivers/2012/04/event_handling_incq.html
        */
    }

    /**
     * Retrieve the property value with the given name.
     *
     * @param propertyName Name of the Property
     * @return String or <code>null</code>if property does not exist or does not have a value
     */
    public synchronized String getProperty(String propertyName, boolean crypted) throws RepositoryException {

        try {
            String property = user.getProperty(propertyName);
            if (property == null) {
                return null;
            } else if (crypted) {
                String decrypted = cryptoSupport.unprotect(property).substring(this.getSalt().length());
                return decrypted;
            } else {
                return property;
            }
        } catch (CryptoException e) {
            throw new RepositoryException(e);
        }
    }


    /**
     * Retrieve the property value with the given name.
     *
     * @param propertyName Name of the Property
     * @return String or <code>null</code>if property does not exist or does not have a value
     */
    public synchronized <T> T getProperty(String propertyName, Class<T> propertyClass, boolean crypted) throws RepositoryException {
        String property = user.getProperty(propertyName);
        if (property == null) {
            return null;
        } else {
            return GsonUtil.fromJson(getProperty(propertyName, crypted), propertyClass);
        }
    }
}
