package com.hyxen.adlocusaar;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.hyxen.adlocusaar.PushAd.OnCloseListener;
import com.hyxen.adlocusaar.obj.AdLocusAd;
import com.hyxen.adlocusaar.obj.AdWebView;
import com.hyxen.adlocusaar.push.BigView;
import com.hyxen.adlocusaar.util.AdLocusNotification;
import com.hyxen.adlocusaar.util.AdLocusUtil;

public class AdActivity extends Activity //NEWPUSH
{

	private static final int PERMISSIONS_REQUEST_CODE = 1;
	public static final String ACTION_CLICK = "com.hyxen.adlocusaar.push.action.CLICK";
	public static final String ACTION_REQUEST_PERMISSION = "com.hyxen.adlocusaar.push.action.REQUEST_PERMISSION";
	public static final String ACTION_BIGVIEW_INTENT = "com.hyxen.adlocusaar.push.action.BIGVIEW_INTENT";
	private AdLocusAd mAdLocusAd;
	private WebView mWebView;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		loadIntent();
	}



	@Override
	protected void onResume() {
		super.onResume();

		final Intent intent = getIntent();
		if(intent == null) return;

		final String action = intent.getAction();
		if (action == null) return;

		if (ACTION_CLICK.equals(action)) {
			final String url = intent.getStringExtra("url");
			final int id = intent.getIntExtra("id", -1);
			intent.removeExtra("url");
			intent.removeExtra("id");
			if (url != null)  {
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						try {
							startActivity(intent);
							AdLocusNotification.cancelNotification(AdActivity.this, id);
						} catch (Exception ignored) {
							ignored.printStackTrace();
						}

						finish();
					}
				}, 500);
			} else {
				finish();
			}
		} else if(ACTION_BIGVIEW_INTENT.equals(action)) {

			String feedback = intent.getStringExtra(BigView.Type.key());

			AdLocusAd ad = intent.getParcelableExtra("adInfo");

			if (!ad.isBigView()) return;

			if (feedback != null && !"".equals(feedback)) {
				BigView.Type type = BigView.Type.valueOf(feedback);
				switch (type) {
					case Setting:
						PushAd.showSettingPage(AdActivity.this, AdLocusUtil.getPushKey(AdActivity.this), new OnCloseListener() {

							@Override
							public void onClose() {
								finish();
							}
						});
						break;
					case Share:
						BigView.shareAd(this, ad);
						finish();
						break;
					default:
						finish();
						break;
				}
				BigView.doFeedback(this, type, ad);
			}
		} else if(ACTION_REQUEST_PERMISSION.equals(action)) {
			checkPermission();
		}
	}

	private void loadIntent()
	{
		final Intent intent = getIntent();
		if(intent == null) return;

		final String action = intent.getAction();

		if(ACTION_BIGVIEW_INTENT.equals(action) || ACTION_CLICK.equals(action) || ACTION_REQUEST_PERMISSION.equals(action)) {
		} else {

			mAdLocusAd = intent.getParcelableExtra("adInfo");
			intent.removeExtra("adInfo");
			if(mAdLocusAd == null)
			{
				finish();
			} else
			{
				initLayout();
			}
		}
	}

	private void checkPermission(){
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

			// Should we show an explanation?
			if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
				AlertDialog.Builder b = new AlertDialog.Builder(this);
				b.setTitle("取用位置")
						.setMessage("提供合適的優惠通知。")
						.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								ActivityCompat.requestPermissions(AdActivity.this,
										new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
										PERMISSIONS_REQUEST_CODE);
							}
						})
						.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								finish();
							}
						})
						.show();
			} else {
				ActivityCompat.requestPermissions(this,
						new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
						PERMISSIONS_REQUEST_CODE);
			}
		} else {
			finish();
		}
	}

	private void initLayout()
	{
		final LinearLayout rootLayout = new LinearLayout(this);
		rootLayout.setBackgroundColor(0xc0000000);
		rootLayout.setGravity(Gravity.CENTER);
		rootLayout.setOrientation(LinearLayout.VERTICAL);
		
		LinearLayout ll = new LinearLayout(this);
		ll.setGravity(Gravity.CENTER);
		ll.setBackgroundColor(Color.BLACK);
		ProgressBar progress = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);

		ll.addView(progress);
		
		final float density = getResources().getDisplayMetrics().density;
		final int dp300 = (int) (300 * density);
		final int dp250 = (int) (250 * density);
		final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp300, dp250);

		mWebView = new AdWebView(this);
		mWebView.setVisibility(View.INVISIBLE);
		mWebView.setWebChromeClient(new WebChromeClient()
		{
			@Override
			public void onProgressChanged(WebView view, int newProgress)
			{
				super.onProgressChanged(view, newProgress);
				if(newProgress == 100)
				{
					rootLayout.removeViewAt(0);
					rootLayout.addView(mWebView, 0, lp);
					mWebView.setVisibility(View.VISIBLE);
					AnimationSet set = new AnimationSet(true);
					Animation animation = new AlphaAnimation(0.0f, 1.0f);
					animation.setDuration(500);
					set.addAnimation(animation);
					mWebView.setAnimation(set);
//				    view.loadUrl("javascript:var scale = " + dp300 + " / document.body.scrollWidth; document.body.style.zoom = scale;");
				}
			}
		});
		mWebView.setWebViewClient(new WebViewClient()
		{

			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				super.onReceivedError(view, errorCode, description, failingUrl);
				AdLocusNotification.showNotification(AdActivity.this, mAdLocusAd);
				Toast.makeText(AdActivity.this, "暫時無法取得資料，請稍候在試", Toast.LENGTH_SHORT).show();
				finish();
			}

		});

		mWebView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//todo: replace by
				//openAdContent?

				finish();

				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mAdLocusAd.link));
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		});
		
		mWebView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP)
				{
					openAdContent();
				}
				return true;
			}
		});
		
		
		rootLayout.addView(ll, lp);

		final int dip10 = AdLocusUtil.convertToScreenPixels(10, density);
		//LinearLayout
		{
			final int dip145 = AdLocusUtil.convertToScreenPixels(145, density);
			final int dip50 = AdLocusUtil.convertToScreenPixels(50, density);
			final int dip1 = AdLocusUtil.convertToScreenPixels(1, density);
			Button btnSetting = new Button(this);
			btnSetting.setText("設  定");
			btnSetting.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
			btnSetting.setTextColor(Color.WHITE);
			btnSetting.setPadding(0, dip1, 0, 0);
			btnSetting.setBackgroundDrawable(getButtonBackgroundDrawable());
			
			btnSetting.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					PushAd.showSettingPage(AdActivity.this, AdLocusUtil.getPushKey(AdActivity.this));
				}
			});
			LinearLayout.LayoutParams btnSettingParams = new LinearLayout.LayoutParams(dip145, dip50);
			

			Button btnOpen = new Button(this);
			btnOpen.setText("觀  看");
			btnOpen.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
			btnOpen.setTextColor(Color.WHITE);
			btnOpen.setPadding(0, dip1, 0, 0);
			btnOpen.setBackgroundDrawable(getButtonBackgroundDrawable());
			
			btnOpen.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					openAdContent();
				}
			});

			LinearLayout.LayoutParams btnbtnOpenParams = new LinearLayout.LayoutParams(dip145, dip50);
			btnbtnOpenParams.leftMargin = dip10;
			
			
			LinearLayout btnLayout = new LinearLayout(this);
			LinearLayout.LayoutParams lpBl = new LinearLayout.LayoutParams(dp300, LayoutParams.WRAP_CONTENT);
			lpBl.topMargin = dip10;
			btnLayout.addView(btnSetting, btnSettingParams);
			btnLayout.addView(btnOpen, btnbtnOpenParams);
			rootLayout.addView(btnLayout, lpBl);
		}

		Button btnClose = new Button(this);
		btnClose.setPaintFlags(btnClose.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
		btnClose.setText("關閉");
		int[][] states = new int[][] {
			    new int[] { android.R.attr.state_focused}, // unchecked
			    new int[] { android.R.attr.state_pressed},  // pressed
			    new int[] {}  // default
			};

			int[] colors = new int[] {
				0xffcf121b,
			    0xffcf121b,
			    Color.WHITE
			};
		ColorStateList csl = new ColorStateList(states, colors);
		btnClose.setTextColor(csl);
		btnClose.setBackgroundColor(Color.TRANSPARENT);
		btnClose.setPadding(dip10, dip10, dip10, dip10);
		
		btnClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		LinearLayout.LayoutParams btnCloseParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		btnCloseParams.topMargin = dip10;
		rootLayout.addView(btnClose, btnCloseParams);


		setContentView(rootLayout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
		
		AnimationSet set = new AnimationSet(true);
		Animation animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(700);
		set.addAnimation(animation);
		rootLayout.setAnimation(set);

		mWebView.loadUrl(mAdLocusAd.scad_url);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		switch (requestCode) {
			case PERMISSIONS_REQUEST_CODE: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					AdLocusUtil.setPause(this, false);
				} else {
					AdLocusUtil.setPause(this, true);
				}
				finish();
			}

		}
	}

	private Drawable getButtonBackgroundDrawable()
	{
        StateListDrawable drawables = new StateListDrawable();
        Drawable dOn = new ColorDrawable(Color.parseColor("#cf121b"));
        Drawable dOff = new ColorDrawable(Color.BLACK);
        drawables.addState(new int[]{android.R.attr.state_checked}, dOn);
        drawables.addState(new int[]{android.R.attr.state_focused}, dOn);
        drawables.addState(new int[]{android.R.attr.state_pressed}, dOn);
        drawables.addState(new int[]{}, dOff);
        return drawables;
	}
	
	private void openAdContent()
	{
		finish();

		//todo, open in app, read parameter from link
		//ex: if(mAdLocusAd.link.contanis("oip=1");
		
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mAdLocusAd.link));
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
	}
	

	
}
