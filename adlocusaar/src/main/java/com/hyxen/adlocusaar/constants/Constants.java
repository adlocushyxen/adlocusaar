package com.hyxen.adlocusaar.constants;

public class Constants {
    public static final String TAG_FCM_TARGET = "AdLocusSDK";
    public static final String TAG_FCM_LC = "LC";//Check FCM go to pull SQLite
    public static final String TAG_FCM_GA = "GA";//Check FCM show notification
    public static final String TAG_FCM_TEST = "TEST";//Check FCM is testing

    public static final String TAG_AD_ID = "ad_id";
    public static final String TAG_CI = "ci";
    public static final String TAG_D_AD_ID = "d_ad_id";
    public static final String TAG_DEVICE_ID = "device_id";
    public static final String TAG_KEY = "key";         //the same with TAG_APP_KEY
    public static final String TAG_APP_KEY = "appkey";  //the same with TAG_KEY
    public static final String TAG_LAC = "lac";
    public static final String TAG_MAC = "mac";
    public static final String TAG_MCC = "mcc";
    public static final String TAG_MNC = "mnc";
    public static final String TAG_SCREEN = "screen";
    public static final String TAG_SESSION_ID = "session_id";
    public static final String TAG_TEST_MODE = "testmode";
    public static final String TAG_V_STR = "v_str";
    public static final String TAG_TYPE = "type";
    public static final String TAG_DEVICE_TYPE = "dev_type";
    public static final String TAG_PLAIN = "plain";
    public static final String TAG_LAT = "lat";
    public static final String TAG_LON = "lon";
    public static final String TAG_RSSI = "rssi";

    public static final String TAG_AD_TYPE_LIMIT = "0"; // 廣告已達標
    public static final String TAG_AD_TYPE_ICON = "1"; // 文 + icon
    public static final String TAG_AD_TYPE_BANNER = "2"; // banner
    public static final String TAG_AD_TYPE_BIG_VIEW = "3"; // Big View

    public static final String TAG_INTENT_URL = "url"; // url key of Notification intent
    public static final String TAG_INTENT_ID = "id"; // id key of Notification intent
    public static final String TAG_INTENT_SHARE = "share"; // share type of bigView click type
    public static final String TAG_INTENT_SHARE_DATA = "share_data"; // The key of share data in Notification intent
    public static final String TAG_INTENT_SETTING = "setting"; // setting type of bigView click type
    public static final String TAG_INTENT_KEY_TYPE = "type"; // big view type
    public static final String TAG_INTENT_KEY_TRACK_IMP = "track_imp"; // track imp key

    public static final String TAG_BROADCAST_LBS_CHECKER_KEY = "lbs_checker_key"; // lbs checker key
    public static final String TAG_BROADCAST_LBS_CHECKER_VALUE = "lbs_checker_value"; // lbs checker value

    public static final int TAG_ANDROID_ID_STATEMENT_STATE_FIRST = 0; // 還沒設過user state
    public static final int TAG_ANDROID_ID_STATEMENT_STATE_GRANT = 1; // 使用者同意App 使用 android ID
    public static final int TAG_ANDROID_ID_STATEMENT_STATE_DENIED = 2;// 使用者拒絕App 使用 android ID
}
