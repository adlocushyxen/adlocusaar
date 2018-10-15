package com.hyxen.adlocusaar.push;


import android.content.Context;

import com.hyxen.adlocusaar.AdLocusTargeting;
import com.hyxen.adlocusaar.net.HxRequest;
import com.hyxen.adlocusaar.util.AdLocusUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

class CheckRequest extends HxRequest
{
	private HxPushCheckRequestListener mListener;
	
	public CheckRequest(Context context, String url, String key, int type, String mac, AdLocusTargeting adLocusTargeting)
	{
		super(context, url);
		init(context, key, adLocusTargeting);
		setPostParameter("type", String.valueOf(type));
		if(mac == null)
		{
			setPostParameter("ip", "1");
			setPostParameter("mac", "null");
		}
		else
		{
			setPostParameter("mac", mac);
		}
		setPostParameter("dev_type", "3");
	}
	
	private void init(Context context, String key, AdLocusTargeting adLocusTargeting)
	{
		setMethod(Method.GET);
		setPostParameter("device_id", AdLocusUtil.getEncodedDeviceId(context));
		setPostParameter("key", key);

		AdLocusUtil.setToRequest(this, adLocusTargeting);
	}

	public CheckRequest(Context context, String url, String key, String mcc, String mnc, String lac, String ci, String rssi, AdLocusTargeting adLocusTargeting)
	{
		super(context, url);
		
		init(context, key, adLocusTargeting);
		setPostParameter("mcc", mcc);
		setPostParameter("mnc", mnc);
		setPostParameter("lac", lac);
		setPostParameter("ci", ci);
		setPostParameter("rssi", rssi);
		setPostParameter("dev_type", "11");
		setPostParameter("ip", "0");
	}
	
	public void setListener(HxPushCheckRequestListener listener)
	{
		mListener = listener;
	}
	
	@Override
	protected void processContent(int errorCode, String content)
	{
		if(errorCode == HttpURLConnection.HTTP_OK && content != null && !content.equals(""))
		{
			try {
				JSONObject o = new JSONObject(content);
				int err = o.optInt("err", -999);
				if(err == 0)
				{
					if(mListener != null)
						mListener.onSucceed(o.optString("dlpath"), o.optInt("ready"), o.optInt("wait"), o.optLong("oid", -1));
				}
				else
				{
					if(mListener != null)
						mListener.onError(err);
				}
			} catch (JSONException e) {
				if(mListener != null)
					mListener.onError(-999);
			}
		}else{
			if(mListener != null)
				mListener.onError(-999);
		}
	}

	public interface HxPushCheckRequestListener
	{
		/**
		 * 
		 * @param dlpatch Returns the empty string if not get
		 * @param ready 0, if not get
		 * @param wait 0, if not get
		 * @param offpush_id -1, if not get
		 */
		void onSucceed(String dlpatch, int ready, int wait, long offpush_id);
		/**
		 * 
		 * @param errorCode 
		 * <br>0 : no error 
		 * <br>-1 : invalid params
		 * <br>-2 : expired
		 * <br>-255 : unexpected error
		 * <br>-999 : local request error
		 */
		void onError(int errorCode);
	}
}
