package com.hyxen.adlocusaar.repository.data.request;

import android.text.TextUtils;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

@JsonObject(fieldNamingPolicy = JsonObject.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
public class PushTokenRequest {
    @JsonField
    private String pushToken;
    @JsonField
    private String deviceId;
    @JsonField
    private String sdkVersion;
    @JsonField
    private String appKey;//Hyxen app key
    @JsonField
    private String deviceMac;
    @JsonField
    private String deviceModel; //device model
    @JsonField
    private String fcmAppKey;// fcm app key
    @JsonField
    private String appPackageName; //app package name

    @JsonField
    private String mcc;
    @JsonField
    private String mnc;

    @JsonField
    private String d_ad_id;



    public String getPushToken() {
        return !TextUtils.isEmpty(pushToken) ? pushToken : "";
    }

    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }

    public String getDeviceId() {
        return !TextUtils.isEmpty(deviceId) ? deviceId : "";
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getSdkVersion() {
        return !TextUtils.isEmpty(sdkVersion) ? sdkVersion : "";
    }

    public void setSdkVersion(String sdkVersion) {
        this.sdkVersion = sdkVersion;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getDeviceMac() {
        return deviceMac;
    }

    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getFcmAppKey() {
        return fcmAppKey;
    }

    public void setFcmAppKey(String fcmAppKey) {
        this.fcmAppKey = fcmAppKey;
    }

    public String getAppPackageName() {
        return appPackageName;
    }

    public void setAppPackageName(String appPackageName) {
        this.appPackageName = appPackageName;
    }

    public String getMcc() {
        return mcc;
    }

    public void setMcc(String mcc) {
        this.mcc = mcc;
    }

    public String getMnc() {
        return mnc;
    }

    public void setMnc(String mnc) {
        this.mnc = mnc;
    }

    public String getD_ad_id() {
        return d_ad_id;
    }

    public void setD_ad_id(String d_ad_id) {
        this.d_ad_id = d_ad_id;
    }
}
