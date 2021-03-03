package com.hyxen.adlocusaar.utils.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.hyxen.adlocusaar.constants.Constants;
import com.hyxen.adlocusaar.view.main.AdLocusActivity;
import com.hyxen.adlocusaar.R;
import com.hyxen.adlocusaar.repository.data.response.GetNewAndResponse;
import com.hyxen.adlocusaar.repository.remote.net.HxRequest;
import com.hyxen.adlocusaar.utils.Logger;
import com.hyxen.adlocusaar.utils.MiscUtils;
import com.hyxen.adlocusaar.view.main.AdLocusContract;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Single;

public class NotificationHelp {
    private static final String TAG = NotificationHelp.class.getSimpleName();

    private static final String TAG_DEFAULT_CHANNEL_ID = "adLocus";
    private static final String TAG_DEFAULT_CHANNEL_NAME = "NOTIFICATION_CHANNEL_NAME_AD_LOCUS";
    private static Bitmap mImage = null;
    private static HxRequest mHxRequest = null;
    private static NotificationCompat.Builder mBuilder;
    private static NotificationManager mManager;
    private static RemoteViews mCustomView;

    /**
     * Set notification config for under Notification 8.0(Oreo)
     *
     * @param context
     * @param adData
     */
    private static void notificationConfig(Context context, GetNewAndResponse adData) {
        mBuilder.setSmallIcon(getIcon(context))
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setContentTitle(!TextUtils.isEmpty(adData.getAdTitle()) ? adData.getAdTitle() : "")
                .setShowWhen(false)
                .setContentText(!TextUtils.isEmpty(adData.getAdBody()) ? adData.getAdBody() : "")
                .setVibrate(new long[0])
                .setContentIntent(getUrlIntent(context, adData.getAdLink(), (adData.getAdId()).hashCode(), adData.getTrackImp()))
                .setAutoCancel(true);

        notificationConfig8();
    }

    /**
     * Set notification config for Android 8.0(Oreo)
     */
    private static void notificationConfig8() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(TAG_DEFAULT_CHANNEL_ID, TAG_DEFAULT_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            mBuilder.setChannelId(TAG_DEFAULT_CHANNEL_ID);
            mManager.createNotificationChannel(notificationChannel);
        }
    }

    /**
     * get Url Intent
     *
     * @param context
     * @param url
     * @param id
     * @param trackImp
     * @return
     */
    private static PendingIntent getUrlIntent(Context context, String url, int id, String trackImp) {
        Intent intent = new Intent(context, AdLocusActivity.class);
        intent.setAction(AdLocusContract.ACTION_CLICK);
        intent.putExtra(Constants.TAG_INTENT_URL, url);
        intent.putExtra(Constants.TAG_INTENT_ID, id);
        intent.putExtra(Constants.TAG_INTENT_KEY_TRACK_IMP, trackImp);

        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Get Big View Setting Intent
     *
     * @param context
     * @param id
     * @param adData
     * @return
     */
    private static PendingIntent getSettingPendingIntent(Context context, int id, GetNewAndResponse adData) {
        Intent intent = new Intent(context, AdLocusActivity.class);
        intent.setAction(AdLocusContract.ACTION_BIG_VIEW_INTENT);
        intent.putExtra(Constants.TAG_INTENT_KEY_TYPE, Constants.TAG_INTENT_SETTING);
        intent.putExtra(Constants.TAG_INTENT_SHARE_DATA, adData);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(context, id + 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Get Big View Sharing Intent
     *
     * @param context
     * @param id
     * @param adData
     * @return
     */
    private static PendingIntent getSharePendingIntent(Context context, int id, GetNewAndResponse adData) {
        Intent intent = new Intent(context, AdLocusActivity.class);
        intent.setAction(AdLocusContract.ACTION_BIG_VIEW_INTENT);
        intent.putExtra(Constants.TAG_INTENT_KEY_TYPE, Constants.TAG_INTENT_SHARE);
        intent.putExtra(Constants.TAG_INTENT_SHARE_DATA, adData);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(context, id + 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * 抓取外部app預設之Icon,若無則使用Default icon
     *
     * @param context
     * @return
     */
    private static int getIcon(Context context) {
        int appIconResId = context.getResources().getIdentifier("ad_locus_icon", "drawable", context.getPackageName());
        if (appIconResId == 0) {
            appIconResId = MiscUtils.getAppIcon(context);
        }
//        int appIconResId = MiscUtils.getAppIcon(context);
        return appIconResId;
    }

    /**
     * 抓取外部app預設之color,若無則使用Default color
     *
     * @param context
     * @return
     */
    private static int getBackgroundColor(Context context) {
        return context.getResources().getIdentifier("ad_app_icon_color", "string", context.getPackageName());
    }

    /**
     * download notification icon
     *
     * @param context
     * @param adIcon
     * @return
     */
    private static void downloadImage(Context context, String adIcon) {
        if (TextUtils.isEmpty(adIcon)) {
            Logger.e(TAG, "[downloadImage] adIcon is null");
            return;
        }
        mHxRequest = new HxRequest(context, adIcon);
        mHxRequest.setGetByte();
        mHxRequest.run();
        if (!mHxRequest.hasError()) {
            byte[] ret = mHxRequest.getResultByteArray();
            mImage = BitmapFactory.decodeByteArray(ret, 0, ret.length);
        }
    }
    public static Single<Bitmap> downloadImageJ(Context context, String adIcon) {
        if (TextUtils.isEmpty(adIcon)) {
            Logger.e(TAG, "[downloadImage] adIcon is null");
            return Single.error(new Throwable("downloadImage] adIcon is null"));
        }
        HxRequest mmHxRequest;
        Bitmap mImage = null;
        try{
            mmHxRequest = new HxRequest(context, adIcon);
            mmHxRequest.setGetByte();
            mmHxRequest.run();
            if (!mmHxRequest.hasError()) {
                byte[] ret = mmHxRequest.getResultByteArray();
                mImage = BitmapFactory.decodeByteArray(ret, 0, ret.length);
            }
        }catch(Exception e){
            return Single.error(new Throwable("[downloadImage] adIcon is exception"));
        }
        if(mImage==null)return Single.error(new Throwable("[downloadImage] adIcon is exception"));
        return Single.just(mImage);
    }

    /**
     * When notification is shown.
     * call this at end of method.
     * Release reference variable.
     */
    static void release() {
        mImage = null;
        mHxRequest = null;
        mManager = null;
        mBuilder = null;
        mCustomView = null;
    }

    /**
     * Init Notification object
     *
     * @param context
     * @param adData
     */
    private static void init(Context context, GetNewAndResponse adData) {
        mBuilder = new NotificationCompat.Builder(context.getApplicationContext(), TAG_DEFAULT_CHANNEL_ID);
        mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (TextUtils.isEmpty(adData.getAdId())) {
            Logger.e(TAG, "[init] adData.getAdId() is null");
            return;
        }
        int backgroundColor = getBackgroundColor(context);

        notificationConfig(context, adData);

        if (backgroundColor != 0)
            mBuilder.setColor(backgroundColor);
    }

    /**
     * Show notification
     *
     * @param requestCode
     */
    private static void show(int requestCode) {
        if (mManager != null)
            mManager.notify(requestCode, mBuilder.build());
        else {
            Logger.e(TAG, "[show] mManager is null");
        }
    }

    /**
     * Setting for banner
     *
     * @param context
     * @param adData
     * @param _mimage
     */
    static void banner(Context context, GetNewAndResponse adData,Bitmap _mimage) {
        if (adData == null) {
            Logger.e(TAG, "[banner] adData is null");
            return;
        }

        if (TextUtils.isEmpty(adData.getAdId())) {
            Logger.e(TAG, "[iconText] adData.getAdId() is null");
            return;
        }

//        downloadImage(context, adData.getAdIcon());
        init(context, adData);

        int requestCode = (adData.getAdId()).hashCode();

        if (!TextUtils.isEmpty(adData.getAdlocusAppPosition())) {
            int position = Integer.parseInt(adData.getAdlocusAppPosition());
            if (position > 0 && position <= 4) {
                GetNewAndResponse.BannerInfo bannerInfo = adData.getBannerInfo(position);
                mCustomView = new RemoteViews(context.getPackageName(), R.layout.bv_notification);
                mCustomView.setImageViewBitmap(R.id.bv_n_iv_ad, _mimage); // banner image
                mCustomView.setViewVisibility(bannerInfo.getBackgroundViewId(), View.VISIBLE);
                mCustomView.setImageViewResource(bannerInfo.getIconViewId(), MiscUtils.getAppIcon(context)); // icon
                mCustomView.setViewVisibility(bannerInfo.getIconViewId(), View.VISIBLE);
                mBuilder.setCustomContentView(mCustomView);
            }
        }

        show(requestCode);
    }

    /**
     * Setting for icon and text
     *
     * @param context
     * @param adData
     * @param _mimage
     */
    static void iconText(Context context, GetNewAndResponse adData,Bitmap _mimage) {
        if (adData == null) {
            Logger.e(TAG, "[iconText] adData is null");
            return;
        }

        if (TextUtils.isEmpty(adData.getAdId())) {
            Logger.e(TAG, "[iconText] adData.getAdId() is null");
            return;
        }

//        downloadImage(context, adData.getAdIcon());
        int requestCode = (adData.getAdId()).hashCode();
        init(context, adData);

        //若Server來的image is null，則使用預設圖
        if (_mimage == null) {
            int defaultResource = adData.getLeftIconResource();
            if (defaultResource != 0)
                _mimage = MiscUtils.resourceToBitmap(context, defaultResource);
        }
        mBuilder.setLargeIcon(_mimage);

        show(requestCode);
    }

    /**
     * Setting for big view
     *
     * @param context
     * @param adData
     *  @param _mimage
     */
    static void bigView(Context context, GetNewAndResponse adData,Bitmap _mimage) {
        if (adData == null) {
            Logger.e(TAG, "[bigView] adData is null");
            return;
        }

        if (TextUtils.isEmpty(adData.getAdId())) {
            Logger.e(TAG, "[bigView] adData.getAdId() is null");
            return;
        }

//        downloadImage(context, adData.getAdIcon());
        String adId = adData.getAdId();
        int requestCode = (adId).hashCode();
        init(context, adData);

        mCustomView = new RemoteViews(context.getPackageName(), R.layout.bv_content);
        mCustomView.setTextViewText(R.id.b_textview_title, !TextUtils.isEmpty(adData.getAdTitle()) ? adData.getAdTitle() : "");
        mCustomView.setTextViewText(R.id.b_textview_subtitle, !TextUtils.isEmpty(adData.getAdBody()) ? adData.getAdBody() : "");
        mCustomView.setImageViewResource(R.id.b_imageview_icon, getIcon(context));
        mCustomView.setImageViewBitmap(R.id.b_imageview_bigimage, _mimage);
        mCustomView.setOnClickPendingIntent(R.id.b_button_setting, getSettingPendingIntent(context, requestCode, adData));
        mCustomView.setOnClickPendingIntent(R.id.b_button_share, getSharePendingIntent(context, requestCode, adData));
        mBuilder.setCustomBigContentView(mCustomView);

        mBuilder.setPriority(Notification.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setTicker(!TextUtils.isEmpty(adData.getAdBody()) ? adData.getAdBody() : "")
                .setCustomHeadsUpContentView(mCustomView)
                .setCustomContentView(mCustomView);

        show(requestCode);
    }
}
