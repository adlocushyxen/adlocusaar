package com.hyxen.adlocusaar.repository.data;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hyxen.adlocusaar.constants.ApiStatus;

/**
 * Base remote response.
 */
@JsonObject(fieldNamingPolicy = JsonObject.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
public class RemoteResponse {

    @JsonField
    private String err;
    @JsonField
    private String errMsg;
    @JsonField
    private String sec;

    /*--------------------------------------------------------------------------------------------*/
    /* Getter & Setter */
    public String getErr() {
        return err;
    }

    public void setErr(String err) {
        this.err = err;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public String getSec() {
        return sec;
    }

    public void setSec(String sec) {
        this.sec = sec;
    }

    /*--------------------------------------------------------------------------------------------*/
    /* Helpers */
    public boolean isSuccess() {
        return Integer.parseInt(err) == ApiStatus.SERVER_CODE_SUCCESS;
    }
}
