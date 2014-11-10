package com.example.pva701.rssreader.processor;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;

import com.example.pva701.rssreader.Source;
import com.example.pva701.rssreader.provider.QueriesManager;
import com.example.pva701.rssreader.provider.RSSDatabaseHelper;
import com.example.pva701.rssreader.rest.RSSFetcher;
import com.example.pva701.rssreader.News;

import org.xml.sax.SAXException;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by pva701 on 06.11.14.
 */
public class Processor {
    public static final int UPDATE_NEWS = 0;
    public static final int UPDATE_SOURCES = 1;
    public static final int NETWORK_ERROR = 2;
    public static final int UPDATE_ONE_NEWS = 3;
    public static final int UPDATE_ONE_SOURCE = 4;
    public static final int INCORRECT_URL = 5;
    public static final int INCORRECT_RSS_FEED = 6;
    public static final int UNKNOWN_ERROR = 7;

    private static Processor instance;
    private Handler handler;

    public static Processor get(Context context) {
        if (instance == null)
            instance = new Processor(context.getApplicationContext());
        return instance;
    }

    private Context context;
    private Processor(Context context) {
        this.context = context;
    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    private boolean merge(int sourceId, ArrayList <News> fetchedNews) {
        if (fetchedNews.size() == 0)
            return false;
        Collections.sort(fetchedNews, new Comparator<News>() {
            @Override
            public int compare(News news1, News news2) {
                return news2.getPubDate().compareTo(news1.getPubDate());
            }
        });
        Cursor cursor = QueriesManager.get(context).queryNews(sourceId);
        cursor.moveToNext();
        News news = new RSSDatabaseHelper.NewsCursor(cursor).getNews();
        for (int i = 0; i < fetchedNews.size() && (news == null || !news.equals(fetchedNews.get(i))); ++i) {
            fetchedNews.get(i).setSourceId(sourceId);
            QueriesManager.get(context).insertNews(fetchedNews.get(i));
        }
        int lastPubDate = (int)(fetchedNews.get(fetchedNews.size() - 1).getPubDate().getTime() / 1000);
        QueriesManager.get(context).deleteWhereEarlyThanTime(sourceId, lastPubDate);
        return true;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void removeHandler() {handler = null;}

    public void loadNewsNetwork(int sourceId, String url) {
        if (!isOnline()) {
            if (handler != null)
                handler.obtainMessage(Processor.NETWORK_ERROR).sendToTarget();
            return;
        }
        try {
            boolean success = merge(sourceId, RSSFetcher.fetch(url));
            QueriesManager.get(context).updateSourceLastUpdate(sourceId, new Date());
            if (handler != null && success) {
                handler.obtainMessage(Processor.UPDATE_ONE_SOURCE).sendToTarget();
                handler.obtainMessage(Processor.UPDATE_NEWS).sendToTarget();
            }
        } catch (UnknownHostException e) {
            if (handler != null)
                handler.obtainMessage(Processor.INCORRECT_URL).sendToTarget();
        } catch (SAXException e) {
            if (handler != null)
                handler.obtainMessage(Processor.INCORRECT_RSS_FEED).sendToTarget();
        } catch (IllegalArgumentException e) {
            if (handler != null)
                handler.obtainMessage(Processor.UNKNOWN_ERROR).sendToTarget();
        }
    }

    public void loadAllNewsNetwork() {
        if (!isOnline()) {
            if (handler != null)
                handler.obtainMessage(Processor.NETWORK_ERROR).sendToTarget();
            return;
        }

        Cursor cursor = QueriesManager.get(context).querySources();
        while (cursor.moveToNext()) {
            Source source = new RSSDatabaseHelper.SourceCursor(cursor).getSource();
            try {
                merge(source.getId(), RSSFetcher.fetch(source.getUrl()));
                QueriesManager.get(context).updateSourceLastUpdate(source.getId(), new Date());
            } catch (Exception e) {}
        }

        if (handler != null) {
            handler.obtainMessage(Processor.UPDATE_SOURCES).sendToTarget();
            handler.obtainMessage(Processor.UPDATE_NEWS).sendToTarget();
        }
    }

    public void updateNews(News news) {
        QueriesManager.get(context).updateNews(news);
        if (handler != null)
            handler.obtainMessage(Processor.UPDATE_ONE_NEWS).sendToTarget();
    }

    public void updateSource(Source source) {
        Source prevSource = QueriesManager.get(context).querySource(source.getId());
        if (!prevSource.getUrl().equals(source.getUrl())) {
            source.setLastUpdate(new Date(0));
            QueriesManager.get(context).deleteNewsBySourceId(source.getId());
            if (handler != null)
                handler.obtainMessage(Processor.UPDATE_NEWS).sendToTarget();
        }

        QueriesManager.get(context).updateSource(source);
        if (handler != null)
            handler.obtainMessage(Processor.UPDATE_ONE_SOURCE).sendToTarget();
    }

    public void insertNews(News news) {
        QueriesManager.get(context).insertNews(news);
        if (handler != null)
            handler.obtainMessage(Processor.UPDATE_NEWS).sendToTarget();
    }

    public void insertSource(Source source) {
        QueriesManager.get(context).insertSource(source);
        if (handler != null)
            handler.obtainMessage(Processor.UPDATE_SOURCES).sendToTarget();
    }

    public void deleteSource(Source source) {
        QueriesManager.get(context).deleteSource(source.getId());
        int cnt = QueriesManager.get(context).deleteSources(source.getId());
        if (handler != null) {
            handler.obtainMessage(Processor.UPDATE_SOURCES).sendToTarget();
            if (cnt != 0)
                handler.obtainMessage(Processor.UPDATE_NEWS).sendToTarget();
        }
    }

    /*public void loadDatabaseNews(int sourceId) {
        QueriesManager.get(context).queryNews(sourceId);
        if (handler != null)
            handler.obtainMessage(Processor.UPDATE_NEWS).sendToTarget();
    }*/
}
