package com.example.pva701.rssreader.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.example.pva701.rssreader.News;
import com.example.pva701.rssreader.Source;
import com.example.pva701.rssreader.provider.RSSDatabaseHelper.NewsCursor;
import com.example.pva701.rssreader.provider.RSSDatabaseHelper.SourceCursor;

import java.util.Date;

/**
 * Created by pva701 on 06.11.14.
 */
public class QueriesManager {
    private static QueriesManager instance;

    public static QueriesManager get(Context context) {
        if (instance == null)
            instance = new QueriesManager(context);
        return instance;
    }
    private Context context;

    private QueriesManager(Context context) {
        this.context = context;
    }

    public long insertSource(Source source) {

        return Long.parseLong(context.getContentResolver().insert(RSSContentProvider.SOURCES_CONTENT_URI, source.toContentValues()).getLastPathSegment());
    }

    public long insertNews(News news) {
        return Long.parseLong(context.getContentResolver().insert(RSSContentProvider.NEWS_CONTENT_URI, news.toContentValues()).getLastPathSegment());
    }

    public SourceCursor querySources() {
        return new RSSDatabaseHelper.SourceCursor(context.getContentResolver().query(RSSContentProvider.SOURCES_CONTENT_URI, null, null, null, null));
    }

    public NewsCursor queryNews(int sourceId) {
        return new NewsCursor(context.getContentResolver().query(RSSContentProvider.NEWS_CONTENT_URI, null, "source_id=" + sourceId,
                null, RSSDatabaseHelper.COLUMN_NEWS_PUB_DATE + " desc"));
    }

    public NewsCursor queryNewses() {
        return new NewsCursor(context.getContentResolver().query(RSSContentProvider.NEWS_CONTENT_URI, null, null, null, null));
    }


    public void deleteNews(int id) {
        context.getContentResolver().delete(RSSContentProvider.NEWS_CONTENT_URI, RSSDatabaseHelper.COLUMN_NEWS_ID + "=" + id, null);
    }

    public int getUnreadMessages() {
        Cursor cur = context.getContentResolver().query(RSSContentProvider.NEWS_CONTENT_URI, null, "read=0", null, null);
        cur.moveToNext();
        return cur.getInt(0);
    }

    public int deleteSources() {//Bad
        return context.getContentResolver().delete(RSSContentProvider.SOURCES_CONTENT_URI, null, null);
    }

    public int deleteSources(int id) {//Bad
        return context.getContentResolver().delete(RSSContentProvider.SOURCES_CONTENT_URI,
                RSSDatabaseHelper.COLUMN_SOURCES_ID + " = " + id, null);
    }

    public void updateNews(News news) {
        ContentValues cv = new ContentValues();
        cv.put(RSSDatabaseHelper.COLUMN_NEWS_TITLE, news.getTitle());
        context.getContentResolver().update(RSSContentProvider.NEWS_CONTENT_URI, news.toContentValues(),
                RSSDatabaseHelper.COLUMN_NEWS_ID + "=" + news.getId(), null);
    }

    public void updateSource(Source source) {
        context.getContentResolver().update(RSSContentProvider.SOURCES_CONTENT_URI, source.toContentValues(), RSSDatabaseHelper.COLUMN_SOURCES_ID + "=" + source.getId(), null);
    }

    public void updateSourceLastUpdate(int sourceId, Date lastUpdate) {
        ContentValues cv = new ContentValues();
        cv.put(RSSDatabaseHelper.COLUMN_SOURCES_LAST_UPDATE, (int) (lastUpdate.getTime() / 1000));
        context.getContentResolver().update(RSSContentProvider.SOURCES_CONTENT_URI, cv, RSSDatabaseHelper.COLUMN_SOURCES_ID + "=" + sourceId, null);
    }

    public void deleteWhereEarlyThanTime(int sourceId, int lastPubDate) {
        context.getContentResolver().delete(RSSContentProvider.NEWS_CONTENT_URI, RSSDatabaseHelper.COLUMN_NEWS_PUB_DATE + "<" + lastPubDate, null);
    }

    public void deleteSource(int id) {
        context.getContentResolver().delete(RSSContentProvider.SOURCES_CONTENT_URI, RSSDatabaseHelper.COLUMN_SOURCES_ID + " = " + id, null);
    }
}
