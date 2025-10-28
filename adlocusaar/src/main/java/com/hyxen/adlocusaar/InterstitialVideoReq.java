package com.hyxen.adlocusaar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.hyxen.adlocusaar.constants.Constants;
import com.hyxen.adlocusaar.engine.CellInfo;
import com.hyxen.adlocusaar.engine.HxCellEngine;
import com.hyxen.adlocusaar.engine.HxWifiEngine;
import com.hyxen.adlocusaar.net.HxRequest;
import com.hyxen.adlocusaar.repository.Repository;
import com.hyxen.adlocusaar.util.AdLocusUtil;

import java.net.HttpURLConnection;

/*
 * InterstitialVideoReq: an isolated request to get Ad, trigger VideoAdActivity (a black box to third-party)
 *                       VideoAdActivtiy interactive with InterstitalVideoAd + InterstitalAdListener
 */

public class InterstitialVideoReq 
{
	private Activity mActivity;	
	private String mKey;
	private AdLocusTargeting mAdLocusTargeting;
	private VideoAdListener mVideoListener = null;
	
	public String BUNDLE_KEY="AdLocus.InterstitialVideoReq";
	
	public InterstitialVideoReq(Activity activity, String key, VideoAdListener l)
	{
		init(activity, key, null);
		
		mVideoListener = l;
	}
	
	public InterstitialVideoReq(Activity activity, String key, AdLocusTargeting adLocusTargeting, VideoAdListener l)
	{
		init(activity, key, adLocusTargeting);

		mVideoListener = l;
	}
	
	private void init(Activity activity, String key, AdLocusTargeting adLocusTargeting)
	{
		mKey = key;
		mActivity = activity;
		mAdLocusTargeting = adLocusTargeting;

//		AdLocusUtil.init(mActivity);
		CellInfo ci=null;
		int userStatement = Repository.getUserAndroidIdState();
		if(userStatement == Constants.TAG_ANDROID_ID_STATEMENT_STATE_GRANT){
			HxCellEngine.getInstance(mActivity);
			HxWifiEngine.getInstance(mActivity);
		}

	}

	public void show()
	{
		Intent intent=new Intent();

		intent.setClass(mActivity, VideoAdActivity.class);
		Bundle bundle=new Bundle();
		bundle.putString("adKey", mKey);
		intent.putExtras(bundle);
		
		//AdLocus
		//01000001 A 
		//01100100 d
		//01001100 L
		//01101111 o
		//01100011 c
		//01110101 u
		//01110011 s
		// 7522335 
		
		mActivity.startActivityForResult(intent, 31245);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data)  
	{
		if(requestCode != 31245) return;
		if(mVideoListener != null)
			mVideoListener.onClose();
	}
	
	public void removeListener()
	{
		mVideoListener = null;
	}
	
	public void load()
	{	
		if(android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.JELLY_BEAN)
		{
			mVideoListener.onNofill();
		    return;
		}

		if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH)
		{
			mVideoListener.onNofill();
		    return;
		}
		
		Thread t = new Thread()
		{
			@Override
			public void run()
			{	
				HxRequest r = new HxRequest(mActivity);
				
				AdLocusUtil.setRequestParameters(mActivity, r, mKey, AdLocusUtil.getScreen(mActivity, AdLocusLayout.AD_SIZE_IAB_MRECT), "VideoAdActivity", mAdLocusTargeting);
				r.setPostParameter("fs", "1");
				r.setPostParameter("vdo", "1");
				r.setPostParameter("dry", "1");
				
				String url = AdLocusUtil.URL_PULL_HTML_REQ + "?" + r.getPostString();
				
				HxRequest r2 = new HxRequest(mActivity, url);
				r2.run();
				final int errorCode = r2.getErrorCode();
				
				if (errorCode == HttpURLConnection.HTTP_OK)
					mVideoListener.onStart();
				else
					mVideoListener.onNofill();
			}	
		};
		
		t.start();
	}
	
}
