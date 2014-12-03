package com.adobe.cqforce.jcr.service;

import com.adobe.cqforce.jcr.domain.InstanceType;
import com.adobe.cqforce.jcr.domain.LogType;

import java.util.List;


public interface PropertyService {

    LogType getLogType();

    InstanceType getInstanceType();

    /**
     * Base application URLs for all nodes in an AdobeHub application environment.
     */
    List<String> getNodesBaseURLs();

    /**
     * Retrieve the application instance ID. This should uniquely identify an application
     * instance.
     */
    String getInstanceId();

}
