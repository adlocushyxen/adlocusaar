package com.hyxen.adlocusaar.push;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import com.hyxen.adlocusaar.AdLocusManager;
import com.hyxen.adlocusaar.engine.HxCellEngine;
import com.hyxen.adlocusaar.engine.HxWifiEngine;
import com.hyxen.adlocusaar.net.HxRequest;
import com.hyxen.adlocusaar.obj.AdLocusAd;
import com.hyxen.adlocusaar.push.EventInfoRequest.RequestListener;
import com.hyxen.adlocusaar.util.AdLocusNotification;
import com.hyxen.adlocusaar.util.AdLocusUtil;
import com.hyxen.adlocusaar.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public final class PushService extends Service
{
	public static final String ACTION_CHECK_EVENT = "com.hyxen.adlocusaar.push.action.CHECK_EVENT";
	public static final String ACTION_CHECK_ALIVE = "com.hyxen.adlocusaar.push.action.CHECK_ALIVE";
	public static final String ACTION_TEST_PUSH = "com.hyxen.adlocusaar.push.action.TEST_PUSH";
	public static final String ACTION_PAUSE_CHECK = "com.hyxen.adlocusaar.push.action.PAUSE_CHECK";
	public static final String ACTION_CHECK_TYPE3 = "com.hyxen.adlocusaar.push.action.CHECK_TYPE3";

	public static final int REQUEST_CODE_CHECK_EVENT = 11;
	public static final int REQUEST_CODE_CHECK_ALIVE = 12;
//	public static final int REQUEST_CODE_BIGVIEW_INTENT = 13;
	
	private HandlerThread mThread;
	private Handler mThreadHandler;

	private OffPushEngine mPushEngine;
	private final AtomicBoolean mIsStart = new AtomicBoolean(false);

	private int mListType3Id = 0;
	
	@Override
	public IBinder onBind(Intent intent) 
	{
		return null;
	}
	private class AdHandler extends Handler{

		AdHandler(Looper looper){
			super(looper);
		}

		@Override
		public void handleMessage(final Message msg)
		{
			final Event pi = (Event)msg.obj;
			final EventInfoRequest r = new EventInfoRequest(PushService.this, pi.getEventId(), pi.getSessionId());

			Log.d("handleMessage:" + pi.getEventId());
//				if (!"144912337721341612".equals(pi.getEventId())) return;

			r.setListener(new RequestListener()
			{
				@Override
				public void onSucceed(String json)
				{
					Log.d("onSucceed:" + pi.getEventId());
					AdLocusAd a = AdLocusManager.parseProMeAdJsonString(json);
					if (a.id.equals("-1")) a.id = "test_" + Math.random();
					try {
						JSONObject o = new JSONObject(json);
						pi.setCdHour(o.optInt("cd_hour", 1));
					}
					catch (JSONException ignored) { }

					if(!a.isBigView() && a.isNewStyle() && AdLocusUtil.getNewPushBackgroundIntent(PushService.this) == null) {
						Type3Manager.removeType3WithAdId(PushService.this, pi.getEventId());
						return;
					}

					int type = a.type;

					if (type == AdLocusUtil.AD_TYPE_BANNER || type == AdLocusUtil.AD_TYPE_ICON || type == AdLocusUtil.AD_TYPE_BIGVIEW) {
						if (!AdLocusNotification.checkNotification(PushService.this, a)) {
							Type3Manager.removeType3WithAdId(PushService.this, pi.getEventId());
							return;
						}
						ImpRequest ir = new ImpRequest(pi);
						ir.run();
						if (ir.hasError()) {
							Type3Manager.addType3Event(PushService.this, pi.getEventJson());
							return;
						}
						Type3Manager.removeType3WithAdId(PushService.this, pi.getEventId());
						if (AdLocusNotification.showNotification(PushService.this, a)) {
							if (a.track_imp != null) mThreadHandler.post(new TrackImpRequest(a.track_imp));
						}

					} else if(type == AdLocusUtil.AD_TYPE_DELAY) {
						Type3Manager.addType3Event(PushService.this, pi.getEventJson());
					} else {
						Type3Manager.removeType3WithAdId(PushService.this, pi.getEventId());
					}
				}

				@Override
				public void onError(int errorCode)
				{
					Log.d("onError:" + pi.getEventId() + ", errorCode:" + errorCode);
					switch (errorCode)
					{
						case EventInfoRequest.ERROR_CONNECTION_FAIL:
							//連線失敗，重新以該eventId 取得資料
							if(r.isFource())
							{
								Type3Manager.addType3Event(PushService.this, pi.getEventJson());
							}
							else
							{
								r.setFource();
								mThreadHandler.removeMessages(pi.hashCode());
								Message m = new Message();
								m.obj = msg.obj;
								m.what = msg.what;
								mListType3Id = m.what;
								mThreadHandler.sendMessageDelayed(m, 60000);
							}
							break;
						case EventInfoRequest.ERROR_NO_DATA:
							//該eventId 已過期或達到推播上限， 無法取得資料。
							//無法以該 eventId 再次取得資料
							Type3Manager.removeType3WithAdId(PushService.this, pi.getEventId());
							break;
					}
				}
			});
			r.run();
		}
	}

	@Override
	public void onCreate() 
	{
		super.onCreate();

		mThread = new HandlerThread("AdLocusPushService");
		mThread.start();

		mThreadHandler = new AdHandler(mThread.getLooper());

		if(isNeedPush())
		{
			PushService.this.start();
		}
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) 
	{
		if(intent == null) return START_STICKY;

        String action = intent.getAction();

		if (action == null) return START_STICKY;

        Log.d("onStartCommand action:" + action);
        
        if(ACTION_CHECK_EVENT.equals(action)) {

			if(mPushEngine != null) mPushEngine.checkIV();

        } else if(ACTION_CHECK_ALIVE.equals(action)) {

            if(mPushEngine == null) {
                if(isNeedPush()) PushService.this.start();
            } else {
                mPushEngine.checkRemote();
            }
			checkType3();
        } else if(ACTION_TEST_PUSH.equals(action)) {
			showTestPush();
        } else if(ServiceUtil.ACTION_EVENT_TRIGGER.equals(action)) {
            String content = intent.getStringExtra(ServiceUtil.EXTRA_EVENT);
			Type3Manager.addType3Event(PushService.this, content);
			checkType3();
        } else if (ACTION_PAUSE_CHECK.equals(action)) {
			done();
		} else if (ACTION_CHECK_TYPE3.equals(action)) {
			checkType3();
		} else {
			done();
		}

        return START_STICKY;
	}

	private void checkType3(){

		new Thread()
		{
			@Override
			public void run()
			{
				ArrayList<String> events = Type3Manager.getType3Events(PushService.this);
				if (events.size() == 0){
					done();
					return;
				}
				mListType3Id = new Event(0, events.get(0)).hashCode();
				for (int i = events.size() - 1; i >= 0; i--) {
					String content = events.get(i);
					if(content == null) continue;

					onEventTriggered(new Event(0, content));
				}
				super.run();
			}
		}.start();
	}

	private void done(){
		if (!isNeedPush()) { stopSelf(); }
	}

	private void showTestPush() {
		new Thread(){
			@Override
			public void run() {

				AdLocusAd ad = new AdLocusAd();
				ad.image = AdLocusUtil.getAssetsBitmap(PushService.this, "banner_320");
				ad.id = "-1";
				ad.leftImageType = (int)(Math.random() * 8) + 1;
				ad.link = "https://ad-locus.com";
				ad.linkType = AdLocusUtil.LINK_TYPE_URL;
				ad.description = "AdLocus 行動廣告平台(測試廣告)";
				ad.clickType = 1;
				ad.type = AdLocusUtil.AD_TYPE_ICON;
				AdLocusNotification.showNotification(PushService.this, ad);
				ad.type = AdLocusUtil.AD_TYPE_BANNER;
				ad.id = "-2";
				ad.leftImageType = (int)(Math.random() * 8) + 1;
				AdLocusNotification.showNotification(PushService.this, ad);
				if (AdLocusUtil.getNewPushBackgroundIntent(PushService.this) != null) {
					ad.scad_txt = "新訊息！";
					ad.scad_url = "https://emaico.rd.hyxencloud.com/fullad/300x250-02.gif";
					ad.id = "-3";
					AdLocusNotification.showNotification(PushService.this, ad);
				}
				String[] aa = new String[]{
//					"{\"ad_id\":-4,\"ad_type\":21,\"act_loc\":1,\"act_type\":12,\"pub_format\":\"bigview\",\"bv_text\":\"\\u7b49\\u8eca\\u597d\\u7121\\u804a\\uff0c\\u6253Game\\u6bba\\u6642\\u9593\\u5440\\uff01\",\"bv_banner\":\"http:\\/\\/adlocus-sti.s3.amazonaws.com\\/camp\\/99fe1f49\\/99fe1f49d11310e99d2f20f8e7f95fec46395eed.jpg\",\"bv_share_text\":\"Star Wars \\u7684\\u904a\\u6232\\u8036\\uff01\\u756b\\u9762\\u770b\\u8d77\\u4f86\\u597d\\u7cbe\\u7dfb\\uff01\\u5148\\u4e0b\\u8f09\\u4f86\\u73a9\\u73a9\\u770b~http:\\/\\/goo.gl\\/Fv2gqg\",\"ad_link_type\":1,\"sid\":\"14491428573943\",\"ad_link\":\"http:\\/\\/1.ad-locus.com\\/dev\\/redirect\\/14491400317294\\/and\\/d59c7c8e2808070acb5dfa3628c493884673c743\\/5f9706f6f361550b59f6019082bc8963539e00e99c2787854c37ad2260306fca158bbbcbeacf4698\\/14491428579198\\/appsflyer\\/\",\"ad_ping\":\"http:\\/\\/api.ad-locus.com\\/dev_html5\\/imp?device_id=and%3A%2F%2Fd59c7c8e2808070acb5dfa3628c493884673c743&key=5f9706f6f361550b59f6019082bc8963539e00e99c2787854c37ad2260306fca158bbbcbeacf4698&session_id=14491428573943&ad_id=14491400317294\",\"ad_right_icon\":2,\"ios_push_text\":\"\",\"ios_unlock_text\":\"\",\"ios_prefix_text\":\"AdLocus \\u884c\\u52d5\\u5ee3\\u544a\\u670d\\u52d9:\",\"sec\":10,\"err\":0,\"rtb_host\":[\"app.appsflyer.com\",null],\"ad_type_res\":\"icontxt_320_50\",\"pub_height\":\"50\",\"pub_width\":\"320\",\"scr\":\"phn\",\"house\":0}",
						"{\"ad_id\":-4,\"ad_type\":21,\"act_loc\":1,\"act_type\":12,\"pub_format\":\"bigview\",\"bv_text\":\"\\u9023\\u5750\\u8eca\\u4e5f\\u4e0d\\u60f3\\u653e\\u4e0b\\u7684\\u904a\\u6232...\",\"bv_banner\":\"http:\\/\\/adlocus-sti.s3.amazonaws.com\\/camp\\/ff695946\\/ff6959466f280e0fff464e45c4615eb50175ac76.jpg\",\"bv_share_text\":\"\\u9019\\u883b\\u597d\\u73a9\\u7684~\\u4f60\\u4e5f\\u8f09\\u4e00\\u4e0b~http:\\/\\/app.appsflyer.com\\/com.efunfun.cqb?pid=adlocus_int&c=android_adlocus&clickid;={clickid}\",\"scad_url\":\"http:\\/\\/i.api.ad-locus.com\\/devpush\\/show\\/http%3A%2F%2Fapi.ad-locus.com%2F837b950db1bf25c7826ac0f8e135a7c0e5991f57.png\\/300\\/250\",\"scad_txt\":\"\\u901a\\u77e5\\uff01\\u60a8\\u6709\\u4e00\\u5247\\u65b0\\u8a0a\\u606f\\u5f85\\u8b80\\u53d6\",\"ad_link_type\":1,\"sid\":\"14509417134143\",\"ad_link\":\"http:\\/\\/1.ad-locus.com\\/dev\\/dry\\/14491233772134\\/and\\/657508a3dc78cbb12147ed41b60b56afa0b5e340\\/10ceec59fc14b9abe291413bcb95d36bae82e56b\\/14509417136211\\/native\\/\",\"ad_ping\":\"http:\\/\\/api.ad-locus.com\\/dev_html5\\/imp?device_id=and%3A%2F%2F657508a3dc78cbb12147ed41b60b56afa0b5e340&key=10ceec59fc14b9abe291413bcb95d36bae82e56b&session_id=14509417134143&ad_id=14491233772134\",\"ad_right_icon\":1,\"ios_push_text\":\"\",\"ios_unlock_text\":\"\",\"ios_prefix_text\":\"AdLocus \\u884c\\u52d5\\u5ee3\\u544a\\u670d\\u52d9:\",\"sec\":10,\"err\":0,\"rtb_host\":[\"app.appsflyer.com\",\"app.appsflyer.com\"],\"ad_type_res\":\"icontxt_320_50\",\"pub_height\":\"50\",\"pub_width\":\"320\",\"scr\":\"phn\",\"house\":0}\n"
				};

//			AdLocusAd a = AdLocusManager.parseProMeAdJsonString("{\"ad_id\":-1,\"ad_type\":21,\"act_loc\":1,\"act_type\":12,\"pub_format\":\"bigview\",\"bv_text\":\"\\u9023\\u5750\\u8eca\\u4e5f\\u4e0d\\u60f3\\u653e\\u4e0b\\u7684\\u904a\\u6232...\",\"bv_banner\":\"http:\\/\\/adlocus-sti.s3.amazonaws.com\\/camp\\/ff695946\\/ff6959466f280e0fff464e45c4615eb50175ac76.jpg\",\"bv_share_text\":\"\\u9019\\u883b\\u597d\\u73a9\\u7684~\\u4f60\\u4e5f\\u8f09\\u4e00\\u4e0b~http:\\/\\/app.appsflyer.com\\/com.efunfun.cqb?pid=adlocus_int&c=android_adlocus&clickid;={clickid}\",\"ad_link_type\":1,\"sid\":\"14491303479188\",\"ad_link\":\"http:\\/\\/1.ad-locus.com\\/dev\\/dry\\/14491233772134\\/and\\/54d1f943da5203325164d5a53f248aab271ee590\\/5f9706f6f361550b59f6019082bc8963539e00e99c2787854c37ad2260306fca158bbbcbeacf4698\\/14491303479518\\/native\\/\",\"ad_ping\":\"http:\\/\\/api.ad-locus.com\\/dev_html5\\/imp?device_id=and%3A%2F%2F54d1f943da5203325164d5a53f248aab271ee590&key=5f9706f6f361550b59f6019082bc8963539e00e99c2787854c37ad2260306fca158bbbcbeacf4698&session_id=14491303479188&ad_id=14491233772134\",\"ad_right_icon\":1,\"ios_push_text\":\"\",\"ios_unlock_text\":\"\",\"ios_prefix_text\":\"AdLocus \\u884c\\u52d5\\u5ee3\\u544a\\u670d\\u52d9:\",\"sec\":10,\"err\":0,\"rtb_host\":[\"app.appsflyer.com\",\"app.appsflyer.com\"],\"ad_type_res\":\"icontxt_320_50\",\"pub_height\":\"50\",\"pub_width\":\"320\",\"scr\":\"phn\",\"house\":0}");
//			AdLocusAd a = AdLocusManager.parseProMeAdJsonString("{\"ad_id\":-1,\"ad_type\":21,\"act_loc\":1,\"act_type\":12,\"pub_format\":\"bigview\",\"bv_text\":\"\\u7b49\\u8eca\\u597d\\u7121\\u804a\\uff0c\\u6253Game\\u6bba\\u6642\\u9593\\u5440\\uff01\",\"bv_banner\":\"http:\\/\\/adlocus-sti.s3.amazonaws.com\\/camp\\/99fe1f49\\/99fe1f49d11310e99d2f20f8e7f95fec46395eed.jpg\",\"bv_share_text\":\"Star Wars \\u7684\\u904a\\u6232\\u8036\\uff01\\u756b\\u9762\\u770b\\u8d77\\u4f86\\u597d\\u7cbe\\u7dfb\\uff01\\u5148\\u4e0b\\u8f09\\u4f86\\u73a9\\u73a9\\u770b~http:\\/\\/goo.gl\\/Fv2gqg\",\"ad_link_type\":1,\"sid\":\"14491402204823\",\"ad_link\":\"http:\\/\\/1.ad-locus.com\\/dev\\/dry\\/14491400317294\\/and\\/5cb7274222d6c1f7900e48e3806b35d15598b50f\\/5f9706f6f361550b59f6019082bc8963539e00e99c2787854c37ad2260306fca158bbbcbeacf4698\\/14491402212211\\/appsflyer\\/\",\"ad_ping\":\"http:\\/\\/api.ad-locus.com\\/dev_html5\\/imp?device_id=and%3A%2F%2F5cb7274222d6c1f7900e48e3806b35d15598b50f&key=5f9706f6f361550b59f6019082bc8963539e00e99c2787854c37ad2260306fca158bbbcbeacf4698&session_id=14491402204823&ad_id=14491400317294\",\"ad_right_icon\":2,\"ios_push_text\":\"\",\"ios_unlock_text\":\"\",\"ios_prefix_text\":\"AdLocus \\u884c\\u52d5\\u5ee3\\u544a\\u670d\\u52d9:\",\"sec\":10,\"err\":0,\"rtb_host\":[\"app.appsflyer.com\",null],\"ad_type_res\":\"icontxt_320_50\",\"pub_height\":\"50\",\"pub_width\":\"320\",\"scr\":\"phn\",\"house\":0}");
				for (String s : aa) {
					AdLocusAd a = AdLocusManager.parseProMeAdJsonString(s);
					AdLocusNotification.showNotification(PushService.this, a);
				}
//			Event e = new Event(Event.TYPE_BROADCAST, "{\n" +
//					"     \"ad_id\":\"144912337721340000\"," +
//					"     \"session_id\":\"14491303479188\"," +
//					"     \"begin_ts\": 0," +
//					"     \"end_ts\": 3449158400," +
//					"     }");
//			ServiceUtil.sendEventTrigger(this, e);
			}
		}.start();
	}


	@Override
	public void onDestroy() {
		super.onDestroy();

		stop();

		if(isNeedPush()) ServiceUtil.sendCheckServiceDelayed(this, 60000);

		if(mThread != null) mThread.quit();

	}
	
	private void start() {
        HxCellEngine.getInstance(this);
        HxWifiEngine.getInstance(this);
		stop();
		synchronized (mIsStart) {
			if(mIsStart.get()) return;

			mPushEngine = new OffPushEngine(this);
			mPushEngine.start();
	
			mIsStart.set(true);
		}
	}
	
	public void stop() {
		synchronized (mIsStart) {
			if(!mIsStart.get()) return;

			if(mPushEngine != null) {
				mPushEngine.stop();
                mPushEngine = null;
			}
			mIsStart.set(false);
		}
	}

	public synchronized void onEventTriggered(Event event) {
		if (!AdLocusUtil.isNotificationEnable(this)) return;
		if (event.isValid()) {
			if(event.isValidHour()) {
				event.setCdHour(1);
				Message msg = new Message();
				msg.what = event.hashCode();
				msg.obj = event;
				Log.d("onEventTriggered:" + event.getEventId());

				if(mThreadHandler != null && mThreadHandler.getLooper() != null) {
					Type3Manager.addType3Event(PushService.this, event.getEventJson());
					mThreadHandler.removeMessages(msg.what);
					mThreadHandler.sendMessage(msg);
				} else {
					event.setCdHour(-1);
					Type3Manager.addType3Event(PushService.this, event.getEventJson());
				}
			}
		} else {
			Type3Manager.removeType3WithAdId(PushService.this, event.getEventId());
		}
		
	}

	private class TrackImpRequest implements Runnable {
		private final HxRequest mRequest = new HxRequest(PushService.this);
		private int mCount = 0;

		TrackImpRequest(String url_Imp) {
			mRequest.setUrl(url_Imp);
			mRequest.setMethod(HxRequest.Method.GET);
		}

		@Override
		public void run() {
			boolean success;
			mRequest.run();
			success = !mRequest.hasError();
			if (success) {
				try {
					JSONObject o = new JSONObject(mRequest.getResult());
					success = o.optInt("err", -999) == 0;
				} catch (JSONException ignored) {
				}
			}
			if (!success && mCount++ < 3) {
				mThreadHandler.post(this);
			}
		}
	}

	private class ImpRequest implements Runnable {
		private final HxRequest mRequest = new HxRequest(PushService.this, AdLocusUtil.URL_PUSH_IMP_NEW);
		private int mCount = 0;
		private int mHash;
		private boolean mHasError = false;

		ImpRequest(Event pi) {
			mHash = pi.hashCode();
			AdLocusUtil.setRequestParameters(PushService.this, mRequest, AdLocusUtil.getPushKey(PushService.this), null, null, null);
			mRequest.setPostParameter("ad_id", pi.getEventId());
			mRequest.setPostParameter("session_id", pi.getSessionId());
		}

		@Override
		public void run() {
			boolean success;
			mHasError = false;
			mRequest.run();
			success = !mRequest.hasError();
			if (success) {
				try {
					JSONObject o = new JSONObject(mRequest.getResult());
					success = o.optInt("err", -999) == 0;

				} catch (JSONException ignored) {
					mHasError = true;
				}
			}
			if (!success && mCount++ < 3) {
				ImpRequest.this.run();
			} else {
				if (!success) mHasError = true;
				if (mListType3Id == mHash) done();
			}
		}

		public boolean hasError() {
			return mHasError;
		}
	}
	
	private boolean isNeedPush()
	{
		return ServiceUtil.isCurrentService(this) && !AdLocusUtil.isPause(this);
	}
	
}
