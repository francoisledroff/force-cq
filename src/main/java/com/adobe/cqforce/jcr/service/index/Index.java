package com.adobe.cqforce.jcr.service.index;

/**
 * Simple index definition. Specifies index creation and validation rules.
 */
public interface Index {

    String getIndexKey(String value);

    boolean isValidIndexKey(String key);
}
