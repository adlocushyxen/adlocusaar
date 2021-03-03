package com.hyxen.adlocusaar;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;

import com.hyxen.adlocusaar.constants.Constants;
import com.hyxen.adlocusaar.push.alarm.clock.PushAlarm;
import com.hyxen.adlocusaar.repository.Repository;
import com.hyxen.adlocusaar.repository.data.request.PushTokenRequest;
import com.hyxen.adlocusaar.repository.data.response.GetFCMDataResponse;
import com.hyxen.adlocusaar.utils.AdLocusUtil;
import com.hyxen.adlocusaar.utils.Logger;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class AdLocus extends AdLocusHelp implements IAdLocus {
    private static final String TAG = AdLocus.class.getSimpleName();
    private static final String KEY_SAVE_API_DEBUG = "KEY_SAVE_API_DEBUG";
    private static final String KEY_SAVE_ALARM_DEBUG = "KEY_SAVE_ALARM_DEBUG";
    private static final String KEY_SAVE_ALARM_BOT_DEBUG = "KEY_SAVE_ALARM_BOT_DEBUG";

    private static AdLocus mInstance;
    private static WeakReference<Context> mContextRef;

    private String mFcmToken;
    private String mFcmAppKey;
    private String mAppPackageName;
    private static String mAppKey;
    private static boolean debug=BuildConfig.DEBUG;
    private static boolean debug_alarm=BuildConfig.DEBUG;
    private static boolean debug_alarm_send_bot=BuildConfig.DEBUG;
    public static Location gpsLocation;

    public static AdLocus getInstance() {
        if (mInstance == null)
            mInstance = new AdLocus();
        return mInstance;
    }

    public static AdLocus getInstance(Context outContext) {
        getInstance();

        mContextRef = new WeakReference<Context>(outContext);
        Context context = mContextRef.get();
        if (context == null) {
            Logger.e(TAG, "[getInstance] context is null");
            return mInstance;
        }
        init(context);
        return mInstance;
    }

    private static void init(Context context) {
        Logger.i(TAG, "[Method] -> init()");
        Repository.init(context);
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
//            ComponentName jobService = new ComponentName(context.getPackageName(), PushJobService.class.getName());
//
//            JobInfo jobInfo = new JobInfo.Builder(100012, jobService) //任务Id等于100012
//                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)// 需要满足网络条件，默认值NETWORK_TYPE_NONE
//                    .setPeriodic(AlarmManager.INTERVAL_HOUR) //循环执行，循环时长为一天（最小为15分钟）
//                    .setRequiresCharging(false)// 需要满足充电状态
//                    .setRequiresDeviceIdle(false)// 设备处于Idle(Doze)
//                    .setPersisted(true) //设备重启后是否继续执行
//                    .setBackoffCriteria(3000, JobInfo.BACKOFF_POLICY_LINEAR) //设置退避/重试策略
//                    .build();
//            scheduler.schedule(jobInfo);
//        }
    }

    @Override
    public void updatePushToken(@NonNull String fcmToken) {
        Logger.i(TAG, "[Method] -> updatePushToken()");
        if (mContextRef == null) {
            Logger.e(TAG, "[updatePushToken] mContextRef is empty");
            return;
        }
        Context context = mContextRef.get();
        if (context == null) {
            Logger.e(TAG, "[updatePushToken] context is empty");
            return;
        }
        if (TextUtils.isEmpty(fcmToken)) {
            Logger.e(TAG, "[updatePushToken] fcmToken is empty");
            return;
        }
        if (Repository.setPushToken(context, fcmToken)) {
            postPushToken(context);
        }
    }

//    public void reportFCMMessage(@NonNull Context ctx,@NonNull Map<String, String> fcmMessage){
//
//    }

    @Override
    public void sendFCMMessage(@NonNull Context ctx,@NonNull Map<String, String> fcmMessage) {
        Logger.i(TAG, "[Method] -> sendFCMMessage()");
        if (fcmMessage == null) {
            Logger.e(TAG, "[sendFCMMessage] fcmMessage is null");
            return;
        }
        Context context=null;
        if (mContextRef == null) {
            Logger.e(TAG, "[sendFCMMessage] mContextRef is empty");
        }else{
            context = mContextRef.get();
        }
        if (context == null) {
            context=ctx;

        }
        if (context == null) {
            Logger.e(TAG, "[sendFCMMessage] context is empty");
            return;
        }
        Repository.init(context);
        PushAlarm.setFcmRecever(context,System.currentTimeMillis());//設定Alarm定期回訪
        GetFCMDataResponse response = GetFCMDataResponse.getInstance();
        response.parseHash(fcmMessage);

//        startGpsLocation();
        /*
        Constants.TAG_FCM_LC:判斷此FCM是LBS廣告要去拉取SQL file
        Constants.TAG_FCM_GA:判斷此FCM是全區廣告走推播流程
        Constants.TAG_FCM_TEST:判斷此FCM是測試，走測試模式
         */
        if (TextUtils.equals(response.getType(), Constants.TAG_FCM_LC)) {
            getLbsFile(context);
        } else if (TextUtils.equals(response.getType(), Constants.TAG_FCM_GA)) {
//        Repository.setFCMMessage(fcmMessage);
            postNewAnd(context, response.getAdId(), response.getSessionId());
        } else if (TextUtils.equals(response.getType(), Constants.TAG_FCM_TEST)) {
            testMode(context);
        }



    }

    @Override
    public void registerApp() {
        Logger.i(TAG, "[Method] -> registerApp()");
        if (Repository.getRegisterState()) {
            Logger.i(TAG, "[registerApp] -> this firebase token already register");
            if (mContextRef == null || mContextRef.get() == null)
                return;
            Context context = mContextRef.get();
            postCollection(context);
            return;
        }
        PushTokenRequest tokenRequest = new PushTokenRequest();

        if (TextUtils.isEmpty(mFcmToken)) {
            mContextRef.get();
            if (mContextRef == null || mContextRef.get() == null)
                return;
            Context context = mContextRef.get();
            String newToken=Repository.getPushToken(context,"").trim();
            if(!newToken.equals(""))mFcmToken=newToken;
            Logger.d(TAG, "[registerApp] -> fcmToken is empty");
        }
        tokenRequest.setPushToken(mFcmToken);
        if (TextUtils.isEmpty(mFcmAppKey)) {
            Logger.d(TAG, "[registerApp] -> fcmAppKey is empty");
        }
        tokenRequest.setFcmAppKey(mFcmAppKey);
        if (TextUtils.isEmpty(mAppPackageName)) {
            Logger.d(TAG, "[registerApp] -> appPackageName is empty");
        }
        tokenRequest.setAppPackageName(mAppPackageName);
        if (TextUtils.isEmpty(mAppKey)) {
            Logger.d(TAG, "[registerApp] -> appKey is empty");
        }
        tokenRequest.setAppKey(mAppKey);
        Disposable disposable = Repository.setUserBaseData(tokenRequest)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (aBoolean) {
                            mContextRef.get();
                            if (mContextRef == null || mContextRef.get() == null)
                                return;
                            Context context = mContextRef.get();
                            postPushToken(context);
                            postCollection(context);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Logger.d(TAG, "set user data failed : " + throwable.getMessage());
                    }
                });
    }

    @Override
    public void checkUserStatement(String fcmToken, @NonNull String fcmAppKey, @NonNull String appPackageName, @NonNull String appKey) {
        mFcmToken = fcmToken;
        mFcmAppKey = fcmAppKey;
        mAppPackageName = appPackageName;
        mAppKey = appKey;

        Context context;
        if (mContextRef == null || mContextRef.get() == null) {
            Logger.d(TAG, "mContextRef is null");
            return;
        }
        context = mContextRef.get();
        //如果已經設定過使用者聲明則直接註冊app
        if (!AdLocusUtil.showAndroidIDStatement(context, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Repository.setUserAndroidIdState(Constants.TAG_ANDROID_ID_STATEMENT_STATE_GRANT);

            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Repository.setUserAndroidIdState(Constants.TAG_ANDROID_ID_STATEMENT_STATE_DENIED);
            }
        }, new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                registerApp();
            }
        })) {
            registerApp();
        }
        PushAlarm.startPushAlarmFromInit(context);//設定Alarm定期回訪
    }

    public static boolean isDebug() {
        return debug;
    }
    public static boolean isDebug(Context ctx) {
        if (ctx != null) {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
            debug = pref.getBoolean(KEY_SAVE_API_DEBUG, false);
        }
        return debug;
    }

    public static AdLocus setDebug(Context ctx,boolean debug) {
        AdLocus.debug = debug;
        if(ctx!=null){
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
            pref.edit().putBoolean(KEY_SAVE_API_DEBUG, debug).apply();
        }
        return getInstance();
    }

    public static boolean isAlarmDebug() {
        return debug_alarm;
    }
    public static boolean isAlarmDebug(Context ctx) {
        if (ctx != null) {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
            debug_alarm = pref.getBoolean(KEY_SAVE_ALARM_DEBUG, false);
        }
        return debug_alarm;
    }
    public static AdLocus setAlarmDebug(Context ctx,boolean debug) {
        AdLocus.debug_alarm = debug;
        if (ctx != null) {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
            pref.edit().putBoolean(KEY_SAVE_ALARM_DEBUG, debug).apply();
        }
        return getInstance();
    }


    public static boolean isAlarmBotDebug() {
        return debug_alarm_send_bot;
    }
    public static boolean isAlarmBotDebug(Context ctx) {
        if (ctx != null) {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
            debug_alarm_send_bot = pref.getBoolean(KEY_SAVE_ALARM_BOT_DEBUG, false);
        }
        return debug_alarm_send_bot;
    }
    public static AdLocus setAlarmBotDebug(Context ctx,boolean debug) {
        AdLocus.debug_alarm_send_bot = debug;
        if (ctx != null) {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
            pref.edit().putBoolean(KEY_SAVE_ALARM_BOT_DEBUG, debug).apply();
        }
        return getInstance();
    }

    public static void startGpsLocation(){
        gpsLocation=null;
        if(mContextRef!=null && mContextRef.get()!=null){
            final LocationManager locationManager = (LocationManager) mContextRef.get().getSystemService(Context.LOCATION_SERVICE);
            if(locationManager==null)return;
            List<String> providers = locationManager.getProviders(true);
            if(providers==null)return;
            if(providers.contains(LocationManager.GPS_PROVIDER) && ActivityCompat.checkSelfPermission(mContextRef.get(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                final LocationListener l=new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        gpsLocation=location;
                    }
                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {}
                    @Override
                    public void onProviderEnabled(String provider) {}
                    @Override
                    public void onProviderDisabled(String provider) {}
                };
                Looper.prepare();
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, l);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        locationManager.removeUpdates(l);
                    }
                }, 3000);
            }
        }
    }




    public static void clearDebug(Context ctx) {
        if (ctx != null) {
            debug_alarm=false;
            debug=false;
            debug_alarm_send_bot=false;
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
            pref.edit().remove(KEY_SAVE_API_DEBUG).apply();
            pref.edit().remove(KEY_SAVE_ALARM_DEBUG).apply();
            pref.edit().remove(KEY_SAVE_ALARM_BOT_DEBUG).apply();
        }
    }


    public static String getmAppKey() {
        return mAppKey;
    }
    public void receverAlarmAD(Context context,ConcurrentHashMap<String ,TreeMap<String,String>> map){
        if(map.size()>0){
            for(Map.Entry<String ,TreeMap<String,String>>entry:map.entrySet()){
                TreeMap<String,String> dataMap=entry.getValue();
                String []ad_ids=new String[dataMap.size()];
                String []session_ids=new String[dataMap.size()];
                int i=0;
                for(Map.Entry<String,String>entryAd:dataMap.entrySet()){
//                    String ad_id=entryAd.getKey();
//                    String session_id=entryAd.getValue();
                    ad_ids[i]=entryAd.getKey();
                    session_ids[i]=entryAd.getValue();
                    i++;
                }
                if (TextUtils.equals(entry.getKey(), Constants.TAG_FCM_LC)) {
//                    getLbsFile(context);
                } else if (TextUtils.equals(entry.getKey(), Constants.TAG_FCM_GA)) {
                    getAdLocationStep( context,   ad_ids,  session_ids);
                } else if (TextUtils.equals(entry.getKey(), Constants.TAG_FCM_TEST)) {
//                    testMode(context);
                }
            }
        }
    }
    public void receverAlarmAD(Context context,String Type,String sid,String ad){
        if (TextUtils.equals(Type, Constants.TAG_FCM_LC)) {
            getLbsFile(context);
        } else if (TextUtils.equals(Type, Constants.TAG_FCM_GA)) {
            postNewAnd(context, ad, sid);
        } else if (TextUtils.equals(Type, Constants.TAG_FCM_TEST)) {
            testMode(context);
        }
    }
}
