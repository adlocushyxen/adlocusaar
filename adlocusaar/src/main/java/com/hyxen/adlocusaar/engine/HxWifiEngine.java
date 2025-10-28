package com.hyxen.adlocusaar.engine;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.hyxen.adlocusaar.constants.Constants;
import com.hyxen.adlocusaar.repository.Repository;
//import android.support.v4.content.ContextCompat;

public final class HxWifiEngine
{
	private static HxWifiEngine INSTANCE; 
	
	protected final static int WIFI_SCAN_INTERVAL = 120000;
	protected final static int WIFI_SCANRESULT_TIMEOUT = 15000;

//	private WifiManager mWifiMgr = null;
	
	private final Context mContext;
	private final HashMap<OnWifiChangeListener, Integer> mListenerList = new HashMap<>();
	
	private volatile boolean mIsStart = false;
	
//	private WifiInfo[] mLastResult;
	private long mLastResultTs = 0;
	private int mScanInterval = WIFI_SCAN_INTERVAL;
	
	private final Handler mHandler = new Handler();
	
	private String mAMac;
//	private WifiInfo mAWifiInfo;
	
	private boolean canScan = false;
	private boolean needScan = false;
	
	private final Runnable mRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			scan();
			if(mIsStart)
			{
				mHandler.removeCallbacks(mRunnable);
				mHandler.postDelayed(mRunnable, mScanInterval);
			}
		}
	};
	
//	private final BroadcastReceiver receiver = new BroadcastReceiver()
//	{
//		@Override
//		public void onReceive(Context context, Intent intent)
//		{
//			if(intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
//			{
//				if(mIsStart)
//				{
//					if(canScan && needScan)
//					{
//						mHandler.removeCallbacks(mRunnable);
//						mHandler.postDelayed(mRunnable, mScanInterval);
//					}
//				}
//				mLastResult = getScanResult();
//				mLastResultTs = System.currentTimeMillis();
//				for (OnWifiChangeListener listener : mListenerList.keySet()) {
//					listener.onWifiInfoChange(mLastResult);
//				}
//			}
//		}
//	};
    
    public static HxWifiEngine getInstance(Context context)
    {
    	if(INSTANCE == null)
    	{
    		INSTANCE = new HxWifiEngine(context.getApplicationContext());
    	}
    	return INSTANCE;
    }
    
	private WifiInfo[] getScanResult()
	{
		if (!hasPermission(mContext)) return null;

		return new WifiInfo[0];
//		android.net.wifi.WifiInfo wi = mWifiMgr.getConnectionInfo();
//		List<ScanResult> hotspots = mWifiMgr.getScanResults();
//		if(hotspots == null) return null;
//
//		ListIterator<ScanResult> results = hotspots.listIterator();
//		WifiInfo[] infos = new WifiInfo[hotspots.size()];
//		int i = 0;
//		boolean isGetAMac = false;
//		while (results.hasNext())
//		{
//			ScanResult info = results.next();
//			WifiInfo wifiInfo = new WifiInfo();
//			wifiInfo.setName(info.SSID);
//			wifiInfo.setMac(info.BSSID.toLowerCase());
//			wifiInfo.setRssi(info.level);
//			if(wifiInfo.getMac().equals(wi.getBSSID()))
//				wifiInfo.setServ(WifiInfo.SERV_CONNECTED);
//			else
//				wifiInfo.setServ(WifiInfo.SERV_NO_CONNECTION);
//			infos[i++] = wifiInfo;
//			if(!isGetAMac)
//			{
//				mAMac = info.BSSID;
//				mAWifiInfo = wifiInfo;
//				isGetAMac = true;
//			}
//		}
//		return infos;
	}
	
	public String getAMac()
	{
		int userStatement = Repository.getUserAndroidIdState();
		if(userStatement != Constants.TAG_ANDROID_ID_STATEMENT_STATE_GRANT){
			return "";
		}
//		if(!mIsStart)
//		{
//			scan();
//			getScanResult();
//		}
		return mAMac;
	}
	
//	public WifiInfo getAWifiInfo()
//	{
//		int userStatement = Repository.getUserAndroidIdState();
//		if(userStatement != Constants.TAG_ANDROID_ID_STATEMENT_STATE_GRANT){
//			return null;
//		}
//		if(!mIsStart)
//		{
//			scan();
//			getScanResult();
//		}
//		return mAWifiInfo;
//	}
	
//	public WifiInfo[] getResult()
//	{
//		if(mLastResult != null && (System.currentTimeMillis() - mLastResultTs) > WIFI_SCANRESULT_TIMEOUT)
//			mLastResult = null;
//		return mLastResult;
//	}

	private HxWifiEngine(Context context)
	{
		mContext = context;
		canScan = hasScanPermission(mContext);
//		mWifiMgr = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
	}
	
//	public boolean getWifiEnable()
//	{
//		return mWifiMgr.isWifiEnabled();
//	}
	
	/**
	 * 
	 * @param listener listener
	 * @param interval ms ,-1 no scan
	 */
//	public synchronized void registerListener(OnWifiChangeListener listener, int interval)
//	{
//		mListenerList.put(listener, interval);
//		clacInterval();
//		start();
//	}

//	public synchronized void removeListener(OnWifiChangeListener listener)
//	{
//		mListenerList.remove(listener);
//		clacInterval();
//		if(mListenerList.size() == 0)
//		{
//			stop();
//		}
//	}
	
	
	private long mLastScanTs = 0;
	public void scan()
	{
		if(canScan && System.currentTimeMillis() - mLastScanTs > 8000)
		{
			mLastScanTs = System.currentTimeMillis();
//			mWifiMgr.startScan();
		}
	}

	private static boolean hasScanPermission(Context context)
	{
		return context.checkCallingOrSelfPermission(Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_GRANTED;
	}

	private static boolean hasPermission(Context context) {

		int userStatement = Repository.getUserAndroidIdState();
		if(userStatement != Constants.TAG_ANDROID_ID_STATEMENT_STATE_GRANT){
			return false;
		}
		boolean isBackAccess=false;
		if(android.os.Build.VERSION.SDK_INT<29 || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED){
			isBackAccess=true;
		}
		return (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        && isBackAccess
                );
	}
	
//	private void clacInterval()
//	{
//		int min = Integer.MAX_VALUE;
//		Iterator<Integer> iter = mListenerList.values().iterator();
//		Integer interval;
//		boolean hasSet = false;
//		while (iter.hasNext())
//		{
//			interval = iter.next();
//			if(interval == -1)
//			{
//				continue;
//			}
//			hasSet = true;
//			min = Math.min(min, interval);
//		}
//		if(!hasSet)
//		{
//			hasSet = true;
//			min = WIFI_SCAN_INTERVAL;
//		}
//		needScan = hasSet;
//		if(min < 8000)
//		{
//			min = 8000;
//		}
//		mScanInterval = min;
//	}
//
//	private synchronized void start()
//	{
//		if (!hasPermission(mContext)) return;
//		if(!mIsStart)
//		{
//			mIsStart = true;
//			IntentFilter inf = new IntentFilter();
//			inf.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
//			mContext.registerReceiver(receiver, inf);
//			scan();
//			if(needScan)
//			{
//				mHandler.postDelayed(mRunnable, mScanInterval);
//			}
//		}
//	}
//
//	private synchronized void stop()
//	{
//		if(mIsStart)
//		{
//			mIsStart = false;
//			mHandler.removeCallbacks(mRunnable);
//			mContext.unregisterReceiver(receiver);
//		}
//	}
	
	
}