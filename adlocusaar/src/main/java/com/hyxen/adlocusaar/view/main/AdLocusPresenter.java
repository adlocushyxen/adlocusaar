package com.hyxen.adlocusaar.view.main;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import com.hyxen.adlocusaar.BuildConfig;
import com.hyxen.adlocusaar.R;
import com.hyxen.adlocusaar.repository.Repository;
import com.hyxen.adlocusaar.repository.data.RemoteResponse;
import com.hyxen.adlocusaar.repository.data.request.FeedbackRequest;
import com.hyxen.adlocusaar.repository.data.response.GetNewAndResponse;
import com.hyxen.adlocusaar.utils.AdLocusUtil;
import com.hyxen.adlocusaar.utils.Logger;
import com.hyxen.adlocusaar.view.BasePresenter;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class AdLocusPresenter extends BasePresenter implements AdLocusContract.Presenter {
    private static final String TAG = AdLocusPresenter.class.getSimpleName();
    private static final String TAG_SETTING_URL = BuildConfig.DEBUG ?
            "http://test.adlocus_api.dev.hxcld.com/pref/set/?device_id=%1$s&app_key=%2$s" :
            "http://user.ad-locus.com/pref/set/?device_id=%1$s&app_key=%2$s";
//private static final String TAG_SETTING_URL = "http://user.ad-locus.com/pref/set/?device_id=%1$s&app_key=%2$s";
    private AdLocusContract.View mView;
    private WebView mWebView;

    public AdLocusPresenter(AdLocusContract.View view) {
        mView = view;
    }

    @Override
    public void setUrlBrowser(String url) {
        mView.showUrlBrowser(url);
    }

    @Override
    public void setSettingEvent() {
        Context context = mView.getContext();
        String appKey = Repository.getAppKey();
        if (TextUtils.isEmpty(appKey)) {
            Logger.e(TAG, "[setSettingEvent] appKey is empty");
            return;
        }


        mView.showSettingDialog(getSettingLayout(context, appKey));
    }

    @Override
    public void setShareEvent(GetNewAndResponse adInfo) {
        if (adInfo == null) {
            Logger.e(TAG, "[GetNewAndResponse] adInfo is empty");
            return;
        }

        Context context = mView.getContext();
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TITLE, adInfo.getAdTitle());
        i.putExtra(Intent.EXTRA_SUBJECT, adInfo.getAdTitle());
        i.putExtra(Intent.EXTRA_TEXT, String.format("%1$s %2$s", adInfo.getAdBody(), adInfo.getBvShareLink()));
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            context.startActivity(i);
        } catch (android.content.ActivityNotFoundException ignored) {
        }

        mView.finish();
        context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
    }

    @Override
    public void doFeedback(String type, String adId) {
        FeedbackRequest request = new FeedbackRequest();
        request.setAdId(adId);
        request.setAppkey(Repository.getAppKey());
        request.setDeviceId(Repository.getHashDeviceId());
        request.setType(type);

        Disposable task = Repository.postFeedback(request)
                .subscribe(new Consumer<RemoteResponse>() {
                    @Override
                    public void accept(RemoteResponse remoteResponse) throws Exception {
                        Logger.i(TAG, "[postFeedback] success");
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Logger.e(TAG, "[postFeedback] failed: " + throwable.getMessage());
                    }
                });

        addTask(task);
    }

    @Override
    public void release() {
        mWebView = null;
        super.release();
    }

    /**
     * Get Web view
     *
     * @param context
     * @param appKey
     * @return
     */
    private View getSettingLayout(Context context, String appKey) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view;
        Button button;
        final TextView loading;

        if (inflater == null) {
            Logger.e(TAG, "[getSettingLayout] inflater is empty");
            return null;
        }

        view = inflater.inflate(R.layout.dialog_big_view_setting, null);
        loading = view.findViewById(R.id.textView_state);
        button = view.findViewById(R.id.button_close);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mView.closeDialog();
            }
        });

        if (mWebView == null)
            mWebView = view.findViewById(R.id.webView);
        String url = String.format(TAG_SETTING_URL, AdLocusUtil.getEncodeDeviceId(context), appKey);
        Logger.i(TAG, "Setting url = " + url);
        WebSettings settings = mWebView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setSupportZoom(false);
        settings.setAllowFileAccess(true);
        settings.setUseWideViewPort(false);
        mWebView.loadUrl(url);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return super.shouldOverrideUrlLoading(view, url);
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(String.valueOf(request.getUrl()));
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                loading.setVisibility(View.GONE);
                super.onPageFinished(view, url);
            }
        });
        return view;
    }

}
