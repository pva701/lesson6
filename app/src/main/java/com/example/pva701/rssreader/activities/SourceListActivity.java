package com.example.pva701.rssreader.activities;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.example.pva701.rssreader.R;
import com.example.pva701.rssreader.provider.RSSContentProvider;

public class SourceListActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_source_list);
        FragmentManager fm = getSupportFragmentManager();
        Fragment f = fm.findFragmentById(R.id.container);
        if (f == null) {
            f = new SourceListFragment();
            fm.beginTransaction().add(R.id.container, f).commit();
        }
        setTitle("Sources");
    }
}
