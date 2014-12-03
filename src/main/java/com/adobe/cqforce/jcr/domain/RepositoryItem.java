package com.adobe.cqforce.jcr.domain;

import org.apache.jackrabbit.value.BinaryValue;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;


public class RepositoryItem {

    public static final String PROP_FILENAME = "fileName";
    public static final String PROP_FILEDESCRIPTION = "fileDescription";

    /**
     * ID will be used as node name in JCR tree.
     */
    private String id;

    /**
     * Friendly name of the resource.
     */
    private String name;

    /**
     * Custom properties of the resource. They will be persisted as JCR node properties.
     */
    private Map<String, String> properties;

    /**
     * Resource content.
     */
    private byte[] data;

    public RepositoryItem() {
    }

    public RepositoryItem(String id, byte[] data) {
        this.id = id;
        this.data = data;
    }

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

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void addProperty(String propName, String propValue) {
        if (properties == null) {
            properties = new HashMap<String, String>();
        }
        properties.put(propName, propValue);
    }

    public BinaryValue getBinaryValue() {
        return new BinaryValue(new ByteArrayInputStream(data));
    }
}
