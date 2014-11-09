package com.example.pva701.rssreader.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.pva701.rssreader.processor.Processor;

import java.util.ArrayList;

/**
 * Created by pva701 on 16.10.14.
 */
public class PollService extends IntentService  {
    private static final String TAG = "PollService";
    public static final String NOTIFICATION = "bundle_notification";
    public static final String POLL_INTERVAL = "bundle_poll_interval";
    public static final String TARGET_SOURCE_URL_EXTRA = "target_source_url";
    public static final String TARGET_SOURCE_ID_EXTRA = "target_source_id";
    public static final int LOAD_ALL = -1;

    private static ArrayList <Integer> sources = new ArrayList<Integer>();
    public PollService() {
        super(TAG);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        for (Integer e:sources)
            if (e == LOAD_ALL)
                return;
        int targetSourceId = intent.getIntExtra(TARGET_SOURCE_ID_EXTRA, LOAD_ALL);
        sources.add(targetSourceId);
        super.onStart(intent, startId);
    }

    public static boolean isLoading(int sourceId) {
        for (Integer e:sources)
            if (e == LOAD_ALL || e == sourceId)
                return true;
        return false;
    }
    @Override
    protected void onHandleIntent(Intent intent) {//Two modes: load one url and background load all sources
        String targetUrl = intent.getStringExtra(TARGET_SOURCE_URL_EXTRA);
        int targetSourceId = intent.getIntExtra(TARGET_SOURCE_ID_EXTRA, LOAD_ALL);


        if (targetUrl != null && targetSourceId != LOAD_ALL)
            Processor.get(getApplicationContext()).loadNewsNetwork(targetSourceId, targetUrl);
        else
            Processor.get(getApplicationContext()).loadAllNewsNetwork();
        sources.remove(0);
        /*boolean isNotify = intent.getExtras().getBoolean(NOTIFICATION);
        int count = NewsManager.getInstance(getApplicationContext()).getCountUnread();
        if (isNotify) {
            PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, SourceListActivity.class), 0);
            Notification notification = new NotificationCompat.Builder(this).
                    setTicker(count + " new news!").
                    setContentTitle("New " + count + " RSS news!").
                    setContentText("Read news").
                    setContentIntent(pi).
                    setAutoCancel(true).
                    build();
            NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(0, notification);
        }
        NewsManager.getInstance(getApplicationContext()).resume();
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(UPDATE_BROADCAST));*/
    }

    public static void setServiceAlarm(Context context, boolean isOn, Bundle bundle) {
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if (isOn) {
            int pollInterval = bundle.getInt(POLL_INTERVAL);
            boolean isNotification = bundle.getBoolean(NOTIFICATION);
            Intent intent = new Intent(context, PollService.class);
            intent.putExtra(NOTIFICATION, isNotification);
            PendingIntent pi = PendingIntent.getService(context, 0, intent, 0);
            alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + pollInterval, pollInterval, pi);
        } else if (isServiceAlarmOn(context)) {
            Log.i("PollService", "isServAlarm");
            Intent intent = new Intent(context, PollService.class);
            PendingIntent pi = PendingIntent.getService(context, 0, intent, 0);
            alarmManager.cancel(pi);
            pi.cancel();
        }
    }

    public static boolean isServiceAlarmOn(Context context) {
        Intent intent = new Intent(context, PollService.class);
        PendingIntent pi = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }
}
