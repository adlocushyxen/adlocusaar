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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.hyxen.adlocusaar.adapters.AdLocusAdapter;
import com.hyxen.adlocusaar.engine.HxCellEngine;
import com.hyxen.adlocusaar.engine.HxWifiEngine;
import com.hyxen.adlocusaar.obj.Flip3dAnimation;
import com.hyxen.adlocusaar.util.AdLocusUtil;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AdLocusLayout extends RelativeLayout implements Ad
{
//	/**
//	 * Smart banner Phones and Tablets
//	 */
//	public static final int AD_SIZE_SMART = 5;
	/**
	 * 320x50 Standard banner Phones and Tablets
	 */
	public static final int AD_SIZE_BANNER = 0;
	/**
	 * 300x250 IAB Medium Rectangle Tablets
	 */
	public static final int AD_SIZE_IAB_MRECT = 1;
	/**
	 * 728x90 IAB Leaderboard Tablets
	 */
	public static final int AD_SIZE_IAB_LEADERBOARD = 3;
//	/**
//	 * 728x90 IAB Leaderboard Tablets
//	 */
////	public static final int AD_SIZE_SMART = 4;

	public static final int ANIMATION_NONE = 0;
	public static final int ANIMATION_FLIP_FROM_LEFT = 1;
	public static final int ANIMATION_FLIP_FROM_RIGHT = 2;
	public static final int ANIMATION_CURL_UP = 3;
	public static final int ANIMATION_CURL_DOWN = 4;
	public static final int ANIMATION_SLIDE_FROM_LEFT = 5;
	public static final int ANIMATION_SLIDE_FROM_RIGHT = 6;
	public static final int ANIMATION_FADE_IN = 7;
	public static final int ANIMATION_RANDOM = 8;

	public enum ErrorCode
	{
		NO_FILL, NETWORK_ERROR, SERVICE_ERROR, INVALID_KEY
	}
	
	// public static final int DISPLAY_

	public static final String ADLOCUS_KEY = "ADLOCUS_KEY";
	public WeakReference<Activity> activityReference;

	// Only the UI thread can update the UI, so we need this for UI callbacks
	public final Handler handler = new Handler();

	// We also need a scheduler for background threads
	public final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	// private String mKeyProMe;
	// public Extra extra;

	// The current custom ad
//	public AdLocusAd adlocusAd;

	// This is just so our threads can reference us explicitly
	public WeakReference<RelativeLayout> superViewReference;

	// Added so we can tell the previous adapter that it is being destroyed.
	private AdLocusAdapter mPreviousAdapter;
	private AdLocusAdapter mCurrentAdapter;

	public AdLocusManager adLocusManager;

//	private boolean mHasFullWindow;
	private boolean mHasWindow;
	private boolean mIsScheduled;

	private int mRefreshRate = -1;

	private int mAnimationType = ANIMATION_FADE_IN;

	private AdListener mAdListener;

	private boolean mIsScreenOn = true;
	
	private final BroadcastReceiver mScreenReceiver = new BroadcastReceiver()
	{
		
		@Override
		public void onReceive(final Context context, final Intent intent)
		{
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
			{
				// do whatever you need to do here
				mIsScreenOn = false;
			}
			else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON))
			{
				// and do whatever you need to do here
				mIsScreenOn = true;
			}

			if(mIsScreenOn)
			{
				if (!mIsScheduled)
				{
					mIsScheduled = true;
					rotateThreadedNow();
				}
			}
		}
	};

	/**
	 * 設定輪播間隔
	 * 
	 * @param second
	 *            最低15秒，如果 < 0 表示不輪播只顯示一則。
	 */
	private void setRefreshRate(int second)
	{
		if (second <= 0)
		{
			mRefreshRate = Integer.MAX_VALUE / 2;
		}
		else if (second < 15)
		{
			mRefreshRate = 15;
		}
		else
		{
			mRefreshRate = second;
		}
	}

	@Override
	public void setListener(AdListener listener)
	{
		mAdListener = listener;
	}

	public AdListener getListener()
	{
		return mAdListener;
	}
	
	public int getRefreshRate()
	{
		return mRefreshRate;
	}

	private int mAdSize = AD_SIZE_BANNER;
	private int mLayoutWidth = -1;
	private int mLayoutHeight = -1;

	public int getAdSize()
	{
		return mAdSize;
	}

	private int mMaxWidth;

	public void setMaxWidth(int width)
	{
		mMaxWidth = width;
	}

	private int mMaxHeight;

	public void setMaxHeight(int height)
	{
		mMaxHeight = height;
	}
	
	public void setLayoutSize(int width, int height)
	{
		mLayoutWidth = width;
		mLayoutHeight = height;
	}

	/**
	 * 
	 * @param context
	 * @param adSize
	 *            {@link AD_SIZE_BANNER}, {@link AD_SIZE_IAB_MRECT},
	 *            {@link AD_SIZE_IAB_BANNER}, {@link AD_SIZE_IAB_LEADERBOARD}
	 * @param appkey
	 * @param rotateInterval
	 *            輪播間隔，最低15秒，如果 < 0 表示不輪播只顯示一則。
	 */
	public AdLocusLayout(final Activity context, int adSize, String appkey, int rotateInterval)
	{
		super(context);
		init(context, adSize, appkey, rotateInterval, null);
	}	
	
	public AdLocusLayout(final Activity context, int adSize, String appkey, int rotateInterval, AdListener l)
	{
		super(context);
		init(context, adSize, appkey, rotateInterval, null);

		mAdListener = l;
	}

	/**
	 * 
	 * @param context
	 * @param adSize
	 *            {@link AD_SIZE_BANNER}, {@link AD_SIZE_IAB_MRECT},
	 *            {@link AD_SIZE_IAB_BANNER}, {@link AD_SIZE_IAB_LEADERBOARD}
	 * @param appkey
	 * @param rotateInterval
	 *            輪播間隔，最低15秒，如果 < 0 表示不輪播只顯示一則。
	 */
	public AdLocusLayout(final Activity context, int adSize, String appkey, int rotateInterval, AdLocusTargeting adLocusTargeting)
	{
		super(context);
		init(context, adSize, appkey, rotateInterval, adLocusTargeting);

	}

	public AdLocusLayout(final Activity context, int adSize, String appkey, int rotateInterval, AdLocusTargeting adLocusTargeting, AdListener l)
	{
		super(context);
		init(context, adSize, appkey, rotateInterval, adLocusTargeting);
		
		mAdListener = l;
	}

	/**
	 * ANIMATION_NONE
	 * 
	 * @param animation
	 */
	public void setTransitionAnimation(int animation)
	{
		mAnimationType = animation;
	}

	protected void init(final Activity context, int adSize, String appkey, int rotateInterval, AdLocusTargeting adLocusTargeting)
	{
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB_MR1) {
			addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
				@Override
				public void onViewAttachedToWindow(View v) {
					try {
						IntentFilter screenFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
						screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
						context.registerReceiver(mScreenReceiver, screenFilter);
					}
					catch (Exception ignored) { }
				}
				
				@Override
				public void onViewDetachedFromWindow(View v) {
					mIsScreenOn = false;
					try {
						context.unregisterReceiver(mScreenReceiver);
					}
					catch (Exception ignored) { }
				}
			});
		}
		
		setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		setRefreshRate(rotateInterval);
		
		HxCellEngine.getInstance(context);
		HxWifiEngine.getInstance(context);
				
		this.activityReference = new WeakReference<>(context);
		this.superViewReference = new WeakReference<RelativeLayout>(this);

		mAdSize = adSize;
		int width = AdLocusUtil.getAdWidth(adSize);
		int height = AdLocusUtil.getAdHeight(adSize);

		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		float density = displayMetrics.density;

		mMaxWidth = (int) (width * density);
		mMaxHeight = (int) (height * density);
		mLayoutWidth = mMaxWidth;
		mLayoutHeight = mMaxHeight;

		this.mHasWindow = true;
		this.mIsScheduled = true;
		
		//get ad
		scheduler.schedule(new InitRunnable(this, appkey, adSize, adLocusTargeting), 0, TimeUnit.SECONDS);

		setHorizontalScrollBarEnabled(false);
		setVerticalScrollBarEnabled(false);

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		com.hyxen.adlocusaar.util.Log.d("onMeasure" + widthSize + "," + heightSize);
		
		if (mLayoutWidth > 0)
		{
			if(widthSize > mLayoutWidth)
			{
				widthMeasureSpec = MeasureSpec.makeMeasureSpec(mLayoutWidth, MeasureSpec.AT_MOST);
			}
			widthSize = MeasureSpec.getSize(widthMeasureSpec);
		}

		if (mLayoutHeight > 0)
		{
			if(heightSize > mLayoutHeight)
			{
				heightMeasureSpec = MeasureSpec.makeMeasureSpec(mLayoutHeight, MeasureSpec.AT_MOST);
			}
			heightSize = MeasureSpec.getSize(heightMeasureSpec);
		}


		if(heightSize != 0 && widthSize != 0 && mLayoutWidth > 0 && mLayoutHeight > 0)
		{
	        double ratio = (double)mLayoutHeight / mLayoutWidth;

			double or = (double) heightSize / (double) widthSize;

	        if(widthSize > heightSize) //扁平
	        {
	            if(or > ratio) // too width
	            {
	                mLayoutHeight = (int) ((double) widthSize * ratio);
					heightMeasureSpec = MeasureSpec.makeMeasureSpec(mLayoutHeight, MeasureSpec.AT_MOST);
	            }
	        }

	        if(widthSize < heightSize) // 長條
	        {
	            if(or > ratio) // too height
	            {
	            	mLayoutWidth = (int) ((double) heightSize / ratio);
					widthMeasureSpec = MeasureSpec.makeMeasureSpec(mLayoutWidth, MeasureSpec.AT_MOST);
	            }
	        }
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onWindowVisibilityChanged(int visibility)
	{
		if (visibility == VISIBLE)
		{
			this.mHasWindow = true;
			if (!this.mIsScheduled)
			{
				this.mIsScheduled = true;
				rotateThreadedNow();
			}
		}
		else
		{
			this.mHasWindow = false;
		}
	}

	private void rotateAd()
	{
		if (!this.mHasWindow || !mIsScreenOn)
		{
			this.mIsScheduled = false;
			return;
		}
		
		handler.post(new HandleAdRunnable(this));
	}

	// Initialize the proper ad view from nextRation
	private void handleAd()
	{
		try
		{
			// Tell the previous adapter that its view will be destroyed.
			if (this.mPreviousAdapter != null)
			{
				this.mPreviousAdapter.willDestroy();
			}
			this.mPreviousAdapter = this.mCurrentAdapter;
			this.mCurrentAdapter = new AdLocusAdapter(this);
			this.mCurrentAdapter.handle();
			
		}
		catch (Throwable t)
		{
			rollover();
		}
	}

	// Rotate immediately
	public void rotateThreadedNow()
	{
		scheduler.schedule(new RefreshAdRunnable(this), 1, TimeUnit.SECONDS);
	}

	// Rotate in extra.cycleTime seconds
	public void rotateThreadedDelayed(int webDelay)
	{
		if (mRefreshRate <= 0)
		{
			this.mIsScheduled = false;
		}
		else
		{
			if(mRefreshRate < webDelay)
			{
				scheduler.schedule(new RefreshAdRunnable(this), webDelay, TimeUnit.SECONDS);
			}
			else
			{
				scheduler.schedule(new RefreshAdRunnable(this), mRefreshRate, TimeUnit.SECONDS);
			}
		}
	}

	// Remove old views and push the new one
	public void pushSubView(View v)
	{
		RelativeLayout superView = superViewReference.get();
		if (superView == null)
		{
			return;
		}
		superView.removeAllViews();

		double density = AdLocusUtil.getDensity(getContext());
		int pxWidth = AdLocusUtil.convertToScreenPixels(AdLocusUtil.getAdWidth(getAdSize()), density);
		int pxHeight = AdLocusUtil.convertToScreenPixels(AdLocusUtil.getAdHeight(getAdSize()), density);

		LinearLayout linearLayout = new LinearLayout(getContext());
		linearLayout.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		linearLayout.setGravity(Gravity.CENTER);
		linearLayout.addView(v, new ViewGroup.LayoutParams(pxWidth, pxHeight));
		superView.addView(linearLayout);
		setAnimation(v, mAnimationType);
		if (mAdListener != null)
		{
			mAdListener.onReceiveAd(this);
		}
	}

	public void rollover()
	{
		handler.post(new HandleAdRunnable(this));
	}

	private void setAnimation(View subView, int transition)
	{
		AnimationSet set = new AnimationSet(true);

		Animation animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(100);
		set.addAnimation(animation);
		if (transition == ANIMATION_RANDOM)
		{
			transition = (int) (Math.random() * 8) + 1;
			// transition = AWBannerAnimationTypeFlipFromLeft;
		}
		switch (transition)
		{
			case ANIMATION_FLIP_FROM_LEFT:
				animation = new Flip3dAnimation(subView, 180, 360, 0, 0);
				animation.setDuration(700);
				set.addAnimation(animation);
				break;
			case ANIMATION_FLIP_FROM_RIGHT:
				animation = new Flip3dAnimation(subView, 180, 0, 0, 0);
				animation.setDuration(700);
				set.addAnimation(animation);
				break;
			case ANIMATION_CURL_UP:
				animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
				animation.setDuration(500);
				set.addAnimation(animation);
				break;
			case ANIMATION_CURL_DOWN:
				animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
				animation.setDuration(500);
				set.addAnimation(animation);
				break;
			case ANIMATION_SLIDE_FROM_LEFT:
				animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
				animation.setDuration(500);
				set.addAnimation(animation);
				break;
			case ANIMATION_SLIDE_FROM_RIGHT:
				animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
				animation.setDuration(500);
				set.addAnimation(animation);
				break;
			case ANIMATION_FADE_IN:
				animation = new AlphaAnimation(0.0f, 1.0f);
				animation.setDuration(500);
				set.addAnimation(animation);
				break;
			default:
				break;
		}
		subView.startAnimation(set);
	}

	private static class InitRunnable implements Runnable
	{
		private final WeakReference<AdLocusLayout> adLocusLayoutReference;
		private final String keyProMe;
		private final int adType;
		private final AdLocusTargeting adLocusTargeting;

		public InitRunnable(AdLocusLayout proMeLayout, String keyProMe, int adType, AdLocusTargeting adLocusTargeting)
		{
			adLocusLayoutReference = new WeakReference<>(proMeLayout);
			this.keyProMe = keyProMe;
			this.adType = adType;
			this.adLocusTargeting = adLocusTargeting;
		}

		public void run()
		{
			AdLocusLayout adLocusLayout = adLocusLayoutReference.get();
			if (adLocusLayout != null)
			{
				Activity activity = adLocusLayout.activityReference.get();
				if (activity == null)
				{
					return;
				}

				if (adLocusLayout.adLocusManager == null)
				{
					adLocusLayout.adLocusManager = new AdLocusManager(new WeakReference<>(activity.getApplicationContext()), keyProMe, AdLocusUtil.getScreen(activity, adType),
							activity.getLocalClassName(), adLocusTargeting);
				}

				if (!adLocusLayout.mHasWindow)
				{
					adLocusLayout.mIsScheduled = false;
					return;
				}
				adLocusLayout.adLocusManager.fetchConfig();
				int err = adLocusLayout.adLocusManager.fetchAuth();

				if (err == -999)
				{
					adLocusLayout.scheduler.schedule(this, 1, TimeUnit.SECONDS);
				}
				else if (err != 0)
				{
					adLocusLayout.mIsScheduled = false;
				}
				else
				{
					adLocusLayout.rotateAd();
				}
			}
		}
	}

	// Callback for external networks
	private static class HandleAdRunnable implements Runnable
	{
		private final WeakReference<AdLocusLayout> adLocusLayoutReference;

		public HandleAdRunnable(AdLocusLayout adLocusLayout)
		{
			adLocusLayoutReference = new WeakReference<>(adLocusLayout);
		}

		public void run()
		{
			AdLocusLayout adLocusLayout = adLocusLayoutReference.get();
			if (adLocusLayout != null)
			{
				adLocusLayout.handleAd();
			}
		}
	}

	private static class RefreshAdRunnable implements Runnable
	{
		private final WeakReference<AdLocusLayout> adLocusLayoutReference;

		public RefreshAdRunnable(AdLocusLayout proMeLayout)
		{
			adLocusLayoutReference = new WeakReference<>(proMeLayout);
		}

		public void run()
		{
			AdLocusLayout proMeLayout = adLocusLayoutReference.get();
			if (proMeLayout != null)
			{
				proMeLayout.rotateAd();
			}
		}
	}

}
