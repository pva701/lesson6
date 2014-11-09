package com.example.pva701.rssreader.services;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;

import com.example.pva701.rssreader.News;
import com.example.pva701.rssreader.Source;
import com.example.pva701.rssreader.processor.Processor;

import java.util.ArrayList;

/**
 * Created by pva701 on 05.11.14.
 */
public class ServiceHelper {//Singlet
    private static ServiceHelper instance;
    private ArrayList <NewsSourceListener> listeners;
    private Context context;
    private ServiceHelper(Context c) {
        context = c;
        listeners = new ArrayList<NewsSourceListener>();
        Processor.get(context).setHandler(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == Processor.NETWORK_ERROR)
                    for (int i = 0; i < listeners.size(); ++i)
                        listeners.get(i).onNetworkError();
                else if (msg.what == Processor.UPDATE_SOURCES)
                    for (int i = 0; i < listeners.size(); ++i)
                        listeners.get(i).onUpdateSources();
                else if (msg.what == Processor.UPDATE_NEWS)
                    for (int i = 0; i < listeners.size(); ++i)
                        listeners.get(i).onUpdateNews();
            }
        });
    }

    public static ServiceHelper get(Context c) {
        if (instance == null)
            instance = new ServiceHelper(c.getApplicationContext());
        return instance;
    }

    public boolean isReloadingNews(int sourceId) {
        return false;//TODO write this method
    }

    public void reloadNewsNetwork(Source source) {
        context.startService(new Intent(context, PollService.class).
                putExtra(PollService.TARGET_SOURCE_ID_EXTRA, source.getId()).
                putExtra(PollService.TARGET_SOURCE_URL_EXTRA, source.getUrl()));
    }

    public void reloadAllNewsNetwork() {
        context.startService(new Intent(context, PollService.class));
    }

    /*public SourcesManager.Source getSource(int sourceId) {
        /*context.startService(new Intent(context, DatabaseService.class).
                putExtra(DatabaseService.QUERY_EXTRA, DatabaseService.UPDATE_NEWS).
                putExtra(DatabaseService.DATA_EXTRA, cur));*
    }*/

    public void updateNews(News cur) {
        context.startService(new Intent(context, DatabaseService.class).
                putExtra(DatabaseService.QUERY_EXTRA, DatabaseService.UPDATE_NEWS).
                putExtra(DatabaseService.DATA_EXTRA, cur));

    }

    public void loadNewsDatabase(Source source) {
        context.startService(new Intent(context, DatabaseService.class).
                putExtra(DatabaseService.QUERY_EXTRA, DatabaseService.LOAD_NEWS).
                putExtra(DatabaseService.DATA_EXTRA, source.getId()));
    }

    public void addListener(NewsSourceListener callback) {
        listeners.add(callback);
    }

    public void removeListener(NewsSourceListener listener) {
        for (int i = 0; i < listeners.size(); ++i)
            if (listeners.get(i) == listener)
                listeners.remove(i);
    }

    public void removeAllListeners() {
        listeners.clear();
    }

    public static class NewsSourceListener {
        public void onUpdateNews() {}
        public void onUpdateSources() {}
        public void onNetworkError() {}
    }
}
