package com.hyxen.adlocusaar.repository.data.request;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

@JsonObject(fieldNamingPolicy = JsonObject.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
public class NewAndRequest {
    ////// require //////

    //Server傳來的廣告ID
    @JsonField
    private String adId;
    //詳見PhoneCellUtil.class
    @JsonField
    private String ci;
    //在Server建立app,所產出的key
    @JsonField
    private String key;
    //詳見PhoneCellUtil.class
    @JsonField
    private String lac;
    //詳見PhoneCellUtil.class
    @JsonField
    private String mac;
    //詳見PhoneCellUtil.class
    @JsonField
    private String mcc;
    //詳見PhoneCellUtil.class
    @JsonField
    private String mnc;
    //手機螢幕寬度，目前塞固定值
    @JsonField
    private String screen;
    //Server傳來之SessionID，與Ad id綁在一起
    @JsonField
    private String sessionId;
    //測試模式，詳見AdLocusUtil.class
    @JsonField
    private String testmode;
    //SDK version
    @JsonField
    private String vStr;
    //手機沒有Encode過的Device ID
    @JsonField
    private String dAdId;
    //Encode過的Device ID
    @JsonField
    private String deviceId;

    ////// optional //////

    @JsonField
    private String rssi;
    //Gps Latitude
    @JsonField
    private String lat;
    //Gps longitude
    @JsonField
    private String lon;
    //手機螢幕方向
    @JsonField
    private String ori;
    //手機羅盤方向
    @JsonField
    private String com;
    //手機目前高度
    @JsonField
    private String h;
    //手機目前數度
    @JsonField
    private String s;
    //手機光感應
    @JsonField
    private String ls;
    //手機是否在充電
    @JsonField
    private String charge;
    //手機剩餘電量
    @JsonField
    private String elec;
    //手機距離感應器
    @JsonField
    private String ds;
    //手機電池溫度
    @JsonField
    private String bt;
    //手機是否在鎖定螢幕的狀況
    @JsonField
    private String sl;
    //手機通知的音量
    @JsonField
    private String volNotice;
    //手機鈴聲的音量
    @JsonField
    private String volRing;
    //手機電池狀況
    @JsonField
    private String health;
    //Android 版本
    @JsonField
    private String ver;
    //手機GPS的精準度
    @JsonField
    private String la;

    public String getAdId() {
        return adId;
    }

    public void setAdId(String adId) {
        this.adId = adId;
    }

    public String getCi() {
        return ci;
    }

    public void setCi(String ci) {
        this.ci = ci;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLac() {
        return lac;
    }

    public void setLac(String lac) {
        this.lac = lac;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
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

    public String getRssi() {
        return rssi;
    }

    public void setRssi(String rssi) {
        this.rssi = rssi;
    }

    public String getScreen() {
        return screen;
    }

    public void setScreen(String screen) {
        this.screen = screen;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getTestmode() {
        return testmode;
    }

    public void setTestmode(String testmode) {
        this.testmode = testmode;
    }

    public String getvStr() {
        return vStr;
    }

    public void setvStr(String vStr) {
        this.vStr = vStr;
    }

    public String getdAdId() {
        return dAdId;
    }

    public void setdAdId(String dAdId) {
        this.dAdId = dAdId;
    }

    public String getOri() {
        return ori;
    }

    public void setOri(String ori) {
        this.ori = ori;
    }

    public String getCom() {
        return com;
    }

    public void setCom(String com) {
        this.com = com;
    }

    public String getH() {
        return h;
    }

    public void setH(String h) {
        this.h = h;
    }

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

    public String getLs() {
        return ls;
    }

    public void setLs(String ls) {
        this.ls = ls;
    }

    public String getCharge() {
        return charge;
    }

    public void setCharge(String charge) {
        this.charge = charge;
    }

    public String getElec() {
        return elec;
    }

    public void setElec(String elec) {
        this.elec = elec;
    }

    public String getDs() {
        return ds;
    }

    public void setDs(String ds) {
        this.ds = ds;
    }

    public String getBt() {
        return bt;
    }

    public void setBt(String bt) {
        this.bt = bt;
    }

    public String getSl() {
        return sl;
    }

    public void setSl(String sl) {
        this.sl = sl;
    }

    public String getVolNotice() {
        return volNotice;
    }

    public void setVolNotice(String volNotice) {
        this.volNotice = volNotice;
    }

    public String getVolRing() {
        return volRing;
    }

    public void setVolRing(String volRing) {
        this.volRing = volRing;
    }

    public String getHealth() {
        return health;
    }

    public void setHealth(String health) {
        this.health = health;
    }

    public String getVer() {
        return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public String getLa() {
        return la;
    }

    public void setLa(String la) {
        this.la = la;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
