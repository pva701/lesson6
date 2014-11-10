package com.example.pva701.rssreader.activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pva701.rssreader.News;
import com.example.pva701.rssreader.R;
import com.example.pva701.rssreader.provider.RSSContentProvider;
import com.example.pva701.rssreader.Source;
import com.example.pva701.rssreader.provider.RSSDatabaseHelper;
import com.example.pva701.rssreader.provider.RSSDatabaseHelper.NewsCursor;
import com.example.pva701.rssreader.services.OperationHelper;

import java.text.SimpleDateFormat;

/**
 * Created by pva701 on 18.10.14.
 */
public class NewsListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String SOURCE = "source";
    private static final String TAG = "NewsListFragment";
    private static final int LOAD_NEWS = 15;

    public static Fragment newInstance(Source source) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(SOURCE, source);
        Fragment f = new NewsListFragment();
        f.setArguments(bundle);
        return f;
    }

    private NewsArrayAdapter adapter;
    private Source source;
    private SwipeRefreshLayout refreshLayout;
    private ListView listView;
    private OperationHelper.NewsSourceListener newsChangedListener;
    private OperationHelper HELPER;

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(),
                RSSContentProvider.NEWS_CONTENT_URI, null,
                RSSDatabaseHelper.COLUMN_NEWS_SOURCE_ID + "=" + source.getId(), null,
                RSSDatabaseHelper.COLUMN_NEWS_PUB_DATE + " desc");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (adapter == null) {
            adapter = new NewsArrayAdapter(getActivity());
            listView.setAdapter(adapter);
        }
        if (cursor.isAfterLast())
            return;
        adapter.clear();
        NewsCursor newsCursor = new NewsCursor(cursor);
        while (newsCursor.moveToNext())
            adapter.add(newsCursor.getNews());
        adapter.notifyDataSetChanged();
        refreshLayout.setRefreshing(false);
        cursor.close();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        adapter = null;
        listView.setAdapter(null);
        refreshLayout.setRefreshing(false);
    }

    public class NewsArrayAdapter extends ArrayAdapter <News> {
        public NewsArrayAdapter(Context context) {
            super(context, R.layout.news_list_item);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = View.inflate(getActivity(), R.layout.news_list_item, null);

            News news = getItem(position);
            TextView title = (TextView)convertView.findViewById(R.id.title);
            TextView pubDate = (TextView)convertView.findViewById(R.id.pub_date);
            if (!news.isRead())
                title.setTypeface(null, Typeface.BOLD);
            else
                title.setTypeface(null, 0);
            title.setText(news.getTitle());
            pubDate.setText("Published: " + NEWS_DATE_FORMAT.format(news.getPubDate()));
            return convertView;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        HELPER = OperationHelper.get(getActivity());
        source = (Source)getArguments().get(SOURCE);
        newsChangedListener = new OperationHelper.NewsSourceListener() {
            @Override
            public void onUpdateNews() {
                getLoaderManager().restartLoader(LOAD_NEWS, null, NewsListFragment.this);
            }

            @Override
            public void onNetworkError() {
                Toast.makeText(getActivity(), "Check your internet connection", Toast.LENGTH_SHORT).show();
                refreshLayout.setRefreshing(false);
            }

            @Override
            public void onIncorrectUrl() {
                Toast.makeText(getActivity(), "Incorrect url", Toast.LENGTH_SHORT).show();
                refreshLayout.setRefreshing(false);
            }

            @Override
            public void onIncorrectRSSFeed() {
                Toast.makeText(getActivity(), "Not RSS feed", Toast.LENGTH_SHORT).show();
                refreshLayout.setRefreshing(false);
            }

            @Override
            public void onUnknownError() {
                Toast.makeText(getActivity(), "Unknown error, try again", Toast.LENGTH_SHORT).show();
                refreshLayout.setRefreshing(false);
            }
        };
        //Log.i("NewsListFragment", source.getName() + " loader = " + getLoaderManager().restartLoader(LOAD_NEWS, null, this));
    }

    private final SimpleDateFormat NEWS_DATE_FORMAT = new SimpleDateFormat("d MMM yyyy, HH:mm");
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_news_list, container, false);
        refreshLayout = (SwipeRefreshLayout)v.findViewById(R.id.refresh);
        refreshLayout.setColorSchemeColors(
                Color.parseColor("#00cb45"),
                Color.parseColor("#00dd66"),
                Color.parseColor("#00ee76"),
                Color.parseColor("#00ff7f"));

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isOnline()) {
                    Toast.makeText(getActivity(), "Check your internet connection", Toast.LENGTH_SHORT).show();
                    refreshLayout.setRefreshing(false);
                    return;
                }
                reloadNews();
            }
        });

        listView = (ListView)v.findViewById(R.id.listNews);
        listView.setEmptyView(v.findViewById(R.id.empty_list));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (!isOnline()) {
                    Toast.makeText(getActivity(), "Check your internet connection", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(getActivity(), ShowWebView.class);
                News cur = adapter.getItem(i);
                intent.putExtra(ShowWebView.URL, cur.getLink());
                startActivity(intent);
                cur.setRead(true);
                HELPER.updateNews(cur);
            }
        });
        if (adapter != null)
            listView.setAdapter(adapter);
        else
            getLoaderManager().restartLoader(LOAD_NEWS, null, this);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        HELPER.addListener(newsChangedListener);
        if (HELPER.isReloadingNews(source.getId()))
            refreshLayout.setRefreshing(true);
        else if (refreshLayout.isRefreshing())
            getLoaderManager().restartLoader(LOAD_NEWS, null, this);
        else if (adapter != null)
            adapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        HELPER.removeListener(newsChangedListener);
    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    public void reloadNews() {
        refreshLayout.setRefreshing(true);
        HELPER.reloadNewsNetwork(source);
    }
}
