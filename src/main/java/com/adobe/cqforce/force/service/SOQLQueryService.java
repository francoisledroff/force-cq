package com.adobe.cqforce.force.service;


import com.force.api.ForceApi;

import javax.jcr.RepositoryException;
import java.util.List;
import java.util.Map;

public interface SOQLQueryService {
    List<Map> query(ForceApi forceApi, String queryNodePath, String[] parameters) throws RepositoryException;

    <T> List<T> query(ForceApi forceApi, String queryNodePath, String[] parameters, Class<T> resultClass) throws RepositoryException;

    /**
     * Used when results are retrieved through a custom SOQL query and we know we will get an unique result (count query for example).
     */
    <T> T queryUniqueResult(ForceApi forceApi, String queryNodePath, String[] parameters, Class<T> resultClass) throws RepositoryException;

    /**
     * Used when we want to fetch all properties of specific object, having the id of that object.
     */
    <T> T queryUniqueResult(ForceApi forceApi, String id, String type, Class<T> resultClass) throws RepositoryException;



}
