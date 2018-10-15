package com.hyxen.adlocusaar.push;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.format.DateUtils;

import com.hyxen.adlocusaar.engine.CellInfo;
import com.hyxen.adlocusaar.engine.HxCellEngine;
import com.hyxen.adlocusaar.engine.OnCellinfoChangeListener;
import com.hyxen.adlocusaar.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

class OffPushEngine implements OnCellinfoChangeListener, DownloadListener
{
//	private static final int CHECK_ALIVE_INTERVAL = 900000;
	private static final int CHECK_ALIVE_INTERVAL = 3600000;
	
	/* cell stage */
	/**
	 * HashMap(lac, HashMap(cid, eids))
	 */
	private final HashMap<Integer, HashMap<Integer, HashSet<String>>> mCgiList = new HashMap<>();

	/**
	 * HashMap(lac, ts)
	 */
	private final HashMap<Integer, Long> mLacTs = new HashMap<>();

	private final AtomicBoolean mIsStart = new AtomicBoolean(false);
	
	public boolean isStart()
	{
		synchronized (mIsStart)
		{
			return mIsStart.get();
		}
	}

	private final Context mContext;

	private HandlerThread mThread;
	private Handler mThreadHandler;

	private ScheduledExecutorService mTriggerManager = Executors.newScheduledThreadPool(1);
	
	private long mTsNearest = 0;

	private final AlarmManager mAlarmManager;
	
    private CellDbDownloader mCellDbDownloader;

    private int mLastMnc = -1;
    private int mLastLac = -1;
    private String mCurrentCity = "";
    private long mLastCityTs = -1;
	
	private final AtomicBoolean mIsCheckingLocal = new AtomicBoolean(false);
	private final Runnable mRunnableCheckLocalCell = new Runnable()
	{
		@Override
		public void run()
		{
			synchronized (mIsCheckingLocal)
			{
				if(mIsCheckingLocal.get())
				{
					return;
				}
				mIsCheckingLocal.set(true);
			}
			synchronized (mIsStart)
			{
				if(!mIsStart.get())
				{
                    synchronized (mIsCheckingLocal)
                    {
                        mIsCheckingLocal.set(false);
                    }
					return;
				}
			}
			checkCellinfo();
			synchronized (mIsCheckingLocal)
			{
				mIsCheckingLocal.set(false);
			}
		}
	};

	synchronized void checkIV()
	{
		synchronized (mIsStart)
		{
			if(!mIsStart.get())
			{
				return;
			}
		}
		checkLocal();
	}

	private synchronized void checkCellinfo()
	{
		CellInfo ci = HxCellEngine.getInstance(mContext).getValidCellInfo();
		if(ci == null)
		{
			return;
		}
		int cid = ci.getCellID();
		int lac = ci.getLac();
	    HashSet<String> eids = new HashSet<>();
	    eids.addAll(EventDbAdapter.getBroadcastEvent(mContext));
        boolean isNeedReloadCity = !DateUtils.isToday(mLastCityTs);
        if(mLastMnc != ci.getMnc() || mLastLac != ci.getLac() || isNeedReloadCity)
        {
            mLastLac = ci.getLac();
            mLastMnc = ci.getMnc();
            String city = City.checkCityWithLac(ci.getMnc(), ci.getLac());
            if (!city.equals(City.UNKNOWN) && (!mCurrentCity.equals(city) || isNeedReloadCity))
            {
                mCurrentCity = city;
                mLastCityTs = System.currentTimeMillis();
                eids.addAll(EventDbAdapter.checkCity(mContext, city));
            }
        }
		if(isValidLac(lac))
		{
            eids.addAll(getMatchEids(lac, cid));
            onMachEventIds(eids);
		}
		else
		{
            onMachEventIds(eids);
            reloadLacFromDatabase(lac);
		}
	}

	OffPushEngine(Context context)
	{
		mContext = context;
		mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
	}

	private synchronized void onMachEventIds(HashSet<String> eids)
	{
		Log.d("onMachEventIds " + eids);
        if(eids.size() == 0)
        {
            return;
        }
		boolean isNeedReload = false;
		boolean hasUpdateTsNearest = false;
		if(mTsNearest < System.currentTimeMillis())
		{
			mTsNearest = 0;
		}


        for (String eid : eids)
        {
            if(eid == null || eid.equals(""))
            {
                continue;
            }
            if(ServiceUtil.isTriggeredEvent(mContext, eid))
            {
				Log.d("isTriggeredEvent " + eid);
                removeEid(eid);
                isNeedReload = true;
                continue;
            }
            //如果無暫存 PushEvent 就從 db 撈
            Event pe = EventDbAdapter.getEventJson(mContext, eid);
            if(pe != null)
            {
                if(pe.isValid())
                {
					Log.d("isValid " + eid);
                    //Match
                    ServiceUtil.saveTriggerEvent(mContext, eid);
//                    String packageName = pe.packages.get(new Random(System.currentTimeMillis()).nextInt(pe.packages.size()));
//                    Log.d("onMetchEvent " + packageName + "," + pe.getEventJson());
                    ServiceUtil.sendEventTrigger(mContext, pe);
                    removeEid(eid);
                    isNeedReload = true;
                }
                else if(pe.isExpired())
                {
					Log.d("isExpired " + eid);
                    removeEid(eid);
                    isNeedReload = true;
                }
                else if(mTsNearest == 0 || pe.getBeginTs() < mTsNearest)
                {
                    mTsNearest = pe.getBeginTs();
                    hasUpdateTsNearest = true;
                }
            }
        }
		if(hasUpdateTsNearest)
		{
			final long ts = mTsNearest + 5000;

			Intent intent =new Intent(mContext, PushService.class);  
			intent.setAction(PushService.ACTION_CHECK_EVENT);
			intent.putExtra("CkeckMinTs", ts);
			PendingIntent sender = PendingIntent.getService(mContext, PushService.REQUEST_CODE_CHECK_EVENT, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT); 

			ServiceUtil.setAlarm(mAlarmManager, AlarmManager.RTC_WAKEUP, ts, sender);
		}
		if(isNeedReload)
		{
			CellInfo ci = HxCellEngine.getInstance(mContext).getValidCellInfo();
			if(ci == null)
			{
				return;
			}
            reloadLacFromDatabase(ci.getLac());
		}
	}
    private void removeEid(String eid)
    {
        CellPushDbAdapter.removeEid(mContext, eid);
        EventDbAdapter.removeEid(mContext, eid);
    }


	private void checkLocal()
	{
		synchronized (mIsStart)
		{
			if(!mIsStart.get())
			{
				return;
			}
		}
		Log.d("checkLocal");
		synchronized (mRunnableCheckLocalCell)
		{
			mThreadHandler.removeCallbacks(mRunnableCheckLocalCell);
			mThreadHandler.post(mRunnableCheckLocalCell);
		}
	}

	private synchronized void reloadLacFromDatabase(int lac)
	{
		updateLac(lac, readDBLac(lac));
		checkIV();
	}

	private HashMap<Integer, HashSet<String>> readDBLac(int lac)
	{
		synchronized (CellPushDbAdapter.LOCK)
		{
			try
			{
				return CellPushDbAdapter.getCis(mContext, lac);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return new HashMap<>();
	}

	public void start()
	{
		synchronized (mIsStart)
		{
			if(mIsStart.get())
			{
				return;
			}

            HxCellEngine.getInstance(mContext).registerListener(this);

			mThread = new HandlerThread("OffPushEngine");
			mThread.start();
			mThreadHandler = new Handler(mThread.getLooper());
            mCellDbDownloader = new CellDbDownloader(mContext, mThreadHandler, this);
		}
	}

	private synchronized void startRun()
	{
		synchronized (mIsStart)
		{
			if(mIsStart.get())
			{
				return;
			}
			mIsStart.set(true);

			HxCellEngine.getInstance(mContext).registerListener(this);
			mTriggerManager.shutdownNow();
			mTriggerManager = Executors.newScheduledThreadPool(1);

			Intent intent = new Intent(mContext, PushService.class);
			intent.setAction(PushService.ACTION_CHECK_ALIVE);
			PendingIntent sender = PendingIntent.getService(mContext, PushService.REQUEST_CODE_CHECK_ALIVE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			long tsNow = System.currentTimeMillis();
			long triggerAtTime = tsNow + new Random(System.currentTimeMillis()).nextInt(CHECK_ALIVE_INTERVAL); //調整到整點
			mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtTime, CHECK_ALIVE_INTERVAL, sender);
		}
	}
	
	synchronized void stop()
	{
		synchronized (mIsStart)
		{
			mTriggerManager.shutdownNow();
			if(!mIsStart.get())
			{
				return;
			}
			HxCellEngine.getInstance(mContext).removeListener(this);
			mThread.quit();
            mCellDbDownloader = null;
			Intent intent = new Intent(mContext, PushService.class);
			intent.setAction(PushService.ACTION_CHECK_ALIVE);  
			PendingIntent sender = PendingIntent.getService(mContext, PushService.REQUEST_CODE_CHECK_ALIVE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			mAlarmManager.cancel(sender);
			
			mIsStart.set(false);
		}
	}

	/**
	 * 
	 * @param lac lac
	 * @param cid hm.put(cid, eids); eids : eid0,eid1,eid2
	 */
	private synchronized void updateLac(int lac, HashMap<Integer, HashSet<String>> cid)
	{
		mCgiList.put(lac, cid);
		mLacTs.put(lac, System.currentTimeMillis());
	}

	private synchronized boolean isValidLac(int lac)
	{
		return mLacTs.get(lac) != null;
	}

	private synchronized ArrayList<String> getMatchEids(int lac, int cid)
	{
		ArrayList<String> ret = new ArrayList<>();
		HashMap<Integer, HashSet<String>>  hm = mCgiList.get(lac);
		if(hm != null)
		{
			HashSet<String> allEids = hm.get(-1);
			HashSet<String> cidEids = hm.get(cid);
			if(allEids != null) ret.addAll(allEids);
			if(cidEids != null) ret.addAll(cidEids);
		}
		return ret;
	}

	@Override
	public void onCellinfoChange(final CellInfo cellInfo)
	{
		Log.d("" + cellInfo);
		if(cellInfo == null || !cellInfo.isValid())
		{
			return;
		}

		checkLocal();
	}


	@Override
	public void onSuccess()
	{
		Log.d("onSuccess");
        //清除重設
        mLastCityTs = -1;
        mLacTs.clear();
        checkIV();
	}
	
	@Override
	public void onLinkGet()
	{
		startRun();
	}

	void checkRemote()
	{
		mCellDbDownloader.check(1000);
	}
}
