package com.hyxen.adlocusaar.utils;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.hyxen.adlocusaar.AdLocus;
import com.hyxen.adlocusaar.constants.Constants;
import com.hyxen.adlocusaar.repository.ApiException;
import com.hyxen.adlocusaar.repository.Repository;
import com.hyxen.adlocusaar.repository.data.domain.LbsTaskLlr;
import com.hyxen.adlocusaar.repository.data.request.GetLbsFileRequest;
import com.hyxen.adlocusaar.repository.data.response.GetLbsTaskResponse;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class CheckLbsReceiver extends BroadcastReceiver {
    private static final String TAG = CheckLbsReceiver.class.getSimpleName();
    private static LocationManager mLocationManager;
    private static MyLocationListener mLocationListener;

    @Override
    public void onReceive(final Context context, Intent intent) {
        Logger.d(TAG, "onReceive");

        Bundle bundle = intent.getExtras();
        if (bundle == null || bundle.get(Constants.TAG_BROADCAST_LBS_CHECKER_KEY) == null) {
            Logger.d(TAG, "onReceive : something is null");
            return;
        }
        if(context!=null)Repository.init(context);
        else{Logger.d(TAG, "onReceive : context is null");
            return;}
        Logger.d(TAG, "onReceive : TAG_BROADCAST_LBS_CHECKER_KEY = " + bundle.get(Constants.TAG_BROADCAST_LBS_CHECKER_KEY));
        if (bundle.get(Constants.TAG_BROADCAST_LBS_CHECKER_KEY).equals(Constants.TAG_BROADCAST_LBS_CHECKER_VALUE)) {
            Logger.d(TAG, "onReceive : AG_BROADCAST_LBS_CHECKER_VALUE");
            LbsChecker.getInstance(context).restartAlarmTimer();
//            AdLocus.startGpsLocation();

            GetLbsFileRequest request = new GetLbsFileRequest();
            request.setDeviceId(Repository.getHashDeviceId());
            request.setKey(Repository.getAppKey());
            Disposable task = Repository.getLbsTask(request)
                    .subscribe(new Consumer<GetLbsTaskResponse>() {
                        @Override
                        public void accept(GetLbsTaskResponse getLbsTaskResponse) throws Exception {
                            if (getLbsTaskResponse == null || getLbsTaskResponse.getPt() == null) {
                                Log.i(TAG, "getLbsTaskResponse is null");
                                return;
                            }
                            String responseJson = MiscUtils.toJSONString(getLbsTaskResponse);
                            Repository.setLbsTaskJson(responseJson);

                            if(mLocationManager!=null && mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                                Disposable disposable = Repository.checkLbsDataIsEmpty(context)
                                        .subscribeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Consumer<GetLbsTaskResponse>() {
                                            @Override
                                            public void accept(GetLbsTaskResponse getLbsTaskResponse) throws Exception {
                                                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                                    Logger.i(TAG, "GSP permission is not granted");
                                                    if (getLbsTaskResponse == null || getLbsTaskResponse.getPt() == null) {
                                                        try{
                                                            LbsTaskLlr lr=getLbsTaskResponse.getPt().get(0).getLlr().get(0);
                                                            MyLocationListener.getInstance(getLbsTaskResponse, context).onLocationChanged(Double.parseDouble(lr.getLat()),Double.parseDouble(lr.getLon()));
                                                        }catch(Exception e){e.printStackTrace();}
                                                    }
                                                    return;
                                                }//
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

                                                mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                                                mLocationListener = MyLocationListener.getInstance(getLbsTaskResponse, context, new MyLocationListener.GPSCallbackListener() {
                                                    @Override
                                                    public void end() {
                                                        Logger.d(TAG, "stop listener gps status ");
                                                        if (mLocationManager != null)
                                                            mLocationManager.removeUpdates(mLocationListener);
                                                    }
                                                });
                                                if (mLocationManager != null)
                                                    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
                                            }
                                        }, new Consumer<Throwable>() {
                                            @Override
                                            public void accept(Throwable throwable) throws Exception {
                                                Logger.d("TAG", "failed : " + throwable.getMessage());
                                            }
                                        });
                            }else{
                                try{
                                    LbsTaskLlr lr=getLbsTaskResponse.getPt().get(0).getLlr().get(0);
                                    MyLocationListener.getInstance(getLbsTaskResponse, context).onLocationChanged(Double.parseDouble(lr.getLat()),Double.parseDouble(lr.getLon()));
                                }catch(Exception e){e.printStackTrace();}
                            }




                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            ApiException e = (ApiException) throwable;
                            int code = e.getCode();
                            Logger.d(TAG, "Failed : " + code + throwable.getMessage());
                        }
                    });



//            if(mLocationManager!=null && mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
//                Disposable disposable = Repository.checkLbsDataIsEmpty(context)
//                        .subscribeOn(AndroidSchedulers.mainThread())
//                        .subscribe(new Consumer<GetLbsTaskResponse>() {
//                            @Override
//                            public void accept(GetLbsTaskResponse getLbsTaskResponse) throws Exception {
//                                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                                    Logger.i(TAG, "GSP permission is not granted");
//                                    if (getLbsTaskResponse == null || getLbsTaskResponse.getPt() == null) {
//                                        try{
//                                            LbsTaskLlr lr=getLbsTaskResponse.getPt().get(0).getLlr().get(0);
//                                            MyLocationListener.getInstance(getLbsTaskResponse, context).onLocationChanged(Double.parseDouble(lr.getLat()),Double.parseDouble(lr.getLon()));
//                                        }catch(Exception e){e.printStackTrace();}
//                                    }
//                                    return;
//                                }//
//                                if ( ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                                    Logger.i(TAG, "GPS permission is not granted");
//                                    if (getLbsTaskResponse == null || getLbsTaskResponse.getPt() == null) {
//                                        try{
//                                            LbsTaskLlr lr=getLbsTaskResponse.getPt().get(0).getLlr().get(0);
//                                            MyLocationListener.getInstance(getLbsTaskResponse, context).onLocationChanged(Double.parseDouble(lr.getLat()),Double.parseDouble(lr.getLon()));
//                                        }catch(Exception e){e.printStackTrace();}
//                                    }
//                                    return;
//                                }
//
//                                mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//                                mLocationListener = MyLocationListener.getInstance(getLbsTaskResponse, context, new MyLocationListener.GPSCallbackListener() {
//                                    @Override
//                                    public void end() {
//                                        Logger.d(TAG, "stop listener gps status ");
//                                        if (mLocationManager != null)
//                                            mLocationManager.removeUpdates(mLocationListener);
//                                    }
//                                });
//                                if (mLocationManager != null)
//                                    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
//                            }
//                        }, new Consumer<Throwable>() {
//                            @Override
//                            public void accept(Throwable throwable) throws Exception {
//                                Logger.d("TAG", "failed : " + throwable.getMessage());
//                            }
//                        });
//            }

        }
    }
}
