package com.example.pva701.rssreader.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.pva701.rssreader.News;
import com.example.pva701.rssreader.Source;

import java.util.Date;
import java.util.StringTokenizer;

/**
 * Created by pva701 on 17.10.14.
 */
public class RSSDatabaseHelper extends SQLiteOpenHelper {
    public static class SourceCursor extends CursorWrapper {
        public SourceCursor(Cursor cursor) {
            super(cursor);
        }

        public Source getSource() {
            if (isBeforeFirst() || isAfterLast())
                return null;
            return new Source(getLong(getColumnIndexOrThrow(COLUMN_SOURCES_ID)),
                    getString(getColumnIndexOrThrow(COLUMN_SOURCES_NAME)),
                    getString(getColumnIndexOrThrow(COLUMN_SOURCES_URL)),
                    new Date(getInt(getColumnIndexOrThrow(COLUMN_SOURCES_LAST_UPDATE)) * 1000L));
        }
    }

    public static class NewsCursor extends CursorWrapper {
        public NewsCursor(Cursor cursor) {
            super(cursor);
        }

        public News getNews() {
            if (isBeforeFirst() || isAfterLast())
                return null;
            News ret = new News();
            ret.setId(getInt(getColumnIndexOrThrow(COLUMN_NEWS_ID)));
            ret.setLink(getString(getColumnIndexOrThrow(COLUMN_NEWS_LINK)));
            ret.setTitle(getString(getColumnIndexOrThrow(COLUMN_NEWS_TITLE)));
            ret.setDescription(getString(getColumnIndexOrThrow(COLUMN_NEWS_DESCRIPTION)));
            ret.setPubDate(new Date(getInt(getColumnIndexOrThrow(COLUMN_NEWS_PUB_DATE)) * 1000L));
            ret.setSourceId(getInt(getColumnIndexOrThrow(COLUMN_NEWS_SOURCE_ID)));
            ret.setRead(getInt(getColumnIndexOrThrow(COLUMN_NEWS_READ)) == 1);
            String category = getString(getColumnIndexOrThrow(COLUMN_NEWS_CATEGORY));
            StringTokenizer tokenizer = new StringTokenizer(category, ",");
            while (tokenizer.hasMoreTokens())
                ret.addCategory(tokenizer.nextToken());
            return ret;
        }
    }

    public static final int VERSION = 1;
    public static final String DB_NAME = "RSS";

    public static final String TABLE_SOURCES = "sources";
    public static final String COLUMN_SOURCES_ID = "_id";
    public static final String COLUMN_SOURCES_NAME = "name";
    public static final String COLUMN_SOURCES_URL = "url";
    public static final String COLUMN_SOURCES_LAST_UPDATE = "last_update";

    public static final String TABLE_NEWS = "news";
    public static final String COLUMN_NEWS_ID = "_id";
    public static final String COLUMN_NEWS_TITLE = "title";
    public static final String COLUMN_NEWS_LINK = "link";
    public static final String COLUMN_NEWS_DESCRIPTION = "description";
    public static final String COLUMN_NEWS_PUB_DATE = "pub_date";
    public static final String COLUMN_NEWS_CATEGORY = "category";
    public static final String COLUMN_NEWS_READ = "read";
    public static final String COLUMN_NEWS_SOURCE_ID = "source_id";

    public RSSDatabaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table sources (_id integer primary key autoincrement, " +
                                                  "name varchar(64), " +
                                                  "url varchar(100), " +
                                                  "last_update integer)");

        db.execSQL("create table news (_id integer primary key autoincrement, " +
                                "title varchar(100), " +
                                "link varchar(100), " +
                                "description varchar(1000), " +
                                "pub_date integer, " +
                                "category varchar(100), " +
                                "`read` integer, " +
                                "source_id integer)");

        /*ContentValues cv = new ContentValues();
        SourcesManager.Source source = new SourcesManager.Source("bash", "http://bash.im/rss/", new Date(0));
        cv.put(COLUMN_SOURCES_NAME, source.getName());
        cv.put(COLUMN_SOURCES_URL, source.getUrl());
        cv.put(COLUMN_SOURCES_LAST_UPDATE, source.getLastUpdate().getTime());
        db.insert(TABLE_SOURCES, null, cv);*/

        ContentValues cv1 = new ContentValues();
        Source source1 = new Source("echo moscow", "http://echo.msk.ru/interview/rss-fulltext.xml", new Date(0));
        cv1.put(COLUMN_SOURCES_NAME, source1.getName());
        cv1.put(COLUMN_SOURCES_URL, source1.getUrl());
        cv1.put(COLUMN_SOURCES_LAST_UPDATE, source1.getLastUpdate().getTime());
        db.insert(TABLE_SOURCES, null, cv1);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        //none
    }
}
