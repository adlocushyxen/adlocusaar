package com.hyxen.adlocusaar.push;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
//import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.hyxen.adlocusaar.AdActivity;
//import com.hyxen.adlocusaar.adlocuslib.R;
import com.hyxen.adlocusaar.UserBaseData;
import com.hyxen.adlocusaar.net.HxRequest;
import com.hyxen.adlocusaar.obj.AdLocusAd;
import com.hyxen.adlocusaar.util.AdLocusNotification;
import com.hyxen.adlocusaar.util.AdLocusUtil;
import com.hyxen.adlocusaar.util.Log;

public class BigView
{
	public enum Type {
		Setting,
		Share;

		static final String KEY = "action";

		static final String[] MAPPING = {
				"setting",
				"share"
		};

		static public String key()
		{
			return KEY;
		}

		public String type()
		{
			return MAPPING[this.ordinal()];
		}
	}

	public static void shareAd(Context context, AdLocusAd ad)
	{
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("text/plain");
		i.putExtra(Intent.EXTRA_TITLE, ad.bv_share_text);
		i.putExtra(Intent.EXTRA_SUBJECT, ad.bv_text);
		i.putExtra(Intent.EXTRA_TEXT, ad.bv_share_text);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		try {
			context.startActivity(i);
		}
		catch (android.content.ActivityNotFoundException ignored) { }
		
		context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
	}	
	
	
	public static void doFeedback(Context context, Type feedback, AdLocusAd ad) {
		HxRequest r = new HxRequest(context, AdLocusUtil.URL_BIGVIEW_FEEDBACK);
		r.setMethod(HxRequest.Method.GET);
		r.setPostParameter("ad_id", ad.id);
		r.setPostParameter("appkey", ServiceUtil.getValidKey(context));
//		r.setPostParameter("device_id", AdLocusUtil.getEncodedDeviceId(context));
		r.setPostParameter("device_id", UserBaseData.getHashDeviceId(context));
		r.setPostParameter("timestamp", "" + System.currentTimeMillis());
		r.setPostParameter("action", "click");
		r.setPostParameter("type", feedback.type());
		new Thread(r).start();
	}


	/**
	 * Will show bigview if Intent content bigview message.
	 * @return	True if intent content bigview message.
	 */
	public static boolean showBigView(Context context, AdLocusAd adInfo ,boolean showNoti)
	{
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) return false;
		if (!adInfo.isBigView()) return false;



		HxRequest r = new HxRequest(context, adInfo.bv_banner);
		r.setGetByte();
		r.run();
		if (r.hasError()) return false;

		byte[] ret = r.getResultByteArray();

		Bitmap image = BitmapFactory.decodeByteArray(ret, 0, ret.length);

		if (image == null) return false;
		if (showNoti) buildBigViewNotification(context, adInfo, image);

		return true;
	}

	private static PendingIntent pendingIntentByResource(Context context, Type resource, AdLocusAd ad)
	{

		Intent intent = new Intent(context, AdActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(AdActivity.ACTION_BIGVIEW_INTENT);
		intent.putExtra("adInfo", ad);

		int ad_id = (ad.id + resource.ordinal()).hashCode() ;
		switch (resource) {
			case Setting:
			case Share:
				intent.putExtra(Type.key(), resource.name());
				break;
			default:
				break;
		}

//		return PendingIntent.getActivity(context, ad_id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		return PendingIntent.getActivity(context, ad_id, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
	}


	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public static void buildBigViewNotification(Context context, AdLocusAd ad, Bitmap image)
	{
		Bitmap largeIcon = AdLocusUtil.getApplicationIconBitmap(context,
				context.getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_width),
				context.getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_height));

		String title = ad.getBigViewTitle(context);


//		Log.d("bv_content id:" + R.layout.bv_content + "\n" + R.layout.class.getCanonicalName() + "\n" + R.layout.class);
		Log.d("bv_content ref id:" + AdLocusUtil.getResId("layout", "bv_content"));
		RemoteViews bigView = new RemoteViews(context.getPackageName(), AdLocusUtil.getResId("layout", "bv_content"));
		bigView.setImageViewBitmap(AdLocusUtil.getResId("id", "b_imageview_icon"), largeIcon);

		bigView.setTextViewText(AdLocusUtil.getResId("id", "b_textview_title"), title);
		bigView.setTextViewText(AdLocusUtil.getResId("id", "b_textview_subtitle"), ad.bv_text);

		bigView.setImageViewBitmap(AdLocusUtil.getResId("id", "b_imageview_bigimage"), image);

		bigView.setOnClickPendingIntent(AdLocusUtil.getResId("id", "b_button_setting"), pendingIntentByResource(context, Type.Setting, ad));
		bigView.setOnClickPendingIntent(AdLocusUtil.getResId("id", "b_button_share"), pendingIntentByResource(context, Type.Share, ad));

		RemoteViews contentView = new RemoteViews(context.getPackageName(), AdLocusUtil.getResId("layout", "bv_basic"));
		contentView.setImageViewBitmap(AdLocusUtil.getResId("id", "b_imageview_icon"), largeIcon);
		contentView.setTextViewText(AdLocusUtil.getResId("id", "b_textview_title"), title);
		contentView.setTextViewText(AdLocusUtil.getResId("id", "b_textview_subtitle"), ad.bv_text);

		Notification noti = new NotificationCompat.Builder(context)
				.setContentIntent(AdLocusNotification.getUrlIntent(context, ad.link, ad.id.hashCode()))
				.setSmallIcon(ad.icon_id == -1 ? context.getApplicationInfo().icon : ad.icon_id)
				.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
				.setPriority(NotificationCompat.PRIORITY_MAX)
				.setContentText(ad.bv_text)
				.setLargeIcon(largeIcon)
				.setContentTitle(title)
				.setTicker(ad.bv_text)
				.setCustomHeadsUpContentView(bigView)
				.setVibrate(new long[]{0})
				.setAutoCancel(true)
				.setCustomBigContentView(bigView)
				.setCustomContentView(contentView)
				.build();


		NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(ad.id.hashCode(), noti);
	}




}
