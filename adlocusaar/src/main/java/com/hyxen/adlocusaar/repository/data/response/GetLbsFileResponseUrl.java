package com.hyxen.adlocusaar.repository.data.response;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hyxen.adlocusaar.repository.data.RemoteResponse;

@JsonObject(fieldNamingPolicy = JsonObject.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
public class GetLbsFileResponseUrl extends RemoteResponse {
    @JsonField
    private String jsonLink;

    public String getJsonLink() {
        return jsonLink;
    }

    public void setJsonLink(String jsonLink) {
        this.jsonLink = jsonLink;
    }
}
