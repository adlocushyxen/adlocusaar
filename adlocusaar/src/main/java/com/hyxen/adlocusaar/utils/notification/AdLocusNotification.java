package com.hyxen.adlocusaar.utils.notification;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.hyxen.adlocusaar.constants.Constants;
import com.hyxen.adlocusaar.repository.data.response.GetLbsTaskResponse;
import com.hyxen.adlocusaar.repository.data.response.GetNewAndResponse;
import com.hyxen.adlocusaar.utils.Logger;

import java.util.HashMap;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class AdLocusNotification extends NotificationHelp {
    private static final String TAG = AdLocusNotification.class.getSimpleName();

    public static void showNotification(Context context, GetNewAndResponse adInfo) {
        Logger.i(TAG, "[Method] -> notificationInit()");
        if (adInfo == null)
            return;
        switch (adInfo.getAdType()) {
            case Constants.TAG_AD_TYPE_ICON:
                showNotificationORIGIN(context, adInfo);
                break;
            case Constants.TAG_AD_TYPE_BANNER:
                showNotificationBanner(context, adInfo);
                break;
            case Constants.TAG_AD_TYPE_BIG_VIEW:
                showNotificationBigView(context, adInfo);
                break;
        }
    }

    private static void showNotificationORIGIN(final Context context, final GetNewAndResponse adData) {
        if (adData == null) {
            Logger.e(TAG, "[iconText] adData is null");
            return;
        }

        if (TextUtils.isEmpty(adData.getAdId())) {
            Logger.e(TAG, "[iconText] adData.getAdId() is null");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                Disposable disposable= downloadImageJ(context, adData.getAdIcon())
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Bitmap>() {
                            @Override
                            public void accept(Bitmap s) throws Exception {
                                iconText(context, adData,s);
                                release();
                                s=null;
                            }
                        },new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable s) throws Exception {
                                iconText(context, adData,null);
                                release();
                            }
                        });
            }
        }).start();

//        iconText(context, adData);
//        release();
    }

    private static void showNotificationBanner(final Context context, final GetNewAndResponse adData) {
        if (adData == null) {
            Logger.e(TAG, "[iconText] adData is null");
            return;
        }

        if (TextUtils.isEmpty(adData.getAdId())) {
            Logger.e(TAG, "[iconText] adData.getAdId() is null");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                Disposable disposable= downloadImageJ(context, adData.getAdIcon())
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Bitmap>() {
                            @Override
                            public void accept(Bitmap s) throws Exception {
                                banner(context, adData,s);
                                release();
                                s=null;
                            }
                        },new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable s) throws Exception {
                                banner(context, adData,null);
                                release();
                            }
                        });
            }
        }).start();

//        banner(context, adData);
//        release();
    }

    private static void showNotificationBigView(final Context context, final GetNewAndResponse adData) {
        if (adData == null) {
            Logger.e(TAG, "[iconText] adData is null");
            return;
        }

        if (TextUtils.isEmpty(adData.getAdId())) {
            Logger.e(TAG, "[iconText] adData.getAdId() is null");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                Disposable disposable= downloadImageJ(context, adData.getAdIcon())
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Bitmap>() {
                            @Override
                            public void accept(Bitmap s) throws Exception {
                                bigView(context, adData,s);
                                release();
                                s=null;
                            }
                        },new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable s) throws Exception {
                                bigView(context, adData,null);
                                release();
                            }
                        });
            }
        }).start();

//        bigView(context, adData);
//        release();
    }
}
