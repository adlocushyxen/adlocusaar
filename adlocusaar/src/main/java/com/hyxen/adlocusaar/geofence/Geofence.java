package com.hyxen.adlocusaar.geofence;

import android.content.Context;
import android.util.Log;


import java.util.ArrayList;

public class Geofence
{
	static final boolean IS_SHOW_LOG = true;
	public static final String ACTION_EVENT_TRIGGERED = "com.hyxen.geofence.EVENT_TRIGGERED";


	public static void clearRegions(Context context)
	{
		GeofenceService.startClear(context);
	}

	public static void addRegion(Context context, final Region region)
	{
		GeofenceService.startAdd(context, region);
	}

	public static void removeRegion(Context context, String identify)
	{
		GeofenceService.startDelete(context, identify);
	}

	public static ArrayList<Region> getAllRegions(Context context)
	{
		return GeofenceDbAdapter.getAllRegion(context);
	}

	static void logD(String text)
	{
		if(IS_SHOW_LOG)
		{
			Log.d("com.hyxen.geofence", text);
		}
	}
	
	static void logE(String text)
	{
		if(IS_SHOW_LOG)
		{
			Log.e("com.hyxen.geofence", text);
		}
	}

}
