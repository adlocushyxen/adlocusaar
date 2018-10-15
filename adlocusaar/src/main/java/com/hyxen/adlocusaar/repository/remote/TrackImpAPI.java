package com.hyxen.adlocusaar.repository.remote;

import com.hyxen.adlocusaar.utils.Logger;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TrackImpAPI extends RemoteAPI {

    private static final String TAG = TrackImpAPI.class.getSimpleName();
    private static final int ERROR_LIMIT = 5;

    private static TrackImpAPI sInstance;
    private static Request mRequest;
    private static OkHttpClient mClient;
    private int mErrorCount;

    private TrackImpAPI() {
        mClient = getOkHttpClient();
    }

    public static TrackImpAPI getInstance() {
        if (sInstance == null || mClient == null) {
            synchronized (TrackImpAPI.class) {
                if (sInstance == null || mClient == null) {
                    sInstance = new TrackImpAPI();
                }
            }
        }

        return sInstance;
    }

    private void release() {
        mClient = null;
        mRequest = null;
        mErrorCount = 0;
    }
    /*--------------------------------------------------------------------------------------------*/
    /* APIs */

    /**
     * Send DCM
     *
     * @param trackUrl
     */
    public void sendTrackImp(final String trackUrl) {
        mRequest = new Request.Builder()
                .url(trackUrl)
                .build();

        if (mClient == null) {
            Logger.w(TAG, "[sendTrackImp] mClient is null");
            return;
        }
        mClient.newCall(mRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Logger.w(TAG, "onFailure" + e.toString());
                mErrorCount++;
                if (mErrorCount < ERROR_LIMIT) {
                    sendTrackImp(trackUrl);
                } else {
                    release();
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Logger.i(TAG, "onSuccess url :" + call.request().url());
                release();
            }
        });
    }
}
