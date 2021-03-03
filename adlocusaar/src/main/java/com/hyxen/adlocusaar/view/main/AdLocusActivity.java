package com.hyxen.adlocusaar.view.main;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslCertificate;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.hyxen.adlocusaar.R;
import com.hyxen.adlocusaar.constants.Constants;
import com.hyxen.adlocusaar.repository.Repository;
import com.hyxen.adlocusaar.repository.data.response.GetNewAndResponse;
import com.hyxen.adlocusaar.repository.remote.TrackImpAPI;
import com.hyxen.adlocusaar.utils.Logger;
import com.hyxen.adlocusaar.view.BaseActivity;

import java.net.URISyntaxException;

public class AdLocusActivity extends BaseActivity implements AdLocusContract.View, DialogInterface.OnDismissListener {
    public static final String TAG = AdLocusActivity.class.getSimpleName();

    private AdLocusContract.Presenter mPresenter;
    private AlertDialog.Builder mSettingDialogBuilder;
    private AlertDialog mSettingDialog;
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_locas);
        mWebView = findViewById(R.id.webView);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);

        Repository.init(this);
        mPresenter = new AdLocusPresenter(this);
        processIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        processIntent(intent);
    }

    @Override
    public void showUrlBrowser(String url) {
//        try {
//            if(url.startsWith("intent") && url.indexOf("scheme=line;")>0){
//                Intent iuri = Intent.parseUri(url, 0);
//                startActivity(iuri);
//                finish();
//            }else if(url.startsWith("http")){
//                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                startActivity(webIntent);
//                finish();
//            }
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        }

        mWebView.setVisibility(View.VISIBLE);
        mWebView.loadUrl(url);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                try {
                    if(request.getUrl().toString().startsWith("intent") && request.getUrl().toString().indexOf("scheme=line;")>0){
                        Intent iuri = Intent.parseUri(request.getUrl().toString(), 0);
                        startActivity(iuri);
                        finish();
                        return true;
                    }
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                try {
                    if(url.startsWith("intent") && url.indexOf("scheme=line;")>0){
                        Intent iuri = Intent.parseUri(url, 0);
                        startActivity(iuri);
                        finish();
                        return true;
                    }
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
            @Override
            public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
                SslCertificate sslCertificate = error.getCertificate();

                final AlertDialog.Builder builder = new AlertDialog.Builder(AdLocusActivity.this);
                builder.setTitle(R.string.ssl_error);
                builder.setMessage (R.string.is_ssl_error_continue);
                builder.setPositiveButton(R.string.ssl_error_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.proceed();
                    }
                });
//                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        handler.cancel();
//                    }
//                });

                final AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    @Override
    public void showSettingDialog(View view) {
        if (mSettingDialogBuilder == null)
            mSettingDialogBuilder = new AlertDialog.Builder(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mSettingDialog = mSettingDialogBuilder
                    .setView(view)
                    .setOnDismissListener(this)
                    .show();
        }
    }

    @Override
    public void closeDialog() {
        if (mSettingDialog == null) {
            Logger.e(TAG, "[closeDialog] mSettingDialog is Setting");
            return;
        }
        mSettingDialog.dismiss();
    }

    @Override
    protected void onDestroy() {
        Logger.i(TAG, "[Method] -> onDestroy");
        release();
        super.onDestroy();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        finish();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (mWebView.canGoBack()) {
                        mWebView.goBack();
                    } else {
                        switchPageToApp();
                    }
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void finish() {
        Logger.d(TAG, "finish");
        super.finish();
    }

    /**
     * Intent entry point
     *
     * @param intent
     */
    private void processIntent(Intent intent) {
        if (intent == null) {
            finish();
            return;
        }

        final String action = intent.getAction();
        if (action == null) {
            finish();
            return;
        }

        //Sent DCM
//leo3x 2018/12/22 上午 09:45 track_imp move to AdLocusNotification
//        String trackImpUrl = intent.getStringExtra(Constants.TAG_INTENT_KEY_TRACK_IMP);
//        if (!TextUtils.isEmpty(trackImpUrl)) {
//            TrackImpAPI.getInstance().sendTrackImp(trackImpUrl);
//        }

        //open url or big view event
        if (AdLocusContract.ACTION_CLICK.equals(action)) {
            Logger.i(TAG, "[processIntent] action is url");
            String url = intent.getStringExtra(Constants.TAG_INTENT_URL);
            int id = intent.getIntExtra(Constants.TAG_INTENT_ID, -1);
            mPresenter.setUrlBrowser(url);
        } else if (AdLocusContract.ACTION_BIG_VIEW_INTENT.equals(action)) {
            GetNewAndResponse adInfo = intent.getParcelableExtra(Constants.TAG_INTENT_SHARE_DATA);
            String type = intent.getStringExtra(Constants.TAG_INTENT_KEY_TYPE);
            if (adInfo == null) {
                Logger.i(TAG, "[processIntent] adInfo is null");
                return;
            }

            if (TextUtils.equals(type, Constants.TAG_INTENT_SETTING)) {
                Logger.i(TAG, "[processIntent] action is Setting");
                mPresenter.setSettingEvent();
            } else if (TextUtils.equals(type, Constants.TAG_INTENT_SHARE)) {
                Logger.i(TAG, "[processIntent] action is Share");
                mPresenter.setShareEvent(adInfo);
            } else {
                Logger.i(TAG, "[processIntent] action is not belong any case");
                finish();
            }
            mPresenter.doFeedback(type, adInfo.getAdId());
        } else {
            finish();
        }
    }

    /**
     * Release reference at onDestroy
     */
    private void release() {
        mSettingDialogBuilder = null;
        mSettingDialog = null;
    }

    /**
     * Go to App
     */
    private void switchPageToApp() {
        Logger.d(TAG, "switchPageToApp");
        Intent i = getPackageManager().getLaunchIntentForPackage(Repository.getAppPackageMame());
        if (i != null) {
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }
    }
}
