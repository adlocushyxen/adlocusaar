package com.hyxen.adlocusaar.push;

import java.util.Random;

import com.hyxen.adlocusaar.util.AdLocusUtil;
import com.hyxen.adlocusaar.util.Log;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class PushReceive extends BroadcastReceiver  //time-up bot, call ad to go
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		if(intent == null) return;
		String action = intent.getAction();
		if(action == null) return;

        Log.d("onReceive action:" + action + "," + context.getPackageName());




//		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//			JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
//			ComponentName jobService = new ComponentName(context.getPackageName(), PushJobService.class.getName());
//
//			JobInfo jobInfo = new JobInfo.Builder(100012, jobService) //任务Id等于100012
//					.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)// 需要满足网络条件，默认值NETWORK_TYPE_NONE
//					.setPeriodic(AlarmManager.INTERVAL_HOUR) //循环执行，循环时长为一天（最小为15分钟）
//					.setRequiresCharging(false)// 需要满足充电状态
//					.setRequiresDeviceIdle(false)// 设备处于Idle(Doze)
//					.setPersisted(true) //设备重启后是否继续执行
//					.setBackoffCriteria(3000,JobInfo.BACKOFF_POLICY_LINEAR) //设置退避/重试策略
//					.build();
//			scheduler.schedule(jobInfo);
//		}else{
//			if(Intent.ACTION_BOOT_COMPLETED.equals(action)) {
//				ServiceUtil.sendCheckServiceDelayed(context, new Random(System.currentTimeMillis()).nextInt(240000) + 60000);
//			} else if(ServiceUtil.INTENT_ACTION_RECEIVE.equals(action)) {
//				ServiceUtil.doAction(context, intent);
//			}
//		}



	}
}