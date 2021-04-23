package com.hyxen.adlocusaar;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;

import com.hyxen.adlocusaar.constants.ApiStatus;
import com.hyxen.adlocusaar.constants.Constants;
import com.hyxen.adlocusaar.repository.ApiException;
import com.hyxen.adlocusaar.repository.Repository;
import com.hyxen.adlocusaar.repository.data.RemoteResponse;
import com.hyxen.adlocusaar.repository.data.domain.LbsTask;
import com.hyxen.adlocusaar.repository.data.domain.LbsTaskLlr;
import com.hyxen.adlocusaar.repository.data.request.CollectionRequest;
import com.hyxen.adlocusaar.repository.data.request.GetLbsFileRequest;
import com.hyxen.adlocusaar.repository.data.request.NewAndRequest;
import com.hyxen.adlocusaar.repository.data.response.GetCollectionResponse;
import com.hyxen.adlocusaar.repository.data.response.GetLbsTaskResponse;
import com.hyxen.adlocusaar.repository.data.response.GetNewAndResponse;
import com.hyxen.adlocusaar.repository.data.response.GetNewImpressionResponse;
import com.hyxen.adlocusaar.utils.Base64;
import com.hyxen.adlocusaar.utils.LbsChecker;
import com.hyxen.adlocusaar.utils.MyLocationListener;
import com.hyxen.adlocusaar.utils.RSAUtils;
import com.hyxen.adlocusaar.utils.notification.AdLocusNotification;
import com.hyxen.adlocusaar.utils.AdLocusUtil;
import com.hyxen.adlocusaar.utils.PhoneCellUtil;
import com.hyxen.adlocusaar.utils.Logger;
import com.hyxen.adlocusaar.utils.MiscUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class AdLocusHelp extends AdLocusHelpBase {
    private static final String TAG = AdLocusHelp.class.getSimpleName();
    private int mRetryCount;
    private int mRetryCountCollection;
    private int hashCollection = 0;

    void postPushToken(final Context context) {
        Logger.i(TAG, "[Method] -> postPushToken()");
        Disposable task = Repository.postPushToken()
                .map(new Function<RemoteResponse, String>() {
                    @Override
                    public String apply(@NonNull RemoteResponse resp) throws Exception {
                        return resp.getErrMsg();
                    }
                })
                .retry(5)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Logger.i(TAG, "postPushToken success : " + s);
                        mRetryCount = 0;
                        Repository.setRegisterSuccess();

//                        //-------- test -----------
//
//
//                        Repository.setLbsTaskJson("{\"pt\":[{\"ad_id\":\"152750029702711734\",\"begin_ts\":\"1528041600\",\"end_ts\":\"1528128000\",\"fcm_push\":\"1\",\"llr\":[],\"session_id\":\"15281335828187\"}],\"err\":\"0\"}");
//
//                        //設置排程
//                        LbsChecker.getInstance(context).startAlarmTimer();


                        //-------- test end -----------
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        ApiException e = (ApiException) throwable;
                        int code = e.getCode();
                        Logger.i(TAG, "postPushToken failed : " + throwable.toString() + " code: " + code);
                    }
                });

        addTask(task);
    }

//    public String getPostCollection(final Context context){
//        PhoneCellUtil cgi = new PhoneCellUtil(context);
//        JSONObject o = new JSONObject();
//        try {
//            o.put("mcc", cgi.getMcc());
//            o.put("mnc", cgi.getMnc());
//            o.put("lang", Locale.getDefault().toString());
//            o.put("mac", AdLocusUtil.getMac(context));
//            o.put("noti", AdLocusUtil.isNotificationEnable(context) ? 1 : 0);
//            o.put("adid", Repository.getGoogleAdId());
//            o.put("tar_enable", AdLocusUtil.isLimitAdTrackingEnabled(context) ? 1 : 0);
//
//            hashCollection = o.toString().hashCode();
//            o.put("pkg", AdLocusUtil.getAppList(context));
//
////            request.setPlain(AdLocusUtil.encrypt("e2e4193b842bb054", o.toString()));
//
//
//
////            String key="-----BEGIN PUBLIC KEY-----\n" +
////                    "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCUlKGQpyjsOqrLkRMeCvbiE/ZG\n" +
////                    "DXzJz6KAtprQ10G4lVVH6kkG82Fmj9hbm1agDCO5EAwHqTnzN0J0tQF+uhifcI54\n" +
////                    "pRyRJ1dKXr+q9XqBIC43fBf5e2lBre8mGBK6WoSkHMxo9KWEhHk8SWVvAEHtVXUL\n" +
////                    "6HQFQ5txU/SgC1vOrwIDAQAB\n" +
////                    "-----END PUBLIC KEY-----";
//            String key=
//                    "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCUlKGQpyjsOqrLkRMeCvbiE/ZG\n" +
//                            "DXzJz6KAtprQ10G4lVVH6kkG82Fmj9hbm1agDCO5EAwHqTnzN0J0tQF+uhifcI54\n" +
//                            "pRyRJ1dKXr+q9XqBIC43fBf5e2lBre8mGBK6WoSkHMxo9KWEhHk8SWVvAEHtVXUL\n" +
//                            "6HQFQ5txU/SgC1vOrwIDAQAB\n" ;
//            byte[] publicBytes = Base64.decode(key, Base64.DEFAULT);
////            request.setPlain(Base64.encodeToString(RSAUtils.encryptByPublicKey(o.toString().getBytes(),publicBytes), Base64.NO_WRAP));
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return o.toString();
//    }
    void postCollection(final Context context) {
        Logger.i(TAG, "[Method] -> postCollection()");
        new Thread(new Runnable() {
            @Override
            public void run() {
                String key=
                        "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCUlKGQpyjsOqrLkRMeCvbiE/ZG\n" +
                                "DXzJz6KAtprQ10G4lVVH6kkG82Fmj9hbm1agDCO5EAwHqTnzN0J0tQF+uhifcI54\n" +
                                "pRyRJ1dKXr+q9XqBIC43fBf5e2lBre8mGBK6WoSkHMxo9KWEhHk8SWVvAEHtVXUL\n" +
                                "6HQFQ5txU/SgC1vOrwIDAQAB\n" ;
                byte[] publicBytes = Base64.decode(key, Base64.DEFAULT);


                final CollectionRequest request = new CollectionRequest();
                request.setDevice_id(Repository.getHashDeviceId());
                request.setKey(Repository.getAppKey());



                PhoneCellUtil cgi = new PhoneCellUtil(context);
                JSONObject o = new JSONObject();
                try {
                    if(cgi.getMcc().length()>0)o.put("mcc", Base64.encodeToString(RSAUtils.encryptByPublicKey(cgi.getMcc().getBytes(),publicBytes), Base64.NO_WRAP));
                    else o.put("mcc", "");
                    if(cgi.getMnc().length()>0)o.put("mnc", Base64.encodeToString(RSAUtils.encryptByPublicKey(cgi.getMnc().getBytes(),publicBytes), Base64.NO_WRAP));
                    else o.put("mnc", "");

                    if(Locale.getDefault().toString().length()>0)o.put("lang", Base64.encodeToString(RSAUtils.encryptByPublicKey(Locale.getDefault().toString().getBytes(),publicBytes), Base64.NO_WRAP));
                    else o.put("lang", "");
                    if(AdLocusUtil.getMac(context).length()>0)o.put("mac", Base64.encodeToString(RSAUtils.encryptByPublicKey(AdLocusUtil.getMac(context).getBytes(),publicBytes), Base64.NO_WRAP));
                    else o.put("mac", "");

                    o.put("noti", AdLocusUtil.isNotificationEnable(context) ? 1 : 0);

                    if(Repository.getGoogleAdId().length()>0)o.put("adid", Base64.encodeToString(RSAUtils.encryptByPublicKey(Repository.getGoogleAdId().getBytes(),publicBytes), Base64.NO_WRAP));
                    else o.put("adid", "");

                    o.put("tar_enable", AdLocusUtil.isLimitAdTrackingEnabled(context) ? 1 : 0);
                    hashCollection = o.toString().hashCode();


                    JSONArray ja=AdLocusUtil.getAppList(context);
                    JSONArray ja2=new JSONArray();
                    for(int i=0;i<ja.length();i++){
                        ja2.put(Base64.encodeToString(RSAUtils.encryptByPublicKey(ja.optString(i).getBytes(),publicBytes), Base64.NO_WRAP));
                    }
                    o.put("pkg", ja2.toString());
                    request.setPlain(o.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (hashCollection == 0 || !AdLocusUtil.hasCollectionTrackConsent(context, hashCollection))
                    return;



                try{
                    Disposable task = Repository.postCollection(request)
                            .subscribe(new Consumer<GetCollectionResponse>() {
                                @Override
                                public void accept(GetCollectionResponse response) throws Exception {
                                    String json = MiscUtils.toJSONString(response);
                                    Logger.i(TAG, "postCollection success : " + json);
                                    mRetryCountCollection = 0;
                                    AdLocusUtil.setCollectionData(context, hashCollection);
                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {
                                    ApiException e = (ApiException) throwable;
                                    int code = e.getCode();
                                    Logger.i(TAG, "postCollection failed : " + code + throwable.getMessage());
                                    if (code == ApiStatus.NETWORK_TIMEOUT && mRetryCountCollection <= 5) {
                                        Logger.i(TAG, "postCollection failed timeout, Retry times: " + mRetryCountCollection);
                                        mRetryCountCollection++;
                                        postCollection(context);
                                    } else {
                                        mRetryCountCollection = 0;
                                    }
                                }
                            });

                    addTask(task);
                }catch(Exception e){e.printStackTrace();}
            }
        }).start();

//        String key=
//            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCUlKGQpyjsOqrLkRMeCvbiE/ZG\n" +
//            "DXzJz6KAtprQ10G4lVVH6kkG82Fmj9hbm1agDCO5EAwHqTnzN0J0tQF+uhifcI54\n" +
//            "pRyRJ1dKXr+q9XqBIC43fBf5e2lBre8mGBK6WoSkHMxo9KWEhHk8SWVvAEHtVXUL\n" +
//            "6HQFQ5txU/SgC1vOrwIDAQAB\n" ;
//        byte[] publicBytes = Base64.decode(key, Base64.DEFAULT);
//
//
//        final CollectionRequest request = new CollectionRequest();
//        request.setDevice_id(Repository.getHashDeviceId());
//        request.setKey(Repository.getAppKey());
//
//
//
//        PhoneCellUtil cgi = new PhoneCellUtil(context);
//        JSONObject o = new JSONObject();
//        try {
//            if(cgi.getMcc().length()>0)o.put("mcc", Base64.encodeToString(RSAUtils.encryptByPublicKey(cgi.getMcc().getBytes(),publicBytes), Base64.NO_WRAP));
//            else o.put("mcc", "");
//            if(cgi.getMnc().length()>0)o.put("mnc", Base64.encodeToString(RSAUtils.encryptByPublicKey(cgi.getMnc().getBytes(),publicBytes), Base64.NO_WRAP));
//            else o.put("mnc", "");
//
//            if(Locale.getDefault().toString().length()>0)o.put("lang", Base64.encodeToString(RSAUtils.encryptByPublicKey(Locale.getDefault().toString().getBytes(),publicBytes), Base64.NO_WRAP));
//            else o.put("lang", "");
//            if(AdLocusUtil.getMac(context).length()>0)o.put("mac", Base64.encodeToString(RSAUtils.encryptByPublicKey(AdLocusUtil.getMac(context).getBytes(),publicBytes), Base64.NO_WRAP));
//            else o.put("mac", "");
//
//            o.put("noti", AdLocusUtil.isNotificationEnable(context) ? 1 : 0);
//
//            if(Repository.getGoogleAdId().length()>0)o.put("adid", Base64.encodeToString(RSAUtils.encryptByPublicKey(Repository.getGoogleAdId().getBytes(),publicBytes), Base64.NO_WRAP));
//            else o.put("adid", "");
//
//            o.put("tar_enable", AdLocusUtil.isLimitAdTrackingEnabled(context) ? 1 : 0);
//            hashCollection = o.toString().hashCode();
//
//
//            JSONArray ja=AdLocusUtil.getAppList(context);
//            JSONArray ja2=new JSONArray();
//            for(int i=0;i<ja.length();i++){
//                ja2.put(Base64.encodeToString(RSAUtils.encryptByPublicKey(ja.optString(i).getBytes(),publicBytes), Base64.NO_WRAP));
//            }
//            o.put("pkg", ja2.toString());
//            request.setPlain(o.toString());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        if (hashCollection == 0 || !AdLocusUtil.hasCollectionTrackConsent(context, hashCollection))
//            return;
//
//
//
//        try{
//            Disposable task = Repository.postCollection(request)
//                    .subscribe(new Consumer<GetCollectionResponse>() {
//                        @Override
//                        public void accept(GetCollectionResponse response) throws Exception {
//                            String json = MiscUtils.toJSONString(response);
//                            Logger.i(TAG, "postCollection success : " + json);
//                            mRetryCountCollection = 0;
//                            AdLocusUtil.setCollectionData(context, hashCollection);
//                        }
//                    }, new Consumer<Throwable>() {
//                        @Override
//                        public void accept(Throwable throwable) throws Exception {
//                            ApiException e = (ApiException) throwable;
//                            int code = e.getCode();
//                            Logger.i(TAG, "postCollection failed : " + code + throwable.getMessage());
//                            if (code == ApiStatus.NETWORK_TIMEOUT && mRetryCountCollection <= 5) {
//                                Logger.i(TAG, "postCollection failed timeout, Retry times: " + mRetryCountCollection);
//                                mRetryCountCollection++;
//                                postCollection(context);
//                            } else {
//                                mRetryCountCollection = 0;
//                            }
//                        }
//                    });
//
//            addTask(task);
//        }catch(Exception e){e.printStackTrace();}



    }

    void getAdLocationStep(final Context context, final String[] adIds, final String[] sessionIds){
        if(context == null || adIds==null || sessionIds==null || adIds.length<=0 || adIds.length!=sessionIds.length)return;
        final LocationManager mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        final MyLocationListener mLocationListener = MyLocationListener.getInstance(context);
        mLocationListener.setGPSCallbackListener(new MyLocationListener.GPSCallbackListener() {
            @Override
            public void end() {
                Logger.d(TAG, "stop listener netwouk status ");
                if (mLocationManager != null)
                    mLocationManager.removeUpdates(mLocationListener);
            }
        });
        mLocationListener.setmLocationListener(new MyLocationListener.GPSOnChangeListener() {
            @Override
            public void onLocationChanged(double lat, double lon) {
                Logger.i(TAG, "[Method] -> postNewAnd() :lat=" + lat + ",lon=" + lon);
                getAdpostNewAndStep(context,adIds,sessionIds,lat+"",lon+"",0);
            }
        });
        if (mLocationManager != null && mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            // mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener, Looper.getMainLooper());
            }else {
                getAdpostNewAndStep(context,adIds,sessionIds,"","",0);
            }
        }
    }
    void getAdpostNewAndStep(final Context context, final String[] adIds, final String[] sessionIds,final String lat,final String lon,final int index){
        if(context == null || adIds.length<=index || sessionIds.length<=index)return;
        final String adId=adIds[index];
        final String sessionId=sessionIds[index];
        final PhoneCellUtil cgi = new PhoneCellUtil(context);
        final NewAndRequest request = new NewAndRequest();
        request.setAdId(adId);
        request.setCi(cgi.getCid());
        request.setKey(Repository.getAppKey());
        request.setLac(cgi.getLac());
        request.setMac(AdLocusUtil.getMac(context));
        request.setMcc(cgi.getMcc());
        request.setMnc(cgi.getMnc());
        request.setRssi(cgi.getRssi());
        request.setCellType(cgi.getCellType());
        request.setLocType("network");
        request.setScreen(AdLocusUtil.getScreen());
        request.setSessionId(sessionId);
        request.setTestmode(AdLocusUtil.TEST_MODE);
        request.setvStr(AdLocusUtil.MAC_SDK_VERSION);
        request.setdAdId(Repository.getGoogleAdId());
        request.setDeviceId(Repository.getHashDeviceId());
        request.setLat( lat);
        request.setLon( lon);
        Disposable task = Repository.postNewAnd(context,request)
                .subscribe(new Consumer<GetNewAndResponse>() {
                    @Override
                    public void accept(GetNewAndResponse response) throws Exception {
                        String json = MiscUtils.toJSONString(response);
                        Logger.i(TAG, "postNewAnd success : " + json);
                        mRetryCount = 0;
                        if (response != null && !TextUtils.isEmpty(response.getAdType()) && (
                                TextUtils.equals(response.getAdType(), Constants.TAG_AD_TYPE_ICON) ||
                                        TextUtils.equals(response.getAdType(), Constants.TAG_AD_TYPE_BANNER) ||
                                        TextUtils.equals(response.getAdType(), Constants.TAG_AD_TYPE_BIG_VIEW))) {
                            if (response.dataIsCorrect())
                                getAdPostImpressionStep(context, request, response, adIds,sessionIds, index);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        ApiException e = (ApiException) throwable;
                        int code = e.getCode();
                        Logger.i(TAG, "postNewAnd failed : " + code + throwable.getMessage());
                        getAdpostNewAndStep(context,adIds,sessionIds,lat,lon,index+1);
                    }
                });

        addTask(task);
    }

    private void getAdPostImpressionStep(final Context context,final  NewAndRequest request, final GetNewAndResponse response,final String[] adIds, final String[] sessionIds,final int index) {
        Logger.i(TAG, "[Method] -> postImpression()");
        if(AdLocus.gpsLocation!=null){
            request.setLocType("gps");
            request.setLat(AdLocus.gpsLocation.getLatitude()+"");
            request.setLon(AdLocus.gpsLocation.getLongitude()+"");
            AdLocus.gpsLocation=null;
        }
        Disposable task = Repository.postNewImpression(request,request.getLat(),request.getLon())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<GetNewImpressionResponse>() {
                    @Override
                    public void accept(GetNewImpressionResponse newImpressionResponse) throws Exception {
                        Logger.i(TAG, "postImpression success : ");
                        AdLocusNotification.showNotification(context, response);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        ApiException e = (ApiException) throwable;
                        int code = e.getCode();
                        Logger.i(TAG, "postImpression failed : " + code + throwable.getMessage());
                        if (code == ApiStatus.NETWORK_TIMEOUT) {
                            Logger.i(TAG, "postImpression failed timeout ");
                            AdLocusNotification.showNotification(context, response);
                        }else{
                            getAdpostNewAndStep(context,adIds,sessionIds,request.getLat(),request.getLon(),index+1);
                        }
                    }
                });

        addTask(task);
    }





    //GA
    void postNewAnd(final Context context, final String adId, final String sessionId) {
        Logger.i(TAG, "[Method] -> postNewAnd()");
        final PhoneCellUtil cgi = new PhoneCellUtil(context);
        final NewAndRequest request = new NewAndRequest();
        request.setAdId(adId);
        request.setCi(cgi.getCid());
        request.setKey(Repository.getAppKey());
        request.setLac(cgi.getLac());
        request.setMac(AdLocusUtil.getMac(context));
        request.setMcc(cgi.getMcc());
        request.setMnc(cgi.getMnc());
        request.setRssi(cgi.getRssi());
        request.setLocType("network");
        request.setCellType(cgi.getCellType());
        request.setScreen(AdLocusUtil.getScreen());
        request.setSessionId(sessionId);
        request.setTestmode(AdLocusUtil.TEST_MODE);
        request.setvStr(AdLocusUtil.MAC_SDK_VERSION);
        request.setdAdId(Repository.getGoogleAdId());
        request.setDeviceId(Repository.getHashDeviceId());


        final LocationManager mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        final MyLocationListener mLocationListener = MyLocationListener.getInstance(context);
        mLocationListener.setGPSCallbackListener(new MyLocationListener.GPSCallbackListener() {
            @Override
            public void end() {
                Logger.d(TAG, "stop listener network status ");
                if (mLocationManager != null)
                    mLocationManager.removeUpdates(mLocationListener);
            }
        });
        mLocationListener.setmLocationListener(new MyLocationListener.GPSOnChangeListener() {
            @Override
            public void onLocationChanged(double lat, double lon) {
                Logger.i(TAG, "[Method] -> postNewAnd() :lat=" + lat + ",lon=" + lon);

                request.setLat(lat + "");
                request.setLon(lon + "");
                Disposable task = Repository.postNewAnd(context,request)
                        .subscribe(new Consumer<GetNewAndResponse>() {
                            @Override
                            public void accept(GetNewAndResponse response) throws Exception {
                                String json = MiscUtils.toJSONString(response);
                                Logger.i(TAG, "postNewAnd success : " + json);
                                mRetryCount = 0;
                                if (response != null && !TextUtils.isEmpty(response.getAdType()) && (
                                        TextUtils.equals(response.getAdType(), Constants.TAG_AD_TYPE_ICON) ||
                                                TextUtils.equals(response.getAdType(), Constants.TAG_AD_TYPE_BANNER) ||
                                                TextUtils.equals(response.getAdType(), Constants.TAG_AD_TYPE_BIG_VIEW))) {
                                    if (response.dataIsCorrect())
                                        postImpression(context, request, response);
                                }
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                ApiException e = (ApiException) throwable;
                                int code = e.getCode();
                                Logger.i(TAG, "postNewAnd failed : " + code + throwable.getMessage());
                                if (code == ApiStatus.NETWORK_TIMEOUT && mRetryCount <= 5) {
                                    Logger.i(TAG, "postNewAnd failed timeout, Retry times: " + mRetryCount);
                                    mRetryCount++;
                                    postNewAnd(context, adId, sessionId);
                                } else {
                                    mRetryCount = 0;
                                }
                            }
                        });

                addTask(task);
            }
        });


        if (mLocationManager != null && mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {//                                            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener, Looper.getMainLooper());
            }else{
                request.setLat( "");
                request.setLon( "");
                Disposable task = Repository.postNewAnd(context,request)
                        .subscribe(new Consumer<GetNewAndResponse>() {
                            @Override
                            public void accept(GetNewAndResponse response) throws Exception {
                                String json = MiscUtils.toJSONString(response);
                                Logger.i(TAG, "postNewAnd success : " + json);
                                mRetryCount = 0;
                                if (response != null && !TextUtils.isEmpty(response.getAdType()) && (
                                        TextUtils.equals(response.getAdType(), Constants.TAG_AD_TYPE_ICON) ||
                                                TextUtils.equals(response.getAdType(), Constants.TAG_AD_TYPE_BANNER) ||
                                                TextUtils.equals(response.getAdType(), Constants.TAG_AD_TYPE_BIG_VIEW))) {
                                    if (response.dataIsCorrect())
                                        postImpression(context, request, response);
                                }
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                ApiException e = (ApiException) throwable;
                                int code = e.getCode();
                                Logger.i(TAG, "postNewAnd failed : " + code + throwable.getMessage());
                                if (code == ApiStatus.NETWORK_TIMEOUT && mRetryCount <= 5) {
                                    Logger.i(TAG, "postNewAnd failed timeout, Retry times: " + mRetryCount);
                                    mRetryCount++;
                                    postNewAnd(context, adId, sessionId);
                                } else {
                                    mRetryCount = 0;
                                }
                            }
                        });

                addTask(task);
            }

        }

//        PhoneCellUtil cgi = new PhoneCellUtil(context);
//        final NewAndRequest request = new NewAndRequest();
//
//        request.setAdId(adId);
//        request.setCi(cgi.getCid());
//        request.setKey(Repository.getAppKey());
//        request.setLac(cgi.getLac());
//        request.setMac(AdLocusUtil.getMac(context));
//        request.setMcc(cgi.getMcc());
//        request.setMnc(cgi.getMnc());
//        request.setScreen(AdLocusUtil.getScreen());
//        request.setSessionId(sessionId);
//        request.setTestmode(AdLocusUtil.TEST_MODE);
//        request.setvStr(AdLocusUtil.MAC_SDK_VERSION);
//        request.setdAdId(Repository.getGoogleAdId());
//        request.setDeviceId(Repository.getHashDeviceId());
//
//        Disposable task = Repository.postNewAnd(request)
//                .subscribe(new Consumer<GetNewAndResponse>() {
//                    @Override
//                    public void accept(GetNewAndResponse response) throws Exception {
//                        String json = MiscUtils.toJSONString(response);
//                        Logger.i(TAG, "postNewAnd success : " + json);
//                        mRetryCount = 0;
//                        if (response != null && !TextUtils.isEmpty(response.getAdType()) && (
//                                TextUtils.equals(response.getAdType(), Constants.TAG_AD_TYPE_ICON) ||
//                                        TextUtils.equals(response.getAdType(), Constants.TAG_AD_TYPE_BANNER) ||
//                                        TextUtils.equals(response.getAdType(), Constants.TAG_AD_TYPE_BIG_VIEW))) {
//                            if (response.dataIsCorrect())
//                                postImpression(context, request, response);
//                        }
//                    }
//                }, new Consumer<Throwable>() {
//                    @Override
//                    public void accept(Throwable throwable) throws Exception {
//                        ApiException e = (ApiException) throwable;
//                        int code = e.getCode();
//                        Logger.i(TAG, "postNewAnd failed : " + code + throwable.getMessage());
//                        if (code == ApiStatus.NETWORK_TIMEOUT && mRetryCount <= 5) {
//                            Logger.i(TAG, "postNewAnd failed timeout, Retry times: " + mRetryCount);
//                            mRetryCount++;
//                            postNewAnd(context, adId, sessionId);
//                        } else {
//                            mRetryCount = 0;
//                        }
//                    }
//                });
//
//        addTask(task);
    }

    //GA
    private void postImpression(final Context context,final  NewAndRequest request, final GetNewAndResponse response) {
        Logger.i(TAG, "[Method] -> postImpression()");
        if(AdLocus.gpsLocation!=null){
            request.setLocType("gps");
            request.setLat(AdLocus.gpsLocation.getLatitude()+"");
            request.setLon(AdLocus.gpsLocation.getLongitude()+"");
            AdLocus.gpsLocation=null;
        }

        Disposable task = Repository.postNewImpression(request,request.getLat(),request.getLon())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<GetNewImpressionResponse>() {
                    @Override
                    public void accept(GetNewImpressionResponse newImpressionResponse) throws Exception {
                        Logger.i(TAG, "postImpression success : ");
                        AdLocusNotification.showNotification(context, response);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        ApiException e = (ApiException) throwable;
                        int code = e.getCode();
                        Logger.i(TAG, "postImpression failed : " + code + throwable.getMessage());
                        if (code == ApiStatus.NETWORK_TIMEOUT) {
                            Logger.i(TAG, "postImpression failed timeout ");
                            AdLocusNotification.showNotification(context, response);
                        }
                    }
                });

        addTask(task);
//        final LocationManager mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//        final MyLocationListener mLocationListener = MyLocationListener.getInstance(context);
//        mLocationListener.setGPSCallbackListener(new MyLocationListener.GPSCallbackListener() {
//            @Override
//            public void end() {
//                Logger.d(TAG, "stop listener netwouk status ");
//                if (mLocationManager != null)
//                    mLocationManager.removeUpdates(mLocationListener);
//            }
//        });
//        mLocationListener.setmLocationListener(new MyLocationListener.GPSOnChangeListener() {
//            @Override
//            public void onLocationChanged(double lat, double lon) {
//                Disposable task = Repository.postNewImpression(request,lat+"",lon+"")
//                        .subscribeOn(AndroidSchedulers.mainThread())
//                        .subscribe(new Consumer<GetNewImpressionResponse>() {
//                            @Override
//                            public void accept(GetNewImpressionResponse newImpressionResponse) throws Exception {
//                                Logger.i(TAG, "postImpression success : ");
//                                AdLocusNotification.showNotification(context, response);
//                            }
//                        }, new Consumer<Throwable>() {
//                            @Override
//                            public void accept(Throwable throwable) throws Exception {
//                                ApiException e = (ApiException) throwable;
//                                int code = e.getCode();
//                                Logger.i(TAG, "postImpression failed : " + code + throwable.getMessage());
//                                if (code == ApiStatus.NETWORK_TIMEOUT) {
//                                    Logger.i(TAG, "postImpression failed timeout ");
//                                    AdLocusNotification.showNotification(context, response);
//                                }
//                            }
//                        });
//
//                addTask(task);
//            }
//        });
//        if (mLocationManager != null) {//                                            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
//            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
//                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener,Looper.getMainLooper());
//            }else{
//                Disposable task = Repository.postNewImpression(request,"","")
//                        .subscribeOn(AndroidSchedulers.mainThread())
//                        .subscribe(new Consumer<GetNewImpressionResponse>() {
//                            @Override
//                            public void accept(GetNewImpressionResponse newImpressionResponse) throws Exception {
//                                Logger.i(TAG, "postImpression success : ");
//                                AdLocusNotification.showNotification(context, response);
//                            }
//                        }, new Consumer<Throwable>() {
//                            @Override
//                            public void accept(Throwable throwable) throws Exception {
//                                ApiException e = (ApiException) throwable;
//                                int code = e.getCode();
//                                Logger.i(TAG, "postImpression failed : " + code + throwable.getMessage());
//                                if (code == ApiStatus.NETWORK_TIMEOUT) {
//                                    Logger.i(TAG, "postImpression failed timeout ");
//                                    AdLocusNotification.showNotification(context, response);
//                                }
//                            }
//                        });
//
//                addTask(task);
//            }
//        }


    }

    void getLbsFile(final Context context) {
        GetLbsFileRequest request = new GetLbsFileRequest();
//        PhoneCellUtil cellUtil = new PhoneCellUtil(context);

        request.setDeviceId(Repository.getHashDeviceId());
        request.setKey(Repository.getAppKey());
//        request.setMcc("466");
//        request.setMnc("92");
//        request.setLac(cellUtil.getLac());
//        request.setCi(cellUtil.getCid());
//        request.setDevType("11");
        Disposable task = Repository.getLbsTask(request)
                .subscribe(new Consumer<GetLbsTaskResponse>() {
                    @Override
                    public void accept(GetLbsTaskResponse getLbsTaskResponse) throws Exception {
                        if (getLbsTaskResponse == null || getLbsTaskResponse.getPt() == null) {
                            Log.i(TAG, "getLbsTaskResponse is null");
                            return;
                        }

                        //將取到的LBS Task Json 存入暫存
                        String responseJson = MiscUtils.toJSONString(getLbsTaskResponse);
                        Repository.setLbsTaskJson(responseJson);
                        //JSONObject dataJ=new JSONObject(responseJson);


                        //設置排程
                        LbsChecker.getInstance(context).startAlarmTimer();


                        // Leo wu test
                        Disposable disposable = Repository.checkLbsDataIsEmpty(context)
                                .subscribeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Consumer<GetLbsTaskResponse>() {
                                    @Override
                                    public void accept(GetLbsTaskResponse getLbsTaskResponse) throws Exception {
                                        Logger.i(TAG, "Disposable accept");
                                        if ( ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                            Logger.i(TAG, "GPS permission is not granted");
                                            if (getLbsTaskResponse == null || getLbsTaskResponse.getPt() == null) {
                                                try{
                                                    LbsTaskLlr lr=getLbsTaskResponse.getPt().get(0).getLlr().get(0);
                                                    MyLocationListener.getInstance(getLbsTaskResponse, context).onLocationChanged(Double.parseDouble(lr.getLat()),Double.parseDouble(lr.getLon()));
                                                }catch(Exception e){e.printStackTrace();}
                                            }

                                            return;
                                        }
//
                                        if ( ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                            Logger.i(TAG, "GPS permission is not granted");
                                            if (getLbsTaskResponse == null || getLbsTaskResponse.getPt() == null) {
                                                try{
                                                    LbsTaskLlr lr=getLbsTaskResponse.getPt().get(0).getLlr().get(0);
                                                    MyLocationListener.getInstance(getLbsTaskResponse, context).onLocationChanged(Double.parseDouble(lr.getLat()),Double.parseDouble(lr.getLon()));
                                                }catch(Exception e){e.printStackTrace();}
                                            }
                                            return;
                                        }




                                        final LocationManager mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                                        if ( !mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                                            Logger.i(TAG, "GSP network is not open");
                                            if (getLbsTaskResponse == null || getLbsTaskResponse.getPt() == null) {
                                                try{
                                                    LbsTaskLlr lr=getLbsTaskResponse.getPt().get(0).getLlr().get(0);
                                                    MyLocationListener.getInstance(getLbsTaskResponse, context).onLocationChanged(Double.parseDouble(lr.getLat()),Double.parseDouble(lr.getLon()));
                                                }catch(Exception e){e.printStackTrace();}
                                            }
                                            return;
                                        }
                                        final MyLocationListener mLocationListener = MyLocationListener.getInstance(getLbsTaskResponse, context);
                                        mLocationListener.setGPSCallbackListener(new MyLocationListener.GPSCallbackListener() {
                                            @Override
                                            public void end() {
                                                Logger.d(TAG, "stop listener netwouk status ");
                                                if (mLocationManager != null)
                                                    mLocationManager.removeUpdates(mLocationListener);
                                            }
                                        });
                                        if (mLocationManager != null){
//                                            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
                                            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
                                        }
//
                                    }
                                }, new Consumer<Throwable>() {
                                    @Override
                                    public void accept(Throwable throwable) throws Exception {
                                        Logger.d("TAG", "failed : " + throwable.getMessage());
                                    }
                                });
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        ApiException e = (ApiException) throwable;
                        int code = e.getCode();
                        Logger.d(TAG, "Failed : " + code + throwable.getMessage());
                    }
                });

        addTask(task);
    }

    void testMode(final Context context) {
        //for Test
        String json_banner = "{\"err\":\"0\",\"ad_icon\":\"https://s3-ap-northeast-1.amazonaws.com/adlocus-rd-ad-pics/camp_2018/07bfc9314be7db07f200b648574dee9bf674ba1d1526027695.png\",\"ad_id\":\"15260564975739\",\"ad_link\":\"http://yusi_new.adlocus_api.dev.hxcld.com/dev/redirect/15260564975739/and/6cb1a61f629c72baa18f70ada20b438604ec9093/a52f5fb66b6c2fc730f19953031a2877631bb09d/15260575927063/native/0/1.0\",\"ad_type\":\"2\",\"adlocus_app_position\":\"2\",\"sid\":\"15260287910597\",\"track_imp\":\"\"}";
        String json_banner2 = "{\"ad_icon\":\"https://s3-ap-northeast-1.amazonaws.com/adlocus-rd-ad-pics/camp_2018/2ff6c7b421a719346e655cb7a06b0f502f0465201525766669.jpg\",\"ad_id\":\"15257954709664\",\"ad_link\":\"http://yusi_new.adlocus_api.dev.hxcld.com/dev/redirect/15257954709664/and/6781bc726655d9a0c0122d6dc06ed102cc859db7/a52f5fb66b6c2fc730f19953031a2877631bb09d/15260593387090/native/0/1.0\",\"ad_type\":\"2\",\"adlocus_app_position\":\"3\",\"sid\":\"15260305377411\",\"track_imp\":\"\",\"err\":\"0\"}";
        final GetNewAndResponse test_banner = MiscUtils.parseJSON(json_banner, GetNewAndResponse.class);
        AdLocusNotification.showNotification(context, test_banner);
        final GetNewAndResponse test_banner2 = MiscUtils.parseJSON(json_banner2, GetNewAndResponse.class);
        AdLocusNotification.showNotification(context, test_banner2);

        String json_origin = "{\"ad_body\":\"TEXT   Text TEXT   Text TEXT   Text\",\"ad_icon\":\"https://s3-ap-northeast-1.amazonaws.com/adlocus-rd-ad-pics/camp_2018/0e452d132e3abe15f0b8daf8774790b6ff0c24a41525751049.png\",\"ad_id\":\"15257798518669\",\"ad_left_icon\":\"1\",\"ad_link\":\"http://yusi_new.adlocus_api.dev.hxcld.com/dev/redirect/15257798518669/and/6781bc726655d9a0c0122d6dc06ed102cc859db7/a52f5fb66b6c2fc730f19953031a2877631bb09d/15260581907075/native/0/1.0\",\"ad_native_text\":\"Text\",\"ad_title\":\"Title\",\"ad_type\":\"1\",\"sid\":\"15260293904015\",\"track_imp\":\"\",\"err\":\"0\"}";
        String json_origin2 = "{\"ad_body\":\"การทดสอบแบบผลักดัน Budnow\",\"ad_icon\":\"\",\"ad_id\":\"15254553487150\",\"ad_left_icon\":\"8\",\"ad_link\":\"http://yusi_new.adlocus_api.dev.hxcld.com/dev/redirect/15254553487150/and/6781bc726655d9a0c0122d6dc06ed102cc859db7/a52f5fb66b6c2fc730f19953031a2877631bb09d/15260594057097/native/0/1.0\",\"ad_native_text\":\"การทดสอบแบบผลักดัน\",\"ad_title\":\"การทดสอบแบบผลัก\",\"ad_type\":\"1\",\"sid\":\"15260306041157\",\"track_imp\":\"\",\"err\":\"0\"}";
        final GetNewAndResponse test_orign = MiscUtils.parseJSON(json_origin, GetNewAndResponse.class);
        AdLocusNotification.showNotification(context, test_orign);
        final GetNewAndResponse test_orign2 = MiscUtils.parseJSON(json_origin2, GetNewAndResponse.class);
        AdLocusNotification.showNotification(context, test_orign2);

        String json_bigView = "{\"ad_body\":\"文字\",\"ad_icon\":\"https://s3-ap-northeast-1.amazonaws.com/adlocus-rd-ad-pics/camp_2018/5aa44773444ec816fd1ff34bf92f36ad86f336d91526029819.jpg\",\"ad_id\":\"15260586205778\",\"ad_link\":\"http://yusi_new.adlocus_api.dev.hxcld.com/dev/redirect/15260586205778/and/6781bc726655d9a0c0122d6dc06ed102cc859db7/a52f5fb66b6c2fc730f19953031a2877631bb09d/15260590887083/native/0/1.0\",\"ad_title\":\"標題\",\"ad_type\":\"3\",\"bv_share_link\":\"https://adlocus.com/\",\"sid\":\"15260302885663\",\"track_imp\":\"\",\"err\":\"0\"}";
        final GetNewAndResponse test_bigView = MiscUtils.parseJSON(json_bigView, GetNewAndResponse.class);
        AdLocusNotification.showNotification(context, test_bigView);
    }



}
