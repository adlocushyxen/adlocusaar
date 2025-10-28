package com.hyxen.adlocusaar.push.alarm.clock;
/**
 * Created by leo3x on 2018/11/9.
 * 定期回訪設定
 * SDK定時喚醒參考以下待播的廣告清單
 *http://192.173.146.162/devpush/json/local_db_and.json
 *自行判斷目前是否有符合條件的廣告
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hyxen.adlocusaar.AdLocus;
import com.hyxen.adlocusaar.constants.Constants;
import com.hyxen.adlocusaar.repository.Repository;
import com.hyxen.adlocusaar.utils.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PushAlarmReceiver extends BroadcastReceiver {
    public static final String FLAG_ACTION="com.hyxen.adlocusaar.push.alarm.clock.PushAlarm";
    private static final String TAG = PushAlarmReceiver.class.getSimpleName();
//    private static final String  debug_url="https://test.adlocus_api.dev.hxcld.com/devpush/json/local_db_and.json";
//    private static final String  normal_url="https://192.173.146.162/devpush/json/local_db_and.json";
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static ConcurrentHashMap<String ,TreeMap<String,String>> map ;
    @Override
    public void onReceive(final Context context, Intent intent) {
        AdLocus.isDebug(context);
        AdLocus.isAlarmDebug(context);
        AdLocus.isAlarmBotDebug(context);
        AdLocus.getInstance(context);
//        String url = AdLocus.isDebug()?debug_url:normal_url;
        Logger.d(TAG, "[onReceive] start is debug:"+AdLocus.isAlarmDebug(context));
//        if(intent == null) return;
//        String action = intent.getAction();
//        if(action == null) return;
//        if(FLAG_ACTION.equals(action)) {
//            if(AdLocus.isAlarmDebug()) Logger.d(TAG, "[onReceive] init");
//            PushAlarm.startPushAlarm(context);
//            if(PushAlarm.isReceverFCM(context))return;
//            if(AdLocus.isAlarmDebug()) Logger.d(TAG, "[onReceive] isReceverFCM true");
//            OkHttpClient client = new OkHttpClient();
//
//
//
//            try{
//                if(AdLocus.isAlarmBotDebug()){
//                    TreeMap<String,String> map=new TreeMap<>();
//                    map.put("data", Repository.getHashDeviceId());
//                    map.put("time", sdf.format(new Date()));
//                    map.put("info", "alarm");
//                    PushBotDebug.push(map);
//                }
//
//                Request.Builder b=new Request.Builder();
//                Request request = b.url(url).build();
//                if(AdLocus.isAlarmDebug()) Logger.d(TAG, "[onReceive] get url "+url);
//                client.newCall(request).enqueue(new Callback() {
//                    @Override
//                    public void onFailure(Call call, IOException e) {
//                        if(AdLocus.isAlarmDebug()) e.printStackTrace();
//                    }
//
//                    @Override
//                    public void onResponse(Call call, Response response) throws IOException {
//                        if(response.isSuccessful()){
//                            String dataS=new String(response.body().bytes(),"UTF-8");
//                            try {
//                                map=new ConcurrentHashMap<>();
//                                JSONObject dataJ=new JSONObject(dataS);
//                                if(dataJ.has("bc")){
//                                    JSONArray dataSubJA=dataJ.optJSONArray("bc");
//                                    for(int i=0;i<dataSubJA.length();i++){
//                                        checkData(context,dataSubJA.optJSONObject(i),Constants.TAG_FCM_GA);
//                                    }
//                                }
////                                if(dataJ.has("city")){
////                                    JSONArray dataSubJA=dataJ.optJSONArray("city");
////                                }
////                                if(dataJ.has("pt")){
////                                    JSONArray dataSubJA=dataJ.optJSONArray("pt");
////                                }
//                                AdLocus.getInstance().receverAlarmAD(context, map);
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                });
//            }catch (Exception e){e.printStackTrace();}
//        }
    }

    private void checkData(Context context,JSONObject jsonObject,final String type) {

        if(jsonObject.has("begin_ts") && jsonObject.has("end_ts")){
            long begin_ts=jsonObject.optLong("begin_ts")*1000;
            long end_ts=jsonObject.optLong("end_ts")*1000;
            long now=System.currentTimeMillis();
            if(begin_ts<=now &&  now<=end_ts){
                if(jsonObject.has("app_key")){
                    String app_key=Repository.getAppKey();
                    if(app_key.length()<=0)return;
                    JSONObject app_keyJO=jsonObject.optJSONObject("app_key");
                    boolean isfindKey=false;
                    if(app_keyJO.has("not_in")){
                        JSONArray appkeyA=app_keyJO.optJSONArray("not_in");
                        for(int i=0;i<appkeyA.length();i++){
                            String key=appkeyA.optString(i);
                            if(app_key.equals(key))break;
                        }
                        isfindKey=true;
                    }else if(app_keyJO.has("in")){
                        JSONArray appkeyA=app_keyJO.optJSONArray("in");
                        for(int i=0;i<appkeyA.length();i++){
                            String key=appkeyA.optString(i);
                            if(app_key.equals(key)){
                                isfindKey=true;
                                break;
                            }
                        }
                    }
                    if(AdLocus.isAlarmDebug()) Logger.d(TAG, "[checkData] isfindKey: "+isfindKey);
                    if(isfindKey){
                        String session_id=jsonObject.optString("session_id");
                        String ad_id=jsonObject.optString("ad_id");
                        if(AdLocus.isAlarmDebug()) Logger.d(TAG, "[checkData] session_id: "+session_id);
                        if(AdLocus.isAlarmDebug()) Logger.d(TAG, "[checkData] ad_id: "+ad_id);
                        //AdLocus.getInstance().sendFCMMessage(this,data);
//                        AdLocus.getInstance().receverAlarmAD(context, type,session_id,ad_id);
                        if(map.containsKey(type)){
                            map.get(type).put(ad_id,session_id);
                        }else{
                            TreeMap <String,String>ad=new TreeMap<>();
                            ad.put(ad_id,session_id);
                            map.put(type,ad);
                        }
                    }
                }
            }
        }
    }
}
