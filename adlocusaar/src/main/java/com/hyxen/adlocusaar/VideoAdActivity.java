package com.hyxen.adlocusaar;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.hyxen.adlocusaar.AdLocusLayout.ErrorCode;

public class VideoAdActivity extends Activity
{
    private InterstitialVideoAd mIVAd = null;
	private Activity mAct;
    private boolean isFirst = true;
    
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setTheme(android.R.style.Theme_Black_NoTitleBar);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		mAct = this;
		
		Intent i = getIntent();
		String mKey = i.getStringExtra("adKey");
		if(mKey == null)
		{
			finish();
			return;
		}

		//dialog
		mIVAd = new InterstitialVideoAd(VideoAdActivity.this, mKey);
		
		mIVAd.setListener(new InterstitialAdListener() {
			
			@Override
			public void onReceiveAd(InterstitialVideoAd ad) {
				mIVAd.show();
				isFirst = false;
			}
			
			@Override
			public void onFailedToReceiveAd(InterstitialVideoAd ad, ErrorCode errorCode) {
				mIVAd.endAd(); 
				setResult(RESULT_CANCELED);
			}
			
			@Override
			public void onEnd(){
				mIVAd.unsetListener();
				mAct.finish();
				setResult(RESULT_OK);
			}

		});
		
		mIVAd.loadAd();
	}
	
	@Override
	protected void onPause()
	{
		if(mIVAd != null)
			mIVAd.stopVideo();
		
		super.onPause();
	}
	
	@Override
	public void onResume()
	{			
		if(!isFirst)
			finish();
		
		super.onResume();
		
	}
	
}
