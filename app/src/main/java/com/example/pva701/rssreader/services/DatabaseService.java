package com.example.pva701.rssreader.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.os.Handler;

import com.example.pva701.rssreader.News;
import com.example.pva701.rssreader.Source;
import com.example.pva701.rssreader.processor.Processor;
/**
 * Created by pva701 on 19.10.14.
 */
public class DatabaseService extends IntentService {
    public static final String TAG = "DatabaseService";
    public static final String QUERY_EXTRA =    "query";
    public static final String DATA_EXTRA =     "data";
    public static final int UPDATE_NEWS =    0;
    public static final int UPDATE_SOURCES = 1;
    public static final int INSERT_NEWS =    2;
    public static final int INSERT_SOURCES = 3;
    public static final int DELETE_SOURCE = 4;

    //public static final String LOAD_NEWS =      "source_id";
    //public static final String HANDLER_EXTRA =  "handler";

    public DatabaseService() {
        super(TAG);
    }
    public static boolean isRunning = false;
    /*@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (isRunning)
            return Service.START_NOT_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }*/


    @Override
    protected void onHandleIntent(Intent intent) {
        int query = intent.getExtras().getInt(QUERY_EXTRA);
        if (query == UPDATE_NEWS) {
            News news = (News)intent.getExtras().get(DATA_EXTRA);
            Processor.get(getApplicationContext()).updateNews(news);
        } else if (query == UPDATE_SOURCES) {
            Source source = (Source)intent.getExtras().get(DATA_EXTRA);
            Processor.get(getApplicationContext()).updateSource(source);
        } else if (query == INSERT_NEWS) {
            News news = (News)intent.getExtras().get(DATA_EXTRA);
            Processor.get(getApplicationContext()).insertNews(news);
        } else if (query == INSERT_SOURCES) {
            Source source = (Source)intent.getExtras().get(DATA_EXTRA);
            Processor.get(getApplicationContext()).insertSource(source);
        } else if (query == DELETE_SOURCE) {
            Source source = (Source)intent.getExtras().get(DATA_EXTRA);
            Processor.get(getApplicationContext()).deleteSource(source);
        }
        /*else if (query.equals(LOAD_NEWS)) {
            int id = intent.getIntExtra(DATA_EXTRA, -1);
            if (id == -1)
                throw new IllegalArgumentException("Illegal index of source in DatabaseService");
            Processor.get(getApplicationContext()).loadDatabaseNews(id);
        }*/

    }
}
