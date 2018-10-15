package com.hyxen.adlocusaar.geofence;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class Region
{
//	public static final int STATUS_INIT = GeofenceDbAdapter.STATUS_NON;
//	public static final int STATUS_TRIGGER_IN = GeofenceDbAdapter.STATUS_TRIGGER_IN;
	
	/**
	 * double
	 */
	public static final String EXTRA_LAT = "lat";
	/**
	 * double
	 */
	public static final String EXTRA_LON = "lon";
	/**
	 * int
	 */
	public static final String EXTRA_RADIUS = "radius";
	/**
	 * String
	 */
	public static final String EXTRA_IDENTIFY = "id";
	/**
	 * int : FENCE_IN, FENCE_OUT
	 */
	public static final String EXTRA_FENCE = "fence";
	/**
	 * long ts
	 */
	public static final String EXTRA_STARTING_TS = "starting_ts";
	/**
	 * long ts
	 */
	public static final String EXTRA_EXPIRING_TS = "expiring_ts";
	/**
	 * int userFance
	 */
	public static final String EXTRA_USER_FENCE = "user_fence";

	/**
	 * int status
	 */
	public static final String EXTRA_STATUS = "status";
	
	public static final int FENCE_IN		= 1;
	public static final int FENCE_OUT		= 2;
	public static final int FENCE_IN_OUT = 3;
	
	
	private double mLat;
	private double mLon;
	private int mRadius;
	private String mIdentify;
	
	private long mTsStarting = -1;
	private long mTsExpiring = -1;

	private int mUserFance;
	
	private int mStatus = -1;
	
	Region(Intent intent)
	{
		mLat = intent.getDoubleExtra(EXTRA_LAT, 0);
		mLon = intent.getDoubleExtra(EXTRA_LON, 0);
		mRadius = intent.getIntExtra(EXTRA_RADIUS, 0);
		mIdentify = intent.getStringExtra(EXTRA_IDENTIFY);
		mTsStarting = intent.getLongExtra(EXTRA_STARTING_TS, -1);
		mTsExpiring = intent.getLongExtra(EXTRA_EXPIRING_TS, -1);
		mUserFance = intent.getIntExtra(EXTRA_USER_FENCE, 0);
		mStatus = intent.getIntExtra(EXTRA_STATUS, -1);
	}
	
	public Region(String josnString)
	{
		try
		{
			JSONObject o = new JSONObject(josnString);
			mLat = o.optDouble(EXTRA_LAT, 0);
			mLon = o.optDouble(EXTRA_LON, 0);
			mRadius = o.optInt(EXTRA_RADIUS, 0);
			mIdentify = o.optString(EXTRA_IDENTIFY);
			mTsStarting = o.optLong(EXTRA_STARTING_TS, -1);
			mTsExpiring = o.optLong(EXTRA_EXPIRING_TS, -1);
			mUserFance = o.optInt(EXTRA_USER_FENCE, 0);
			mStatus = o.optInt(EXTRA_STATUS);
		}
		catch (JSONException ignored) { }
	}

	/**
	 * 
	 * @param lat the latitude of center 
	 * @param lon the longitude of center
	 * @param radius unit is meter, from 500m to 1500m.
	 * @param identify identify can not be null.
	 * @param userFance FENCE_IN, FENCE_OUT or FENCE_IN_OUT
	 */
	public Region(double lat, double lon, int radius, String identify, int userFance)
	{
		init(lat, lon, radius, identify, -1, -1, userFance);
	}

	/**
	 *
	 * @param lat latitude
	 * @param lon longitude
	 * @param radius unit is meter, need in 500m ~ 1500m.
	 * @param identify can not be null.
	 * @param tsStarting ts of starting time, or -1 to indicate now.
	 * @param tsExpiring ts of expiring date, or -1 to indicate no expiration
	 * @param userFance FENCE_IN, FENCE_OUT or FENCE_IN_OUT
	 */
	public Region(double lat, double lon, int radius, String identify, long tsStarting, long tsExpiring, int userFance)
	{
		init(lat, lon, radius, identify, tsStarting, tsExpiring, userFance);
	}
	
	void init(double lat, double lon, int radius, String identify, long tsStarting, long tsExpiring, int userFance)
	{
		if(radius < 500 || radius > 1500) throw new IllegalArgumentException("Radius need in 500m ~ 1500m.");
		if(identify == null) throw new NullPointerException("Identify can not be null.");
		
		mLat = lat;
		mLon = lon;
		mRadius = radius;
		mUserFance = userFance;
		mIdentify = identify;
		mTsStarting = tsStarting;
		mTsExpiring = tsExpiring;
	}
	
	void setStatus(int status)
	{
		mStatus = status;
	}
	
	public int getStatus()
	{
		return mStatus;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getUserFence()
	{
		return mUserFance;
	}

	/**
	 * @param userFance 
	 */
	public void setUserFance(int userFance)
	{
		this.mUserFance = userFance;
	}
	
	public void setStartingTs(long tsStarting)
	{
		mTsStarting = tsStarting;
	}
	
	public long getStartingTs()
	{
		return mTsStarting;
	}
	
	public void setExpiringTs(long tsExpiring)
	{
		mTsExpiring = tsExpiring;
	}
	
	public long getExpiringTs()
	{
		return mTsExpiring;
	}

	/**
	 * @return the lat
	 */
	public double getLat()
	{
		return mLat;
	}

	/**
	 * @param lat the Lat to set
	 */
	public void setLat(double lat)
	{
		this.mLat = lat;
	}

	/**
	 * @return the Lon
	 */
	public double getLon()
	{
		return mLon;
	}

	/**
	 * @param lon the Lon to set
	 */
	public void setLon(double lon)
	{
		this.mLon = lon;
	}

	/**
	 * @return the Radius
	 */
	public int getRadius()
	{
		return mRadius;
	}

	/**
	 * @param radius the Radius to set <br>
	 * Radius need in 500m ~ 1500m.
	 */
	public void setRadius(int radius)
	{
		if(radius < 500 || radius > 1500) throw new IllegalArgumentException("Radius need in 500m ~ 1500m.");
		this.mRadius = radius;
	}

	/**
	 * @return the Identify
	 */
	public String getIdentify()
	{
		return mIdentify;
	}

	/**
	 * @param identify the Identify to set
	 */
	public void setIdentify(String identify)
	{
		if(identify == null) throw new NullPointerException("Identify can not be null.");
		this.mIdentify = identify;
	}

	
	void putExtra(Intent intent)
	{
		intent.putExtra(EXTRA_LAT, mLat);
		intent.putExtra(EXTRA_LON, mLon);
		intent.putExtra(EXTRA_RADIUS, mRadius);
		intent.putExtra(EXTRA_IDENTIFY, mIdentify);
		intent.putExtra(EXTRA_STARTING_TS, mTsStarting);
		intent.putExtra(EXTRA_EXPIRING_TS, mTsExpiring);
		intent.putExtra(EXTRA_USER_FENCE, mUserFance);
		intent.putExtra(EXTRA_STATUS, mStatus);
	}
	
	public String toJsonString()
	{
		try {
			JSONObject o = new JSONObject();
			o.put(EXTRA_LAT, mLat);
			o.put(EXTRA_LON, mLon);
			o.put(EXTRA_RADIUS, mRadius);
			o.put(EXTRA_IDENTIFY, mIdentify);
			o.put(EXTRA_STARTING_TS, mTsStarting);
			o.put(EXTRA_EXPIRING_TS, mTsExpiring);
			o.put(EXTRA_USER_FENCE, mUserFance);
			o.put(EXTRA_STATUS, mStatus);
			return o.toString(2);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public String toShowString(Context context, int fence, long ints)
	{
		String time = mIdentify;
		try
		{
			long ts = Long.valueOf(mIdentify);
			time = tsToString(context, ts);
		}
		catch (Exception ignored) { }
		return String.format(Locale.US, "Add Time : %s\nTrggiger Time : %s\nlat : %f\nlon : %f", time, fence == 0 ? "" : tsToString(context, ints), mLat, mLon);
	}
	
	private String tsToString(Context context, long ts)
	{
		return DateUtils.formatDateTime(context, ts, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
	}
}
