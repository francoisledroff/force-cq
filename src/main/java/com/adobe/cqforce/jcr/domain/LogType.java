package com.adobe.cqforce.jcr.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public enum LogType {

    NONE("No information will be displayed to the user"),
    SIMPLE("A simple generic message wil be displayed"),
    ALL("Full messages - with error stack traces - will be displayed");

    private static final Logger logger = LoggerFactory.getLogger(LogType.class);

    private String description;

    LogType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static LogType getLogTypeByName(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            logger.warn("Invalid value [" + name + "] specified for LogType...will be using default value [NONE]");
        }
        //default value
        return NONE;
    }

    public boolean isNone() {
        return this.equals(NONE);
    }

    public boolean isSimple() {
        return this.equals(SIMPLE);
    }

    public boolean isAll() {
        return this.equals(ALL);
    }

}
