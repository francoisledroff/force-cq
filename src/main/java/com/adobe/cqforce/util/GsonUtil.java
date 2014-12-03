package com.adobe.cqforce.util;

import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

public class GsonUtil
{
    private static GsonBuilder getGsonBuilder()
    {
        //TODO we could add dateformat and more cf. http://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/GsonBuilder.html
        // note that disableHtmlEscaping is mandatory for us (at least in Concur because we d'ont want to encode = and & in the urls )
        return new GsonBuilder().disableHtmlEscaping();
    }
    
    /**
     * @param objectToSerialize
     * @return a pretty printed json marshalled object with no html escaping 
     */
    public static String toJson(Object objectToSerialize)
    {
        return  getGsonBuilder().setPrettyPrinting().create().toJson(objectToSerialize);
    }

    public static String toJson(Object objectToSerialize, Type type, Object typeAdapter)
    {
        return  getGsonBuilder().setPrettyPrinting().registerTypeAdapter(type, typeAdapter).create().toJson(objectToSerialize);
    }

    /**
     * @param objectToSerialize
     * @param prettyPrint if true the json will be pretty-printed/human-readable
     * @return a json marshalled object with no html escaping 
     */
    public static String toJson(Object objectToSerialize, boolean prettyPrint)
    {
        
        if (prettyPrint)
           return toJson(objectToSerialize);
        else
           return getGsonBuilder().create().toJson(objectToSerialize);
    }


    public static <T> T fromJson(String objectToDeserialize, Class<T> classOfT)
    {
        return  getGsonBuilder().create().fromJson(objectToDeserialize, classOfT);
    }

    public static <T> T fromJson(String objectToDeserialize, Type type)
    {
        return  getGsonBuilder().create().fromJson(objectToDeserialize, type);
    }
}
