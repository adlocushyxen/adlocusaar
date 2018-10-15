package com.hyxen.adlocusaar.push;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hyxen.adlocusaar.util.AdLocusUtil;
import com.hyxen.adlocusaar.util.Log;

import android.content.Context;
import android.content.SharedPreferences;

public class Type3Manager {

	public static final String PREFERENCE_NAME = AdLocusUtil.PREFIX + "AdLocusPushService";
	public static final String PREFERENCE_TYPE3_EVENT = "type3events";
//	private static long mLastCheckType3 = -1;
	private static final Object TYPE3_LOCK = new Object();
	
	static ArrayList<String> getType3Events(final Context context) {
		synchronized (TYPE3_LOCK) {
			ArrayList<String> ret = new ArrayList<>();
//			long thisHour = System.currentTimeMillis() / 3600000;
//			if(mLastCheckType3 == thisHour) return ret;
//			mLastCheckType3 = thisHour;
			SharedPreferences sp = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
			String str = sp.getString(PREFERENCE_TYPE3_EVENT, "[]");
			Log.d("getType3Events:" + str);
			try {
				JSONArray a = new JSONArray(str);
				final int SIZE = a.length();
				for (int i = 0; i < SIZE; i++) {
					String event = a.optString(i, null);
					if(event != null) ret.add(event);
				}
			} catch (JSONException ignored) { }
			return ret;
		}
	}
	
	static void removeType3WithAdId(Context context, String adId)
	{
		synchronized (TYPE3_LOCK)
		{
			SharedPreferences sp = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
			String str = sp.getString(PREFERENCE_TYPE3_EVENT, "[]");
			try {
				JSONArray newArray = new JSONArray();
				JSONArray a = new JSONArray(str);
				final int SIZE = a.length();
				for (int i = 0; i < SIZE; i++) {
					JSONObject o = new JSONObject(a.getString(i));
					String s = o.getString("ad_id");
					if(!s.equals(adId)) newArray.put(o.toString());
				}
				sp.edit().putString(PREFERENCE_TYPE3_EVENT, newArray.toString()).apply();
			} catch (JSONException ignored) { }
		}
	}
	
	static void addType3Event(Context context, String event) {
		synchronized (TYPE3_LOCK) {
			SharedPreferences sp = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
			String str = sp.getString(PREFERENCE_TYPE3_EVENT, "[]");
			try {
				JSONObject o = new JSONObject(event);
				String adId = o.getString("ad_id");
				JSONArray a = new JSONArray(str);
				JSONArray aa = new JSONArray();
				final int SIZE = a.length();
				
				for (int i = 0; i < SIZE; i++) {
					JSONObject oo = new JSONObject(a.getString(i));
					String ss = oo.getString("ad_id");

					if(adId.equals(ss)) continue;

					aa.put(oo);
				}
				aa.put(event);
				String arr = aa.toString();
				sp.edit().putString(PREFERENCE_TYPE3_EVENT, arr).apply();
				Log.d("addType3Event:" + arr);
			}
			catch (JSONException ignored) { }
		}
	}
	static void clear(Context context) {
		synchronized (TYPE3_LOCK) {
//			mLastCheckType3 = -1;
			context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).edit().remove(PREFERENCE_TYPE3_EVENT).apply();
		}
	}
}
