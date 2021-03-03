package com.hyxen.adlocusaar.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class PhoneCellUtil {
    private final static String TAG = PhoneCellUtil.class.getSimpleName();
    private static WeakReference<Context> mContextRef;
    private static Context ctx;

    private TelephonyManager mTelephonyManager;
    private CellLocation location;

    public PhoneCellUtil(Context context) {
        mContextRef = new WeakReference<Context>(context);
        ctx=context;

        mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

//        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
//                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            try{
//                location = (GsmCellLocation) mTelephonyManager.getCellLocation();
//            }catch(Exception e){e.printStackTrace();}
//        } else {
//            Logger.e(TAG, "[PhoneCellUtil] User not granted permission");
//        }


        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try{
                location = mTelephonyManager.getCellLocation();
            }catch(Exception e){e.printStackTrace();}
        } else {
            Logger.e(TAG, "[PhoneCellUtil] User not granted permission");
        }

    }

    public String getCellType() {
        int networkType=mTelephonyManager.getNetworkType();
        int phoneType =mTelephonyManager.getPhoneType();
        String mType="";
        switch (networkType)
        {
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case 12:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                mType = "c";
                break;
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
                mType = "g";
                break;
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_UMTS:
                mType = "w";
                break;
            case 13:
                mType = "e";
                break;
            default:
                if(phoneType == TelephonyManager.PHONE_TYPE_CDMA)
                {
                    mType = "c";
                }
                else if(phoneType == TelephonyManager.PHONE_TYPE_GSM)
                {
                    mType = "g";
                }
                break;
        }
        return mType;
    }

    public String getCid() {
        Logger.i(TAG, "[Method] -> getCid()");
        if (location == null) {
            Logger.i(TAG, "[getCid] -> location is null");
            return "";
        }
//        return String.valueOf(location.getCid());

        if(mTelephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM){
            if (location instanceof GsmCellLocation){
                GsmCellLocation gsm = (GsmCellLocation) location;
                return String.valueOf(gsm.getCid());
            }
        }else if(mTelephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA){
            if (location instanceof CdmaCellLocation){
                CdmaCellLocation cdma = (CdmaCellLocation) location;
                return String.valueOf(cdma.getBaseStationId());
            }
        }
        return "";
    }

    public String getLac() {
        Logger.i(TAG, "[Method] -> getLac()");
        if (location == null) {
            Logger.i(TAG, "[getLac] -> location is null");
            return "";
        }
//        return String.valueOf(location.getLac());

        if(mTelephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM){
            if (location instanceof GsmCellLocation){
                GsmCellLocation gsm = (GsmCellLocation) location;
                return String.valueOf(gsm.getLac());
            }
        }else if(mTelephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA){
            if (location instanceof CdmaCellLocation){
                CdmaCellLocation cdma = (CdmaCellLocation) location;
                return String.valueOf(cdma.getNetworkId());
            }
        }
        return "";
    }

    public String getMcc() {
        Logger.i(TAG, "[Method] -> getMcc()");
        if (mTelephonyManager == null) {
            Logger.i(TAG, "[getMcc] -> mTelephonyManager is null");
            return "";
        }
        String networkOperator = mTelephonyManager.getSimOperator();
        if (TextUtils.isEmpty(networkOperator)) {
            Logger.i(TAG, "[getMcc] -> networkOperator is empty");
            return "";
        }
        return networkOperator.substring(0, 3);
    }

    public String getMnc() {
        Logger.i(TAG, "[Method] -> getMnc()");
        if (mTelephonyManager == null) {
            Logger.i(TAG, "[getMnc] -> mTelephonyManager is null");
            return "";
        }

        String networkOperator = mTelephonyManager.getSimOperator();
        if (TextUtils.isEmpty(networkOperator)) {
            Logger.i(TAG, "[getMnc] -> networkOperator is empty");
            return "";
        }
        return networkOperator.substring(3);
    }

    public String getRssi() {
        int Mrssi=0;
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return Mrssi+"";
        }
//        List<CellInfo> cellInfo = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            List<CellInfo> cellInfo = mTelephonyManager.getAllCellInfo();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                Mrssi=newCellFromNewApi_JB_MR2(cellInfo);
            } else {
                Mrssi=newCellFromNewApi_JB_MR1(cellInfo);
            }
        }

//        CellInfoGsm cellinfogsm = (CellInfoGsm) mTelephonyManager.getAllCellInfo().get(0);
//        CellSignalStrengthGsm cellSignalStrengthGsm = cellinfogsm.getCellSignalStrength();
//        int Mrssi = cellSignalStrengthGsm.getDbm();
        return Mrssi+"";
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private int newCellFromNewApi_JB_MR1(List<CellInfo> cellInfo){
        if (cellInfo == null)
            return 0;
        for (CellInfo info: cellInfo)
        {
            if (info instanceof CellInfoGsm) {
                CellInfoGsm gsm = (CellInfoGsm) info;
                CellSignalStrengthGsm ss = gsm.getCellSignalStrength();
                if (ss != null){
                    return ss.getDbm();
                }
            } else if (info instanceof CellInfoLte) {
                CellInfoLte gsm = (CellInfoLte) info;
                CellSignalStrengthLte ss = gsm.getCellSignalStrength();
                if (ss != null){
                    return ss.getDbm();
                }
            } else if(info instanceof CellInfoCdma) {
                CellInfoCdma cdma = (CellInfoCdma) info;
                CellSignalStrengthCdma ss = cdma.getCellSignalStrength();
                if (ss != null){
                    return ss.getDbm();
                }
            }
        }
        return 0;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private int newCellFromNewApi_JB_MR2(List<CellInfo> cellInfo){
        if (cellInfo == null)
            return 0;
        ArrayList<CellInfo> newList = new ArrayList<CellInfo>();
        for (CellInfo info: cellInfo) {
            if (info instanceof CellInfoWcdma) {
                CellInfoWcdma gsm = (CellInfoWcdma) info;
                CellSignalStrengthWcdma ss = gsm.getCellSignalStrength();
                if (ss != null){
                    return ss.getDbm();
                }
            }
        }
        return newCellFromNewApi_JB_MR1(newList);
    }
}
