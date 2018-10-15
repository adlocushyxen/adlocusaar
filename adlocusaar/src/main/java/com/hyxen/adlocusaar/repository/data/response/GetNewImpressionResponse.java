package com.hyxen.adlocusaar.repository.data.response;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hyxen.adlocusaar.repository.data.RemoteResponse;

@JsonObject(fieldNamingPolicy = JsonObject.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
public class GetNewImpressionResponse extends RemoteResponse {
    @JsonField
    private String adId;
    @JsonField
    private String adIdImp;

    public String getAdId() {
        return adId;
    }

    public void setAdId(String adId) {
        this.adId = adId;
    }

    public String getAdIdImp() {
        return adIdImp;
    }

    public void setAdIdImp(String adIdImp) {
        this.adIdImp = adIdImp;
    }
}
