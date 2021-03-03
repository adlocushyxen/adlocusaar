package com.hyxen.adlocusaar.utils;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Pair;

import com.hyxen.adlocusaar.AdLocus;
import com.hyxen.adlocusaar.constants.ApiStatus;
import com.hyxen.adlocusaar.constants.Constants;
import com.hyxen.adlocusaar.repository.ApiException;
import com.hyxen.adlocusaar.repository.Repository;
import com.hyxen.adlocusaar.repository.data.domain.LbsTask;
import com.hyxen.adlocusaar.repository.data.domain.LbsTaskLlr;
import com.hyxen.adlocusaar.repository.data.request.NewAndRequest;
import com.hyxen.adlocusaar.repository.data.response.GetLbsTaskResponse;
import com.hyxen.adlocusaar.repository.data.response.GetNewAndResponse;
import com.hyxen.adlocusaar.repository.data.response.GetNewImpressionResponse;
import com.hyxen.adlocusaar.utils.notification.AdLocusNotification;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiPredicate;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class MyLocationListener implements LocationListener {
    private static final String TAG = MyLocationListener.class.getSimpleName();

    private static MyLocationListener mInstance;
    private static GetLbsTaskResponse mTaskItems;
    private static WeakReference<Context> mContextRef;

    private NewAndRequest mRequest = new NewAndRequest();
    private GetNewAndResponse mResponse;

    private static final int TAG_API_IMPRESSION = 10000; //API Impression tag
    private static final int TAG_ERROR_NORMAL = 20000; // normal error tag
    private static final int TAG_ERROR_LOCATION = 20001; // location error tag

    private static final int TAG_TIMEOUT_RETRY_TIMES = 5; //timeout retry次數

    private Set<String> mCacheAdIDList = new HashSet<>();//用來記錄此次query api 失敗的AD ID
    private String mCurrentAdId = "";//紀錄目前query的AD ID
    private int mCurrentAPI = -1; //紀錄目前api
    private boolean mAPIRunning; //api flow in running

    private static GPSCallbackListener mListener;
    private static GPSOnChangeListener mLocationListener;

    public interface GPSCallbackListener {
        void end();
    }
    public interface GPSOnChangeListener {
        void onLocationChanged(double lat,double lon);
    }

    public static MyLocationListener getInstance(@NonNull GetLbsTaskResponse lbsTaskResponse, Context context, GPSCallbackListener listener) {
        if (mInstance == null)
            mInstance = new MyLocationListener();

        if (mContextRef == null)
            mContextRef = new WeakReference<>(context);

        mListener = listener;
        mTaskItems = lbsTaskResponse;
        return mInstance;
    }
    public static MyLocationListener getInstance(@NonNull GetLbsTaskResponse lbsTaskResponse, Context context ) {
        if (mInstance == null)
            mInstance = new MyLocationListener();

        if (mContextRef == null)
            mContextRef = new WeakReference<>(context);

        mTaskItems = lbsTaskResponse;
        return mInstance;
    }
    public static MyLocationListener getInstance( Context context) {
        if (mInstance == null)
            mInstance = new MyLocationListener();

        if (mContextRef == null)
            mContextRef = new WeakReference<>(context);

        return mInstance;
    }
    public static void setGPSCallbackListener(GPSCallbackListener listener){
        mListener = listener;
    }

    public static void setmLocationListener(GPSOnChangeListener _mLocationListener) {
        mLocationListener = _mLocationListener;
    }

    @Override
    public void onLocationChanged(Location location) {
        Logger.i(TAG, "[Method] -> onLocationChanged,"+mAPIRunning+","+(mLocationListener!=null));
//        if (mTaskItems == null) {
//            Logger.e(TAG, "mTaskItem is null");
//            return;
//        }

        double lat = location.getLatitude();
        double lon = location.getLongitude();

        if (!mAPIRunning) {
            mAPIRunning = true;
            if(mLocationListener!=null)mLocationListener.onLocationChanged(lat,lon);
            else {
                if (mTaskItems == null) {
                    Logger.e(TAG, "mTaskItem is null");
                    return;
                }
                checkShowNotification(lat, lon);
            }
        } else {
            release();
            mLocationListener=null;
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    private void checkShowNotification(final double lat, final double lon) {
        Logger.i(TAG, "[Method] checkShowNotification");
        if (mContextRef == null || mContextRef.get() == null) {
            Logger.i(TAG, "[checkShowNotification] context is null");
            release();
            return;
        }
        final Context context = mContextRef.get();

        Disposable disposable = checkLbsData(lat, lon)
                .subscribeOn(Schedulers.single())
                .flatMap(new Function<Pair<String, String>, SingleSource<?>>() {
                    @Override
                    public SingleSource<?> apply(Pair<String, String> stringStringPair) throws Exception {
                        mCurrentAdId = stringStringPair.first;
                        Logger.i(TAG, "checkLbsData flapMap : " + mCurrentAdId + " " + stringStringPair.second);

                        return postNewAnd(context, mCurrentAdId, stringStringPair.second,lat, lon);
                    }
                })
                .flatMap(new Function<Object, SingleSource<?>>() {
                    @Override
                    public SingleSource<?> apply(Object o) throws Exception {
                        if (o instanceof GetNewAndResponse) {
                            mResponse = (GetNewAndResponse) o;
                        } else {
                            return Single.error(new ApiException(TAG_ERROR_NORMAL, "o is not GetNewAndResponse object"));
                        }

//                        if (!mResponse.isSuccess()) {
//                            String responseJson="{\"ad_body\":\"原來大家都一樣？上班族的一日寫照…\",\"ad_icon\":\"http://s3-ap-northeast-1.amazonaws.com/adlocus-ad-pics/camp_2018/1b2e9781cbcc35c1315baf09bdd6b49fdfda9bfd1529915374.jpg\",\"ad_id\":\"152994417920651055\",\"ad_left_icon\":\"0\",\"ad_link\":\"http://1.ad-locus.com/dev/redirect/15299441792065/and/a34bf96563da39e35791c28c0ca44da8e0404154/5f9706f6f361550b59f6019082bc8963539e00e9e3325455710b3e02fa8750e86cbdc352776d3f34/15307028456305/native/0/1.10\",\"ad_native_text\":\"上班族的一日寫照…\",\"ad_title\":\"原來大家都一樣？\",\"ad_type\":\"1\",\"sid\":\"15306740449192\",\"track_imp\":\"\",\"err\":\"0\"}";
//                            mResponse=MiscUtils.parseJSON(responseJson,GetNewAndResponse.class);
//                        }
                        String json = MiscUtils.toJSONString(mResponse);
                        Logger.i(TAG, "postNewAnd flipMap : " + json);
//                        mRetryCount = 0;
                        if ((TextUtils.equals(mResponse.getAdType(), Constants.TAG_AD_TYPE_ICON) ||
                                TextUtils.equals(mResponse.getAdType(), Constants.TAG_AD_TYPE_BANNER) ||
                                TextUtils.equals(mResponse.getAdType(), Constants.TAG_AD_TYPE_BIG_VIEW))) {
                            if (mResponse.dataIsCorrect()) {
                                return postImpression(lat,lon);
                            }
                        }
                        return Single.error(new ApiException(TAG_ERROR_NORMAL, "Notification type is not match"));
                    }
                })
                .retry(new BiPredicate<Integer, Throwable>() {
                    @Override
                    public boolean test(Integer integer, Throwable throwable) throws Exception {
                        Logger.d(TAG, "[postNewAnd] retry test : " + integer);
                        ApiException e = (ApiException) throwable;
                        int code = e.getCode();
                        Logger.d(TAG, String.format("[postNewAnd] retry test throwable : %1$d.%2$s", code, e.getMessage()));

                        //true: retry
                        //false: no retry
                        switch (code) {
                            case TAG_ERROR_LOCATION://若是此使用者不在LBS task 範圍內
                                return false;
                            case ApiStatus.NETWORK_TIMEOUT://若是取得AD 素材Timeout 則重新call api直到 [TAG_TIMEOUT_RETRY_TIMES]
                                return mCurrentAPI != TAG_API_IMPRESSION && integer < TAG_TIMEOUT_RETRY_TIMES;
                            default://如果是單純call api失敗則重新取得下一筆做嘗試
                                mCacheAdIDList.add(mCurrentAdId);
                                return mCacheAdIDList.size() < mTaskItems.getPt().size();

                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        Logger.d(TAG, "Total onSuccess");
                        for (LbsTask item : mTaskItems.getPt()) {
                            if(item.getAdId().equals(mCurrentAdId)){
                                item.setIsShow(true);
                                break;
                            }
                        }

                        String task = MiscUtils.toJSONString(mTaskItems);
                        Repository.setLbsTaskJson(task);

                        AdLocusNotification.showNotification(context, mResponse);

                        release();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        if(throwable!=null){
                            ApiException e = (ApiException) throwable;
                            int code = e.getCode();
                            Logger.d(TAG, String.format("Total onFailed : %1$d.%2$s", code, e.getMessage()));

                            if (code == ApiStatus.NETWORK_TIMEOUT && mCurrentAPI == TAG_API_IMPRESSION) {
                                AdLocusNotification.showNotification(context, mResponse);
                            }
                        }
                        release();
                    }
                });
    }

    /**
     * Release reference
     */
    private void release() {
        mCacheAdIDList.clear();
        mCurrentAdId = "";
        mCurrentAPI = -1;
        mAPIRunning = false;
        stopListenerGPS();

    }

    /**
     * Stop gps
     */
    private void stopListenerGPS() {
        mListener.end();
    }

    /**
     * 檢查現在的location 是否在 LBS task json ad list 範圍中
     *
     * @param lat 緯度
     * @param lon 經度
     * @return
     */
    private Single<Pair<String, String>> checkLbsData(final double lat, final double lon) {
        Logger.i(TAG, "[Method] -> checkLbsData()");

        return Single.just(mTaskItems)
                .flatMap(new Function<GetLbsTaskResponse, Single<Pair<String, String>>>() {
                    @Override
                    public Single<Pair<String, String>> apply(GetLbsTaskResponse getLbsTaskResponse) throws Exception {
                        for (LbsTask item : getLbsTaskResponse.getPt()) {
                            //若已經Show過則不顯示 或者包含已query失敗的Ad id
                            if (!item.getIsShow() && !mCacheAdIDList.contains(item.getAdId())) {
                                Logger.d(TAG, "is not contains " + item.getAdId());
                                try{
                                    for (LbsTaskLlr subItem : item.getLlr()) {
                                        if (checkTargetInRange(lat, lon, MiscUtils.toDouble(subItem.getLat(), 0), MiscUtils.toDouble(subItem.getLon(), 0), MiscUtils.toInt(subItem.getRadius(), 0))) {
                                            String adId = item.getAdId();
                                            Pair<String, String> p = new Pair<>(adId, item.getSessionId());
                                            return Single.just(p);
                                        }
                                    }
                                }catch (Exception e){e.printStackTrace();}
                            }
                        }
                        return Single.error(new ApiException(TAG_ERROR_LOCATION, "Location No Match"));
                    }
                });
    }

    /**
     * 計算取得的GPS經緯度是否在半徑範圍內
     *
     * @param lat1   第一點 緯度
     * @param long1  第一點 經度
     * @param lat2   第二點 緯度
     * @param long2  第二點 經度
     * @param radius 目標半徑距離
     * @return
     */
    private boolean checkTargetInRange(double lat1, double long1, double lat2, double long2, int radius) {
        double a;
        double b;
        double R;
        R = 6378137;//地球半徑
        lat1 = lat1 * Math.PI / 180.0;
        lat2 = lat2 * Math.PI / 180.0;
        a = lat1 - lat2;
        b = (long1 - long2) * Math.PI / 180.0;
        double d;
        double sa2, sb2;
        sa2 = Math.sin(a / 2.0);
        sb2 = Math.sin(b / 2.0);
        d = 2 * R * Math.asin(Math.sqrt(sa2 * sa2 + Math.cos(lat1) * Math.cos(lat2) * sb2 * sb2));

        return radius - d > 0;
    }

    /**
     * 取得AD 素材
     *
     * @param context
     * @param adId
     * @param sessionId
     * @return
     */
    //LBS
    private Single<GetNewAndResponse> postNewAnd(final Context context, final String adId, final String sessionId,final double lat, final double lon) {
        Logger.i(TAG, "[Method] postNewAnd");

        PhoneCellUtil cgi = new PhoneCellUtil(context);

        mRequest.setAdId(adId);
        mRequest.setCi(cgi.getCid());
        mRequest.setKey(Repository.getAppKey());
        mRequest.setLac(cgi.getLac());
        mRequest.setMac(AdLocusUtil.getMac(context));
        mRequest.setMcc(cgi.getMcc());
        mRequest.setMnc(cgi.getMnc());
        mRequest.setRssi(cgi.getRssi());
        mRequest.setCellType(cgi.getCellType());
        mRequest.setLocType("network");
        mRequest.setScreen(AdLocusUtil.getScreen());
        mRequest.setSessionId(sessionId);
        mRequest.setTestmode(AdLocusUtil.TEST_MODE);
        mRequest.setvStr(AdLocusUtil.MAC_SDK_VERSION);
        mRequest.setdAdId(Repository.getGoogleAdId());
        mRequest.setDeviceId(Repository.getHashDeviceId());
        mRequest.setLat(lat + "");
        mRequest.setLon(lon + "");

        return Repository.postNewAnd(context,mRequest);
    }

    /**
     * call impression 檢查ad 是否可show
     *
     * @return
     * @param lat
     * @param lon
     */
    //LBS
    private Single<GetNewImpressionResponse> postImpression(double lat, double lon) {
        Logger.i(TAG, "[Method] -> postImpression()");
        mCurrentAPI = TAG_API_IMPRESSION;
        if(AdLocus.gpsLocation!=null){
            mRequest.setLocType("gps");
            lat=AdLocus.gpsLocation.getLatitude();
            lon=AdLocus.gpsLocation.getLongitude();
            AdLocus.gpsLocation=null;
        }
        return Repository.postNewImpression(mRequest, lat+"", lon+"");
    }
}
