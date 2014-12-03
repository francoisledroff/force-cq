package com.adobe.cqforce.jcr.service.index;

import org.apache.commons.lang.StringUtils;


public class IndexImpl implements Index {

    public final static int INDEX_LENGTH = 2;

    @Override
    public String getIndexKey(String value) {
        if (value != null) {
            String trimValue = value.trim().toLowerCase();

            if (trimValue.length() > INDEX_LENGTH) {
                //yes, some names have the format "x ytf". A trim is necessary.
                return trimValue.substring(0, INDEX_LENGTH).trim();
            }
            return trimValue;
        }

        return null;
    }

    @Override
    public boolean isValidIndexKey(String key) {
        return StringUtils.isAlphanumeric(key);
    }

}
