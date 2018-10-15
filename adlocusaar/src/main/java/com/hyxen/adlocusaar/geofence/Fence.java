package com.hyxen.adlocusaar.geofence;

import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;

class Fence extends Region
{
	private int mFence;

	/**
	 * @param lat lat
	 * @param lon lon
	 * @param radius need in 200m ~ 1500m.
	 * @param fence true is in the range, false is out the range.
	 * @param identify can not be null.
	 */

	public Fence(double lat, double lon, int radius, int fence, String identify, long tsStarting, long tsExpiring, int status, int userFence)
	{
		super(lat, lon, radius, identify, tsStarting, tsExpiring, userFence);
		mFence = fence;
		setStatus(status);
	}

	public Fence(Intent intent) {
		super(intent);
		mFence = intent.getIntExtra(EXTRA_FENCE, mFence);
	}
	
	public Fence(String jsonString)
	{
		super(jsonString);
		try
		{
			JSONObject o = new JSONObject(jsonString);
			mFence = o.optInt(EXTRA_FENCE);
		}
		catch (JSONException ignored) { }
		
	}

//	public boolean isValid()
//	{
//		long ts = System.currentTimeMillis();
//		return (ts > getStartingTs() && ts < getExpiringTs());
//	}
	
	boolean isExpired()
	{
		long ts = getExpiringTs();
		return (ts != -1) && (System.currentTimeMillis() > ts);
	}
	
	boolean isStart(){
		long ts = getStartingTs();
		return (ts == -1) || (System.currentTimeMillis() > ts);
	}
	
	public int getFence()
	{
		return mFence;
	}

	public void setFence(int fence)
	{
		this.mFence = fence;
	}

	@Override
	public void putExtra(Intent intent)
	{
		super.putExtra(intent);
		intent.putExtra(EXTRA_FENCE, mFence);
	}
	
	public String toJsonString()
	{
		try
		{
			JSONObject o = new JSONObject();
			o.put(EXTRA_LAT, getLat());
			o.put(EXTRA_LON, getLon());
			o.put(EXTRA_RADIUS, getRadius());
			o.put(EXTRA_IDENTIFY, getIdentify());
			o.put(EXTRA_USER_FENCE, getUserFence());
			o.put(EXTRA_FENCE, getFence());
			o.put(EXTRA_STATUS, getStatus());
			o.put(EXTRA_STARTING_TS, getStartingTs());
			o.put(EXTRA_EXPIRING_TS, getExpiringTs());
			return o.toString(2);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		return "";
	}
}
