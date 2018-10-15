package com.hyxen.adlocusaar.push;

import android.content.Context;
import android.os.Handler;

import com.hyxen.adlocusaar.engine.CellInfo;
import com.hyxen.adlocusaar.engine.HxCellEngine;
import com.hyxen.adlocusaar.engine.HxWifiEngine;
import com.hyxen.adlocusaar.net.HxRequest;
import com.hyxen.adlocusaar.push.ServiceUtil.PackageInfo;
import com.hyxen.adlocusaar.util.AdLocusUtil;
import com.hyxen.adlocusaar.util.Downloader;
import com.hyxen.adlocusaar.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class CellDbDownloader
{
	private final Context mContext;

	private final Handler mThreadHandler;

	private final DownloadListener mListener;

    private final HashSet<Event> mPendingEvents = new HashSet<>();
    
    private long checkUrlDelayTime = 15000;
	private boolean isRetry = false;

	private final Runnable mRunnableGetUrl = new Runnable()
	{
		@Override
		public void run()
		{
			mLastGetUrlTs = System.currentTimeMillis();
			Log.d("getUrl");
			boolean isDone = true;
			ArrayList<PackageInfo> infos = ServiceUtil.getCurrentServiceList(mContext);
			for (PackageInfo packageInfo : infos)
			{
				Log.d("getUrl " + packageInfo.getPackageName());
				if(!getUrl(packageInfo))
				{
					Log.d("getUrl fail " + packageInfo.getPackageName());
					isDone = false;
				}
				Log.d("getUrl success " + packageInfo.getPackageName());
			}
			ServiceUtil.saveCurrentServiceList(mContext, infos);
			Log.d("getUrl done:" + isDone);
			if(isDone)
			{
				checkUrlDelayTime = 15000;
				if(mListener != null)
				{
					mListener.onLinkGet();
				}
				check(0);
			}
			else
			{
				checkUrlDelayTime *= 2;
				if (checkUrlDelayTime > 1800000) checkUrlDelayTime = 1800000;
				getUrl();
			}
		}
	};

	private final Runnable mRunnableCheck = new Runnable()
	{
		@Override
		public void run()
		{
			int ret;
			ArrayList<PackageInfo> infos = ServiceUtil.getCurrentServiceList(mContext);
			boolean isAllDone = true;
			for (ServiceUtil.PackageInfo packageInfo : infos)
			{
				if (!packageInfo.isReadyToCheck()) continue;
				Log.d("checkRemote " + packageInfo.getPackageName());
				ret = checkRemote(packageInfo);
				Log.d("checkRemote ret:" + ret);
				if (ret == -1)
				{
					Log.d("checkRemote success " + packageInfo.getPackageName());
					packageInfo.updateCheckTs();
					ServiceUtil.saveCurrentServiceList(mContext, infos);
					continue;
				} else if (ret == -2) {
					Log.d("checkRemote -2 " + packageInfo.getPackageName());
					ret = 30000;
				} else if (ret == -3) {
					Log.d("checkRemote no link " + packageInfo.getPackageName());
					getUrl();
					ret = 30000;
				} else {
					Log.d("checkRemote sleep " + ret + " " + packageInfo.getPackageName());
					if(ret < 60000) ret = 60000;
				}
				isAllDone = false;
				check(ret);
			}
			if (isAllDone) {
				downloadDb();
				mListener.onSuccess();
			}
		}

        private void downloadDb()
        {
        	Iterator<Event> iterator = mPendingEvents.iterator();
        	while (iterator.hasNext())
        	{
        		Event event = iterator.next();
        		if (Downloader.syncDownloadAndUnzip(mContext, event.getInfo(), CellPushDbAdapter.DB_NAME + event.getEventId()))
        		{
        			iterator.remove();
        		}
        	}
        }
    };

	public CellDbDownloader(Context context, Handler threadHandler, DownloadListener listener)
	{
		mContext = context;
		mListener = listener;
		mThreadHandler = threadHandler;
		getUrl();
	}

	public void check(long delayMillis)
	{
		synchronized (mRunnableCheck)
		{
			mThreadHandler.removeCallbacks(mRunnableCheck);
			mThreadHandler.postDelayed(mRunnableCheck, delayMillis);
		}
	}

	private long mLastGetUrlTs = 0;
	private void getUrl() {
		synchronized (mRunnableGetUrl) {
			mThreadHandler.removeCallbacks(mRunnableGetUrl);
			Log.d("System.currentTimeMillis() - mLastGetUrlTs > checkUrlDelayTime:" + (System.currentTimeMillis() - mLastGetUrlTs > checkUrlDelayTime));
			if (System.currentTimeMillis() - mLastGetUrlTs > checkUrlDelayTime) mThreadHandler.post(mRunnableGetUrl);
			else mThreadHandler.postDelayed(mRunnableGetUrl, checkUrlDelayTime);
		}
	}
	
	/**
	 * 
	 * @param packageInfo listinfo
	 * @return >0 then wait and retry, -1 has new and success, -2 no need download 
	 */
	private int checkRemote(PackageInfo packageInfo)
	{
		Downloader.deleteOldFiles(mContext.getDatabasePath("aa").getParentFile(), CellPushDbAdapter.DB_NAME);
		CheckRequest r;
		String link;

		CellInfo ci = HxCellEngine.getInstance(mContext).getValidCellInfo();
		if(ci == null && !isRetry)
		{
			isRetry = true;
			return 120000;
		}
		if (ci != null) {
			link = packageInfo.getLink(mContext, ("11," + ci.getMcc() + "," + ci.getMnc()).hashCode());
			if(link == null)
			{
				return -3;
			}
			r = new CheckRequest(mContext, link, packageInfo.getKey(), String.valueOf(ci.getMcc()), String.valueOf(ci.getMnc()), String.valueOf(ci.getLac()), String.valueOf(ci.getCellID()), String.valueOf(ci.getRssi()), AdLocusUtil.getPushTargeting(mContext));

		} else {
			String mac = HxWifiEngine.getInstance(mContext).getAMac();
			int linkKey;
			if (mac != null)
			{
				linkKey = 3;
			}
			else
			{
				linkKey = 5;
			}
			link = packageInfo.getLink(mContext, linkKey);
			if(link == null)
			{
				return -3;
			}
			r = new CheckRequest(mContext, link, packageInfo.getKey(), 2, mac, AdLocusUtil.getPushTargeting(mContext));
		}
		Log.d(link + "\n" + r.getPostString());
		r.run();

		String content = r.getResult();

        Log.d("result :" + content);
		Log.d("he :" + r.getErrorCode());

		if(r.hasError() || content == null || content.equals(""))
		{
			return 120000;
		}
		JSONObject o;
		try 
		{
			o = new JSONObject(content);
		}
		catch (JSONException e)
		{
			return -2;
		}

		Log.d("err :" + o.optInt("err", -999));
		if(o.optInt("err", -999) != 0)
		{
			return -2;
		}
		int ready = o.optInt("ready");
		if(ready == 0)
		{
			return o.optInt("wait", 60) * 3000;
		}
		if(ready != 1)
		{
			return -2;
		}

        EventDbAdapter edb = new EventDbAdapter(mContext);
        edb.open();

        for (int j = 0; j < 3; j++)
        {
            JSONArray a = o.optJSONArray("bc");
            int type = Event.TYPE_BROADCAST;
            switch (j)
            {
                case 0:
                    a = o.optJSONArray("bc");
                    type = Event.TYPE_BROADCAST;
                    break;
                case 1:
                    a = o.optJSONArray("city");
                    type = Event.TYPE_CITY;
                    break;
                case 2:
                    a = o.optJSONArray("pt");
                    type = Event.TYPE_POINT;
                    break;
            }
            if (a != null)
            {
                final int SIZE = a.length();
                for (int i = 0; i < SIZE; i++)
                {
                    JSONObject oo = a.optJSONObject(i);
                    String eid = oo.optString("ad_id");
					if (ServiceUtil.isTriggeredEvent(mContext, eid)) continue;

                    if(type == Event.TYPE_POINT)
                    {
                        mPendingEvents.add(new Event(type, oo.toString()));
                    }
                    edb.addEvent(type, eid, oo.toString(), packageInfo.getPackageName());
                }
            }
        }
        edb.close();
        return -1;
	}

	private boolean getUrl(PackageInfo packageInfo)
	{
		HxRequest r = new HxRequest(mContext, AdLocusUtil.URL_PUSH_CLICK);
		int devType = AdLocusUtil.setRequestParameters(mContext, r, packageInfo.getKey(), null, null, AdLocusUtil.getPushTargeting(mContext));

		String link = packageInfo.getLink(mContext, devType);
		if(link != null)
		{
			return true;
		}
		Log.d("getUrl r " + AdLocusUtil.URL_PUSH_CLICK + "\n" + r.getPostString());
		r.run();
		if(r.hasError())
		{
			Log.d("getUrl re " + r.getErrorCode() + packageInfo.getPackageName());
			return false;
		}
		try
		{
			JSONObject o = new JSONObject(r.getResult());
			if(o.getInt("err") == 0)
			{
				Log.d("getUrl sucess type:" + devType + "," + packageInfo.getPackageName());
				packageInfo.setLink(mContext, o.getString("json_link"), devType);
				return true;
			}
		}
		catch (JSONException ignored) {}
		return false;
	}

}
