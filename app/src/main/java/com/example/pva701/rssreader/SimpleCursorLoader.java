package com.example.pva701.rssreader;


import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;

import com.example.pva701.rssreader.provider.RSSDatabaseHelper;

/**
 * Created by pva701 on 03.11.14.
 */
public class SimpleCursorLoader extends CursorLoader {
    private Cursor cursor;
    private int id;
    private RSSDatabaseHelper helper;

    public SimpleCursorLoader(Context context, int sourceId) {
        super(context);
        id = sourceId;
        helper = new RSSDatabaseHelper(context.getApplicationContext());
    }

    @Override
    public Cursor loadInBackground() {
        return cursor = helper.queryNews(id);
    }
}
