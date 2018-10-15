package com.hyxen.adlocusaar.push;

import android.content.Context;
import android.content.Intent;

import com.hyxen.adlocusaar.PushAd;
import com.hyxen.adlocusaar.util.AdLocusUtil;
import com.hyxen.adlocusaar.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by kiddchen on 2/23/16.
 */
public class TestUtil {

    private static final String SHARE_PREFERENCE_NAME = AdLocusUtil.PREFIX + "test";
    private static final String KEY_CONFIG = "c";
    private static final String KEY_TEST_MODE = "tm";

    static final String ACTION_TEST_PREFERENCE = "ACTION_TEST_PREFERENCE";
    static final String ACTION_SEND_TO_TEST_APP = "ACTION_SEND_TO_TEST_APP";
    static final String EXTRA_INFO = "i";
    static final String EXTRA_HASH = "h";

    static final String JSON_ACTION = "a";

    static final String ACTION_TEST_PUSH = "tp";

    static final String ACTION_READ_CONFIG = "rc";
    static final String ACTION_CONFIG = "c";
    static final String J_EXTRA_CONFIG = "c";

    //Config

    /** boolean is show log */
    private static final String CONFIG_SHOW_LOG = "l";
    /** boolean is test mode */
    private static final String CONFIG_TEST_MODE = "tm";
    //Config end

    static void sendToTestApp(Context context) {
        Intent i = ServiceUtil.getIntent(context, ACTION_TEST_PREFERENCE, "com.hyxen.adlocusaar.adlocustester");
        JSONObject o = jsonWithAction(ACTION_SEND_TO_TEST_APP);
        try {
            o.put(J_EXTRA_CONFIG, getConfig(context));
        } catch (JSONException ignored) { }
        setExtraInfo(i, o.toString());
        context.sendBroadcast(i);
    }

    static JSONObject jsonWithAction(String action) {

        JSONObject o = new JSONObject();
        try {
            o.put(JSON_ACTION, action);
        } catch (JSONException ignored) { }
        return o;
    }

    static void setExtraInfo(Intent intent, String info) {
        intent.putExtra(EXTRA_INFO, info);
        intent.putExtra(EXTRA_HASH, AdLocusUtil.sha1(AdLocusUtil.PREFIX + info));
    }

    static boolean testAction(Context context, Intent intent, String recipient) {

        Log.d("recipient:" + recipient + "," + context.getPackageName());
        if(recipient != null && !context.getPackageName().equals(recipient)) return false;
        if (!TestUtil.ACTION_TEST_PREFERENCE.equals(intent.getStringExtra(ServiceUtil.EXTRA_ACTION))) return false;
        String json = intent.getStringExtra(EXTRA_INFO);
        String hash = intent.getStringExtra(EXTRA_HASH);
        if (json == null) return true;
        Log.d("testAction:" + json);
        if (!hash.equals(AdLocusUtil.sha1(AdLocusUtil.PREFIX + json))) return true;

        try {
            JSONObject o = new JSONObject(json);
            String action = o.getString(JSON_ACTION);
            if (ACTION_TEST_PUSH.equals(action)) {
                PushAd.test(context);
            } else if (ACTION_CONFIG.equals(action)) {
                String config = o.getString(J_EXTRA_CONFIG);
                new JSONObject(config);
                saveConfig(context, config);
                loadConfig(context);
            } else if (ACTION_READ_CONFIG.equals(action)) {
                sendToTestApp(context);
            }

        } catch (JSONException e) {
            Log.d("testAction: JsonError: " + json);
        }
        return true;
    }

    private static void saveConfig(Context context, String config) {
        context.getSharedPreferences(SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE).edit().putString(KEY_CONFIG, config).apply();
    }

    private static String getConfig(Context context){
        return context.getSharedPreferences(SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE).getString(KEY_CONFIG, "{}");
    }

    private static void loadConfig(Context context) {
        String jsonConfig = getConfig(context);
        try {
            JSONObject o = new JSONObject(jsonConfig);
            Log.isShowLog = o.optBoolean(CONFIG_SHOW_LOG, false);
            saveTestMode(context, o.optBoolean(CONFIG_TEST_MODE, false));
        } catch (JSONException e) {
            Log.d("testAction: JsonError: " + jsonConfig);
        }

    }

    private static void saveTestMode(Context context, boolean isTestMode) {
        context.getSharedPreferences(SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_TEST_MODE, isTestMode).apply();
        if (isTestMode) {

            ServiceUtil.clearTriggeredEvent(context);

            ArrayList<ServiceUtil.PackageInfo> infos = ServiceUtil.getCurrentServiceList(context);
            for (ServiceUtil.PackageInfo packageInfo : infos) {
                packageInfo.resetCheckTs();
            }
            ServiceUtil.saveCurrentServiceList(context, infos);
            Type3Manager.clear(context);
            Intent i = new Intent(context, PushService.class);
            i.setAction(PushService.ACTION_CHECK_ALIVE);
            context.startService(i);
        }
    }

    public static boolean isTestMode(Context context){
        return context.getSharedPreferences(SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE).getBoolean(KEY_TEST_MODE, false);
    }
}
