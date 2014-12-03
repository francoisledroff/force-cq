package com.adobe.cqforce.jcr.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public enum InstanceType {
    TEST("Application is a TEST instance"),
    PROD("Application is a PROD instance");

    private static final Logger logger = LoggerFactory.getLogger(InstanceType.class);

    private String description;

    InstanceType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static InstanceType getInstanceTypeByName(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            logger.warn("Invalid value [" + name + "] specified for InstanceType...will be using default value [TEST]");
        }
        return TEST;
    }

    public boolean isTest() {
        return this.equals(TEST);
    }

    public boolean isProd() {
        return this.equals(PROD);
    }

}
