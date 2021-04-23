package com.hyxen.adlocusaar.geofence;

import android.Manifest;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.hyxen.adlocusaar.util.AdLocusUtil;

import java.util.ArrayList;
import java.util.HashSet;


public class GeofenceService extends IntentService {

    private static final String ACTION_CHECK_ALIVE = "com.hyxen.geofence.action.CHECK_ALIVE";
    private static final String ACTION_REPORT = "com.hyxen.geofence.action.REPORT";
    private static final String ACTION_ON_LOC_CHENGE = "com.hyxen.geofence.action.ACTION_ON_LOC_CHENGE";
    private static final String ACTION_CLEAR = "com.hyxen.geofence.action.CLEAR";
    private static final String ACTION_ADD = "com.hyxen.geofence.action.ADD";
    private static final String ACTION_DELETE = "com.hyxen.geofence.action.DELETE";


    private static final int CHECK_ALIVE_INTERVAL = 120000;

    public GeofenceService() {
        super("GeofenceService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) return;

        final String action = intent.getAction();
        if (action == null) return;

        if (!isStart()) {
            start();
        }
        Geofence.logD("onHandleIntent:" + action);
        switch (action) {
            case ACTION_CHECK_ALIVE:
                checkWithLoc();
                break;
            case ACTION_ON_LOC_CHENGE:
                Bundle bundle = intent.getExtras();
                if(bundle != null) {
                    Location loc = (Location) bundle.get(LocationManager.KEY_LOCATION_CHANGED);
                    checkLocation(loc);
                }
                break;
            case ACTION_REPORT:
                if(intent.getExtras() != null) {
                    Fence fence = new Fence(intent);
                    report(fence);
                }
                break;
            case ACTION_CLEAR:
                GeofenceDbAdapter.delete(this);
                break;
            case ACTION_ADD:
                Region region = new Region(intent);
                GeofenceDbAdapter.deleteEvent(this, region.getIdentify().hashCode());
                GeofenceDbAdapter.addRegion(this, region);
                startActionCheck(this);
                break;
            case ACTION_DELETE:
                String identify = intent.getStringExtra("identify");
                GeofenceDbAdapter.deleteEvent(this, identify.hashCode());
                break;
        }
    }

    static Intent startActionCheck(Context context){
        Intent intent = new Intent(context, GeofenceService.class);
        intent.setAction(GeofenceService.ACTION_CHECK_ALIVE);
        context.startService(intent);
        return intent;
    }

    static Intent startClear(Context context){
        Intent intent = new Intent(context, GeofenceService.class);
        intent.setAction(GeofenceService.ACTION_CLEAR);
        context.startService(intent);
        return intent;
    }
    static Intent startAdd(Context context, Region region){
        Intent intent = new Intent(context, GeofenceService.class);
        intent.setAction(GeofenceService.ACTION_ADD);
        region.putExtra(intent);
        context.startService(intent);
        return intent;
    }
    static Intent startDelete(Context context, String identify){
        Intent intent = new Intent(context, GeofenceService.class);
        intent.setAction(GeofenceService.ACTION_ADD);
        intent.putExtra("identify", identify);
        context.startService(intent);
        return intent;
    }

    private boolean isStart() {
        SharedPreferences sp = getSharedPreferences(GeofenceService.class.getName(), MODE_PRIVATE);
        long ts = sp.getLong("ts", -1);
        long r_ts = sp.getLong("r_ts", -1);
        return ts != -1 && ((System.currentTimeMillis() - ts) - (SystemClock.elapsedRealtime() - r_ts)) < 1000;
    }
    private void updateStart() {
        getSharedPreferences(GeofenceService.class.getName(), MODE_PRIVATE).edit()
                .putLong("ts", System.currentTimeMillis())
                .putLong("r_ts", SystemClock.elapsedRealtime())
                .commit();
    }

    private void updateStop() {
        getSharedPreferences(GeofenceService.class.getName(), MODE_PRIVATE).edit()
                .putLong("ts", -1)
                .putLong("r_ts", -1)
                .commit();
    }


    void onMatchEventIds(HashSet<String> eids, String tag) {
        if(eids.size() == 0) {
            int count = GeofenceDbAdapter.getAllRegion(this).size();
            if (count == 0) stop();
            return;
        }

        for (String eid : eids) {
            if(eid == null || eid.equals("")) continue;
            int ieid;
            try {
                ieid = Integer.valueOf(eid);
            } catch (Exception e) {
                continue;
            }
            Fence region = GeofenceDbAdapter.getRegion(this, ieid);
            if(region != null) {
                if(region.isExpired()) {
                    GeofenceDbAdapter.deleteEvent(this, ieid);
                    if(Geofence.IS_SHOW_LOG) {
                        Log.d("geofence", "isExpired:" + String.format("%s%s", region.getIdentify(), region.getFence() == Region.FENCE_IN ? ":進" : ":出"));
                    }
                } else if(region.isStart()) {
                    int fence = GeofenceDbAdapter.setMatchEid(this, ieid);
                    region.setFence(fence);
                    Log.d("geofence", "onEventTriggered:" + String.format("%s%s", region.getIdentify(), region.getFence() == Region.FENCE_IN ? ":進" : ":出"));

                    //if only reg fence out no send trigger fence in
                    if(fence != GeofenceDbAdapter.FENCE_IN || region.getUserFence() != Region.FENCE_OUT) {
                        Intent intent = new Intent().setAction(Geofence.ACTION_EVENT_TRIGGERED);
                        region.putExtra(intent);
                        intent.putExtra("tag", tag);
                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                        report(region);
                    }
                    if(fence == GeofenceDbAdapter.FENCE_IN && region.getUserFence() != Region.FENCE_IN) {
                        GeofenceDbAdapter.updateStatusSuccess(this, region.getIdentify().hashCode(), GeofenceDbAdapter.STATUS_TRIGGER_IN);
                    }
                }
            }
        }
        boolean isNeedToStop = GeofenceDbAdapter.getAllRegion(this).size() == 0;
        if (isNeedToStop) stop();

    }

    private void report(final Fence region) {

        //call new IMP
//        final ReportRequest r = new ReportRequest(this, region);
//        r.setListener(new ReportRequest.RequestListener() {
//
//            @Override
//            public void onSucceed() { }
//
//            @Override
//            public void onError(int errorCode) {
//                startActionReport(region, 10000);
//            }
//        });
//        r.run();
//        String st = r.getResult();
//        String gg ="";
    }

    private void startActionReport(Fence region, long delay){

        Intent intent = new Intent(this, GeofenceService.class);
        intent.setAction(ACTION_REPORT);
        region.putExtra(intent);

        PendingIntent sender = PendingIntent.getService(this, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, sender);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, CHECK_ALIVE_INTERVAL, sender);
    }

    void checkExpiring() {
        GeofenceDbAdapter.removeExpiringEvent(this);
    }

    public synchronized void start()
    {
        updateStart();
        sendCheckAlive();
        checkExpiring();
        checkWithLoc();
    }

    public synchronized void stop() {
        updateStop();
        Intent intent = new Intent(this, GeofenceService.class);
        intent.setAction(GeofenceService.ACTION_CHECK_ALIVE);
        PendingIntent sender = PendingIntent.getService(this, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

    void sendCheckAlive() {
        Intent intent = new Intent(this, GeofenceService.class);
        intent.setAction(ACTION_CHECK_ALIVE);
        PendingIntent sender = PendingIntent.getService(this, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, CHECK_ALIVE_INTERVAL, sender);
    }

    void checkWithLocation(Location loc)
    {
        ArrayList<Region> r = GeofenceDbAdapter.checkLatLon(this, loc.getLatitude(), loc.getLongitude());
        HashSet<String> eids = new HashSet<>();
        for (Region region : r)
        {
            eids.add(Integer.toString(region.getIdentify().hashCode()));
        }

        if(Geofence.IS_SHOW_LOG)
        {
            Log.d("machEventIds", "loc");
        }
        onMatchEventIds(eids, "loc");
    }


    private void checkWithLoc(){
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (AdLocusUtil.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) && lm.getProvider(LocationManager.GPS_PROVIDER) != null && lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (checkLocation(loc)) return;
        }
        if (lm.getProvider(LocationManager.NETWORK_PROVIDER) != null) {
            Location loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (checkLocation(loc)) return;

            Intent i = new Intent(this, GeofenceService.class);
            i.setAction(ACTION_ON_LOC_CHENGE);
            PendingIntent sender = PendingIntent.getService(this, 0, i, PendingIntent.FLAG_ONE_SHOT);
            lm.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, sender);
        }
    }

    private boolean checkLocation(Location location) {
        if (isValidLocation(location)) {
            checkWithLocation(location);
            return true;
        }
        return false;
    }

    private boolean isValidLocation(Location loc) {
        return loc != null && (System.currentTimeMillis() - loc.getTime() < CHECK_ALIVE_INTERVAL);
    }
}
