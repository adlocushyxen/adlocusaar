package com.hyxen.adlocusaar.util;

public class Log
{
	public static boolean isShowLog = AdLocusUtil.SHOW_LOG;
	public static void d(String msg)
	{
		if(isShowLog)
		{
			android.util.Log.d(AdLocusUtil.ADLOCUS, msg);
		}
	}
	public static void e(String msg, Exception tr)
	{
		if (isShowLog) {
			android.util.Log.e(AdLocusUtil.ADLOCUS, msg, tr);
		}
	}

	public static void v(String msg) 
	{
		android.util.Log.v(AdLocusUtil.ADLOCUS, msg);
	}
	
	public static void w(String msg) 
	{
		android.util.Log.w(AdLocusUtil.ADLOCUS, msg);
	}
}
