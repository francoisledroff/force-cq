package com.adobe.cqforce.force.service.impl;

import com.adobe.cqforce.force.service.ApiAuthService;
import com.adobe.cqforce.force.service.SalesForceService;
import com.adobe.cqforce.jcr.service.PropertyService;
import com.adobe.cqforce.force.service.SOQLQueryService;
import com.adobe.cqforce.security.service.HubUser;
import com.adobe.cqforce.security.service.SecurityService;
import com.force.api.ApiConfig;
import com.force.api.ApiVersion;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.SlingHttpServletRequest;
import org.osgi.service.component.ComponentContext;

import javax.jcr.RepositoryException;
import java.util.Dictionary;


@Component(immediate = true, metatype = true, label = ApiAuthServiceImpl.COMPONENT_LABEL, description = ApiAuthServiceImpl.COMPONENT_DESC)
@Properties({
        @Property(name = ApiAuthServiceImpl.LOGIN_HOST_KEY, value = ApiAuthServiceImpl.LOGIN_HOST_DEFAULT, description = "SalesForce Login Host"),
        @Property(name = ApiAuthServiceImpl.CONSUMER_KEY_KEY, value = ApiAuthServiceImpl.CONSUMER_KEY_DEFAULT, description = "Adobe Hub SalesForce ConsumerKey"),
        @Property(name = ApiAuthServiceImpl.SECRET_KEY_KEY, value = ApiAuthServiceImpl.SECRET_KEY_DEFAULT, description = "Adobe Hub SalesForce SECRET Key"),
        @Property(name = ApiAuthServiceImpl.REDIRECT_URI_KEY, value = ApiAuthServiceImpl.REDIRECT_URI_DEFAULT, description = "Adobe Hub SalesForce redirect_uri"),
        @Property(name = ApiAuthServiceImpl.API_VERSION_KEY, value = ApiAuthServiceImpl.API_VERSION_DEFAULT, description = "SalesForce API version (v27.0 for now)")
})
@Service(ApiAuthService.class)
public class ApiAuthServiceImpl implements ApiAuthService
{
    static final String COMPONENT_LABEL = "Adobe Hub SalesForce API Authentication service";
    static final String COMPONENT_DESC = "Adobe Hub SalesForce API Authentication service";

    static final String LOGIN_HOST_KEY = "loginHost";
    static final String LOGIN_HOST_DEFAULT = "https://yourinstanceof.salesforce.com";

    static final String CONSUMER_KEY_KEY = "consumerKey";
    static final String CONSUMER_KEY_DEFAULT = "your-salesforce-remoteapp-consumer-key";

    static final String SECRET_KEY_KEY = "secretKey";
    static final String SECRET_KEY_DEFAULT = "your-salesforce-remoteapp-secret-key";

    static final String REDIRECT_URI_KEY = "redirect_uri";
    static final String REDIRECT_URI_DEFAULT = "https://yourserver/content/force/api/cqforce/apiauth.json";

    static final String API_VERSION_KEY = "apiVersion";
    static final String API_VERSION_DEFAULT = "v27.0";

    private ApiConfig apiConfig;
    private String soapServiceEndpoint;

    @Reference
    private SecurityService securityService;

    @Reference
    private SOQLQueryService queryService;

    @Reference
    private PropertyService propertyService;

    @Activate
    protected void activate(ComponentContext context) throws Exception
    {
        Dictionary<?, ?> props = context.getProperties();
        apiConfig = new ApiConfig();
        apiConfig.setClientId(props.get(CONSUMER_KEY_KEY).toString());
        apiConfig.setClientSecret(props.get(SECRET_KEY_KEY).toString());
        apiConfig.setLoginEndpoint(props.get(LOGIN_HOST_KEY).toString());
        apiConfig.setRedirectURI(props.get(REDIRECT_URI_KEY).toString());
        apiConfig.setApiVersion(new ApiVersion(props.get(API_VERSION_KEY).toString()));

    }

    @Override
    public ApiConfig getApiConfig()
    {
        return this.apiConfig;
    }

    @Override
    public SalesForceService getSalesForceService(SlingHttpServletRequest request) throws RepositoryException
    {
        HubUser user = this.securityService.getUser(request);
        return new SalesForceService(user, this.apiConfig, this.queryService);
    }

    @Override
    public SalesForceService getSalesForceService(HubUser user)
    {
        return new SalesForceService(user, this.apiConfig, this.queryService);
    }

    @Override
    public String getSoapServiceEndpoint()
    {
        return soapServiceEndpoint;
    }
}
