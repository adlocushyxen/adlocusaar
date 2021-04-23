package com.hyxen.adlocusaar.util;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;

import com.hyxen.adlocusaar.AdActivity;
import com.hyxen.adlocusaar.AdLocusLayout;
//import com.hyxen.adlocusaar.AdLocusLibrary.BuildConfig;
import com.hyxen.adlocusaar.AdLocusManager;
import com.hyxen.adlocusaar.AdLocusTargeting;
import com.hyxen.adlocusaar.AdLocusTargeting.Gender;
import com.hyxen.adlocusaar.BuildConfig;
import com.hyxen.adlocusaar.engine.CellInfo;
import com.hyxen.adlocusaar.engine.HxCellEngine;
import com.hyxen.adlocusaar.engine.HxWifiEngine;
import com.hyxen.adlocusaar.engine.WifiInfo;
import com.hyxen.adlocusaar.net.HxRequest;
import com.hyxen.adlocusaar.obj.AdLocusAd;
import com.hyxen.adlocusaar.push.PushService;
import com.hyxen.adlocusaar.push.TestUtil;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.net.NetworkInterface;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

public class AdLocusUtil
{

    public static final Boolean DEBUG = false;
	public static final Boolean SHOW_LOG = BuildConfig.DEBUG;

	private static final String DEBUG_DEVICE_ID = DEBUG ? ""+Math.random() : "";

    public static final String VERSION = "8cb5e87946ccaa3e769daa1228f4aa8d72620d6d";
    public static final String VERSION_STRING = BuildConfig.VERSION_NAME;

    public static final String PREFIX = "alo_";

	public static final String CLASSS_NAME_ADLOCUS_LIB = "com.hyxen.adlocusaar.AdLocusLibrary.R$";

	private static final String PREFERENCE_NAME = PREFIX + "AdLocusUtil";
	private static final String PREFERENCE_KEY = "key";
	private static final String PREFERENCE_PAUSE = "pause";
	private static final String PREFERENCE_COLLECTION = "collection";
	private static final String PREFERENCE_COLLECTION_TRACKTIME = "collectiontracktime";
	private static final long NEXT_TRACK_TIME = (long) 86400 * 30 * 1000;

	private static final String PREFERENCE_BACKGROUND_INTENT = "backgroundintent";
	private static final String PREFERENCE_TARGETING = "targeting";

//	private static final String HOST_RD = "http://hyxen-adlocus-api.rd.hyxencloud.com/";
	private static final String HOST_RD = "https://paul.adlocus_api.dev.hxcld.com/";
	private static final String HOST_ADLOCUS = "https://a.api.ad-locus.com/";

	private static final String HOST_DATA_RD = "https://data.rd.adlocus.com/";
	private static final String HOST_DATA_ADLOCUS = "https://data.adlocus.com/";

	private static final String REDIRECTS_CHECK_URL_RD = "hyxencloud.com";
	private static final String REDIRECTS_CHECK_URL_ADLOCUS = "ad-locus.com";

	private static final String MAC_DEFAULT  = "02:00:00:00:00:00";

	//-------------
	
	public static final String HOST;
	public static final String HOST_DATA;
	public static final String REDIRECTS_CHECK_URL;
    public static final int VERSION_INT;
    static {
        if (DEBUG) {
            HOST = HOST_RD;
			HOST_DATA = HOST_DATA_RD;
            REDIRECTS_CHECK_URL = REDIRECTS_CHECK_URL_RD;
            VERSION_INT = 0;
        } else {
            HOST = HOST_ADLOCUS;
			HOST_DATA = HOST_DATA_ADLOCUS;
	        REDIRECTS_CHECK_URL = REDIRECTS_CHECK_URL_ADLOCUS;
            VERSION_INT = 17; // modified this if you change the one service algorithm
        }
    }

	
	public static final String URL_AUTH = HOST + "dev/auth";
	
	public static final String URL_PULL_HTML_REQ = HOST + "dev_html5/req";
	public static final String URL_PULL_JSON_REQ = HOST + "dev/json_req";

	public static final String URL_PUSH_REQ = HOST + "devpush/newReq";
	public static final String URL_PUSH_REQ_TEST = HOST + "devpush/req_test";
	public static final String URL_PUSH_CLICK = HOST + "devpush/get_json_link";
	public static final String URL_PUSH_FEEDBACK = HOST + "devpush/feedback";
	public static final String URL_PUSH_IMP = HOST + "devpush/imp";
	public static final String URL_PUSH_IMP_NEW = HOST + "devpush/newimp";

	public static final String URL_BIGVIEW_FEEDBACK = HOST + "bigview/feedback";
//	public static final String URL_DATA_COLLECTION = HOST_DATA + "log/device";
public static final String URL_DATA_COLLECTION = HOST_DATA + "new_log/and_device";

	public static final String ADLOCUS = "AdLocus SDK";

	public static final int AD_TYPE_NOAD    = 0;
	public static final int AD_TYPE_ICON    = 1;
	public static final int AD_TYPE_BANNER  = 2;
	public static final int AD_TYPE_DELAY   = 3;
	public static final int AD_TYPE_HTML5   = 11;
	public static final int AD_TYPE_BIGVIEW = 21;

	public static final int ICON_TYPE_CUSTOM = 0;

	public static final int LINK_TYPE_URL = 1;
	public static final int LINK_TYPE_OTHER = 2;

	//debug config by alex
	public static final boolean DEBUG_IGNORE_OID = false;
	//end debug config

	private static String SID = null;


	public static boolean isNotificationEnable(Context context){
		return NotificationManagerCompat.from(context).areNotificationsEnabled();
	}

	public static Bitmap base64ToBitmap(String base64String)
	{
		byte[] bPhoto = null;
		if(base64String != null)
		{
			bPhoto = Base64.decode(base64String, Base64.DEFAULT);
		}
		Bitmap ret = bPhoto == null ? null : BitmapFactory.decodeByteArray(bPhoto, 0, bPhoto.length);
		System.gc();
		return ret;
	}

	public static double getDensity(Context context)
	{
		return context.getResources().getDisplayMetrics().density;
	}

	public static String getSize(Context context)
	{
		double d = getDensity(context);
		int screenSize = 480;
		if(d <= 1)
		{
			screenSize = 320;
		}
		else if(d >= 2)
		{
			screenSize = 640;
		}
		return String.valueOf(screenSize);
	}

	public static String getScreen(Context context, int adSize)
	{
		String screen = "320";
		double d = getDensity(context);
		if(d < 1)
		{
			d = 1;
		}
		d = 1;
		int w = (int) (getAdWidth(adSize) * d);
		switch (adSize)
		{
		case AdLocusLayout.AD_SIZE_BANNER:
			screen = String.valueOf(w);
			break;
		case AdLocusLayout.AD_SIZE_IAB_MRECT:
		case AdLocusLayout.AD_SIZE_IAB_LEADERBOARD:
			screen = "p" + w;
			break;
		default:
			break;
		}
		return screen;
	}

	static long sLastCheckTs = 0;
	public static boolean checkSelfPermission(final Context context){
        return false;
//		boolean ret = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
//		setPause(context, !ret);
//		if (!ret && System.currentTimeMillis() - sLastCheckTs > 1000) {
//			sLastCheckTs = System.currentTimeMillis();
//			new Handler(context.getMainLooper()).postDelayed(new Runnable() {
//				@Override
//				public void run() {
//					Intent intent = new Intent(context, AdActivity.class);
//					intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
//					intent.setAction(AdActivity.ACTION_REQUEST_PERMISSION);
//					context.startActivity(intent);
//				}
//			}, 100);
//		}
//		return  ret;
	}

	/**
	 * Converts device independent pixels to screen pixels.
	 * 
	 * @param dipPixels
	 *          is the amount of device independent pixels.
	 * @param density
	 *          is the device's screen density.
	 * 
	 * @return An integer representing the value in screen pixels.
	 */
	public static int convertToScreenPixels(int dipPixels, double density)
	{
		return (int) convertToScreenPixels((double) dipPixels, density);
	}

	/**
	 * Converts device independent pixels to screen pixels.
	 * 
	 * @param dipPixels
	 *          is the amount of device independent pixels.
	 * @param density
	 *          is the device's screen density.
	 * 
	 * @return A double representing the value in screen pixels.
	 */
	public static double convertToScreenPixels(double dipPixels, double density)
	{
		return (density > 0) ? (dipPixels * density) : dipPixels;
	}

	/**
	 * Get the ProMe key.
	 * 
	 * @param context
	 *          the application context.
	 *          
	 * @return The ProMe key.
	 */
	public static String getPushKey(Context context)
	{
		String key = MultiProcessPreferences.getDefaultSharedPreferences(context).getString(PREFERENCE_KEY, null);
		if(key != null)
		{
			return key;
		}
		final String packageName = context.getPackageName();
		final String activityName = context.getClass().getName();
		final PackageManager pm = context.getPackageManager();
		Bundle bundle;
		// Attempts to retrieve Activity-specific ProMe key first. If not
		// found, retrieve Application-wide ProMe key.
		try
		{
			ActivityInfo activityInfo = pm.getActivityInfo(new ComponentName(packageName, activityName), PackageManager.GET_META_DATA);
			bundle = activityInfo.metaData;
			if (bundle != null)
			{
				return bundle.getString(AdLocusLayout.ADLOCUS_KEY);
			}
		}
		catch (NameNotFoundException exception)
		{
			// Activity cannot be found. Shouldn't be here.
			//			return null;
		}

		try
		{
			ApplicationInfo appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
			bundle = appInfo.metaData;
			if (bundle != null)
			{
				return bundle.getString(AdLocusLayout.ADLOCUS_KEY);
			}
		}
		catch (NameNotFoundException exception)
		{
			// Application cannot be found. Shouldn't be here.
			return null;
		}
		return null;
	}
	
	public static String getSid()
	{
		return SID;
	}
	
	public static void setSid(String sid)
	{
		SID = sid;
	}

	public static void setPushKey(Context context, String key)
	{
		MultiProcessPreferences.getDefaultSharedPreferences(context).edit().putString(PREFERENCE_KEY, key).commit();
	}


	public static void setCollectionData(Context context, int collectionData) {
		MultiProcessPreferences.getDefaultSharedPreferences(context).edit().putInt(PREFERENCE_COLLECTION, collectionData).putLong(PREFERENCE_COLLECTION_TRACKTIME, System.currentTimeMillis()).commit();
	}

	public static boolean hasCollectionTrackConsent(Context context, int collectionData) {
		int lastData = MultiProcessPreferences.getDefaultSharedPreferences(context).getInt(PREFERENCE_COLLECTION, 0);
		if (collectionData != lastData) return true;
		long lastTrackTime = MultiProcessPreferences.getDefaultSharedPreferences(context).getLong(PREFERENCE_COLLECTION_TRACKTIME, 0);
		return (System.currentTimeMillis() - lastTrackTime > NEXT_TRACK_TIME);
	}

	public static void setPause(Context context, boolean isPause) {
		MultiProcessPreferences.getDefaultSharedPreferences(context).edit().putBoolean(PREFERENCE_PAUSE, isPause).commit();

		Intent i = new Intent(context, PushService.class);
		i.setAction(PushService.ACTION_PAUSE_CHECK);
		context.startService(i);
	}

	public static boolean isPause(Context context) {
		return MultiProcessPreferences.getDefaultSharedPreferences(context).getBoolean(PREFERENCE_PAUSE, true);
	}
	
	public static AdLocusTargeting getPushTargeting(Context context)
	{
		String json = MultiProcessPreferences.getDefaultSharedPreferences(context).getString(PREFERENCE_TARGETING, "{}");
		return toTargeting(json);
	}
	
	public static void setPushTargeting(Context context, AdLocusTargeting adLocusTargeting)
	{
		MultiProcessPreferences.getDefaultSharedPreferences(context).edit().putString(PREFERENCE_TARGETING, toString(adLocusTargeting)).apply();
	}
	
	public static void setNewPushBackgroundIntent(Context context, Intent backgroundIntent)
	{
		if(backgroundIntent == null)
		{
			MultiProcessPreferences.getDefaultSharedPreferences(context).edit().remove(PREFERENCE_BACKGROUND_INTENT).apply();
		}
		else
		{
			MultiProcessPreferences.getDefaultSharedPreferences(context).edit().putString(PREFERENCE_BACKGROUND_INTENT, backgroundIntent.toUri(Intent.URI_INTENT_SCHEME)).commit();
		}
	}
	
	public static Intent getNewPushBackgroundIntent(Context context)
	{
		String uri = MultiProcessPreferences.getDefaultSharedPreferences(context).getString(PREFERENCE_BACKGROUND_INTENT, null);
		Log.d("getNewPushBackgroundIntent:" + uri);
		if(uri != null)
		{
			try {
				Intent i = Intent.parseUri(uri, Intent.URI_INTENT_SCHEME);
				if(isIntentAvailable(context, i))
				{
					return i;
				}
			} catch (URISyntaxException ignored) { }
		}
		return null;
	}
	
	/**
	 * Indicates whether the specified action can be used as an intent. This
	 * method queries the package manager for installed packages that can
	 * respond to an intent with the specified action. If no suitable package is
	 * found, this method returns false.
	 *
	 * @param context The application's environment.
	 *
	 * @return True if an Intent with the specified action can be sent and
	 *         responded to, false otherwise.
	 */
	public static boolean isIntentAvailable(Context context, Intent intent) {
	    final PackageManager packageManager = context.getPackageManager();
	    List<ResolveInfo> list =
	            packageManager.queryIntentActivities(intent,
						PackageManager.MATCH_DEFAULT_ONLY);
	    return list.size() > 0;
	}

	public static String toString(AdLocusTargeting adLocusTargeting)
	{
		JSONObject o = new JSONObject();
		if(adLocusTargeting != null)
		{
			try
			{
				o.put("age", adLocusTargeting.getAge());
				o.put("test_mode", adLocusTargeting.getTestMode());
				o.put("gender", adLocusTargeting.getGender().name());
				o.put("tag", adLocusTargeting.getTag());
			}
			catch (JSONException ignored) { }
		}
		return o.toString();
	}
	
	public static AdLocusTargeting toTargeting(String jsonString)
	{
		if(jsonString != null)
		{
			try
			{
				AdLocusTargeting alt = new AdLocusTargeting();
				JSONObject o = new JSONObject(jsonString);
				alt.setAge(o.optInt("age", -1));
				alt.setGender(Gender.valueOf(o.optString("gender", "UNKNOWN")));
				alt.setTestMode(o.optBoolean("test_mode", false));
				alt.setTag(o.optString("tag", null));
				return alt;
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Gets the hashed device id.
	 *
	 * @param context
	 *          the application context.
	 *
	 * @return The encoded device id.
	 */
	public static String getEncodedDeviceId(Context context)
	{
		String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        androidId = androidId + DEBUG_DEVICE_ID;
		
		String hashedId;
		if ((androidId == null) || isEmulator())
		{
			hashedId = hashId("emulator");
		}
		else
		{
			hashedId = hashId(androidId);
		}

		if (hashedId == null)
		{
			return null;
		}

		return hashedId;
	}

	public static String getAdId(Context context){
		try {
			AdvertisingIdClient.Info idInfo = AdvertisingIdClient.getAdvertisingIdInfo(context.getApplicationContext());
			if (idInfo != null) return "and:"+idInfo.getId();
		}
		catch (Exception ignored) {

		}

		return null;
	}


	public static String getMac(Context context) {

		WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		android.net.wifi.WifiInfo info = manager.getConnectionInfo();
		String mac = info.getMacAddress();
		if (mac != null && !mac.equals(MAC_DEFAULT)) return mac;
		return getMac6();
	}

	private static String getMac6() {
		try {
			List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface nif : all) {
				if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

				byte[] macBytes = nif.getHardwareAddress();
				if (macBytes == null) {
					return "";
				}

				StringBuilder res1 = new StringBuilder();
				for (byte b : macBytes) {
					res1.append(Integer.toHexString(b & 0xFF)).append(":");
				}

				if (res1.length() > 0) {
					res1.deleteCharAt(res1.length() - 1);
				}
				return res1.toString();
			}
		} catch (Exception ignored) { }
		return MAC_DEFAULT;
	}

	/**
	 * Method for returning an hashId hash of a string.
	 *
	 * @param val
	 *          the string to hash.
	 *
	 * @return A hex string representing the hashId hash of the input.
	 */
	private static String hashId(String val)
	{
		return "and://" + sha1(val);
	}


    /**
     * Method for returning an sha1 hash of a string.
     *
     * @param val
     *          the string to hash.
     *
     * @return A hex string representing the sha1 hash of the input.
     */
    public static String sha1(String val)
    {
        String result = null;

        if ((val != null) && (val.length() > 0))
        {
            try
            {
                MessageDigest md = MessageDigest.getInstance("SHA-1");
                md.update(val.getBytes());
                return String.format("%040x", new BigInteger(1, md.digest()));
            }
            catch (NoSuchAlgorithmException nsae)
            {
                result = val.substring(0, 40);
            }
        }
        return result;
    }


	/**
	 * Checks whether or not the running device is an emulator.
	 *
	 * @return Boolean indicating if the app is currently running in an emulator.
	 */
	public static boolean isEmulator()
	{
		return (Build.BOARD.equals("unknown") && Build.DEVICE.equals("generic") && Build.BRAND.equals("generic"));
	}

	private static String getLeftIconName(int type)
	{
		switch (type)
		{
		case 1:
			return "pm_ic_01";
		case 2:
			return "pm_ic_02";
		case 3:
			return "pm_ic_03";
		case 4:
			return "pm_ic_04";
		case 5:
			return "pm_ic_05";
		case 6:
			return "pm_ic_06";
		case 7:
			return "pm_ic_07";
		case 8:
			return "pm_ic_08";
		default:
			return "pm_ic_08";
		}
	}

	private static String getClickIconName(int type)
	{
		switch (type)
		{
		case 1:
			return "pm_status_01";
		case 2:
			return "pm_status_02";
		case 3:
			return "pm_status_03";
		case 4:
			return "pm_status_04";
		case 5:
			return "pm_status_05";
		default:
			return null;
		}
	}

	public static Bitmap getLeftIconBitmap(Context context, int type)
	{
		return getAssetsBitmap(context, getLeftIconName(type));
	}

	public static Bitmap getClickIconBitmap(Context context, int type)
	{
		return getAssetsBitmap(context, getClickIconName(type));
	}

	public static int getAdWidth(int adSize)
	{
		switch (adSize) {
		case AdLocusLayout.AD_SIZE_BANNER:
			return 320;
		case AdLocusLayout.AD_SIZE_IAB_MRECT:
			return 300;
		case AdLocusLayout.AD_SIZE_IAB_LEADERBOARD:
			return 728;
		default:
			return -1;
		}
	}

	public static int getAdHeight(int adSize)
	{
		switch (adSize) {
		case AdLocusLayout.AD_SIZE_BANNER:
			return 50;
		case AdLocusLayout.AD_SIZE_IAB_MRECT:
			return 250;
		case AdLocusLayout.AD_SIZE_IAB_LEADERBOARD:
			return 90;
		default:
			return -1;
		}
	}

	public static Bitmap getAssetsBitmap(Context context, String name)
	{
		int id = getResId(getDrawableClass(), name);
		return BitmapFactory.decodeResource(context.getResources(), id);
	}


	public static Class<?> getDrawableClass()
	{

		Class<?> classDrawable = null;
		try {
			classDrawable = Class.forName( AdLocusUtil.CLASSS_NAME_ADLOCUS_LIB + "drawable" );
		} catch (ClassNotFoundException ignored) { }
		return classDrawable;
	}

	public static Drawable getAssetsDrawable(Context context, String name)
	{
		int id = getResId(getDrawableClass(), name);
        return ContextCompat.getDrawable(context, id);
    }

	public static int getResId(Class<?> resourceType, String variableName)
	{
		try {
			final Field field = resourceType.getField(variableName);
			return field.getInt(null);
		} catch (final Exception e) {
			Log.e("Lookup id for resource " + variableName + " failed", e);
			return -1;
		}
	}

	public static int getResId(String type, String variableName)
	{
		try {
			Class<?> classType = Class.forName( AdLocusUtil.CLASSS_NAME_ADLOCUS_LIB + type );
			final Field field = classType.getField(variableName);
			return field.getInt(null);
		} catch (final Exception e) {
			Log.e("Lookup id for resource " + variableName + " failed", e);
			return -1;
		}
	}
	

	/**
	 * wiki http://wiki.rd.hyxencloud.com/index.php?title=
	 * HifreeAdLocus_dev_html_req
	 * 
	 * @param context context
	 * @param screen size
	 * @param activityName background intent
	 * @return get parameters
	 */
	public static String getAdLocusParameters(Context context, String key, String screen, String activityName, AdLocusTargeting adLocusTargeting)
	{
		HxRequest r = new HxRequest(context);
		setRequestParameters(context, r, key, screen, activityName, adLocusTargeting);
		return "?" + r.getPostString();
	}

	public static int setRequestParameters(Context context, HxRequest r, String key, String screen, String activityName, AdLocusTargeting adLocusTargeting)
	{
		int ret;
		setToRequest(r, adLocusTargeting);
		r.setPostParameter("device_id", AdLocusUtil.getEncodedDeviceId(context));
		r.setPostParameter("d_ad_id", AdLocusUtil.getAdId(context));
		r.setPostParameter("device_mac", AdLocusUtil.getMac(context));
		r.setPostParameter("key", key);
		r.setPostParameter("v_str", AdLocusUtil.VERSION_STRING);
		if (screen != null) r.setPostParameter("screen", screen);
		String sid = AdLocusUtil.getSid();
		if (sid != null)
		{
			r.setPostParameter("session_id", sid);
		}

		if (isAccelAvailable(context, activityName))
		{
			r.setPostParameter("accel", "1");
		}

		CellInfo ci = HxCellEngine.getInstance(context).getValidCellInfo();

		if (ci != null)
		{
			/*
			 * 1 - Android Phone 3 - Android Pad 4 - iOS 加密 5 - IP
			 */
			r.setPostParameter("dev_type", "11");
			r.setPostParameter("mcc", String.valueOf(ci.getMcc()));
			r.setPostParameter("mnc", String.valueOf(ci.getMnc()));
			r.setPostParameter("lac", String.valueOf(ci.getLac()));
			r.setPostParameter("ci", String.valueOf(ci.getCellID()));
			r.setPostParameter("rssi", String.valueOf(ci.getRssi()));
			String mac = HxWifiEngine.getInstance(context).getAMac();
			if (mac != null) r.setPostParameter("mac", mac);
			ret = ("11," + ci.getMcc() + "," + ci.getMnc()).hashCode();

		}
		else
		{
			String mac = HxWifiEngine.getInstance(context).getAMac();
			if (mac != null)
			{
				r.setPostParameter("dev_type", "3");
				r.setPostParameter("mac", mac);
				ret = 3;
			}
			else
			{
				r.setPostParameter("dev_type", "5");
				r.setPostParameter("mcc", "-1");
				r.setPostParameter("mnc", "-1");
				r.setPostParameter("ip", "1");
				ret = 5;
			}
		}
		return ret;
	}
	
	public static int getJsonLinkDevType(Context context)
	{
		CellInfo ci = HxCellEngine.getInstance(context).getValidCellInfo();
		if (ci != null)
		{
			/*
			 * 1 - Android Phone, 3 - Android Pad, 4 - iOS 加密,  5 - IP
			 */
			return ("11," + ci.getMcc() + "," + ci.getMnc()).hashCode();
		}
		else
		{
			String mac = HxWifiEngine.getInstance(context).getAMac();
			if (mac != null)
			{
				return 3;
			}
			else
			{
				return 5;
			}
		}
	}

	/**
	 *
	 * @param permissuion android.Manifest.permission.*
	 */
	public static boolean hasPermission(Context context, String permissuion)
	{
		return context.checkCallingOrSelfPermission(permissuion) == PackageManager.PERMISSION_GRANTED;
	}

	public static boolean isAccelAvailable(Context context, String activityName)
	{
		if (context == null || activityName == null) return false;
		
		PackageManager packageManager = context.getPackageManager();
		try
		{
			PackageInfo info = packageManager.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
			
			ActivityInfo[] ais = info.activities;
			for (ActivityInfo activityInfo : ais)
			{
				if (activityInfo.name.endsWith(activityName))
				{	
					if ((activityInfo.flags & ActivityInfo.FLAG_HARDWARE_ACCELERATED) != 0)
					{
						return true;
					}
				}
			}
		}
		catch (NameNotFoundException ignored) { }
		
		return false;
	}

	public static void setToRequest(HxRequest request, AdLocusTargeting adLocusTargeting)
	{
		if(adLocusTargeting == null)
		{
			return;
		}
		Gender g = adLocusTargeting.getGender();
		if (g != null)
		{
			if (g == Gender.MALE)
			{
				request.setPostParameter("gender", "1");
			}
			else if (g == Gender.FEMALE)
			{
				request.setPostParameter("gender", "2");
			}
		}
		int age = adLocusTargeting.getAge();
		if (age != -1)
		{
			request.setPostParameter("age", String.valueOf(age));
		}
		if (adLocusTargeting.getTestMode() || TestUtil.isTestMode(request.getContext()))
		{
			request.setPostParameter("testmode", "1");
		}
		if(adLocusTargeting.getTag() != null)
		{
			request.setPostParameter("tag", adLocusTargeting.getTag());
		}
	}
	

	public static void setLocationToRequest(Context context, HxRequest request)
	{
		CellInfo ci = HxCellEngine.getInstance(context).getValidCellInfo();
		if(ci != null)
		{
			request.setPostParameter("mcc", String.valueOf(ci.getMcc()));
			request.setPostParameter("mnc", String.valueOf(ci.getMnc()));
			request.setPostParameter("lac", String.valueOf(ci.getLac()));
			request.setPostParameter("ci", String.valueOf(ci.getCellID()));
			request.setPostParameter("rssi", String.valueOf(ci.getRssi()));
		}
		else
		{
			WifiInfo wi = HxWifiEngine.getInstance(context).getAWifiInfo();
			if(wi !=null)
			{
				request.setPostParameter("mcc", "-1");
				request.setPostParameter("mnc", "-1");
				request.setPostParameter("mac", wi.getMac());
				request.setPostParameter("rssi", String.valueOf(wi.getRssi()));
			}
		}
	}
	

	/**
	 * Success(0)<br/>
	 * Error(-1) Key錯誤<br/>
	 * Error(-2) Version停用<br/>
	 * Error(-255)系統錯誤<br/>
	 */
	public static void auth(final Context context, final String key, final AuthListener listener)
	{
		/*fixme: move code from AdLocusManager to here
com/adlocus/AdLocusLayout.java:607:				int err = adLocusLayout.adLocusManager.fetchAuth();
com/adlocus/AdLocusManager.java:126:	public int fetchAuth()
com/adlocus/AdLocusManager.java:135:		return fetchAuth(context, key);
com/adlocus/AdLocusManager.java:148:	public synchronized static int fetchAuth(Context context, String key)
com/adlocus/util/AdLocusUtil.java:934:				while((err = AdLocusManager.fetchAuth(context, key)) == -999)
com/adlocus/PushAd.java:60:        AdLocusUtil.auth(context, appKey, new AdLocusUtil.AuthListener()
com/adlocus/AdLocusManager.java:188:	private static int parseAuth(String authJson)
com/adlocus/AdLocusManager.java:191:		if (authJson != null)
com/adlocus/AdLocusManager.java:195:				JSONObject o = new JSONObject(authJson); 
		 */
		
		Log.v("checking key...");
		Log.v("version:" + AdLocusUtil.VERSION_STRING + ", type: " + AdLocusUtil.HOST );
		
		new Thread()
		{
			int delay = 5000;				
			
			@Override
			public void run()
			{
				int err = -999;
				while((err = AdLocusManager.fetchAuth(context, key)) == -999)
				{
					SystemClock.sleep(delay *= 2);
				}
				if(err == 0)
				{
					Log.v("key is vaild.");
				}
				else
				{
					Log.v("key is invaild.");
				}
				listener.onChecked(err);
			}
		}.start();
	}
	
	public interface AuthListener
	{
		void onChecked(int err);
	}
	
    public static Bitmap getApplicationIconBitmap(Context context, int widthInPixels, int heightInPixels)
    {
            Drawable iconD = null;
            try {
                    iconD = context.getPackageManager().getApplicationIcon(context.getPackageName());
            } catch (NameNotFoundException e) {
                    e.printStackTrace();
            }

            return convertToBitmap(iconD, widthInPixels, heightInPixels);
    }

    private static Bitmap convertToBitmap(Drawable drawable, int widthInPixels, int heightInPixels)
    {
            Bitmap image = Bitmap.createBitmap(widthInPixels, heightInPixels, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(image);
            drawable.setBounds(0, 0, widthInPixels, heightInPixels);
            drawable.draw(canvas);

            return image;
    }
    
    public static boolean supportBigview() {
		return Build.VERSION.SDK_INT >=  Build.VERSION_CODES.JELLY_BEAN;
	}
    

	public static boolean targetSdkStyleLollipop(Context context)
	{			
		int version = 0;
		
		PackageManager packageManager = context.getPackageManager();
		
		try {
			PackageInfo info;
			info = packageManager.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
			version = info.applicationInfo.targetSdkVersion;
			
		} catch (NameNotFoundException ignored) { }
        
		return (version >= Build.VERSION_CODES.LOLLIPOP);
	}

	public static void testString(String json,Context ctx)
	{

		AdLocusAd a = AdLocusManager.parseProMeAdJsonString(json);

		int type = a.type;

		if(type == AdLocusUtil.AD_TYPE_BANNER || type == AdLocusUtil.AD_TYPE_ICON || type == AdLocusUtil.AD_TYPE_BIGVIEW)
		{
			AdLocusNotification.showNotification(ctx, a);
		}

	}
}
