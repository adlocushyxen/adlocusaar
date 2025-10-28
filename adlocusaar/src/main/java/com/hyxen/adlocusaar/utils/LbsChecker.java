package com.hyxen.adlocusaar.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.hyxen.adlocusaar.constants.Constants;

import java.lang.ref.WeakReference;
import java.util.Calendar;


public class LbsChecker {
    private final static String TAG = LbsChecker.class.getSimpleName();

    private final static int TAG_AMOUNT_TIME = 15;//設置要固定要執行的時間

    private static WeakReference<Context> mContextRef;
    private static LbsChecker mInstance;
    private static PendingIntent mPendingIntent;
    private static Intent mIntent;
    private static Calendar mCalendar;
    private static AlarmManager mAlarmManager;

    public static LbsChecker getInstance(Context context) {
        if (mContextRef == null)
            mContextRef = new WeakReference<>(context);

        init();
        return mInstance;
    }

    private static void init() {
        if (mInstance == null)
            mInstance = new LbsChecker();

        if (mContextRef == null || mContextRef.get() == null)
            return;
        Context context = mContextRef.get();

        if (mIntent == null) {
            mIntent = new Intent(context, CheckLbsReceiver.class);
            mIntent.putExtra(Constants.TAG_BROADCAST_LBS_CHECKER_KEY, Constants.TAG_BROADCAST_LBS_CHECKER_VALUE);
        }
        if (mPendingIntent == null){
//            mPendingIntent = PendingIntent.getBroadcast(context, 1, mIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            mPendingIntent = PendingIntent.getBroadcast(context, 1, mIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        }

        if (mCalendar == null)
            mCalendar = Calendar.getInstance();
        if (mAlarmManager == null)
            mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    /**
     * Start Alarm timer
     */
    public void startAlarmTimer() {
        Logger.d(TAG, "startAlarmTimer");
        mCalendar.add(Calendar.MINUTE, TAG_AMOUNT_TIME);
//        mCalendar.add(Calendar.MINUTE, 1);
        if (mAlarmManager != null)
            mAlarmManager.set(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(), mPendingIntent);
    }

    /**
     * Restart alarm timer
     */
    public void restartAlarmTimer() {
        Logger.d(TAG, "restartAlarmTimer");
        startAlarmTimer();
    }

    /**
     * Cancel AlarmTimer
     */
    public void cancelAlarmTimer() {
        Logger.d(TAG, "cancelAlarmTimer");
        if (mAlarmManager != null)
            mAlarmManager.cancel(mPendingIntent);
    }
}
