package com.example.pva701.rssreader.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.example.pva701.rssreader.provider.RSSDatabaseHelper;

public class RSSContentProvider extends ContentProvider {
    public static final String LOG_TAG = "ContentProvider";
    public static final String AUTHORITY = "com.example.pva701.rssreader.provider.RSSContentProvider";
    public static final String NEWS_PATH = RSSDatabaseHelper.TABLE_NEWS;
    public static final String SOURCES_PATH = RSSDatabaseHelper.TABLE_SOURCES;

    public static final Uri NEWS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + NEWS_PATH);
    public static final Uri SOURCES_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + SOURCES_PATH);
    public static final int URI_NEWS = 1;
    public static final int URI_SOURCES = 2;

    static final String NEWS_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + NEWS_PATH;
    static final String SOURCES_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + SOURCES_PATH;
    //static final String NEWS_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd."
    //+ AUTHORITY + "." + NEWS_PATH;
    static final String SOURCES_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd."
            + AUTHORITY + "." + NEWS_PATH;

    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, NEWS_PATH, URI_NEWS);
        uriMatcher.addURI(AUTHORITY, SOURCES_PATH, URI_SOURCES);
    }
    private RSSDatabaseHelper dbHelper;

    public RSSContentProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int cnt;
        if (uriMatcher.match(uri) == URI_NEWS)
            cnt = dbHelper.getWritableDatabase().delete(RSSDatabaseHelper.TABLE_NEWS, selection, selectionArgs);
        else
            cnt = dbHelper.getWritableDatabase().delete(RSSDatabaseHelper.TABLE_SOURCES, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }

    @Override
    public String getType(Uri uri) {
        //Log.i("RSSContentProvider", "getType");
        if (uriMatcher.match(uri) == URI_NEWS)
            return NEWS_CONTENT_TYPE;
        else if (uriMatcher.match(uri) == URI_SOURCES)
            return SOURCES_CONTENT_TYPE;
        throw new RuntimeException("incorrect getType");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        //Log.d(LOG_TAG, "insert " + uri.toString());
        int m = uriMatcher.match(uri);
        Uri resultUri;
        if (m == URI_NEWS) {
            long rowID = dbHelper.getWritableDatabase().insert(RSSDatabaseHelper.TABLE_NEWS, null, values);
            resultUri = ContentUris.withAppendedId(NEWS_CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(resultUri, null);
        } else if (m == URI_SOURCES) {
            long rowID = dbHelper.getWritableDatabase().insert(RSSDatabaseHelper.TABLE_SOURCES, null, values);
            resultUri = ContentUris.withAppendedId(SOURCES_CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(resultUri, null);
        } else
            throw new IllegalArgumentException("Wrong URI: " + uri.toString());
        return resultUri;
    }

    @Override
    public boolean onCreate() {
        dbHelper = new RSSDatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        //Log.i(LOG_TAG, "query, " + uri.toString());
        int m = uriMatcher.match(uri);
        Cursor cursor;
        if (m == URI_NEWS) {
            cursor = dbHelper.getReadableDatabase().query(RSSDatabaseHelper.TABLE_NEWS, projection, selection, selectionArgs, null, null, sortOrder);
            //cursor.setNotificationUri(getContext().getContentResolver(), NEWS_CONTENT_URI);
        } else if (m == URI_SOURCES) {
            cursor = dbHelper.getReadableDatabase().query(RSSDatabaseHelper.TABLE_SOURCES, projection, selection, selectionArgs, null, null, sortOrder);
            //cursor.setNotificationUri(getContext().getContentResolver(), SOURCES_CONTENT_URI);
        } else
            throw new IllegalArgumentException("Wrong URI: " + uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        int cnt;
        if (uriMatcher.match(uri) == URI_NEWS)
            cnt = dbHelper.getWritableDatabase().update(RSSDatabaseHelper.TABLE_NEWS, values, selection, selectionArgs);
        else
            cnt = dbHelper.getWritableDatabase().update(RSSDatabaseHelper.TABLE_SOURCES, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }
}
