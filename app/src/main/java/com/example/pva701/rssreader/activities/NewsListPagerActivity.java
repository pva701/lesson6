package com.example.pva701.rssreader.activities;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import com.example.pva701.rssreader.R;
import com.example.pva701.rssreader.provider.RSSContentProvider;
import com.example.pva701.rssreader.Source;
import com.example.pva701.rssreader.provider.RSSDatabaseHelper;

import java.util.ArrayList;

/**
 * Created by pva701 on 18.10.14.
 */
public class NewsListPagerActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String SOURCE_ID = "id_clicked";
    private ViewPager viewPagerNews;
    private ArrayList <Source> sources = new ArrayList<Source>();
    private int startIndex;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewPagerNews = new ViewPager(this);
        viewPagerNews.setId(R.id.viewPagerNews);
        setContentView(viewPagerNews);
        if (savedInstanceState != null && savedInstanceState.get(SOURCE_ID) != null)
            startIndex = savedInstanceState.getInt(SOURCE_ID);
        else
            startIndex = getIntent().getIntExtra(SOURCE_ID, -1);
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SOURCE_ID, viewPagerNews.getCurrentItem());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this, RSSContentProvider.SOURCES_CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        RSSDatabaseHelper.SourceCursor sourceCursor = new RSSDatabaseHelper.SourceCursor(cursor);
        while (sourceCursor.moveToNext())
            sources.add(sourceCursor.getSource());

        viewPagerNews.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int i) {
                return NewsListFragment.newInstance(sources.get(i));
            }

            @Override
            public int getCount() {
                return sources.size();
            }
        });

        if (startIndex == -1)
            throw new RuntimeException("incorrect index of clicked source");

        Source clickedSource = sources.get(startIndex);
        if (clickedSource.getName().equals(""))
            setTitle("News");
        else
            setTitle(clickedSource.getName());
        viewPagerNews.setCurrentItem(startIndex);
        viewPagerNews.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {
                Source c = sources.get(i);
                if (c.getName() != null)
                    setTitle(c.getName());
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });
        cursor.close();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }
}
