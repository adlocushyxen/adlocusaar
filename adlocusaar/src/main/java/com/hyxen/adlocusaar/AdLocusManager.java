/*
 Copyright 2009-2010 AdMob, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.hyxen.adlocusaar;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import com.hyxen.adlocusaar.net.HxRequest;
import com.hyxen.adlocusaar.obj.AdLocusAd;
import com.hyxen.adlocusaar.util.AdLocusUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Locale;

public class AdLocusManager
{
	private static final long AUTH_TIMEOUT = 18000000;
	public final String key;

	private final WeakReference<Context> contextReference;

	public final String localeString;
	public final String deviceIDHash;

	public Location location;

	private final static String PREFS_STRING_TIMESTAMP = "timestamp";
	private final static String PREFS_STRING_CONFIG = "config";

	private final static String PREFS_STRING_AUTH_TIMESTAMP = "auth_timestamp";
	private final static String PREFS_STRING_AUTH = "auth";

	private final String mScreen;

	private final String mActivityName;
	private final AdLocusTargeting mAdLocusTargeting;

	public AdLocusManager(WeakReference<Context> contextReference, String key, String screen, String activityName, AdLocusTargeting adLocusTargeting)
	{
		mScreen = screen;
		// Log.i(ProMeUtil.PROME, "Creating adWhirlManager...");
		this.contextReference = contextReference;
		this.key = key;

		localeString = Locale.getDefault().toString();
		// Log.d(ProMeUtil.PROME, "Locale is: " + localeString);

//		deviceIDHash = AdLocusUtil.getEncodedDeviceId(contextReference.get());
		deviceIDHash = UserBaseData.getHashDeviceId(contextReference.get());
		// Log.d(ProMeUtil.PROME, "Hashed device ID is: " + deviceIDHash);

		// Log.i(ProMeUtil.PROME, "Finished creating adWhirlManager");
		mActivityName = activityName;
		mAdLocusTargeting = adLocusTargeting;
	}

	public String getWebLink()
	{
		// if(true)
		// {
		// return "http://www.google.com";
		// }
		Context context = contextReference.get();
		if (context == null)
		{
			return null;
		}
		return AdLocusUtil.URL_PULL_HTML_REQ + AdLocusUtil.getAdLocusParameters(context, key, mScreen, mActivityName, mAdLocusTargeting);
	}

//	public String getJsonReq()
//	{
//		Context context = contextReference.get();
//		if (context == null)
//		{
//			return null;
//		}
//		return AdLocusUtil.URL_PULL_JSON_REQ + AdLocusUtil.getAdLocusParameters(context, key, mScreen, mActivityName, mAdLocusTargeting);
//	}

	public void fetchConfig()
	{
		Context context = contextReference.get();

		// If the context is null here something went wrong with initialization.
		if (context == null)
		{
			return;
		}

		SharedPreferences proMePrefs = context.getSharedPreferences(key, Context.MODE_PRIVATE);
		String jsonString = "{\"background_color_rgb\": {\"red\": 0,\"green\": 0,\"blue\": 0,\"alpha\": 1},\"text_color_rgb\": {\"red\": 255,\"green\": 255,\"blue\": 255,\"alpha\": 1}}";
		SharedPreferences.Editor editor = proMePrefs.edit();
		editor.putString(PREFS_STRING_CONFIG, jsonString);
		editor.putLong(PREFS_STRING_TIMESTAMP, System.currentTimeMillis());
		editor.apply();
	}

	/**
	 * Success(0)<br/>
	 * Error(-1) Key錯誤<br/>
	 * Error(-2) Version停用<br/>
	 * Error(-255)系統錯誤<br/>
	 * 
	 * @return
	 */
	public int fetchAuth()
	{
		Context context = contextReference.get();

		// If the context is null here something went wrong with initialization.
		if (context == null)
		{
			return -1000;
		}
		return fetchAuth(context, key);
	}

	/**
	 * Success(0)<br/>
	 * Error(-1) Key錯誤<br/>
	 * Error(-2) Version停用<br/>
	 * Error(-255)系統錯誤<br/>
	 * 
	 * @param context
	 * @param key
	 * @return
	 */
	public synchronized static int fetchAuth(Context context, String key)
	{
		SharedPreferences proMePrefs = context.getSharedPreferences(key, Context.MODE_PRIVATE);

		String jsonString = proMePrefs.getString(PREFS_STRING_AUTH, null);
		long timestamp = proMePrefs.getLong(PREFS_STRING_AUTH_TIMESTAMP, -1);

		if (jsonString == null || System.currentTimeMillis() >= timestamp + AUTH_TIMEOUT)
		{
			HxRequest r = new HxRequest(context, AdLocusUtil.URL_AUTH);
//			r.setPostParameter("device_id", AdLocusUtil.getEncodedDeviceId(context));
			r.setPostParameter("device_id", UserBaseData.getHashDeviceId(context));
			r.setPostParameter("key", key);
			r.setPostParameter("vid", AdLocusUtil.VERSION);
			r.setPostParameter("v_str", AdLocusUtil.VERSION_STRING);
			r.run();
			jsonString = r.getResult();

			int err = parseAuth(jsonString);
			
			if (err == 0)
			{
				SharedPreferences.Editor editor = proMePrefs.edit();
				editor.putString(PREFS_STRING_AUTH, jsonString);
				editor.putLong(PREFS_STRING_AUTH_TIMESTAMP, System.currentTimeMillis());
				editor.apply();
			}
			return err;
		}
		
		return parseAuth(jsonString);
	}

	private static int parseAuth(String authJson)
	{
		int err = -999;
		if (authJson != null)
		{
			try
			{
				JSONObject o = new JSONObject(authJson);
				String msg = o.optString("msg", null);
				if (msg != null)
				{
					Log.e(AdLocusUtil.ADLOCUS, msg);
				}
				err = o.optInt("err", -999);
			}
			catch (JSONException ignored)
			{
			}
		}
		switch (err)
		{
			case 0:
				Log.v(AdLocusUtil.ADLOCUS, "appkey 正確");
				break;
			case -1:
				Log.e(AdLocusUtil.ADLOCUS, "appkey 錯誤");
				break;
			case -2:
				Log.e(AdLocusUtil.ADLOCUS, "SDK版本已停用");
				break;
			case -255:
				Log.e(AdLocusUtil.ADLOCUS, "系統錯誤");
				break;
			default:
				Log.e(AdLocusUtil.ADLOCUS, "檢查失敗");
				break;
		}
		return err;
	}

	public String getAuth()
	{
		Context context = contextReference.get();

		// If the context is null here something went wrong with initialization.
		if (context == null)
		{
			return null;
		}

		SharedPreferences proMePrefs = context.getSharedPreferences(key, Context.MODE_PRIVATE);

		return proMePrefs.getString(PREFS_STRING_AUTH, null);
	}

	public static AdLocusAd parseProMeAdJsonString(String jsonString)
	{
		// Log.d(ProMeUtil.PROME, "Received custom jsonString: " + jsonString);
		if (jsonString == null)
		{
			return null;
		}
		AdLocusAd ad = new AdLocusAd();
		try
		{
			JSONObject json = new JSONObject(jsonString);

			ad.type = json.optInt("ad_type", -1);
			ad.id = json.optString("ad_id");

			ad.image = AdLocusUtil.base64ToBitmap(json.optString("ad_img"));
			if (ad.type == AdLocusUtil.AD_TYPE_ICON)
			{
				ad.leftImageType = json.optInt("ad_left_icon", -1);
				if (ad.leftImageType == AdLocusUtil.ICON_TYPE_CUSTOM)
				{
					ad.image = AdLocusUtil.base64ToBitmap(json.optString("ad_icon"));
				}
			}

			ad.link = json.optString("ad_link");
			ad.linkType = json.optInt("ad_link_type");
			ad.description = json.optString("ad_body");
			ad.clickType = json.optInt("ad_right_icon");
			ad.sid = json.optString("sid");
			AdLocusUtil.setSid(ad.sid);
			ad.second = json.optInt("sec");
			ad.distance = json.optInt("dist", -1);
			ad.isHouseAd = json.optInt("house", 0) == 1;
			ad.scad_txt = json.optString("scad_txt", null);
			ad.scad_url = json.optString("scad_url", null);
			ad.bv_banner = json.optString("bv_banner", null);
			ad.bv_text = json.optString("bv_text", null);
			ad.bv_share_text = json.optString("bv_share_text", null);
			ad.track_imp = json.optString("track_imp", null);

		}
		catch (JSONException e)
		{
			// Log.e(ProMeUtil.PROME,
			// "Caught JSONException in parseCustomJsonString()", e);
			return null;
		}

		return ad;
	}
}
