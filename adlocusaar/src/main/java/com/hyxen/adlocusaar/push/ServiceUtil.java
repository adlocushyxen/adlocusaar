package com.hyxen.adlocusaar.push;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.text.format.DateUtils;

import com.hyxen.adlocusaar.AdLocusTargeting;
import com.hyxen.adlocusaar.util.AdLocusUtil;
import com.hyxen.adlocusaar.util.Log;
import com.hyxen.adlocusaar.util.MultiProcessPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class ServiceUtil
{
	static final String INTENT_ACTION_RECEIVE = "com.hyxen.adlocusaar.action.RECEIVE";

	static final String ACTION_CHECK_DELAY = "ACTION_CHECK_DELAY";
	static final String ACTION_CHECK_EVERY_HOUR = "ACTION_CHECK_EVERY_HOUR";
	static final String ACTION_CHECK_ALIVE = "ACTION_CHECK_ALIVE";
	static final String ACTION_IM_ALIVE = "ACTION_IM_ALIVE";
	/**
	 * 檢查是否有 service
	 * EXTRA_KEY string, EXTRA_PACTAGE string, EXTRA_IS_PUSH_ENABLE boolean
	 */
	static final String ACTION_CHECK = "ACTION_CHECK";
	static final String ACTION_CHECK_BACK = "ACTION_CHECK_BACK";
	static final String ACTION_CHECK_DONE = "ACTION_CHECK_DONE";

	static final String ACTION_UPDATE_PACKAGE_INFO = "ACTION_UPDATE_PACKAGE_INFO";
	static final String EXTRA_PACKAGE_INFO = "PACKAGE_INFO";

	public static final int REQUEST_CODE_CHECK_DONE = 101;
	public static final int REQUEST_CODE_CHECK = 102;
	public static final int REQUEST_CODE_CHECK_EVENT_HOUR = 103;

	/**
	 * 啟動 service
	 * EXTRA_PACTAGE string, EXTRA_TS long
	 */
	static final String ACTION_STARTUP_SERVICE = "ACTION_STARTUP_SERVICE";
	static final String ACTION_ERROR = "ACTION_ERROR";

	static final String EXTRA_ACTION = "a";
	static final String EXTRA_SENDER = "s";
	static final String EXTRA_RECIPIENT = "recipient";
	static final String EXTRA_VERSION = "v";
	static final String EXTRA_VERSION_STRING = "vs";
    static final String EXTRA_CHECK = "c";
	

	static final String EXTRA_ERROR = "e";

	static final String ACTION_EVENT_TRIGGER = "ACTION_EVENT_TRIGGER";
	static final String EXTRA_EVENT = "t";
	
	private static final String KEY_NAME = ServiceUtil.class.getName();

	public static boolean isCurrentService(Context context)
	{
//		Log.d("getStartTs" + getStartTs(context));
		return DateUtils.isToday(getStartTs(context));
	}

	/**
	 * 儲存驗證正確的 key 有 key 才能繼續檢查
	 */
	public static void saveValidKey(Context context, String key)
	{
		Log.d("valid_key:" + key);
		MultiProcessPreferences.getDefaultSharedPreferences(context).edit().putString("valid_key", key).apply();
	}
	
	/**
	 * 取得驗證過的 key , null 表示沒驗證過
	 */
	public static String getValidKey(Context context)
	{
		String key = MultiProcessPreferences.getDefaultSharedPreferences(context).getString("valid_key", null);
		Log.d("valid_key:" + key);
		return key;
	}

	
	/**
	 * 儲存已觸發的Event
	 */
	public static void saveTriggerEvent(Context context, String eventId)
	{
		String eids = context.getSharedPreferences(KEY_NAME, Context.MODE_PRIVATE).getString("trigger_eid", "");
		if(eids.contains(eventId))
		{
			context.getSharedPreferences(KEY_NAME, Context.MODE_PRIVATE).edit().putLong("trigger_ts", System.currentTimeMillis()).commit();
			return;
		}
		eids = eids + eventId + ",";
		context.getSharedPreferences(KEY_NAME, Context.MODE_PRIVATE)
		.edit()
		.putString("trigger_eid", eids)
		.putLong("trigger_ts", System.currentTimeMillis())
		.apply();
	}
	
	/**
	 * 檢查是否為已觸發的Event ，最後觸發時間不非當日一律重新檢查
	 */
	public static boolean isTriggeredEvent(Context context, String eventId)
	{
		long ts = context.getSharedPreferences(KEY_NAME, Context.MODE_PRIVATE).getLong("trigger_ts", 0);
		if(!DateUtils.isToday(ts))
		{
			context.getSharedPreferences(KEY_NAME, Context.MODE_PRIVATE).edit().putString("trigger_eid", "").putLong("trigger_ts", 0).commit();
			return false;
		}
		String eids = context.getSharedPreferences(KEY_NAME, Context.MODE_PRIVATE).getString("trigger_eid", "");
		return eids.contains(eventId);
	}
	
	public static void clearTriggeredEvent(Context context)
	{
		context.getSharedPreferences(KEY_NAME, Context.MODE_PRIVATE).edit().putString("trigger_eid", "").commit();
	}
	
	/**
	 * 
	 * @param context context
	 * @param startTs null 如果沒有正在執行的Serivce
	 */
	public static void saveStartTs(Context context, long startTs)
	{
		context.getSharedPreferences(KEY_NAME, Context.MODE_PRIVATE).edit().putLong("startTs", startTs).commit();
	}
	
	public static long getStartTs(Context context)
	{
		return context.getSharedPreferences(KEY_NAME, Context.MODE_PRIVATE).getLong("startTs", -1);
	}
	
	static boolean isWaitingForCheck(Context context, long delayMillis)
	{
		long d = System.currentTimeMillis() - context.getSharedPreferences(KEY_NAME, Context.MODE_PRIVATE).getLong("isChecking", 0);
		return d < 40000 || Math.abs(d) < delayMillis;
	}
	
	static void saveStartForCheckDelayed(Context context, long delayMillis)
	{
		context.getSharedPreferences(KEY_NAME, Context.MODE_PRIVATE).edit().putLong("isChecking", System.currentTimeMillis() + delayMillis).commit();
	}
	public static void savePackageInfo(Context context, PackageInfo packageInfo)
	{
		context.getSharedPreferences(KEY_NAME, Context.MODE_PRIVATE).edit().putString("packageInfo", packageInfo.toString()).commit();
	}

	public static PackageInfo getPackageInfo(Context context)
	{
		String json = context.getSharedPreferences(KEY_NAME, Context.MODE_PRIVATE).getString("packageInfo", null);

		if (json == null) return new PackageInfo(context.getPackageName(), getValidKey(context), getStartTs(context), AdLocusUtil.isPause(context));

		PackageInfo info = new PackageInfo(json);
		info.key = getValidKey(context);
		info.pushStartTs = getStartTs(context);
		info.isPause = AdLocusUtil.isPause(context);
		return info;
	}
	
	/**
	 * 取得正可用service 列表
	 */
	public synchronized static ArrayList<PackageInfo> getServiceList(Context context)
	{
		return getServiceList(context, "serviceList");
	}
	
	private synchronized static ArrayList<PackageInfo> getServiceList(Context context, String key)
	{
		return phaseToList(context.getSharedPreferences(KEY_NAME, Context.MODE_PRIVATE).getString(key, "[]"));
	}
	
	private synchronized static ArrayList<PackageInfo> phaseToList(String json)
	{
		Log.d("phaseToList" + json);
		ArrayList<PackageInfo> ret = new ArrayList<>();
		try
		{
			JSONArray a = new JSONArray(json);
			final int SIZE = a.length();
			for (int i = 0; i < SIZE; i++)
			{
				ret.add(new PackageInfo(a.getString(i)));
			}
		}
		catch (JSONException ignored)
		{
		}
		return ret;
	}
	
	public static void saveServiceList(Context context, ArrayList<PackageInfo> serviceList)
	{
        JSONArray a = new JSONArray();
        for (PackageInfo packageInfo : serviceList)
        {
            a.put(packageInfo.toString());
        }
		context.getSharedPreferences(KEY_NAME, Context.MODE_PRIVATE).edit().putString("serviceList", a.toString()).commit();
	}
	
	public synchronized static void saveCurrentServiceList(Context context, ArrayList<PackageInfo> serviceList)
	{
        JSONArray a = new JSONArray();
        for (PackageInfo packageInfo : serviceList)
        {
            a.put(packageInfo.toString());
        }
		String s = a.toString();
		Log.d("saveCurrentServiceList:" + s);
		context.getSharedPreferences(KEY_NAME, Context.MODE_PRIVATE).edit().putString("CurrentServiceList", s).commit();
		sendUpdatePackageInfo(context, s);
	}

	public synchronized static void updatePackageInfo(Context context, String newInfo){

		String packageName = context.getPackageName();

		ArrayList<PackageInfo> listNew = phaseToList(newInfo);

		for (PackageInfo packageInfo : listNew) {
			if (packageName.equals(packageInfo.getPackageName())) savePackageInfo(context, packageInfo);
		}
	}
	
	public synchronized static ArrayList<PackageInfo> getCurrentServiceList(Context context)
	{
		return getServiceList(context, "CurrentServiceList");
	}

	public synchronized static void saveCurrentServiceList(Context context, String serviceList)
	{
		ArrayList<PackageInfo> listNew = phaseToList(serviceList);
		ArrayList<PackageInfo> listCurrent = getCurrentServiceList(context);
		for (PackageInfo packageInfo : listNew)
		{
			if(!listCurrent.contains(packageInfo))
			{
				listCurrent.add(packageInfo);
				Log.d("saveCurrentServiceList:add:" + packageInfo);
			}
		}
		Iterator<PackageInfo> iCurrent = listCurrent.iterator();
		while (iCurrent.hasNext())
		{
			PackageInfo packageInfo = iCurrent.next();
			if(!listNew.contains(packageInfo))
			{
				Log.d("saveCurrentServiceList:remove:" + packageInfo);
				iCurrent.remove();
			}
		}
		saveCurrentServiceList(context, listCurrent);
//		context.getSharedPreferences(KEY_NAME, Context.MODE_PRIVATE).edit().putString("CurrentServiceList", serviceList).commit();
	}
	
	public synchronized static boolean isInCurrentServiceList(Context context, String packageName)
	{
		ArrayList<PackageInfo> listCurrent = getCurrentServiceList(context);
		for (PackageInfo packageInfo : listCurrent)
		{
			if(packageName.equals(packageInfo.packageName))
			{
				return true;
			}
		}
		return false;
	}

	public synchronized static PackageInfo chooseAService(Context context)
	{
		ArrayList<PackageInfo> services = getServiceList(context);
		PackageInfo ret = null;
		long minTs = Long.MAX_VALUE;
		for (PackageInfo packageInfo : services)
		{
			if (packageInfo.isPause) continue;
			if(packageInfo.isCurrentService())
			{
				return packageInfo;
			}
			if(packageInfo.pushStartTs < minTs)
			{
				minTs = packageInfo.pushStartTs;
				ret = packageInfo;
			}
		}
		if(ret != null)
		{
			ret.pushStartTs = System.currentTimeMillis();
		}
		saveServiceList(context, services);
		return ret;
	}

	public static void sendUpdatePackageInfo(Context context, String newInfo)
	{
		Intent i = getIntent(context, ACTION_UPDATE_PACKAGE_INFO, null);
		i.putExtra(EXTRA_PACKAGE_INFO, newInfo);
		context.sendBroadcast(i);
	}

	public static void sendStartupService(Context context, String packageName)
	{
		Intent i = getIntent(context, ACTION_STARTUP_SERVICE, packageName);

        JSONArray a = new JSONArray();
        ArrayList<PackageInfo> serviceList = getServiceList(context);
        for (PackageInfo packageInfo : serviceList)
        {
            a.put(packageInfo.toString());
        }
		i.putExtra(EXTRA_PACKAGE_INFO, a.toString());
		context.sendBroadcast(i);
	}
	
	public static void sendCheckService(Context context, String appKey)//, boolean isPushEnable)
	{
		saveValidKey(context, appKey);
		sendCheckServiceDelayed(context, 1000);
	}
	
	public synchronized static void sendCheckServiceDelayed(Context context, long delayMillis)
	{
		if(!ServiceUtil.isWaitingForCheck(context, delayMillis))
		{
			AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			Log.d("!isWaitingForCheck");
			ServiceUtil.saveStartForCheckDelayed(context, delayMillis);
			Intent i = getIntent(context, ACTION_CHECK_DELAY, context.getPackageName());
			i.setClass(context, PushReceive.class);
			
			PendingIntent pi = PendingIntent.getBroadcast(context, REQUEST_CODE_CHECK, i, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT);
			setAlarm(am, AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delayMillis, pi);
		}
	}
	
	public synchronized static void setAlarm(AlarmManager alarmManager, int type, long triggerAtMillis, PendingIntent operation)
	{
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT)
        {
    		alarmManager.set(type, triggerAtMillis, operation);
        }
        else
        {
        	setAlarm19(alarmManager, type, triggerAtMillis, operation);
        }
	}

	@TargetApi(19)
	private synchronized static void setAlarm19(AlarmManager alarmManager, int type, long triggerAtMillis, PendingIntent operation)
	{
		alarmManager.setExact(type, triggerAtMillis, operation);
	}
	
	/**
	 * 每小時檢查是否死掉，正在執行的在每半點送存活訊號<br/>
	 * 非執行中的Service，每小時的35分如果沒收到存活訊號就做檢查
	 */
	public synchronized static void checkEveryHour(Context context)
	{
		//如果這個小時已經收到我活著就註冊為下一小時

		long aliveTime = context.getSharedPreferences(KEY_NAME, Context.MODE_PRIVATE).getLong("alive_time", 0);
		boolean isThisHourReceiveImAlive = ((System.currentTimeMillis() / 3600000) == (aliveTime / 3600000));
		int hourDiff = isThisHourReceiveImAlive ? 300000 : 0;
		
		//非正在執行的+5分鐘
		boolean isCurrentService = isCurrentService(context);
		int diff = isCurrentService ? 0 : 300000;
		long now = System.currentTimeMillis() + hourDiff;
		long regTs = now + (3600000 - ((now + 1800000) % 3600000)) + diff;
//		Log.d(now + "\n" + regTs + "\n" + (3600000 - ((now + 1800000) % 3600000)));
//		Log.d(DateUtils.formatDateTime(context, now, DateUtils.FORMAT_SHOW_TIME));
//		Log.d(DateUtils.formatDateTime(context, regTs, DateUtils.FORMAT_SHOW_TIME));
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		
		Intent i = getIntent(context, isCurrentService ? ACTION_CHECK_ALIVE : ACTION_CHECK_EVERY_HOUR, context.getPackageName());
		i.setClass(context, PushReceive.class);
		PendingIntent pi = PendingIntent.getBroadcast(context, REQUEST_CODE_CHECK_EVENT_HOUR, i, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT);  


		setAlarm(am, AlarmManager.RTC_WAKEUP, regTs, pi);
	}
	
	
	public synchronized static void sendStartCheckService(Context context)
	{
		if(ServiceUtil.isInCurrentServiceList(context, context.getPackageName()) &&
				hasCurrentServiceRunning(context) &&
				(System.currentTimeMillis() - getLastCheckTs(context) < 10000)) return;

		ServiceUtil.saveLastCheckTs(context, System.currentTimeMillis());
		ServiceUtil.saveStartForCheckDelayed(context, 0);
		ServiceUtil.clearServiceList(context);
		Intent i = getIntent(context, ACTION_CHECK, null);
		context.sendBroadcast(i);
	}

	/**
	 * 送傳service 起動時間
	 */
	static void sendCheckBack(Context context, String backPackage)
	{
		String key = getValidKey(context);
		if(key == null)
		{
			return;
		}
		Intent i = getIntent(context, ACTION_CHECK_BACK, backPackage);
		i.putExtra(EXTRA_PACKAGE_INFO, getPackageInfo(context).toString());
		context.sendBroadcast(i);
	}
	
	static void sendError(Context context, String backPackage, String error)
	{
		Intent i = getIntent(context, ACTION_ERROR, backPackage);
		i.putExtra(EXTRA_ERROR, error);
		context.sendBroadcast(i);
	}
	
	static void sendEventTrigger(Context context, Event event)
	{
        String triggerPackage = null;
        while (event.packages.size() > 0 && !isAppInstalled(context, (triggerPackage = event.packages.get(new Random(System.currentTimeMillis()).nextInt(event.packages.size())))))
        {
			event.packages.remove(triggerPackage);
        	EventDbAdapter.removePackage(context, triggerPackage);
        }
        if(triggerPackage == null || event.packages.size() == 0)
        {
        	return;
        }
        String triggerJson = event.getEventJson();
        Log.d("onMatchEvent " + triggerPackage + "," + triggerJson);
		Intent i = getIntent(context, ACTION_EVENT_TRIGGER, triggerPackage);
		i.putExtra(EXTRA_EVENT, triggerJson);
		context.sendBroadcast(i);
	}
	
	static boolean isAppInstalled(Context context, String packageName)
	{
        PackageManager pm = context.getPackageManager();
       
        boolean app_installed;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        }
        catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed ;
	}
	
	static Intent getIntent(Context context, String action, String recipient)
	{
		Intent i = new Intent(INTENT_ACTION_RECEIVE);
		i.putExtra(EXTRA_ACTION, action);
		i.putExtra(EXTRA_RECIPIENT, recipient);
		i.putExtra(EXTRA_SENDER, context.getPackageName());
		i.putExtra(EXTRA_VERSION, AdLocusUtil.VERSION_INT);
		i.putExtra(EXTRA_VERSION_STRING, AdLocusUtil.VERSION_STRING);
        i.putExtra(EXTRA_CHECK, checkSum(context.getPackageName(), action, recipient));
		//Log.d("send :" + i.getExtras().toString());
		return i;
	}

    static String checkSum(String packageName, String action, String recipient){
		String o = AdLocusUtil.PREFIX + action + recipient + packageName + AdLocusUtil.VERSION_INT;
		//		Log.d("checkSum:" + o + "," + c);
        return AdLocusUtil.sha1(o);
    }

    static boolean check(String checkSum, String packageName, String action, String recipient) {
        return checkSum.equals(checkSum(packageName, action, recipient));
    }

	static class PackageInfo
	{
		private String packageName = null;
		private String key = null;
		private long pushStartTs = -1;
		private String link = null;
		private long linkTs = -1;
		private int linkKey = -1;
		private String targeting = null;
		private long currentOid = -1;
		private boolean isPause = false;
		private long lastCheckTs = 0;

		private PackageInfo(String packageName, String key, long pushStartTs, boolean isPause)
		{
			this.packageName = packageName;
			this.key = key;
			this.pushStartTs = pushStartTs;
			this.isPause = isPause;
		}

		public void updateCheckTs(){
			lastCheckTs = System.currentTimeMillis();
		}

		public void resetCheckTs(){
			lastCheckTs = 0;
		}

		public boolean isReadyToCheck(){
			return System.currentTimeMillis() - lastCheckTs > 3600000;
		}
		
		public String getPackageName()
		{
			return this.packageName;
		}
		
		public String getKey()
		{
			return this.key;
		}
		
		public boolean isCurrentService()
		{
			return DateUtils.isToday(this.pushStartTs);
		}
		
		public void setLink(Context context, String link, int linkKey)
		{
			this.link = link;
			this.linkKey = linkKey;
			this.linkTs = System.currentTimeMillis();
			this.targeting = AdLocusUtil.toString(AdLocusUtil.getPushTargeting(context));
		}

		public long getCurrentOid()
		{
			return currentOid;
		}

		public void setCurrentOid(long currentOid)
		{
			this.currentOid = currentOid;
		}

		public PackageInfo(String json)
		{
			try
			{
				JSONObject o = new JSONObject(json);
				packageName = o.optString("pn", null);
				key = o.optString("k", null);
				pushStartTs = o.optLong("e", -1);
				link = o.optString("u", null);
				linkTs = o.optLong("lt", -1);
				linkKey = o.optInt("lk", -1);
				targeting = o.optString("t", "{}");
				currentOid = o.optLong("co", -1);
				isPause = o.optBoolean("p", false);
				lastCheckTs = o.optLong("l", 0);
			}
			catch (JSONException ignored)
			{
			}
			
		}
		
		@Override
		public String toString()
		{
			JSONObject o = new JSONObject();
			try {
				o.put("pn", packageName);
				o.put("k", key);
				o.put("e", pushStartTs);
				o.put("u", link);
				o.put("lt", linkTs);
				o.put("lk", linkKey);
				o.put("t", targeting);
				o.put("co", currentOid);
				o.put("p", isPause);
				o.put("l", lastCheckTs);
			} catch (JSONException ignored) {
			}
			
			return o.toString();
		}
		
		@Override
		public boolean equals(Object o)
		{
			if(o instanceof PackageInfo)
			{
				PackageInfo li = (PackageInfo) o;
				return isStringEqual(li.packageName, packageName) && isStringEqual(li.key, key);
			}
			return super.equals(o);
		}
		
		public String getLink(Context context, int linkKey)
		{
			if(this.linkKey != linkKey)
			{
				Log.d("getLink key :" + this.linkKey + "!=" + linkKey);
				return null;
			}
			AdLocusTargeting alt = AdLocusUtil.toTargeting(targeting);
			AdLocusTargeting alt1 = AdLocusUtil.getPushTargeting(context);
			if(alt.equals(alt1))
			{
				if(System.currentTimeMillis() - linkTs < 86400000 * 7)
				{
					return link;
				}
			}
			return null;
		}
	}

	public static void saveLastCheckTs(Context context, long ts)
	{
		context.getSharedPreferences(KEY_NAME, Context.MODE_PRIVATE).edit().putLong("lastCheckTs", ts).commit();
	}
	
	public static long getLastCheckTs(Context context)
	{
		return context.getSharedPreferences(KEY_NAME, Context.MODE_PRIVATE).getLong("lastCheckTs", 0);
	}
	
	public static void clearServiceList(Context context)
	{
		context.getSharedPreferences(KEY_NAME, Context.MODE_PRIVATE).edit().remove("serviceList").commit();
	}

	public synchronized static void updateServiceList(Context context, PackageInfo packageInfo)
	{
		ArrayList<PackageInfo> list = getServiceList(context);
		int index = list.indexOf(packageInfo);
		if(index == -1)
		{
			list.add(packageInfo);
		}
		else
		{
			list.remove(index);
			list.add(packageInfo);
		}
		saveServiceList(context, list);
	}

	/**
	 * 判斷兩字串是否相等，字串可為 null
	 * @param string1 string1
	 * @param string2 string2
	 * @return true, if two string is the same.
	 */
	public static boolean isStringEqual(String string1, String string2) {
		return string1 == string2 || !(string1 == null || string2 == null) && string1.equals(string2);
	}
	
	/**
	 * 
	 * @param context context
	 * @return running service count
	 */
	static synchronized boolean hasCurrentServiceRunning(Context context)
	{

		ArrayList<PackageInfo> listCurrent = getCurrentServiceList(context);
		PackageInfo currentInfo = null;
		for (PackageInfo packageInfo : listCurrent)
		{
			if(packageInfo.isCurrentService())
			{
				currentInfo = packageInfo;
				break;
			}
		}
		if(currentInfo == null)
		{
			return false;
		}
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
		{
			if(service.service.getPackageName().equals(currentInfo.packageName) && 
			   PushService.class.getName().equals(service.service.getClassName()))
			{
				return true;
			}
			
		}
		return false;
	}

	public static void sendImAlive(Context context)
	{
		if(hasCurrentServiceRunning(context))
		{
			Intent i = getIntent(context, ACTION_IM_ALIVE, null);
			context.sendBroadcast(i);
		}
	}
	
	/**
	 * 收到我存活
	 */
	public static void receiveImAlive(Context context)
	{
		context.getSharedPreferences(KEY_NAME, Context.MODE_PRIVATE).edit().putLong("alive_time", System.currentTimeMillis()).commit();
        checkEveryHour(context);
	}

	static String validAction(Context context, Intent intent){

		String recipient = intent.getStringExtra(ServiceUtil.EXTRA_RECIPIENT);
		if (TestUtil.testAction(context, intent, recipient)) return null;

		if(intent.getIntExtra(ServiceUtil.EXTRA_VERSION, 0) != AdLocusUtil.VERSION_INT) return null;

		String sender    = intent.getStringExtra(ServiceUtil.EXTRA_SENDER);
		String action    = intent.getStringExtra(ServiceUtil.EXTRA_ACTION);
		String checkSum  = intent.getStringExtra(ServiceUtil.EXTRA_CHECK);
		Log.d(sender + ", " + recipient + "," + action);
		if (checkSum == null || !ServiceUtil.check(checkSum, sender, action, recipient)) return null;


		Log.d("onReceive check ok");

		if(recipient != null && !context.getPackageName().equals(recipient))
		{
			//call correct service up and terminate myself ?
			if(ServiceUtil.ACTION_STARTUP_SERVICE.equals(action))
			{
				String serviceList = intent.getStringExtra(ServiceUtil.EXTRA_PACKAGE_INFO);
				ServiceUtil.saveCurrentServiceList(context, serviceList);
				context.stopService(new Intent(context, PushService.class));
			}
			return null;
		}
		return action;
	}

	static void doAction(Context context, Intent intent){
		String action = ServiceUtil.validAction(context, intent);
		if (action == null) return;
		Log.d("doAction:" + action);

		Intent iPushService = new Intent(context, PushService.class);
		ServiceUtil.checkEveryHour(context);

		if(ServiceUtil.ACTION_CHECK_DELAY.equals(action))
		{
			ServiceUtil.sendStartCheckService(context);
		}
		else if(ServiceUtil.ACTION_CHECK.equals(action))
		{
			if(context.getPackageName().equals(intent.getStringExtra(ServiceUtil.EXTRA_SENDER))) {
				Intent ii = ServiceUtil.getIntent(context, ServiceUtil.ACTION_CHECK_DONE, context.getPackageName());
				ii.setClass(context, PushReceive.class);
				PendingIntent pi = PendingIntent.getBroadcast(context, 12345, ii, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT);

				AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
				ServiceUtil.setAlarm(am, AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 30000, pi);
			}
			if (AdLocusUtil.isNotificationEnable(context)) ServiceUtil.sendCheckBack(context, intent.getStringExtra(ServiceUtil.EXTRA_SENDER));
		}
		else if(ServiceUtil.ACTION_CHECK_BACK.equals(action))
		{
			String packageInfo = intent.getStringExtra(ServiceUtil.EXTRA_PACKAGE_INFO);
			ServiceUtil.updateServiceList(context, new PackageInfo(packageInfo));
		}
		else if(ServiceUtil.ACTION_CHECK_DONE.equals(action))
		{
			PackageInfo li = ServiceUtil.chooseAService(context);
			if(li != null)
			{
				ServiceUtil.sendStartupService(context, li.getPackageName());

				Log.d("sent start service: " + li.getPackageName());
			}
		}
		else if(ServiceUtil.ACTION_STARTUP_SERVICE.equals(action))
		{
			String serviceList = intent.getStringExtra(ServiceUtil.EXTRA_PACKAGE_INFO);
			//Log.d("starting service: " + recipient + "," + serviceList);

			ServiceUtil.saveCurrentServiceList(context, serviceList);
			ServiceUtil.saveStartTs(context, System.currentTimeMillis());
			iPushService.setAction(PushService.ACTION_CHECK_ALIVE);

			context.startService(iPushService);
		}
		else if(ServiceUtil.ACTION_ERROR.equals(action))
		{
			String error = intent.getStringExtra(ServiceUtil.EXTRA_ERROR);
			Log.e(error, new Exception());
		}
		else if(ServiceUtil.ACTION_EVENT_TRIGGER.equals(action))
		{
			String event = intent.getStringExtra(ServiceUtil.EXTRA_EVENT);
			iPushService.setAction(ServiceUtil.ACTION_EVENT_TRIGGER);
			iPushService.putExtra(ServiceUtil.EXTRA_EVENT, event);

			//check can I start service ?
			context.startService(iPushService);
		}
		else if(ServiceUtil.ACTION_CHECK_ALIVE.equals(action))
		{
			ServiceUtil.sendImAlive(context);
		}
		else if(ServiceUtil.ACTION_IM_ALIVE.equals(action))
		{
			ServiceUtil.receiveImAlive(context);
		}
		else if(ServiceUtil.ACTION_CHECK_EVERY_HOUR.equals(action))
		{
			iPushService.setAction(PushService.ACTION_CHECK_TYPE3);
			context.startService(iPushService);
			ServiceUtil.sendStartCheckService(context);
		}
		else if (ServiceUtil.ACTION_UPDATE_PACKAGE_INFO.equals(action)){
			String newInfo = intent.getStringExtra(ServiceUtil.EXTRA_PACKAGE_INFO);
			ServiceUtil.updatePackageInfo(context, newInfo);
		}
	}
}
