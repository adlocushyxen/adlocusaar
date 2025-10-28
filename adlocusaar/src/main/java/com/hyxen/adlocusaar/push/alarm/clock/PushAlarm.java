package com.hyxen.adlocusaar.push.alarm.clock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import com.hyxen.adlocusaar.AdLocus;
import com.hyxen.adlocusaar.repository.Repository;
import com.hyxen.adlocusaar.utils.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by leo3x on 2018/11/9.
 * 定期回訪設定
 * SDK定時喚醒參考以下待播的廣告清單
  *http://192.173.146.162/devpush/json/local_db_and.json
  *自行判斷目前是否有符合條件的廣告
 */

public class PushAlarm {
    public static final int PendingIntentID = 4525;
    private static final String KEY_SAVE_FCM_RECEVER_TIME = "KEY_SAVE_FCM_RECEVER_TIME";
    private static final String KEY_ALARM_START_TIME = "KEY_ALARM_START_TIME";
    private static final String KEY_ALARM_RUN_TIME = "KEY_ALARM_RUN_TIME";
    private static final long time_interval=AlarmManager.INTERVAL_HOUR*2;
    private static final String TAG = PushAlarm.class.getSimpleName();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void setFcmRecever(Context context, long l) {
       if(context==null) return;
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit().putLong(KEY_SAVE_FCM_RECEVER_TIME, l).apply();
    }
    public static boolean startPushAlarmFromReboot(Context context){
        if(AdLocus.isAlarmDebug())Logger.d(TAG, "[startPushAlarmFromReboot] start");
//        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
//        long time= pref.getLong(KEY_ALARM_START_TIME, -1);
        return startPushAlarm( context);
    }
    public static boolean startPushAlarmFromInit(Context context){
        if(AdLocus.isAlarmDebug())Logger.d(TAG, "[startPushAlarmFromInit] start");
//        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
//        long time= pref.getLong(KEY_ALARM_START_TIME, -1);
//        if(time>=0)startPushAlarm( context);
        return startPushAlarm( context);
    }
    public static boolean startPushAlarm(Context context){
        if(context==null) return false;
        AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(context, PushAlarmReceiver.class);
        intent.setAction(PushAlarmReceiver.FLAG_ACTION);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        long firstTime= pref.getLong(KEY_ALARM_START_TIME, -1);
        if(firstTime>=0){
//            PendingIntent stop  = PendingIntent.getBroadcast(context, PendingIntentID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent stop  = PendingIntent.getBroadcast(context, PendingIntentID, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            alarm.cancel(stop);
            if(AdLocus.isAlarmDebug())Logger.d(TAG, "[startPushAlarm] cancel:"+PendingIntentID);
        }else{
            firstTime = (long)(Math.random()* time_interval/1000*1000);
            pref.edit().putLong(KEY_ALARM_START_TIME, firstTime).apply();
            if(AdLocus.isAlarmDebug())Logger.d(TAG, "[startPushAlarm] new:"+firstTime);
        }

        long nowtime=System.currentTimeMillis();
        long base_time=nowtime%time_interval/1000*1000;
        if(firstTime>base_time){
            firstTime=firstTime-base_time+nowtime;
        }else {
            firstTime=firstTime-base_time+nowtime+time_interval;
        }
        long last_time= pref.getLong(KEY_ALARM_RUN_TIME, -1);
        if(firstTime<=last_time)firstTime=firstTime+time_interval;


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if(!alarm.canScheduleExactAlarms()){
                return false;
            }
        }
        PendingIntent sender  = PendingIntent.getBroadcast(context, PendingIntentID, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, firstTime, sender);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarm.setExact(AlarmManager.RTC_WAKEUP, firstTime, sender);
        } else {
            alarm.set(AlarmManager.RTC_WAKEUP, firstTime, sender);
        }
        return true;
    }
    public static boolean isReceverFCM(Context context){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        long time= pref.getLong(KEY_SAVE_FCM_RECEVER_TIME, 0);
        if(time<=0)return false;
        if(System.currentTimeMillis()-time>time_interval)return false;
        return true;

    }


}
