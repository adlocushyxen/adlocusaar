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

package com.hyxen.adlocusaar.adapters;

import com.hyxen.adlocusaar.AdListener;
import com.hyxen.adlocusaar.AdLocusLayout;
import com.hyxen.adlocusaar.AdLocusLayout.ErrorCode;
import com.hyxen.adlocusaar.net.HxRequest;
import com.hyxen.adlocusaar.net.RequestListener;
import com.hyxen.adlocusaar.obj.AdWebView;
import com.hyxen.adlocusaar.util.AdLocusUtil;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class AdLocusAdapter
{
	private static final String JAVASCRIPT_DELAY = "javascript:window.HtmlViewer.showHTML(document.getElementsByName('second')[0].getAttribute('content'));";
	protected final WeakReference<AdLocusLayout> proMeLayoutReference;

	private WebView mWebView;
	public AdLocusAdapter(AdLocusLayout layout)
	{
		this.proMeLayoutReference = new WeakReference<>(layout);
		initWebView(layout);
	}
	
	@SuppressLint("JavascriptInterface")
	private void initWebView(final AdLocusLayout layout)
	{

		Activity activity = layout.activityReference.get();
		if (activity == null) {
			return;
		}
		final WebView webView = new AdWebView(activity);

        webView.addJavascriptInterface(new MyJavaScriptInterface(layout), "HtmlViewer");
		webView.removeJavascriptInterface("searchBoxJavaBridge_");
		webView.removeJavascriptInterface("accessibility");
		webView.removeJavascriptInterface("accessibilityTraversal");
		webView.getSettings().setAllowFileAccess(false);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			webView.getSettings().setAllowFileAccessFromFileURLs(false);
			webView.getSettings().setAllowUniversalAccessFromFileURLs(false);
		}


		final AtomicBoolean isRotate = new AtomicBoolean(false);
        webView.setWebChromeClient(new WebChromeClient()
        {
			@Override
			public void onProgressChanged(WebView view, int newProgress)
			{
				super.onProgressChanged(view, newProgress);
				if(newProgress == 100)
				{
					synchronized (isRotate)
					{
						if(isRotate.get())
						{
							return;
						}
						isRotate.set(true);
					}
					layout.handler.post(new DisplayRunnable(AdLocusAdapter.this));
					webView.loadUrl(JAVASCRIPT_DELAY);
				}
			}
        });
		webView.setWebViewClient(new WebViewClient()
		{
			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
			{
				if(JAVASCRIPT_DELAY.equals(failingUrl))
				{
					layout.rotateThreadedDelayed(0);
				}
				else
				{
					view.setVisibility(View.GONE);
					synchronized (isRotate)
					{
						if (!isRotate.get())
						{
							isRotate.set(true);
							layout.rotateThreadedNow();
						}
					}
				}
				super.onReceivedError(view, errorCode, description, failingUrl);
			}


			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url)
			{
				if((url.startsWith("http://") || url.startsWith("https://") || url.contains(AdLocusUtil.REDIRECTS_CHECK_URL)) || url.startsWith("tel"))
				{
					Activity activity = layout.activityReference.get();
			        // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
					if(activity != null)
					{
				        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				        activity.startActivity(intent);
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
							layout.rotateThreadedNow();
						}
					}
					AdListener listener = layout.getListener();
					if (listener != null)
					{
						listener.onFailedToReceiveAd(layout, ErrorCode.NETWORK_ERROR);
					}
					return true;
				}
			}
		});
		mWebView = webView;
	}

	public void handle()
	{
		AdLocusLayout layout = proMeLayoutReference.get();
		
		if (layout == null) //listener?
			return;
		
		final String url = layout.adLocusManager.getWebLink();
		Activity activity = layout.activityReference.get();
		
		HxRequest r = new HxRequest(activity, url);

		r.setListener(new RequestListener()
		{
			@Override
			public void processContent(int errorCode, final String content)
			{
				AdLocusLayout layout = proMeLayoutReference.get();
				
				if (layout == null) 
					return;
				
				AdListener mListener = layout.getListener();
				
				if (errorCode == HttpURLConnection.HTTP_OK)
				{
					layout.handler.post(new Runnable()
					{
						@Override
						public void run()
						{
							//AdLocusUtil.URL_PULL_HTML_REQ
							//"http://api.ad-locus.com/dev_html5/imp"
							mWebView.loadDataWithBaseURL("https://api.ad-locus.com/", content, "text/html", "utf-8", url);
						}
					});
				}
				else
				{
					if (mListener != null)
					{
						switch (errorCode)
						{
							case 404:
								mListener.onFailedToReceiveAd(layout, ErrorCode.NO_FILL);
								break;
							case 403:
								mListener.onFailedToReceiveAd(layout, ErrorCode.INVALID_KEY);
								break;
							default:
								if (errorCode >= 500)
								{
									mListener.onFailedToReceiveAd(layout, ErrorCode.SERVICE_ERROR);
								}
								else
								{
									mListener.onFailedToReceiveAd(layout, ErrorCode.NETWORK_ERROR);
								}
								break;
						}
					}
				}
			}
		});
		
		layout.scheduler.schedule(r, 0, TimeUnit.SECONDS);
	}

	public void display()
	{
		AdLocusLayout proMeLayout = proMeLayoutReference.get();
		if (proMeLayout == null)
		{
			return;
		}

		Activity activity = proMeLayout.activityReference.get();
		if (activity == null)
		{
			return;
		}
		View v = mWebView;
		if(v != null)
		{
			proMeLayout.pushSubView(v);
		}
		else
		{
			proMeLayout.rotateThreadedNow();
		}
	}

    class MyJavaScriptInterface {

        private final AdLocusLayout adLocusLayout;

        MyJavaScriptInterface(AdLocusLayout adLocusLayout) {
            this.adLocusLayout = adLocusLayout;
        }

        public void showHTML(String second)
        {
        	int sec = 0;
        	try
			{
        		sec = Integer.valueOf(second);
			}
			catch (Exception ignored)
			{
			}
        	adLocusLayout.rotateThreadedDelayed(sec);
        }

    }
	// Added to tell adapter that it's view will be destroyed.
	public void willDestroy()
	{
		mWebView.stopLoading();
		mWebView.loadUrl("about:blank");
		mWebView.removeAllViews();
		mWebView.destroy();
	}

	private static class DisplayRunnable implements Runnable
	{
		private final AdLocusAdapter proMeAdapter;

		public DisplayRunnable(AdLocusAdapter proMeAdapter)
		{
			this.proMeAdapter = proMeAdapter;
		}

		public void run()
		{
			proMeAdapter.display();
		}
	}
}
