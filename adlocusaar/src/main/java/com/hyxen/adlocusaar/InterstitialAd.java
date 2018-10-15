package com.hyxen.adlocusaar;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.hyxen.adlocusaar.AdLocusLayout.ErrorCode;
import com.hyxen.adlocusaar.net.HxRequest;
import com.hyxen.adlocusaar.util.AdLocusUtil;

import java.net.HttpURLConnection;
import java.util.concurrent.atomic.AtomicBoolean;

public class InterstitialAd implements Ad
{
	private Dialog mDialog;
	
	private Activity mActivity;
	
	private WebView mWebView;
	
	private String mKey;
	private AdLocusTargeting mAdLocusTargeting;
	private AdListener mListener = null;
	
	private final AtomicBoolean mIsReadyToShow = new AtomicBoolean(false);
	private final AtomicBoolean mIsLoading = new AtomicBoolean(false);
	
	private Handler mHandler;
	
	public InterstitialAd(Activity activity, String key)
	{
		init(activity, key, null);
	}
	
	public InterstitialAd(Activity activity, String key, AdLocusTargeting adLocusTargeting)
	{
		init(activity, key, adLocusTargeting);
	}
	
	private void init(Activity activity, String key, AdLocusTargeting adLocusTargeting)
	{
		mHandler = new Handler(Looper.getMainLooper());
//		AdLocusUtil.init(activity);
		mKey = key;
		mActivity = activity;
		mAdLocusTargeting = adLocusTargeting;
		
		mDialog = new Dialog(mActivity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
		
        mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				endAd();
			}
		});

        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				endAd();
			}
		});
        
		initViews();
	}

	private void endAd()
	{
		if(mWebView != null)
			mWebView.loadUrl("javascript:if(typeof(end_ad)=='function') end_ad()");

		if(mListener != null)
			mListener.onEnd();
	}

	private void initViews()
	{
		RelativeLayout rootLayout = new RelativeLayout(mActivity);
		
		final AtomicBoolean isRotate = new AtomicBoolean(false);
		final WebView webView = new WebView(mActivity)
		{
			@Override
			protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
			{
		        invalidate();
		        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		    }
			
			@Override
			public void loadUrl(final String url)
			{
				if(!url.startsWith("http"))
				{
					super.loadUrl(url);
					return;
				}

				Thread t = new Thread()
				{
					@Override
					public void run()
					{	
						final HxRequest r = new HxRequest(mActivity, url);
						r.run();
						final int errorCode = r.getErrorCode();
						if (errorCode == HttpURLConnection.HTTP_OK)
						{
							mHandler.post(new Runnable() {
								
								@Override
								public void run() {

									loadDataWithBaseURL(AdLocusUtil.URL_PULL_HTML_REQ, r.getResult(), "text/html", "utf-8", url);
								}
							});
						}
						else
						{
							mHandler.post(new Runnable()
							{
								public void run()
								{
									synchronized (mIsLoading)
									{
										mIsLoading.set(false);
										synchronized (mIsReadyToShow)
										{
											mIsReadyToShow.set(false);
											if(mListener != null)
											{
												switch (errorCode)
												{
													case 404:
														mListener.onFailedToReceiveAd(InterstitialAd.this, ErrorCode.NO_FILL);
														break;
													case 403:
														mListener.onFailedToReceiveAd(InterstitialAd.this, ErrorCode.INVALID_KEY);
														break;
													default:
														if(errorCode > 500)
														{
															mListener.onFailedToReceiveAd(InterstitialAd.this, ErrorCode.SERVICE_ERROR);
														}
														else
														{
															mListener.onFailedToReceiveAd(InterstitialAd.this, ErrorCode.NETWORK_ERROR);
														}
														break;
												}
											}
										}
									}
								}
							});
						}
					}
				};
				t.start();
			}
		};
		webView.setBackgroundColor(0x00000000);
		webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		webView.setVerticalScrollBarEnabled(false);
		webView.setHorizontalScrollBarEnabled(false);
		webView.setScrollContainer(false);


		WebSettings webSettings = webView.getSettings();
		  
		webSettings.setJavaScriptEnabled(true);
		webSettings.setSupportZoom(false);
		webSettings.setAllowFileAccess(true);
		webSettings.setUseWideViewPort(false);
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
		{
			webSettings.setMediaPlaybackRequiresUserGesture(false);
		}
		
        if(Build.VERSION.SDK_INT > 7)
        {
    		webSettings.setPluginState(PluginState.ON);
        }
        
        webView.setWebChromeClient(new WebChromeClient()
        {
			@Override
			public void onProgressChanged(WebView view, final int newProgress)
			{
				super.onProgressChanged(view, newProgress);
				if(newProgress != 100)
				{
					return;
				}
				
				mHandler.postDelayed(new Runnable()
				{
					public void run()
					{
						synchronized (mIsLoading)
						{
							synchronized (mIsReadyToShow)
							{
								if(mIsReadyToShow.get())
								{
									return;
								}
								mIsReadyToShow.set(true);
								mIsLoading.set(false);
								if(mListener != null)
								{
									mListener.onReceiveAd(InterstitialAd.this);
								}
							}
						}
					}
				}, 100);
			}
        	
        });
        
        
        webView.setWebViewClient(new WebViewClient()
        {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url)
			{
				if((url.startsWith("http://") || url.contains(AdLocusUtil.REDIRECTS_CHECK_URL)) || url.startsWith("tel"))
				{
					Activity activity = mActivity;
			        // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
					if(activity != null)
					{
						openUrl(url);
					}
			        return true;
				}
				else
				{
					view.setVisibility(View.GONE);
					synchronized (isRotate)
					{
						if (!isRotate.get())
						{
							isRotate.set(true);
						}
					}
					AdListener listener = mListener;

					synchronized (mIsLoading)
					{
						mIsLoading.set(false);
						synchronized (mIsReadyToShow)
						{
							mIsReadyToShow.set(false);
							if (listener != null)
							{
								listener.onFailedToReceiveAd(InterstitialAd.this, ErrorCode.NETWORK_ERROR);
							}
						}
					}
					return true;
				}
			}
			
			@Override
			public void onLoadResource (WebView view, String url)
			{
				if(view.getHitTestResult() != null && view.getHitTestResult().getType() > 0 && !url.contains("/dev_html5/imp"))
				{
					view.stopLoading();//需停止原本網頁載入動作
					openUrl(url);
				}
			}
			
        	@Override
        	public void onReceivedError(WebView view, final int errorCode, String description, String failingUrl)
        	{
				mHandler.post(new Runnable()
				{
					public void run() {

						synchronized (mIsLoading)
						{
							mIsLoading.set(false);
							synchronized (mIsReadyToShow)
							{
								mIsReadyToShow.set(false);
								if(mListener != null)
								{
									switch (errorCode)
									{
										case 404:
											mListener.onFailedToReceiveAd(InterstitialAd.this, ErrorCode.NO_FILL);
											break;
										case 403:
											mListener.onFailedToReceiveAd(InterstitialAd.this, ErrorCode.INVALID_KEY);
											break;
										default:
											if(errorCode > 500)
											{
												mListener.onFailedToReceiveAd(InterstitialAd.this, ErrorCode.SERVICE_ERROR);
											}
											else
											{
												mListener.onFailedToReceiveAd(InterstitialAd.this, ErrorCode.NETWORK_ERROR);
											}
											break;
									}
								}
							}
						}
					}
				});
				
        		super.onReceivedError(view, errorCode, description, failingUrl);
        	}
        });
        
        
		mWebView = webView;
		
		RelativeLayout.LayoutParams webParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		rootLayout.addView(webView, webParams);
		
		Button btnClose = new Button(mActivity);
		btnClose.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				mDialog.dismiss();
			}
		});

        
        StateListDrawable drawables = new StateListDrawable();
         
         
        Drawable dOn = AdLocusUtil.getAssetsDrawable(mActivity, "btn_closeevent_click");
        Drawable dOff = AdLocusUtil.getAssetsDrawable(mActivity, "btn_closeevent");
         
        drawables.addState(new int[]{android.R.attr.state_checked}, dOn);
        drawables.addState(new int[]{android.R.attr.state_focused}, dOn);
        drawables.addState(new int[]{android.R.attr.state_pressed}, dOn);
        drawables.addState(new int[]{}, dOff);
		btnClose.setBackgroundDrawable(drawables);
		
		final float density = mActivity.getResources().getDisplayMetrics().density;
		final int dip40 = AdLocusUtil.convertToScreenPixels(40, density);
		final int dip5 = AdLocusUtil.convertToScreenPixels(5, density);
		RelativeLayout.LayoutParams btnParams = new RelativeLayout.LayoutParams(dip40, dip40);
		btnParams.topMargin = dip5;
		btnParams.leftMargin = dip5;
		rootLayout.addView(btnClose, btnParams);
		mDialog.setContentView(rootLayout);	
	}
	
	private void openUrl(String url)
	{
		mActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));//用瀏覽器開啟連結
		mDialog.dismiss();
	}
	
	
	public void loadAd()
	{
		synchronized (mIsLoading)
		{
			if(mIsLoading.get())
			{
				return;
			}
			mIsLoading.set(true);
			synchronized (mIsReadyToShow)
			{
				mIsReadyToShow.set(false);
				HxRequest r = new HxRequest(mActivity);
				AdLocusUtil.setRequestParameters(mActivity, r, mKey, AdLocusUtil.getScreen(mActivity, AdLocusLayout.AD_SIZE_IAB_MRECT), mActivity.getLocalClassName(), mAdLocusTargeting);
				r.setPostParameter("fs", "1");
				mWebView.loadUrl(AdLocusUtil.URL_PULL_HTML_REQ + "?" + r.getPostString());				
			}
		}
	}
	
	public void show()
	{
		synchronized (mIsReadyToShow)
		{
			if(!mIsReadyToShow.get())
				return;
			
			//can I show?
			if(mActivity.isFinishing())
				return;
			
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && mActivity.isDestroyed())
				return;			
			
			mDialog.show();
		}
	}

	@Override
	public void setListener(AdListener listener)
	{
		mListener = listener;
	}

}
