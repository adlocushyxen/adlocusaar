package com.hyxen.adlocusaar.repository.remote;

import android.content.Context;
import android.webkit.WebSettings;

import com.github.aurae.retrofit2.LoganSquareConverterFactory;
import com.hyxen.adlocusaar.AdLocus;

import com.hyxen.adlocusaar.constants.Constants;
import com.hyxen.adlocusaar.repository.data.RemoteResponse;
import com.hyxen.adlocusaar.repository.data.request.CollectionRequest;
import com.hyxen.adlocusaar.repository.data.request.FeedbackRequest;
import com.hyxen.adlocusaar.repository.data.request.GetLbsFileRequest;
import com.hyxen.adlocusaar.repository.data.request.NewAndRequest;
import com.hyxen.adlocusaar.repository.data.request.PushTokenRequest;
import com.hyxen.adlocusaar.repository.data.response.GetCollectionResponse;
import com.hyxen.adlocusaar.repository.data.response.GetLbsFileResponseUrl;
import com.hyxen.adlocusaar.repository.data.response.GetLbsTaskResponse;
import com.hyxen.adlocusaar.repository.data.response.GetNewAndResponse;
import com.hyxen.adlocusaar.repository.data.response.GetNewImpressionResponse;
import com.hyxen.adlocusaar.utils.Logger;
import com.hyxen.adlocusaar.utils.MiscUtils;

import java.io.IOException;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

public class AdLocusAPI extends RemoteAPI {

    private static final String TAG = AdLocusAPI.class.getSimpleName();
    private static final String TYPE_JSON = "application/json; charset=utf-8";

    private static AdLocusAPI sInstance;
    private AdLocusService mService;

    private AdLocusAPI() {
//        String url = BuildConfig.DEBUG ?
//                "http://test.adlocus_api.dev.hxcld.com/" :
//                "todo";

        String url = AdLocus.isDebug() ?
                "https://test.adlocus_api.dev.hxcld.com/" :
                "https://a.api.ad-locus.com/";
        OkHttpClient client = getOkHttpClient();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .client(client)
                .addConverterFactory(LoganSquareConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build();

        mService = retrofit.create(AdLocusService.class);
    }

    public static AdLocusAPI getInstance(Context ctx) {
        if(ctx!=null)init(ctx);
        if (sInstance == null) {
            synchronized (AdLocusAPI.class) {
                if (sInstance == null) {
                    sInstance = new AdLocusAPI();
                }
            }
        }

        return sInstance;
    }
    public static AdLocusAPI getInstance() {
        if (sInstance == null) {
            synchronized (AdLocusAPI.class) {
                if (sInstance == null) {
                    sInstance = new AdLocusAPI();
                }
            }
        }

        return sInstance;
    }

    /*--------------------------------------------------------------------------------------------*/
    /* APIs */

    public Single<RemoteResponse> postPushToken(PushTokenRequest pushTokenRequest) {
        Logger.i(TAG, "[Method] -> postPushToken()");
        String outPut = MiscUtils.toJSONString(pushTokenRequest);
        Logger.d(TAG, "[postPushToken] request = ", outPut);
        RequestBody requestBody = RequestBody.create(MediaType.parse(TYPE_JSON), outPut);
        return mService.postPushToken(requestBody);
    }

    public Single<GetCollectionResponse> postCollection(CollectionRequest request) {
        Logger.i(TAG, "[Method] -> postCollection()");
        String outPut = MiscUtils.toJSONString(request);
        Logger.d(TAG, "[postCollection] request = ", outPut);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(Constants.TAG_DEVICE_ID, request.getDevice_id())
                .addFormDataPart(Constants.TAG_KEY, request.getKey())
                .addFormDataPart(Constants.TAG_PLAIN, request.getPlain())
                .build();
        return mService.postCollection("https://data.adlocus.com/new_log/and_device",requestBody);
//        return mService.postCollection("https://data.adlocus.com/log/device",requestBody);
    }
    public Single<GetNewAndResponse> postNewAnd(NewAndRequest request) {
        Logger.i(TAG, "[Method] -> postNewAnd()");
        String outPut = MiscUtils.toJSONString(request);
        Logger.d(TAG, "[postNewAnd] request = ", outPut);
        if(request.getLat()==null)request.setLat("");
        if(request.getLon()==null)request.setLon("");
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(Constants.TAG_AD_ID, request.getAdId())
                .addFormDataPart(Constants.TAG_CI, request.getCi())
                .addFormDataPart(Constants.TAG_D_AD_ID, request.getdAdId())
                .addFormDataPart(Constants.TAG_DEVICE_ID, request.getDeviceId())
                .addFormDataPart(Constants.TAG_KEY, request.getKey())
                .addFormDataPart(Constants.TAG_LAC, request.getLac())
                .addFormDataPart(Constants.TAG_MAC, request.getMac())
                .addFormDataPart(Constants.TAG_MCC, request.getMcc())
                .addFormDataPart(Constants.TAG_MNC, request.getMnc())
                .addFormDataPart(Constants.TAG_SCREEN, request.getScreen())
                .addFormDataPart(Constants.TAG_SESSION_ID, request.getSessionId())
                .addFormDataPart(Constants.TAG_TEST_MODE, request.getTestmode())
                .addFormDataPart(Constants.TAG_V_STR, request.getvStr())
                .addFormDataPart(Constants.TAG_LAT, request.getLat())
                .addFormDataPart(Constants.TAG_LON, request.getLon())
                .addFormDataPart(Constants.TAG_RSSI, request.getRssi())
                .addFormDataPart(Constants.TAG_CELL_TYPE, request.getCellType())
                .addFormDataPart(Constants.TAG_LOC_TYPE, request.getLocType())

                .build();
        return mService.postNewAnd(requestBody);
    }

    public Single<GetNewImpressionResponse> postNewImpression(NewAndRequest request, String lat, String lon) {
        Logger.i(TAG, "[Method] -> postNewImpression()");
        String outPut = MiscUtils.toJSONString(request);
        Logger.d(TAG, "[postNewImpression] request = ", outPut);
        if(lat==null)lat="";
        if(lon==null)lon="";
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
//                .addFormDataPart(Constants.TAG_DEVICE_ID, request.getDeviceId())
//                .addFormDataPart(Constants.TAG_KEY, request.getKey())
//                .addFormDataPart(Constants.TAG_AD_ID, request.getAdId())
//                .addFormDataPart(Constants.TAG_SESSION_ID, request.getSessionId())

                .addFormDataPart(Constants.TAG_AD_ID, request.getAdId())
                .addFormDataPart(Constants.TAG_CI, request.getCi())
                .addFormDataPart(Constants.TAG_D_AD_ID, request.getdAdId())
                .addFormDataPart(Constants.TAG_DEVICE_ID, request.getDeviceId())
                .addFormDataPart(Constants.TAG_KEY, request.getKey())
                .addFormDataPart(Constants.TAG_LAC, request.getLac())
                .addFormDataPart(Constants.TAG_MAC, request.getMac())
                .addFormDataPart(Constants.TAG_MCC, request.getMcc())
                .addFormDataPart(Constants.TAG_MNC, request.getMnc())
                .addFormDataPart(Constants.TAG_SCREEN, request.getScreen())
                .addFormDataPart(Constants.TAG_SESSION_ID, request.getSessionId())
                .addFormDataPart(Constants.TAG_TEST_MODE, request.getTestmode())
                .addFormDataPart(Constants.TAG_V_STR, request.getvStr())
                .addFormDataPart(Constants.TAG_LAT, lat)
                .addFormDataPart(Constants.TAG_LON, lon)
                .addFormDataPart(Constants.TAG_CELL_TYPE, request.getCellType())
                .addFormDataPart(Constants.TAG_LOC_TYPE, request.getLocType())
                .addFormDataPart(Constants.TAG_RSSI, request.getRssi())
                .build();

//        Logger.d(TAG, "[postNewImpression] requestBody = ", MiscUtils.getRequestBodyToString(requestBody));
        return mService.postNewImpression(requestBody);
    }

    public Single<RemoteResponse> postFeedback(FeedbackRequest request) {
        Logger.i(TAG, "[Method] -> postFeedback()");
        String outPut = MiscUtils.toJSONString(request);
        Logger.d(TAG, "[postFeedback] request = ", outPut);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(Constants.TAG_AD_ID, request.getAdId())
                .addFormDataPart(Constants.TAG_APP_KEY, request.getAppkey())
                .addFormDataPart(Constants.TAG_DEVICE_ID, request.getDeviceId())
                .addFormDataPart(Constants.TAG_TYPE, request.getType())
                .build();
        return mService.postFeedback(requestBody);
    }

    public Single<GetLbsFileResponseUrl> getLbsFileUrl(GetLbsFileRequest request) {
        Logger.i(TAG, "[Method] -> getLbsFileUrl()");
        String outPut = MiscUtils.toJSONString(request);
        Logger.d(TAG, "[getLbsFileUrl] request = ", outPut);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(Constants.TAG_DEVICE_ID, request.getDeviceId())
                .addFormDataPart(Constants.TAG_KEY, request.getKey())
                .addFormDataPart(Constants.TAG_MCC, request.getMcc())
                .addFormDataPart(Constants.TAG_MNC, request.getMnc())
                .addFormDataPart(Constants.TAG_LAC, request.getLac())
                .addFormDataPart(Constants.TAG_CI, request.getCi())
                .addFormDataPart(Constants.TAG_DEVICE_TYPE, request.getDevType())
                .build();
        return mService.getLbsFileUrl(requestBody);
    }

    public Single<GetLbsTaskResponse> getLbsTask(GetLbsFileRequest request) {
        Logger.i(TAG, "[Method] -> getLbsTask()");
        String outPut = MiscUtils.toJSONString(request);
        Logger.d(TAG, "[getLbsTask] request = ", outPut);
        RequestBody requestBody = RequestBody.create(MediaType.parse(TYPE_JSON), outPut);
        return mService.getLbsTask(requestBody);
    }
}
