package com.hyxen.adlocusaar.engine;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

import com.hyxen.adlocusaar.util.AdLocusUtil;
import com.hyxen.adlocusaar.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public final class HxCellEngine
{
	
	private static HxCellEngine INSTANCE;

	private final CellInfo mGSMCellInfo = new CellInfo();
	private final CellInfo mCDMACellInfo = new CellInfo();
	private final HashSet<OnCellinfoChangeListener> mListener = new HashSet<>();
	private final HashSet<OnCellinfoChangeListener> mRssiListener = new HashSet<>();
	
	private final WeakReference<Context> mContextReference;
	private boolean mIsStart = false;
	
	private long mLastInitCellInfoTime = 0;
	
	private CellInfo mLastCellInfo = null;
	
	private final TelephonyManager mTelephonyManager;
	
	private int mMcc = -1;
	private int mMnc = -1;

	final PhoneStateListener mPhoneListener;
	final PhoneStateListener mPhoneRssiListener = new CellRssiListener();
	
	private boolean isUseNewApiWithOldListener = false;
	private boolean isUseNewApi                = true;


	public static HxCellEngine getInstance(Context context)
    {
    	if(INSTANCE == null || INSTANCE.mContextReference.get() == null)
    	{
    		INSTANCE = new HxCellEngine(context.getApplicationContext());
    	}
    	return INSTANCE;
    }
    
    public int getMcc()
    {
    	return mMcc;
    }
    
    public int getMnc()
    {
    	return mMnc;
    }
    
    private HxCellEngine(Context context)
    {
        if(Build.MANUFACTURER.equals("samsung") || Build.BRAND.equals("samsung"))
        {
            if(AdLocusUtil.targetSdkStyleLollipop(context))
            {
                isUseNewApiWithOldListener = true;
                isUseNewApi                = false;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
        {
            mPhoneListener = new Cell18Listener();
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
        {
            mPhoneListener = new Cell17Listener();
        }
        else
        {
            mPhoneListener = new CellListener();
        }
    	mContextReference = new WeakReference<>(context.getApplicationContext());
    	mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

		String operator = mTelephonyManager.getNetworkOperator();
		if (operator != null)
		{
			try
			{
				mMcc = Integer.parseInt(operator.substring(0, 3));
				mMnc = Integer.parseInt(operator.substring(3));
			}
			catch (Exception ignored) {}
		}
    }
	
	public void initCellinfo()
	{
		if(System.currentTimeMillis() - mLastInitCellInfoTime < 1000) return;

        mLastInitCellInfoTime = System.currentTimeMillis();

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && isUseNewApi)
		{
			newCellFromNewApi(null);
		}
		else
		{
			mGSMCellInfo.setType(CellInfo.CELL_TYPE_GSM);
			mCDMACellInfo.setType(CellInfo.CELL_TYPE_CDMA);
			TelephonyManager tm = mTelephonyManager;
			if (tm != null)
			{
				String operator = tm.getNetworkOperator();
				if (operator != null && !operator.equals(""))
				{
					try
					{
						int mcc = Integer.parseInt(operator.substring(0, 3));
						int mnc = Integer.parseInt(operator.substring(3));
						mMcc = mcc;
						mMnc = mnc;
					}
					catch (Exception ignored) {}
				}
	            else
	            {
	                Context c = mContextReference.get();
	                if (c != null)
	                {
	    				int mcc = c.getResources().getConfiguration().mcc;
	    				int mnc = c.getResources().getConfiguration().mnc;
	    				if (mcc != 0) mMcc = mcc;
	    				if (mnc != 0){
	    					if (mnc == Configuration.MNC_ZERO) mnc = 0;
	    					mMnc = mnc;
	    				}
	                }
	            }
	            mGSMCellInfo.setMcc(mMcc);
	            mGSMCellInfo.setMnc(mMnc);
	            mCDMACellInfo.setMcc(mMcc);
	            mCDMACellInfo.setMnc(mMnc);
				
				CellLocation  location = tm.getCellLocation();
				if(location != null)
				{
					if(tm.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM)
					{
						if (location instanceof GsmCellLocation)
						{
							GsmCellLocation gsm = (GsmCellLocation) location;
							int lac = gsm.getLac();
							int cid = gsm.getCid();
							if (lac != -1) mGSMCellInfo.setLac(lac & 0xffff);
							if (cid != -1) mGSMCellInfo.setCellID(cid & 0xffff);
							checkType(mGSMCellInfo);
							onCellinfoChange(mGSMCellInfo);
						}
					}
					else if(tm.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA)
					{
						if (location instanceof CdmaCellLocation)
						{
							checkType(mCDMACellInfo);
							CdmaCellLocation cdma = (CdmaCellLocation) location;
							mCDMACellInfo.setLac(cdma.getNetworkId());
							mCDMACellInfo.setCellID(cdma.getBaseStationId());
							mCDMACellInfo.setSid(cdma.getSystemId());
							mCDMACellInfo.setLat(cdma.getBaseStationLatitude());
							mCDMACellInfo.setLon(cdma.getBaseStationLongitude());
							onCellinfoChange(mCDMACellInfo);
						}
					}
				}
				else
				{
					mGSMCellInfo.setLac(-1);
					mGSMCellInfo.setCellID(-1);
					mCDMACellInfo.setLac(-1);
					mCDMACellInfo.setCellID(-1);
				}
			}
		}
	}
	
	private void checkType(CellInfo ci)
	{
		ci.setTypeWithNetworktype(mTelephonyManager.getNetworkType(), mTelephonyManager.getPhoneType());
	}
	
	

	private synchronized void start()
	{
		if(!mIsStart)
		{
			initCellinfo();
			Context context = mContextReference.get();
			if(context == null)
			{
				return;
			}
			mIsStart = true;

	        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && isUseNewApi) //original: MR2
	        {
	        	mTelephonyManager.listen(mPhoneListener, PhoneStateListener.LISTEN_CELL_INFO);
	        	new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						if (isUseNewApiWithOldListener) {
							mTelephonyManager.listen(mPhoneListener, PhoneStateListener.LISTEN_CELL_LOCATION);
                            //Log.d("HxCell:isUseNewApiWithOldListener:"+String.valueOf(isUseNewApiWithOldListener)+",isUseNewApi:"+String.valueOf(isUseNewApi));
						}
					}
				}, 1000);
	        }
	        else
	        {
	        	mTelephonyManager.listen(mPhoneListener, PhoneStateListener.LISTEN_CELL_LOCATION | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | PhoneStateListener.LISTEN_SERVICE_STATE);
	        }
		}
	}
	
	private synchronized void stop()
	{
		if(mIsStart)
		{
			mIsStart = false;
			Context context = mContextReference.get();
			if(context == null)
			{
				return;
			}
			TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	        tm.listen(mPhoneListener, PhoneStateListener.LISTEN_NONE);
		}
	}

	private synchronized void onCellinfoRssiChange(CellInfo cellInfo)
	{
		cellInfo = cellInfo.clone();
		if(cellInfo.isValidRssi())
		{
			cellInfo.setTimespan(System.currentTimeMillis());
			mLastCellInfo = cellInfo;
			for (OnCellinfoChangeListener occl : mRssiListener) {
				occl.onCellinfoChange(cellInfo.clone());
			}
		}
	}
	
	private synchronized void onCellinfoChange(CellInfo cellInfo)
	{
		cellInfo = cellInfo.clone();
		if(cellInfo.isValid())
		{
			cellInfo.setTimespan(System.currentTimeMillis());
			mLastCellInfo = cellInfo;
			for (OnCellinfoChangeListener occl : mListener) {
				occl.onCellinfoChange(cellInfo.clone());
			}
			if(cellInfo.isValidRssi())
			{
				for (OnCellinfoChangeListener occl : mRssiListener) {
					occl.onCellinfoChange(cellInfo.clone());
				}
			}
		}
	}

	public CellInfo getGSMCellinfo()
	{
		if(!mIsStart)
		{
			initCellinfo();
		}
		return mGSMCellInfo.clone();
	}

	public CellInfo getCDMACellinfo()
	{
		if(!mIsStart)
		{
			initCellinfo();
		}
		return mCDMACellInfo.clone();
	}
	
	/**
	 * 
	 * @return null if not vaild cellinfo for GSM and CDMA
	 */

	public CellInfo getValidCellInfo()
	{
		if (!hasPermission()) return null;

		if(!mIsStart) initCellinfo();

		if(mGSMCellInfo.isValid()) return mGSMCellInfo.clone();
		else if(mCDMACellInfo.isValid()) return mCDMACellInfo.clone();

		return null;
	}

	private boolean hasPermission() {

		Context context = mContextReference.get();

		return context != null && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
	}


	public synchronized void registerRssiListener(OnCellinfoChangeListener listener)
	{
		if (!hasPermission()) return;
		if(mRssiListener.size() == 0)
		{
			Context context = mContextReference.get();
			if(context == null)
			{
				stop();
				return;
			}
			TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	        tm.listen(mPhoneRssiListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		}
		mRssiListener.add(listener);
		if(mLastCellInfo != null)
		{
			listener.onCellinfoChange(mLastCellInfo);
		}
		start();
	}
	
	public synchronized void registerListener(OnCellinfoChangeListener listener)
	{
		if (!hasPermission()) return;
		mListener.add(listener);
		if(mLastCellInfo != null)
		{
			listener.onCellinfoChange(mLastCellInfo);
		}
		start();
	}
	
	public synchronized void removeListener(OnCellinfoChangeListener listener)
	{
		if(mRssiListener.remove(listener) && mRssiListener.size() == 0)
		{
			Context context = mContextReference.get();
			if(context == null)
			{
				stop();
				return;
			}
			TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			tm.listen(mPhoneRssiListener, PhoneStateListener.LISTEN_NONE);
		}
		mListener.remove(listener);
		if(mListener.size() == 0)
		{
			stop();
		}
	}
	
	
	private class CellRssiListener extends PhoneStateListener
	{
		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength)
		{
    		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && isUseNewApi) //original:MR2
    		{
    			newCellFromNewApi(null);
    		}
    		else
    		{
    			int cdmaRssi = signalStrength.getCdmaDbm();
    			int gsmRssi  = signalStrength.getGsmSignalStrength();
    			int tempRssi = 0;
    			if(signalStrength.isGsm())
    			{
    				tempRssi = -113 + 2 * gsmRssi;
    				if (tempRssi < -110) tempRssi = -110;
    				if(tempRssi != mGSMCellInfo.getRssi())
    				{
    					mGSMCellInfo.setRssi(tempRssi);
    					checkType(mGSMCellInfo);
    					onCellinfoRssiChange(mGSMCellInfo);
    				}
    			}
    			else
    			{
    				tempRssi = cdmaRssi;
    				if (tempRssi < -110) tempRssi = -110;
    				if(tempRssi != mCDMACellInfo.getRssi())
    				{
    					mCDMACellInfo.setRssi(tempRssi);
    					checkType(mCDMACellInfo);
    					onCellinfoRssiChange(mCDMACellInfo);
    				}
    			}
    		}
			super.onSignalStrengthsChanged(signalStrength);
		}
	}

    private class CellListener extends PhoneStateListener
    {
        @Override
        public void onCellLocationChanged(CellLocation location)
        {
    		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && isUseNewApi)
    		{
    			newCellFromNewApi(null);
    		}
    		else
    		{
                if (location instanceof GsmCellLocation)
                {
                    GsmCellLocation gsm = (GsmCellLocation) location;
                    int lac = gsm.getLac();
                    int cid = gsm.getCid();
                    if (lac != -1) mGSMCellInfo.setLac(lac & 0xffff);
                    if (cid != -1) mGSMCellInfo.setCellID(cid & 0xffff);
                    onCellinfoChange(mGSMCellInfo);
				}
                else if(location instanceof CdmaCellLocation)
                {
                    CdmaCellLocation cdma = (CdmaCellLocation) location;
                    mCDMACellInfo.setLac(cdma.getNetworkId());
                    mCDMACellInfo.setCellID(cdma.getBaseStationId());
                    mCDMACellInfo.setSid(cdma.getSystemId());
                    mCDMACellInfo.setLat(cdma.getBaseStationLatitude());
                    mCDMACellInfo.setLon(cdma.getBaseStationLongitude());
                    onCellinfoChange(mCDMACellInfo);
                }
    		}

            super.onCellLocationChanged(location);
        }

        @Override
        public void onServiceStateChanged(ServiceState serviceState)
        {
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && isUseNewApi)
    		{
    			newCellFromNewApi(null);
    		}
    		else
    		{
                try
                {
                    String operator = serviceState.getOperatorNumeric();
                    int mcc = Integer.parseInt(operator.substring(0, 3));
					int mnc = Integer.parseInt(operator.substring(3));

					mGSMCellInfo.setMcc(mcc);
                    mCDMACellInfo.setMcc(mcc);
                    mGSMCellInfo.setMnc(mnc);
                    mCDMACellInfo.setMnc(mnc);
                    onCellinfoChange(mGSMCellInfo);
                    onCellinfoChange(mCDMACellInfo);
                }
                catch (Exception ignored) { }
    		}

            super.onServiceStateChanged(serviceState);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private class Cell17Listener extends CellListener
    {

        @Override
        public void onCellInfoChanged(List<android.telephony.CellInfo> cellInfo)
		{
			newCellFromNewApi_JB_MR1(cellInfo);
            super.onCellInfoChanged(cellInfo);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private class Cell18Listener extends Cell17Listener
    {
        @Override
        public void onCellInfoChanged(List<android.telephony.CellInfo> cellInfo)
        {
			newCellFromNewApi_JB_MR2(cellInfo);
            super.onCellInfoChanged(cellInfo);
        }
        
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void newCellFromNewApi(List<android.telephony.CellInfo> cellInfo){
    	if(cellInfo == null)
			cellInfo = mTelephonyManager.getAllCellInfo();

		if(cellInfo == null) {
			if(isUseNewApiWithOldListener) isUseNewApi = false;
			else isUseNewApiWithOldListener = true;

            Log.d("HxCell:isUseNewApiWithOldListener:"+String.valueOf(isUseNewApiWithOldListener)+",isUseNewApi:"+String.valueOf(isUseNewApi));
			return;
		}

        Log.d("HxCell:isUseNewApiWithOldListener:"+String.valueOf(isUseNewApiWithOldListener)+",isUseNewApi:"+String.valueOf(isUseNewApi));


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			newCellFromNewApi_JB_MR2(cellInfo);
		} else {
			newCellFromNewApi_JB_MR1(cellInfo);
		}
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void newCellFromNewApi_JB_MR1(List<android.telephony.CellInfo> cellInfo){
        if (cellInfo == null)
            return;
        for (android.telephony.CellInfo info: cellInfo)
        {
            if (info instanceof CellInfoGsm)
            {
                CellInfoGsm gsm = (CellInfoGsm) info;
                CellIdentityGsm ci = gsm.getCellIdentity();
                if(ci != null)
                {
                    if(ci.getLac() != Integer.MAX_VALUE) mGSMCellInfo.setLac(ci.getLac());
                    if(ci.getCid() != Integer.MAX_VALUE) mGSMCellInfo.setCellID(ci.getCid());
                    if(ci.getMcc() != Integer.MAX_VALUE) mGSMCellInfo.setMcc(ci.getMcc());
                    if(ci.getMnc() != Integer.MAX_VALUE) mGSMCellInfo.setMnc(ci.getMnc());
                    mGSMCellInfo.setType(CellInfo.CELL_TYPE_GSM);

				}
                CellSignalStrengthGsm ss = gsm.getCellSignalStrength();
                if (ss != null)
                {
                    mGSMCellInfo.setRssi(ss.getDbm());
                }
                onCellinfoChange(mGSMCellInfo);

            }
            else if (info instanceof CellInfoLte)
            {
                CellInfoLte gsm = (CellInfoLte) info;
                CellIdentityLte ci = gsm.getCellIdentity();
                if(ci != null)
                {
                    if(ci.getTac() != Integer.MAX_VALUE) mGSMCellInfo.setLac(ci.getTac());
                    if(ci.getCi() != Integer.MAX_VALUE) mGSMCellInfo.setCellID(ci.getCi());
                    if(ci.getMcc() != Integer.MAX_VALUE) mGSMCellInfo.setMcc(ci.getMcc());
                    if(ci.getMnc() != Integer.MAX_VALUE) mGSMCellInfo.setMnc(ci.getMnc());
                    mGSMCellInfo.setType(CellInfo.CELL_TYPE_LTE);

				}
                CellSignalStrengthLte ss = gsm.getCellSignalStrength();
                if (ss != null)
                {
                    mGSMCellInfo.setRssi(ss.getDbm());
				}
				onCellinfoChange(mGSMCellInfo);
            }
            else if(info instanceof CellInfoCdma)
            {
                CellInfoCdma cdma = (CellInfoCdma) info;
                CellIdentityCdma ci = cdma.getCellIdentity();
                if(ci != null)
                {
                    if(ci.getNetworkId() != Integer.MAX_VALUE) mCDMACellInfo.setLac(ci.getNetworkId());
                    if(ci.getBasestationId() != Integer.MAX_VALUE) mCDMACellInfo.setCellID(ci.getBasestationId());
                    if(ci.getSystemId() != Integer.MAX_VALUE) mCDMACellInfo.setSid(ci.getSystemId());
                    if(ci.getLatitude() != Integer.MAX_VALUE) mCDMACellInfo.setLat(ci.getLatitude());
                    if(ci.getLongitude() != Integer.MAX_VALUE) mCDMACellInfo.setLon(ci.getLongitude());
                    mGSMCellInfo.setType(CellInfo.CELL_TYPE_CDMA);
				}
                CellSignalStrengthCdma ss = cdma.getCellSignalStrength();
                if (ss != null)
                {
                    mCDMACellInfo.setRssi(ss.getCdmaDbm());
                }
                onCellinfoChange(mCDMACellInfo);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void newCellFromNewApi_JB_MR2(List<android.telephony.CellInfo> cellInfo){

		if (cellInfo == null) {
			return;
		}

		ArrayList<android.telephony.CellInfo> newList = new ArrayList<>();
        for (android.telephony.CellInfo info: cellInfo)
        {
            if (info instanceof CellInfoWcdma) //CellInfoWcdma: MR2 only
            {
                CellInfoWcdma gsm = (CellInfoWcdma) info;
                CellIdentityWcdma ci = gsm.getCellIdentity();
                if(ci != null)
                {
                    if(ci.getLac() != Integer.MAX_VALUE) mGSMCellInfo.setLac(ci.getLac());
                    if(ci.getCid() != Integer.MAX_VALUE) mGSMCellInfo.setCellID(ci.getCid());
                    if(ci.getMcc() != Integer.MAX_VALUE) mGSMCellInfo.setMcc(ci.getMcc());
                    if(ci.getMnc() != Integer.MAX_VALUE) mGSMCellInfo.setMnc(ci.getMnc());
                    mGSMCellInfo.setType(CellInfo.CELL_TYPE_WCDMA);

				}
                CellSignalStrengthWcdma ss = gsm.getCellSignalStrength();
                if (ss != null)
                {
                    mGSMCellInfo.setRssi(ss.getDbm());
				}
                onCellinfoChange(mGSMCellInfo);

			}
            else {
            	newList.add(info);
            }
        }

		newCellFromNewApi_JB_MR1(newList);
    }
}

