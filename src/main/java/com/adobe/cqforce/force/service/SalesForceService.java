package com.adobe.cqforce.force.service;

import com.adobe.cqforce.force.domain.Group;
import com.adobe.cqforce.force.domain.User;

import com.adobe.cqforce.security.service.HubUser;
import com.force.api.*;

import javax.jcr.RepositoryException;
import java.util.List;

import static com.adobe.cqforce.force.Constant.SF_API_SESSION;
import static com.adobe.cqforce.force.Constant.SF_IDENTITY;



public class SalesForceService
{
    private HubUser hubUser;

    private static final String GET_GROUPS_SOQL_QUERY_PATH = "/content/force/api/cqforce/queries/getGroups/query.soql.txt";
    private static final String GET_USERS_SOQL_QUERY_PATH = "/content/force/api/cqforce/queries/getUsers/query.soql.txt";

    private ApiConfig apiConfig;
    private SOQLQueryService queryService;
    private ForceApiCallback callback;

    public SalesForceService(HubUser hubUser, ApiConfig apiConfig, SOQLQueryService queryService)
    {
        this.hubUser = hubUser;
        this.apiConfig = apiConfig;
        this.queryService = queryService;
        callback = new DatCallback(this);
    }

    public ApiSession getApiSession() throws RepositoryException
    {
        return hubUser.getProperty(SF_API_SESSION, ApiSession.class, true);
    }

    public void setApiSession(ApiSession apiSession) throws RepositoryException
    {
        hubUser.setProperty(SF_API_SESSION, apiSession, true);
        this.setIdentity();
    }

    public boolean isHubAuthorizedInSalesForce() throws RepositoryException
    {
        //how can we also check if the existent session (oauth token) did not expire?
        //if the session is expired, every query with the existent tokens will throw
        //an exception with invalid session message.
        return getApiSession() != null;
    }

    public void removeSalesForceTokens()  throws RepositoryException
    {
        hubUser.removeProperties(SF_API_SESSION, SF_IDENTITY);
    }

    private Identity setIdentity() throws RepositoryException
    {
        Identity identity = null;
        ForceApi forceApi = this.getForceApi();

        if (forceApi != null)
        {
            identity = forceApi.getIdentity();
            hubUser.setProperty(SF_IDENTITY, identity, false);
        }
        return identity;
    }

    public Identity getIdentity() throws  RepositoryException
    {
        return getIdentity(false);
    }

    public Identity getIdentity(boolean refresh) throws  RepositoryException
    {
        return (refresh) ? setIdentity() : hubUser.getProperty(SF_IDENTITY, Identity.class, false);
    }

    public ForceApi getForceApi() throws RepositoryException
    {
        ApiSession apiSession = this.getApiSession();
        if (apiSession != null)
        {
            return new ForceApi(this.apiConfig, apiSession, callback);
        }
        return null;
    }


    public <T> T getObject(String id, String type, Class<T> clazz) throws RepositoryException
    {
        return queryService.queryUniqueResult(this.getForceApi(), id, type, clazz);
    }


    private String getInUserIdAndGroupIdsQuerySet() throws RepositoryException
    {
        StringBuffer sb = new StringBuffer();
        sb.append("'");
        sb.append(this.getIdentity().getUserId());
        sb.append("'");
        for (Group group : this.getGroups()) {
            sb.append((","));
            sb.append("'");
            sb.append(group.getGroupId());
            sb.append("'");
        }
        return sb.toString();
    }

    public List<Group> getGroups() throws RepositoryException
    {
        String[] parameters = new String[1];
        parameters[0] = this.getIdentity().getUserId();
        return queryService.query(this.getForceApi(), GET_GROUPS_SOQL_QUERY_PATH, parameters, Group.class );
    }

    public List<User> getUsers() throws RepositoryException
    {
        String[] parameters = new String[1];
        parameters[0] = this.getIdentity().getUserId();
        return queryService.query(this.getForceApi(), GET_USERS_SOQL_QUERY_PATH, parameters, User.class );
    }


}
