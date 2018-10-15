package com.hyxen.adlocusaar;

import android.app.AlarmManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import com.hyxen.adlocusaar.BuildConfig;
import com.hyxen.adlocusaar.constants.Constants;
import com.hyxen.adlocusaar.push.PushJobService;
import com.hyxen.adlocusaar.repository.Repository;
import com.hyxen.adlocusaar.repository.data.request.PushTokenRequest;
import com.hyxen.adlocusaar.repository.data.response.GetFCMDataResponse;
import com.hyxen.adlocusaar.utils.AdLocusUtil;
import com.hyxen.adlocusaar.utils.Logger;

import java.lang.ref.WeakReference;
import java.util.Map;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class AdLocus extends AdLocusHelp implements IAdLocus {
    private static final String TAG = AdLocus.class.getSimpleName();

    private static AdLocus mInstance;
    private static WeakReference<Context> mContextRef;

    private String mFcmToken;
    private String mFcmAppKey;
    private String mAppPackageName;
    private static String mAppKey;
    private static boolean debug=BuildConfig.DEBUG;

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

        GetFCMDataResponse response = GetFCMDataResponse.getInstance();
        response.parseHash(fcmMessage);

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
    }

    public static boolean isDebug() {
        return debug;
    }

    public AdLocus setDebug(boolean debug) {
        AdLocus.debug = debug;
        return getInstance();
    }

    public static String getmAppKey() {
        return mAppKey;
    }
}
