package com.adobe.cqforce.jcr.service.index;

public interface Indexable {

    String getPropertyValue(String propertyName);

    byte[] getContent();
}
