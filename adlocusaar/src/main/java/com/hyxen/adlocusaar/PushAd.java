package com.hyxen.adlocusaar;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hyxen.adlocusaar.push.Collection;
import com.hyxen.adlocusaar.push.PushService;
import com.hyxen.adlocusaar.push.ServiceUtil;
import com.hyxen.adlocusaar.util.AdLocusUtil;

public class PushAd
{
//    public static void enablePush(final Context context, final String appKey, Intent backgroundIntent)
//    {
//        init(context, appKey, null, backgroundIntent);
//    }
//
//    public static void enablePush(final Context context, final String appKey, Intent backgroundIntent, AdLocusTargeting adLocusTargeting)
//    {
//        init(context, appKey, adLocusTargeting, backgroundIntent);
//    }
//
//    public static void enablePush(final Context context, final String appKey)
//    {
//        init(context, appKey, null, null);
//    }
//
//    public static void enablePush(final Context context, final String appKey, AdLocusTargeting adLocusTargeting)
//    {
//        init(context, appKey, adLocusTargeting, null);
//    }
    
//    private static void init(final Context context, final String appKey, AdLocusTargeting adLocusTargeting, Intent backgroundIntent)
//    {
//        AdLocusUtil.setNewPushBackgroundIntent(context, backgroundIntent);
//        AdLocusUtil.setPushTargeting(context, adLocusTargeting);
//        AdLocusUtil.setPushKey(context, appKey);
//        Collection.report(context);
//        AdLocusUtil.auth(context, appKey, new AdLocusUtil.AuthListener() {
//
//            @Override
//            public void onChecked(int err) {
//                if (err == 0) {
//                    ServiceUtil.sendCheckService(context, appKey);
//                } else {
//                    ServiceUtil.saveValidKey(context, null);
//                }
//            }
//        });
//        AdLocusUtil.checkSelfPermission(context);
//    }
//
//
//    public static void disablePush(Context context)
//    {
//        AdLocusUtil.setPushKey(context, null);
//        ServiceUtil.saveValidKey(context, null);
//        ServiceUtil.sendCheckService(context, null);
//    }

    public static void test(Context context)
    {
        Intent i = new Intent(context, PushService.class);
        i.setAction(PushService.ACTION_TEST_PUSH);
        context.startService(i);
    }

    public static void showSettingPage(Activity activity, String appKey)
    {
    	showSettingPage(activity, appKey, null);
    }

    static void showSettingPage(Activity activity, String appKey, final OnCloseListener listener)
    {
        final Dialog dialog = new Dialog(activity, AdLocusUtil.getResId("style", "AdLocusDialogTheme"));
        final RelativeLayout rootLayout = new RelativeLayout(activity);
        final TextView tv = new TextView(activity);
        tv.setText("讀取中...");
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        final WebView webView = new WebView(activity)
        {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
            {
                invalidate();
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        }
        ;
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
        {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        
        WebSettings webSettings = webView.getSettings();  
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(false);
        webSettings.setAllowFileAccess(true);
        webSettings.setUseWideViewPort(false);
        
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                return false;
            }
            public void onPageFinished(WebView view, String url)
            {
                rootLayout.removeView(tv);
            }
        });

        if(android.os.Build.VERSION.SDK_INT > 7)
        {
            webSettings.setPluginState(PluginState.ON);
        }
        RelativeLayout.LayoutParams webParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        rootLayout.addView(webView, webParams);
        
        Button btnClose = new Button(activity);
        btnClose.setOnClickListener(new OnClickListener()
        {
            
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				if (listener != null) {
					listener.onClose();
				}
			}
		});

        StateListDrawable drawables = new StateListDrawable();
         
        Drawable dOn = AdLocusUtil.getAssetsDrawable(activity, "btn_closeevent_click");
        Drawable dOff = AdLocusUtil.getAssetsDrawable(activity, "btn_closeevent_click");
         
        drawables.addState(new int[]{android.R.attr.state_checked}, dOn);
        drawables.addState(new int[]{android.R.attr.state_focused}, dOn);
        drawables.addState(new int[]{android.R.attr.state_pressed}, dOn);
        drawables.addState(new int[]{}, dOff);
        btnClose.setBackgroundDrawable(drawables);
        final float density = activity.getResources().getDisplayMetrics().density;
        final int dip40 = AdLocusUtil.convertToScreenPixels(40, density);
        final int dip5 = AdLocusUtil.convertToScreenPixels(5, density);
        RelativeLayout.LayoutParams btnParams = new RelativeLayout.LayoutParams(dip40, dip40);
        btnParams.topMargin = dip5;
        btnParams.leftMargin = dip5;
        rootLayout.addView(btnClose, btnParams);
        rootLayout.addView(tv, lp);
        dialog.setContentView(rootLayout);
        
//        webView.loadUrl(String.format("https://user.ad-locus.com/pref/set?device_id=%s&app_key=%s", AdLocusUtil.getEncodedDeviceId(activity), appKey));
        webView.loadUrl(String.format("https://user.ad-locus.com/pref/set?device_id=%s&app_key=%s", UserBaseData.getHashDeviceId(activity), appKey));
        dialog.show();
    }
    
    interface OnCloseListener
    {
    	void onClose();
    }
}
