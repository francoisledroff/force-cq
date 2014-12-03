package com.adobe.cqforce.util;

import org.slf4j.Logger;
import java.nio.ByteBuffer;

public class CommonUtils
{

    /**
     * Concatenate multiple byte arrays.
     */
    public static byte[] concatenateArrays(byte[]... arrays) {

        int size = 0;
        for (byte[] array : arrays) {
            size += array.length;
        }

        ByteBuffer bb = ByteBuffer.allocate(size);

        for (int i = 0; i < arrays.length; i++) {
            bb.put(arrays[i]);
        }

        return bb.array();
    }

    //TODO this method will be removed as it's no longer needed. It's preserved for compatibility reasons.
    public static String logAndIdError(Logger log, String errorMessage)
    {
        //errorMessage = errorMessage + " - logId:"+new Date().getTime();
        log.error(errorMessage);
        return errorMessage;
    }

    /**
     * This is used for logging exceptions with additional information in the log message. This method is called for non
     * identifiable exceptions (which will not be surfaced to the user).
     */
    public static void logError(String message, Throwable ex, Logger logger, String userId)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(getLogElement("LdapId", userId))
                .append(getLogElement("Exception", message))
                .append(getLogElement("Cause", ex.getMessage()));

        logger.error(sb.toString(), ex);
    }


    private static String getLogElement(String label, Object value) {
        if (value != null) {
            return "[" + label + ": " + value + "] ";
        }
        return "";
    }



}
