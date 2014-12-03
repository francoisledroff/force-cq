package com.adobe.cqforce.force.service.impl;


import com.adobe.cqforce.force.service.SOQLQueryService;
import com.force.api.ForceApi;
import com.force.api.QueryResult;
import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component(immediate = true, label = SOQLQueryServiceImpl.COMPONENT_LABEL, description = SOQLQueryServiceImpl.COMPONENT_DESC)
@Service(SOQLQueryService.class)
public class SOQLQueryServiceImpl implements SOQLQueryService
{
    static final String COMPONENT_LABEL = "Adobe Hub Approval DAT SOQL Query Service";
    static final String COMPONENT_DESC = "Adobe Hub Approval DAT SOQL Query Service";
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Activate
    protected void activate(ComponentContext context) throws Exception
    {
        log.info("SOQLQueryServiceImpl activated...");
    }

    public List<Map> query(ForceApi forceApi, String queryNodePath, String[] parameters) throws RepositoryException
    {
        return getQueryResultList(forceApi, queryNodePath, parameters, Map.class);
    }

    public <T> List<T> query(ForceApi forceApi, String queryNodePath, String[] parameters, Class<T> resultClass) throws RepositoryException
    {
        return getQueryResultList(forceApi, queryNodePath, parameters, resultClass);
    }

    public <T> T queryUniqueResult(ForceApi forceApi, String queryNodePath, String[] parameters, Class<T> resultClass) throws RepositoryException
    {
        List<T> resultList = getQueryResultList(forceApi, queryNodePath, parameters, resultClass);
        if (resultList != null && resultList.size() > 0){
            return resultList.get(0);
        }
        return null;
    }

    public <T> T queryUniqueResult(ForceApi forceApi, String id, String type, Class<T> resultClass) throws RepositoryException
    {
        return forceApi.getSObject(type, id).as(resultClass);
    }

    /**
     * Whenever a custom query is run, the response contains some metadata associated to the query:
     *
     * a) "done" - if all the expected information is included in the response, the value is true; otherwise is false
     * b) if "done" == false, the response also contains "nextRecordsURl". This url will be used to retrieve
     * the rest of the content.
     *
     * This applies for responses which are very large (e.g. all sales-force users).
     */
    private <T> List<T> getQueryResultList(ForceApi forceApi, String queryNodePath,String[] parameters, Class<T> resultClass)
            throws RepositoryException
    {
        long t1 = System.currentTimeMillis();
        log.debug("Running SF query list for [" + resultClass.getSimpleName() + "]");

        String queryText = getSOQLQueryText(queryNodePath);
        String parametirizedQuery = String.format(queryText, parameters);

        List<T> allItems = new ArrayList<T>();
        QueryResult<T> results = forceApi.query(parametirizedQuery, resultClass);

        allItems.addAll(results.getRecords());

        //check if response contains all the information
        while (!results.isDone()) {
            log.debug("SF response does not contain all results for [" + resultClass.getSimpleName() + "]. Running new query using <nextRecordsUrl>");
            //get all remaining content as long as the response is not complete

            results = forceApi.queryMore(results.getNextRecordsUrl(), resultClass);
            allItems.addAll(results.getRecords());
        }

        log.debug("SF list query for type [" + resultClass.getSimpleName() + "] took [" + (System.currentTimeMillis() - t1) + "]ms");
        return allItems;
    }



    private String getSOQLQueryText(String queryNodePath) throws RepositoryException {
        ResourceResolver resourceResolver = null;
        Session session = null;

        try {
            resourceResolver = resolverFactory.getAdministrativeResourceResolver(null);
            session = resourceResolver.adaptTo(Session.class);

            Node storedQuery = session.getNode(queryNodePath);
            InputStream in = storedQuery.getNode("jcr:content").getProperty("jcr:data").getBinary().getStream();

            return new String(IOUtils.toByteArray(in));
        } catch (LoginException e) {
            throw new RepositoryException(e);
        } catch (IOException e) {
            throw new RepositoryException(e);
        } finally {
            if (session != null){
                session.logout();
            }
            if (resourceResolver != null) {
                resourceResolver.close();
            }
        }
    }
}