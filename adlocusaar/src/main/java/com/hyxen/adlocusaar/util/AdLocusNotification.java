package com.hyxen.adlocusaar.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;

import com.hyxen.adlocusaar.AdActivity;
import com.hyxen.adlocusaar.obj.AdLocusAd;
import com.hyxen.adlocusaar.push.BigView;

public class AdLocusNotification
{
    static final String[] icon_list = new String[]
    {
        "al_icon_0_1",//0
        "al_icon_0_2",
        "al_icon_0_3",
        "al_icon_0_4",
        "al_icon_0_5",
        "al_icon_0_6",
        "al_icon_0_7",
        "al_icon_1_1",//7
        "al_icon_1_2",
        "al_icon_1_3",
        "al_icon_1_4",
        "al_icon_1_5",
        "al_icon_2_1",//12
        "al_icon_2_2",
        "al_icon_3_1",//14
        "al_icon_3_2",
        "al_icon_3_3",
        "al_icon_3_4",
        "al_icon_3_5",
        "al_icon_3_6",
        "al_icon_3_7",
        "al_icon_4_1",//21
        "al_icon_4_2",
        "al_icon_4_3",
        "al_icon_4_4",
        "al_icon_4_5",
        "al_icon_5_1",//26
        "al_icon_5_2",
        "al_icon_5_3",
        "al_icon_5_4",
        "al_icon_6_1",//30
        "al_icon_6_2",
        "al_icon_6_3",
        "al_icon_6_4",
        "al_icon_6_5",
        "al_icon_7_1",//35
        "al_icon_7_2",
        "al_icon_7_3",
        "al_icon_7_4",
        "al_icon_7_5",
        "al_icon_8_1",//40
        "al_icon_9_1",//41
        "al_icon_9_2",
        "al_icon_9_3",
        "al_icon_9_4",
        "al_icon_9_5"
    };
    private static final int[][] icon_index = new int[][]
            {
        {0,7},
        {7,5},
        {12,2},
        {14,7},
        {21,5},
        {26,4},
        {30,5},
        {35,5},
        {40,1},
        {41,5}
            };

    /**
     * 檢查Notification可否顯示
     */
    public static boolean checkNotification(Context context, AdLocusAd adInfo) {
        return showNotification(context, adInfo, false);
    }
    /**
     * 顯示Notification, 如果下載橫幅圖片失敗，即顯示預設樣式
     * @param adInfo 橫幅圖片連結
     */
    public static boolean showNotification(Context context, AdLocusAd adInfo) {
        return showNotification(context, adInfo, true);
    }

    /**
     * 顯示Notification, 如果下載橫幅圖片失敗，即顯示預設樣式
     * @param adInfo 橫幅圖片連結
     * @param showNotif   false 單純取得boolean值 過程不會產生notification ; true 顯示廣告(如果可以)
     */

    private static boolean showNotification(Context context, AdLocusAd adInfo, boolean showNotif) {
        if(adInfo.isHouseAd) {
            adInfo.icon_id = context.getApplicationInfo().icon;
        } else {
            try {
                Class<?> classDrawable = Class.forName( AdLocusUtil.CLASSS_NAME_ADLOCUS_LIB + "drawable" );
                String icon_name = icon_list[icon_index[0][0] + (int)(Math.random() * icon_index[0][1])];
                
                if(AdLocusUtil.targetSdkStyleLollipop(context))
                	icon_name = icon_name + "_5";
                
                adInfo.icon_id = AdLocusUtil.getResId(classDrawable, icon_name);
                
            } catch (ClassNotFoundException ignored) { }
        }
            
    	if(adInfo.type == AdLocusUtil.AD_TYPE_BIGVIEW)
    	{
            return BigView.showBigView(context, adInfo, showNotif);
    	}
    	else //old
    	{
	        if(adInfo.isNewStyle())
	        {
	            if(AdLocusUtil.getNewPushBackgroundIntent(context) != null)
	            {
                    if (!showNotif) return true;
	                else return showNotificationNew(context, adInfo);
	            }
	        }
	        
	        return showNotificationOld(context, adInfo, showNotif);
    	}
    }


    /**
     * 顯示Notification, 如果下載橫幅圖片失敗，即顯示預設樣式
     * @param adInfo 橫幅圖片連結
     */
    private static boolean showNotificationNew(Context context, AdLocusAd adInfo)
    {
        //設定icon
        int icon = context.getApplicationInfo().icon;

        //設定要開啟的class
        Intent notificationIntent = new Intent(context, AdActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
        notificationIntent.putExtra("adInfo", adInfo);
        int requestCode = adInfo.id.hashCode();

        // add all of DetailsActivity's parents to the stack,
        // followed by DetailsActivity itself

        PendingIntent contentIntent =
                TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(AdLocusUtil.getNewPushBackgroundIntent(context))
                .addNextIntent(notificationIntent)
                .getPendingIntent(requestCode, PendingIntent.FLAG_UPDATE_CURRENT);
        
        NotificationCompat.Builder b = new NotificationCompat.Builder(context);
        b.setAutoCancel(true);
        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), icon);
        int dp64 = (int)(context.getResources().getDisplayMetrics().density * 64);
        if (bm.getWidth() > dp64)
        {
            b.setLargeIcon(Bitmap.createScaledBitmap(bm, dp64, dp64, false));
        }
        bm.recycle();
        System.gc();
        
        b.setSmallIcon(icon);
        b.setContentTitle(context.getString(context.getApplicationInfo().labelRes));
        b.setContentText(adInfo.scad_txt);
        b.setStyle(new NotificationCompat.BigTextStyle().bigText(adInfo.scad_txt));
        b.setContentIntent(contentIntent);
        
        Notification n = b.build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(requestCode, n);

        return true;
    }

    /**
     * 顯示Notification, 如果下載橫幅圖片失敗，即顯示預設樣式
     * @param adInfo 橫幅圖片連結
     */
    private static boolean showNotificationOld(Context context, AdLocusAd adInfo, boolean showNoti)
    {
        //設定icon
        int icon = -1;
        if(adInfo.isHouseAd)
        {
            icon = context.getApplicationInfo().icon;
        } else {
            Class<?> classDrawable;
            try {
                classDrawable = Class.forName( AdLocusUtil.CLASSS_NAME_ADLOCUS_LIB + "drawable" );
            } catch (ClassNotFoundException e) {
                return false;
            }

            String icon_postfix = "";
            
            if(AdLocusUtil.targetSdkStyleLollipop(context)) icon_postfix = "_5";

            
            if(adInfo.type == AdLocusUtil.AD_TYPE_BANNER || adInfo.leftImageType == AdLocusUtil.ICON_TYPE_CUSTOM)
            {
                icon = AdLocusUtil.getResId(classDrawable, icon_list[icon_index[0][0] + (int)(Math.random() * icon_index[0][1])]+icon_postfix);
            }
            else if(adInfo.leftImageType!=-1 && adInfo.leftImageType < icon_index.length)
            {
                icon = AdLocusUtil.getResId(classDrawable, icon_list[icon_index[adInfo.leftImageType][0] + (int)(Math.random() * icon_index[adInfo.leftImageType][1])]+icon_postfix);
            }
            if (icon == -1)
            {
                icon = AdLocusUtil.getResId(classDrawable, "al_icon");
            }
        }


        PendingIntent contentIntent = getNotificationIntent(context, adInfo);
        if (contentIntent == null) return false;

        NotificationCompat.Builder b = new NotificationCompat.Builder(context);
        b.setAutoCancel(true);
        b.setSmallIcon(icon);
        RemoteViews remoteViews = getView(context, adInfo);
        if (remoteViews == null) return false;
        if (!showNoti) return true;
        b.setContent(remoteViews);
        b.setContentTitle(adInfo.description);
        b.setContentText("");
        b.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        b.setTicker(adInfo.description);
        b.setContentIntent(contentIntent);
        b.setVibrate(new long[]{0});
        b.setPriority(Notification.PRIORITY_HIGH);
        Notification n = b.build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        int requestCode = adInfo.id.hashCode();
        notificationManager.notify(requestCode, n);
        return true;
    }
    
    
    private static RemoteViews getView(Context context, AdLocusAd promeAd)
    {
        Class<?> classLayout;
        try {
            classLayout = Class.forName( AdLocusUtil.CLASSS_NAME_ADLOCUS_LIB + "layout" );
        } catch (ClassNotFoundException e) { return null; }

        Class<?> classId;
        try {
            classId = Class.forName( AdLocusUtil.CLASSS_NAME_ADLOCUS_LIB + "id" );
        } catch (ClassNotFoundException e) { return null; }

        RemoteViews contentView = new RemoteViews(context.getPackageName(), AdLocusUtil.getResId(classLayout, "prome_layout"));

        int appIcon = AdLocusUtil.getResId(classId, "AppIcon");

        int dp10 = (int) (context.getResources().getDisplayMetrics().density * 10);
        Bitmap icon = AdLocusUtil.getApplicationIconBitmap(context, dp10, dp10);
        contentView.setImageViewBitmap(appIcon, icon);
        int appName = AdLocusUtil.getResId(classId, "AppName");
        contentView.setTextViewText(appName, context.getString(context.getApplicationInfo().labelRes));

        switch (promeAd.type)
        {
            case AdLocusUtil.AD_TYPE_BANNER:
                if (promeAd.image == null) return null;

                int pnimid = AdLocusUtil.getResId(classId, "ProMe_NotificationImage");
                contentView.setViewVisibility(pnimid, View.VISIBLE);
                contentView.setImageViewBitmap(pnimid, promeAd.image);

                int idClickIcon = AdLocusUtil.getResId(classId, "ProMe_NotificationClickIcon");
                contentView.setViewVisibility(idClickIcon, View.GONE);

                int idLogo = AdLocusUtil.getResId(classId, "ProMe_NotificationLogo");
                contentView.setViewVisibility(idLogo, View.GONE);
                return contentView;
            case AdLocusUtil.AD_TYPE_ICON:
                if (promeAd.leftImageType == AdLocusUtil.ICON_TYPE_CUSTOM && promeAd.image == null) return null;

                int pnicid = AdLocusUtil.getResId(classId, "ProMe_NotificationIcon");
                contentView.setViewVisibility(pnicid, View.VISIBLE);

                switch (promeAd.leftImageType)
                {
                    case AdLocusUtil.ICON_TYPE_CUSTOM:
                        contentView.setImageViewBitmap(pnicid, promeAd.image);
                        break;
                    default:
                        contentView.setImageViewBitmap(pnicid, AdLocusUtil.getLeftIconBitmap(context, promeAd.leftImageType));
                        break;
                }
                int idText = AdLocusUtil.getResId(classId, "ProMe_NotificationText");
                contentView.setViewVisibility(idText, View.VISIBLE);
                contentView.setTextViewText(idText, promeAd.description);

                int idClickIcon1 = AdLocusUtil.getResId(classId, "ProMe_NotificationClickIcon");
                contentView.setImageViewBitmap(idClickIcon1, AdLocusUtil.getClickIconBitmap(context, promeAd.clickType));

                int idLogo1 = AdLocusUtil.getResId(classId, "ProMe_NotificationLogo");
                contentView.setImageViewBitmap(idLogo1, AdLocusUtil.getAssetsBitmap(context, "pm_ad_logo_728"));

                return contentView;
        }
        
        return contentView;
    }
    

    private static PendingIntent getNotificationIntent(Context context, AdLocusAd adInfo)
    {

        switch (adInfo.linkType)
        {
            case AdLocusUtil.LINK_TYPE_URL:
                if(adInfo.link != null)
                {
                    return getUrlIntent(context, adInfo.link, adInfo.id.hashCode());
                }
                else
                {
                    Log.d("In onInterceptTouchEvent(), but custom.link is null");
                }
                break;
            default:
                break;
        }
        return null;
    }

    public static PendingIntent getUrlIntent(Context context, String url, int id) {

        Intent intent = new Intent(context, AdActivity.class);
        intent.setAction(AdActivity.ACTION_CLICK);
        intent.putExtra("url", url);
        intent.putExtra("id", id);

        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void cancelNotification(Context context, int id) {

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
    }
}
