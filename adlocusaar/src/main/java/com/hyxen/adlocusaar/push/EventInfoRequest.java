package com.hyxen.adlocusaar.push;

import android.content.Context;

import com.hyxen.adlocusaar.UserBaseData;
import com.hyxen.adlocusaar.net.HxRequest;
import com.hyxen.adlocusaar.util.AdLocusUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

public class EventInfoRequest extends HxRequest
{
	public static final int ERROR_NO_DATA = -1;
	public static final int ERROR_CONNECTION_FAIL = -999;
	
	private RequestListener mListener;
	private boolean isFource = false;
	
	public EventInfoRequest(Context context, String adId, String sessionId)
	{
		super(context, TestUtil.isTestMode(context) ? AdLocusUtil.URL_PUSH_REQ_TEST : AdLocusUtil.URL_PUSH_REQ);

//		setPostParameter("device_id", AdLocusUtil.getEncodedDeviceId(context));
//		setPostParameter("device_id", UserBaseData.getHashDeviceId(context));
//		setPostParameter("key", AdLocusUtil.getPushKey(context));
//		setPostParameter("screen", AdLocusUtil.getSize(context));
//		setPostParameter("ad_id", adId);
//		setPostParameter("session_id", sessionId);
//		setPostParameter("dev_type", "11");
//
//		if(AdLocusUtil.getNewPushBackgroundIntent(context) != null)
//		{
//			setPostParameter("new_push", "1");
//		}
//
//		if(AdLocusUtil.supportBigview())
//		{
//			setPostParameter("bv", "1");
//		}
//
//		AdLocusUtil.setToRequest(this, AdLocusUtil.getPushTargeting(context));
//		AdLocusUtil.setLocationToRequest(context, this);
	}
	
	public void setFource()
	{
		isFource = true;
		setPostParameter("force", "1");
	}
	
	public boolean isFource()
	{
		return isFource;
	}
	
	public void setListener(RequestListener listener)
	{
		mListener = listener;
	}
	
	@Override
	protected void processContent(int errorCode, String content)
	{
        //bigview
        //content="{\"ad_id\":-1,\"ad_type\":21,\"act_loc\":1,\"act_type\":12,\"pub_format\":\"bigview\",\"bv_text\":\"test\",\"bv_banner\":\"http:\\/\\/adlocus-sti.s3.amazonaws.com\\/camp\\/5adfb023\\/5adfb0239599ebca67de24e22deb633a37a1c497.jpg\",\"bv_share_text\":\"xxxhttp:\\/\\/hyxen.com.tw\",\"ad_link_type\":1,\"sid\":\"14331530002151\",\"ad_link\":\"http:\\/\\/hyxen-adlocus-api.rd.hyxencloud.com\\/dev\\/dry\\/14327229411704\\/and\\/3cc548ba5b2ca66c3190eb48f10bac514aa29944\\/abbe42c6d2be97cf641eb55a5dc4b494f9f7e107\\/14331530019231\\/native\\/\",\"ad_ping\":\"http:\\/\\/hyxen-adlocus-api.rd.hyxencloud.com\\/dev_html5\\/imp?device_id=and%3A%2F%2F3cc548ba5b2ca66c3190eb48f10bac514aa29944&key=abbe42c6d2be97cf641eb55a5dc4b494f9f7e107&session_id=14331530002151&ad_id=14327229411704\",\"ad_right_icon\":1,\"ios_push_text\":\"\",\"ios_unlock_text\":\"\",\"ios_prefix_text\":\"AdLocus \\u884c\\u52d5\\u5ee3\\u544a\\u670d\\u52d9:\",\"sec\":10,\"err\":0,\"rtb_host\":[\"hyxen.com\",\"hyxen.com\"],\"ad_type_res\":\"icontxt_320_50\",\"pub_height\":\"50\",\"pub_width\":\"320\",\"scr\":\"phn\",\"house\":0}";

        if(errorCode == HttpURLConnection.HTTP_OK && content != null && !content.equals(""))
		{
			try
			{
				JSONObject o = new JSONObject(content);
				int err = o.optInt("err", -999);
				if(err == 0)
				{
					if(mListener != null)
						mListener.onSucceed(content); //com.hyxen.adlocusaar.push.PushService.java EventInfoRequest.onSucceed
				}
				else
				{
					if(mListener != null)
						mListener.onError(err);
				}
			}
			catch (JSONException e)
			{
				if(mListener != null)
					mListener.onError(-999);
			}
		}
		else
		{
			if(mListener != null)
				mListener.onError(-999);
		}
	}

	public interface RequestListener
	{
		/**
		 * 
		 * @param json
		 */
		void onSucceed(String json);
		/**
		 * 
		 * @param errorCode
		 * <br>0 : 正常
		 * <br>-1 : 無法取得資料
		 * <br>-999 : 連線異常
		 */
		void onError(int errorCode);
	}
}
