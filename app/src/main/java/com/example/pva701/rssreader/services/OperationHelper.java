package com.example.pva701.rssreader.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.pva701.rssreader.News;
import com.example.pva701.rssreader.Source;
import com.example.pva701.rssreader.processor.Processor;

import java.util.ArrayList;

/**
 * Created by pva701 on 05.11.14.
 */
public class OperationHelper {//Singlet
    private static OperationHelper instance;
    private ArrayList <NewsSourceListener> listeners;
    private Context context;
    private OperationHelper(Context c) {
        context = c;
        listeners = new ArrayList<NewsSourceListener>();

        Processor.get(context).setHandler(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == Processor.NETWORK_ERROR)
                    for (NewsSourceListener e: listeners)
                        e.onNetworkError();
                else if (msg.what == Processor.UPDATE_SOURCES)
                    for (NewsSourceListener e: listeners)
                        e.onUpdateSources();
                else if (msg.what == Processor.UPDATE_NEWS)
                    for (NewsSourceListener e: listeners)
                        e.onUpdateNews();
                else if (msg.what == Processor.UPDATE_ONE_NEWS)
                    for (NewsSourceListener e: listeners)
                        e.onUpdateOneNews();
                else if (msg.what == Processor.UPDATE_ONE_SOURCE)
                    for (NewsSourceListener e: listeners)
                        e.onUpdateOneSource();
                else if (msg.what == Processor.INCORRECT_URL)
                    for (NewsSourceListener e: listeners)
                        e.onIncorrectUrl();
                else if (msg.what == Processor.INCORRECT_RSS_FEED)
                    for (NewsSourceListener e: listeners)
                        e.onIncorrectRSSFeed();
            }
        });
    }

    public static OperationHelper get(Context c) {
        if (instance == null)
            instance = new OperationHelper(c.getApplicationContext());
        return instance;
    }

    public boolean isReloadingNews(int sourceId) {
        return PollService.isLoading(sourceId);
    }

    public void reloadNewsNetwork(Source source) {
        context.startService(new Intent(context, PollService.class).
                putExtra(PollService.TARGET_SOURCE_ID_EXTRA, source.getId()).
                putExtra(PollService.TARGET_SOURCE_URL_EXTRA, source.getUrl()));
    }

    public void reloadAllNewsNetwork() {
        context.startService(new Intent(context, PollService.class));
    }

    public void updateSource(Source source) {
        context.startService(new Intent(context, DatabaseService.class).
                putExtra(DatabaseService.QUERY_EXTRA, DatabaseService.UPDATE_SOURCES).
                putExtra(DatabaseService.DATA_EXTRA, source));
    }

    public void insertSource(Source source) {
        context.startService(new Intent(context, DatabaseService.class).
                putExtra(DatabaseService.QUERY_EXTRA, DatabaseService.INSERT_SOURCES).
                putExtra(DatabaseService.DATA_EXTRA, source));
    }

    public void updateNews(News news) {
        context.startService(new Intent(context, DatabaseService.class).
                putExtra(DatabaseService.QUERY_EXTRA, DatabaseService.UPDATE_NEWS).
                putExtra(DatabaseService.DATA_EXTRA, news));

    }

    public void deleteSource(Source source) {
        context.startService(new Intent(context, DatabaseService.class).
        putExtra(DatabaseService.QUERY_EXTRA, DatabaseService.DELETE_SOURCE).
                putExtra(DatabaseService.DATA_EXTRA, source));
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
        public void onUpdateOneNews() {}
        public void onUpdateOneSource() {}
        public void onIncorrectUrl() {}
        public void onIncorrectRSSFeed() {}
    }
}
