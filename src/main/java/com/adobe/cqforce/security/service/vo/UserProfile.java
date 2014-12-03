package com.adobe.cqforce.security.service.vo;

import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;

public class UserProfile
{
    private String userId;
    private String firstName;
    private String lastName;
    private String email;

    public String getUserId()
    {
        return userId;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    @Override
    public String toString()
    {
        return "UserProfile [userId=" + userId + ", firstName=" + firstName + ", lastName=" + lastName + ", email="
                + email + "]";
    }

    public String getLastName()
    {
        return lastName;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }


    public String getFullName()
    {
        return firstName + " " + lastName;
    }

    public JSONObject toJson() throws JSONException
    {
        JSONObject json = new JSONObject();
        json.put("userId", userId);
        json.put("firstName", firstName);
        json.put("lastName", lastName);
        json.put("email", email);
        return json;
    }
}
