package com.hyxen.adlocusaar.obj;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebSettings.PluginState;

public class AdWebView extends WebView
{

	public AdWebView(Context context)
	{
		super(context);
		init();
	}

	public AdWebView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	public AdWebView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
        invalidate();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
	
	private void init()
	{
		setBackgroundColor(0x00000000);
		setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
//		webView.setScrollbarFadingEnabled(false);
		setVerticalScrollBarEnabled(false);
		setHorizontalScrollBarEnabled(false);
		setScrollContainer(false);

        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
        {
    		setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
		
		WebSettings webSettings = getSettings();  
		webSettings.setJavaScriptEnabled(true);
		webSettings.setSupportZoom(false);
		webSettings.setAllowFileAccess(true);
		webSettings.setUseWideViewPort(false);
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) 
		{
			webSettings.setAllowUniversalAccessFromFileURLs(true);
		}
		
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO)
        {
    		webSettings.setPluginState(PluginState.ON);
        }
        
	}

}
