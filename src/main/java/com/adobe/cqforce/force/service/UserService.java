package com.adobe.cqforce.force.service;

import com.adobe.cqforce.force.domain.User;

import java.util.List;


public interface UserService {

    /**
     * Get the list of users using the specified filter. Filter applies to all user properties
     * (email, full name, firstname, lastname). An user object is included in the response if any of
     * its string property values start with the specified filter value. Comparison should be case insensitive.
     */
    List<User> getUserList(String currentUserId, String filter);

    void loadUserList(String currentUserId);

    User getUserByUsername(String currentUserId, String email);

}
