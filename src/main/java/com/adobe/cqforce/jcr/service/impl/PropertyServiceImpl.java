package com.adobe.cqforce.jcr.service.impl;

import com.adobe.cqforce.jcr.domain.InstanceType;
import com.adobe.cqforce.jcr.domain.LogType;
import com.adobe.cqforce.jcr.service.PropertyService;
import org.apache.felix.scr.annotations.*;
import org.osgi.service.component.ComponentContext;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;


@Component(immediate = false, metatype = true, label = PropertyServiceImpl.COMPONENT_LABEL, description = PropertyServiceImpl.COMPONENT_DESC)
@Service(PropertyService.class)
@Properties({
        @Property(name = PropertyServiceImpl.PROP_LOG_TYPE, value = PropertyServiceImpl.PROP_LOG_TYPE_DEFAULT_VALUE, description = PropertyServiceImpl.PROP_LOG_TYPE_DESCRIPTION),
        @Property(name = PropertyServiceImpl.PROP_INSTANCE_TYPE, value = PropertyServiceImpl.PROP_INSTANCE_TYPE_DEFAULT_VALUE, description = PropertyServiceImpl.PROP_INSTANCE_TYPE_DESCRIPTION),
        @Property(name = PropertyServiceImpl.PROP_CQ_NODES_BASE_URLS, value = PropertyServiceImpl.PROP_CQ_NODES_BASE_URLS_DEFAULT_VALUE, description = PropertyServiceImpl.PROP_CQ_NODES_BASE_URLS_DESCRIPTION)
})
public class PropertyServiceImpl implements PropertyService {

    static final String COMPONENT_LABEL = "Adobe Hub Approval Property Service";
    static final String COMPONENT_DESC = "Cross-concern generic properties.";

    static final String PROP_LOG_TYPE = "LOG_TYPE";
    static final String PROP_LOG_TYPE_DESCRIPTION = "Possible values: NONE, SIMPLE, ALL";
    static final String PROP_LOG_TYPE_DEFAULT_VALUE = "SIMPLE";

    static final String PROP_INSTANCE_TYPE = "INSTANCE_TYPE";
    static final String PROP_INSTANCE_TYPE_DESCRIPTION = "Possible values: TEST, PROD";
    static final String PROP_INSTANCE_TYPE_DEFAULT_VALUE = "TEST";

    static final String PROP_CQ_NODES_BASE_URLS = "CQ_NODES_BASE_URLS";
    static final String PROP_CQ_NODES_BASE_URLS_DESCRIPTION = "Application base URLs for all nodes in the current environment. Values are separated by comma.";
    static final String PROP_CQ_NODES_BASE_URLS_DEFAULT_VALUE = "";

    private LogType logType;
    private InstanceType instanceType;
    private List<String> nodesBaseURLs;
    private String instanceId;

    @Activate
    protected void activate(ComponentContext context) throws Exception {
        Dictionary<?, ?> props = context.getProperties();
        String logTypeValue = props.get(PROP_LOG_TYPE).toString();
        String instanceTypeValue = props.get(PROP_INSTANCE_TYPE).toString();
        String nodeURLs = props.get(PROP_CQ_NODES_BASE_URLS).toString();

        this.logType = LogType.getLogTypeByName(logTypeValue);
        this.instanceType = InstanceType.getInstanceTypeByName(instanceTypeValue);
        this.nodesBaseURLs = getValidTokensFromString(nodeURLs);
        this.instanceId = String.valueOf(getRandomInteger());
    }

    @Override
    public LogType getLogType() {
        return logType;
    }

    @Override
    public InstanceType getInstanceType() {
        return instanceType;
    }

    private List<String> getValidTokensFromString(String nodeURLs) {
        List<String> validTokens = new ArrayList<String>();

        if (nodeURLs != null) {
            String[] tokens = nodeURLs.split(",");
            for (String token : tokens) {
                if (token.trim().length() > 0) {
                    validTokens.add(token.trim());
                }
            }
        }

        return validTokens;
    }

    @Override
    public List<String> getNodesBaseURLs() {
        return nodesBaseURLs;
    }


    public String getInstanceId() {
        return instanceId;
    }

    /**
     * Generate an int between 0 and 1000000.
     */
    private int getRandomInteger(){
        int maxValue = 1000000;
        return (int)(Math.random() * maxValue);
    }
}
