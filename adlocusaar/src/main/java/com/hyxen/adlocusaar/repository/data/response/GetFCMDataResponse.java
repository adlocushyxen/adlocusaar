package com.hyxen.adlocusaar.repository.data.response;

import java.util.HashMap;
import java.util.Map;

public class GetFCMDataResponse {
    public static final String TAG_TARGET = "target";
    private static final String TAG_TITLE = "title";
    private static final String TAG_BODY = "body";
    private static final String TAG_URL = "url";
    private static final String TAG_TYPE = "type";
    private static final String TAG_AD_ID = "adid";
    private static final String TAG_SESSION_ID = "sessionid";

    private String target;
    private String title;
    private String body;
    private String url;
    private String type;
    private String adid;
    private String sessionid;

    private static GetFCMDataResponse mInstance;

    public static GetFCMDataResponse getInstance() {
        if (mInstance == null)
            mInstance = new GetFCMDataResponse();

        return mInstance;
    }

    public void parseHash(Map<String, String> map) {
        setTarget(map.get(TAG_TARGET));
        setTitle(map.get(TAG_TITLE));
        setBody(map.get(TAG_BODY));
        setUrl(map.get(TAG_URL));
        setType(map.get(TAG_TYPE));
        setAdId(map.get(TAG_AD_ID));
        setSessionId(map.get(TAG_SESSION_ID));
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAdId() {
        return adid;
    }

    public void setAdId(String adId) {
        this.adid = adId;
    }

    public String getSessionId() {
        return sessionid;
    }

    public void setSessionId(String sessionId) {
        this.sessionid = sessionId;
    }
}
