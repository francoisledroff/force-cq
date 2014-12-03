package com.adobe.cqforce.force.domain;

import com.adobe.cqforce.jcr.service.index.Indexable;
import com.adobe.cqforce.util.GsonUtil;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.beanutils.PropertyUtils;


public class User implements Indexable {

    public static final String PROPERTY_USERNAME = "username";
    public static final String PROPERTY_FIRSTNAME = "firstName";
    public static final String PROPERTY_LASTNAME = "lastName";
    public static final String PROPERTY_NAME = "name";

    public static final String PROPERTY_ALL = "all";

    public static final String[] INDEXABLE_PROPERTIES = new String[]{
            PROPERTY_USERNAME, PROPERTY_FIRSTNAME, PROPERTY_LASTNAME, PROPERTY_NAME
    };

    @SerializedName("Id")
    private String id;

    @SerializedName("Name")
    private String name;

    @SerializedName("FirstName")
    private String firstName;

    @SerializedName("LastName")
    private String lastName;

    @SerializedName("Username")
    private String username;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return GsonUtil.toJson(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (!id.equals(user.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String getPropertyValue(String propertyName) {
        try {
            return (String) PropertyUtils.getProperty(this, propertyName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] getContent() {
        return toString().getBytes();
    }
}
