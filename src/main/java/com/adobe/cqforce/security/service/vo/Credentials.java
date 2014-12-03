package com.adobe.cqforce.security.service.vo;

import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;

public class Credentials
{
    private String login;
    final private static String EMAIL_SUFFIX = "@adobe.com";

    public Credentials(String login)
    {
        this.login = login;
    }

    
    public Credentials()
    {
    }


    public String getLogin()
    {
        return login;
    }

    public void setLogin(String login)
    {
        this.login = login;
    }

    public String getEmail()
    {
        return login + EMAIL_SUFFIX;
    }


    @Override
    public String toString()
    {
        return "Credentials [login=" + login + "]";
    }

    public JSONObject toJson() throws JSONException
    {
        JSONObject json = new JSONObject();
        json.put("login", login);
        return json;
    }

    public String toXml()
    {
        StringBuffer xml = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?><user><login>");
        xml.append(login);
        xml.append("</login></user>");
        return xml.toString();
    }
}
