package com.hyxen.adlocusaar.repository.data.response;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hyxen.adlocusaar.repository.data.RemoteResponse;
import com.hyxen.adlocusaar.repository.data.domain.LbsTask;

import java.util.ArrayList;

@JsonObject(fieldNamingPolicy = JsonObject.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
public class GetLbsTaskResponse extends RemoteResponse {
    @JsonField
    private ArrayList<LbsTask> pt;

    public ArrayList<LbsTask> getPt() {
        return pt;
    }

    public void setPt(ArrayList<LbsTask> pt) {
        this.pt = pt;
    }
}
