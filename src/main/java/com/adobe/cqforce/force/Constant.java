package com.adobe.cqforce.force;

public class Constant {


    /**
     * Request parameter names.
     */
    public static final String REQUEST_PARAM_USER_ID = "userid";

    /**
     * Names of properties used for storing/processing SF tokens.
     */
    public static final String SF_API_SESSION = "salesforce-apiSession";
    public static final String SF_IDENTITY = "salesforce-identity";

    public static final String DAT_AUTH_SUCCESS_PAGE = "/content/force/api/cqforce/html/success.html";
    public static final String DAT_AUTH_ERROR_PAGE = "/content/force/api/cqforce/html/error.html";
    public static final String DAT_AUTH_SERVICE = "/content/force/api/cqforce/apiauth.json";

    public static final String DAT_AUTH_ERROR_REFRESHING_TOKEN = "Error while refreshing token...Will delete the sf tokens to restart oauth web flow";
    public static final String DAT_AUTH_ERROR_FLOW = "We could not complete the oAuth authentication flow and fetch the refresh token. Try to hit this authentication endpoint again or contact your administrator.";
    public static final String DAT_ERROR_STATE_ALREADY_CHANGED = "%s of the approval request was changed in another session. ";



}
