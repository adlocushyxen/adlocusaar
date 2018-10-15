package com.hyxen.adlocusaar.repository.data.domain;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.util.ArrayList;

@JsonObject(fieldNamingPolicy = JsonObject.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
public class LbsTask {
    @JsonField
    private String sessionId;
    @JsonField
    private String adId;
    @JsonField
    private String beginTs;
    @JsonField
    private String endTs;
    @JsonField
    private String fcmPush;
    @JsonField
    private ArrayList<LbsTaskLlr> llr;
    private boolean isShow;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getAdId() {
        return adId;
    }

    public void setAdId(String adId) {
        this.adId = adId;
    }

    public String getBeginTs() {
        return beginTs;
    }

    public void setBeginTs(String beginTs) {
        this.beginTs = beginTs;
    }

    public String getEndTs() {
        return endTs;
    }

    public void setEndTs(String endTs) {
        this.endTs = endTs;
    }

    public String getFcmPush() {
        return fcmPush;
    }

    public void setFcmPush(String fcmPush) {
        this.fcmPush = fcmPush;
    }

    public ArrayList<LbsTaskLlr> getLlr() {
        return llr;
    }

    public void setLlr(ArrayList<LbsTaskLlr> llr) {
        this.llr = llr;
    }

    public boolean getIsShow() {
        return isShow;
    }

    public void setIsShow(boolean isShow) {
        this.isShow = isShow;
    }
}
