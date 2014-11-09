package com.example.pva701.rssreader.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.pva701.rssreader.provider.RSSContentProvider;
import com.example.pva701.rssreader.provider.RSSDatabaseHelper;
import com.example.pva701.rssreader.services.OperationHelper;
import com.example.pva701.rssreader.services.PollService;
import com.example.pva701.rssreader.R;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.example.pva701.rssreader.Source;

/**
 * Created by pva701 on 14.10.14.
 */
public class SourceListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int REQUEST_ADD_SOURCE = 1;
    private static final int REQUEST_CHANGE_SOURCE = 2;
    private static final String DIALOG_ADD_SOURCE = "DIALOG_ADD_SOURCE";
    private ListView listView;
    private SourcesArrayAdapter adapter;
    private OperationHelper HELPER;
    private OperationHelper.NewsSourceListener sourcesChangedListener;

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(), RSSContentProvider.SOURCES_CONTENT_URI,
                null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (adapter == null) {
            adapter = new SourcesArrayAdapter(getActivity());
            listView.setAdapter(adapter);
        }
        adapter.clear();
        RSSDatabaseHelper.SourceCursor sourceCursor = new RSSDatabaseHelper.SourceCursor(cursor);
        while (sourceCursor.moveToNext())
            adapter.add(sourceCursor.getSource());
        adapter.notifyDataSetChanged();
        cursor.close();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        adapter = null;
        listView.setAdapter(null);
    }

    public class SourcesArrayAdapter extends ArrayAdapter<Source> {
        public SourcesArrayAdapter(Context context) {
            super(context, R.layout.source_list_item);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = getActivity().getLayoutInflater().inflate(R.layout.source_list_item, null);
            TextView sourceName = (TextView)convertView.findViewById(R.id.source_name);
            TextView url = (TextView)convertView.findViewById(R.id.source);
            TextView lastUpdate = (TextView)convertView.findViewById(R.id.last_update);
            Source source = getItem(position);
            sourceName.setText(source.getName());
            if (source.getLastUpdate().getTime() != 0)
                lastUpdate.setText("Last update: " + new SimpleDateFormat("d MMM yyyy, HH:mm").format(source.getLastUpdate()));
            else
                lastUpdate.setText("");
            url.setText(source.getUrl());
            return convertView;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.fragment_source_list_context, menu);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        HELPER = OperationHelper.get(getActivity());
        sourcesChangedListener = new OperationHelper.NewsSourceListener() {
            @Override
            public void onUpdateSources() {
                getLoaderManager().restartLoader(0, null, SourceListFragment.this);
            }

            @Override
            public void onUpdateOneSource() {
                getLoaderManager().restartLoader(0, null, SourceListFragment.this);
            }
        };
        HELPER.addListener(sourcesChangedListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View v = inflater.inflate(R.layout.fragment_source_list, container, false);
        listView = (ListView)v.findViewById(R.id.listSourceIems);
        registerForContextMenu(listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), NewsListPagerActivity.class);
                intent.putExtra(NewsListPagerActivity.SOURCE_ID, i);
                startActivity(intent);
            }
        });
        if (adapter != null)
            listView.setAdapter(adapter);
        else
            getLoaderManager().restartLoader(0, null, this);
        return v;
    }

    private int lastChoseSource;

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int pos = info.position;
        if (item.getItemId() == R.id.menu_item_delete_source)
            HELPER.deleteSource(adapter.getItem(pos));
        else if (item.getItemId() == R.id.menu_item_change_source) {
            FragmentManager fm = getActivity().getSupportFragmentManager();
            AddChangeSourceDialog dialog = new AddChangeSourceDialog();
            Bundle args = new Bundle();
            args.putString(AddChangeSourceDialog.EXTRA_NAME, adapter.getItem(pos).getName());
            args.putString(AddChangeSourceDialog.EXTRA_URL, adapter.getItem(pos).getUrl());
            dialog.setArguments(args);
            dialog.setTargetFragment(SourceListFragment.this, REQUEST_CHANGE_SOURCE);
            dialog.show(fm, DIALOG_ADD_SOURCE);
            lastChoseSource = pos;
            return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_source_list, menu);
    }

    private AlertDialog intervalDialog;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_item_add_source) {
            FragmentManager fm = getActivity().getSupportFragmentManager();
            AddChangeSourceDialog dialog = new AddChangeSourceDialog();
            dialog.setTargetFragment(SourceListFragment.this, REQUEST_ADD_SOURCE);
            dialog.show(fm, DIALOG_ADD_SOURCE);
            return true;
        } else if (item.getItemId() == R.id.menu_item_auto_update) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Interval");
            final CharSequence[] intervals = {"Never", "1 minute", "5 minute", "15 minute", "30 minute",
                                               "1 hour", "3 hour", "6 hour", "12 hour", "1 day"};
            builder.setSingleChoiceItems(intervals, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (i == 0)
                        PollService.setServiceAlarm(getActivity(), false, null);
                    else {
                        int j = 0, num = 0;
                        String s = intervals[i].toString();
                        while (Character.isDigit(s.charAt(j))) {
                            num = num * 10 + s.charAt(j) - '0';
                            ++j;
                        }
                        ++j;
                        int mills = 0;
                        if (s.charAt(j) == 'm') mills = num * 60;
                        else if (s.charAt(j) == 'h') mills = num * 3600;
                        else if (s.charAt(j) == 'd') mills = num * 3600 * 24;
                        PollService.setServiceAlarm(getActivity(), false, null);
                        mills *= 1000;
                        Bundle bundle = new Bundle();
                        bundle.putInt(PollService.POLL_INTERVAL, mills);
                        bundle.putBoolean(PollService.NOTIFICATION, false);
                        PollService.setServiceAlarm(getActivity(), true, bundle);
                    }
                    intervalDialog.dismiss();
                }
            });
            intervalDialog = builder.create();
            intervalDialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ADD_SOURCE) {
            if (resultCode == Activity.RESULT_OK) {
                String name = data.getStringExtra(AddChangeSourceDialog.EXTRA_NAME);
                String url = data.getStringExtra(AddChangeSourceDialog.EXTRA_URL);
                if (!url.startsWith("http://") && !url.startsWith("https://"))
                    url = "http://" + url;
                Source cur = new Source(name, url, new Date(0));
                adapter.add(cur);
                adapter.notifyDataSetChanged();
                HELPER.insertSource(cur);
            }
        } else if (requestCode == REQUEST_CHANGE_SOURCE) {
            if (resultCode == Activity.RESULT_OK) {
                String name = data.getStringExtra(AddChangeSourceDialog.EXTRA_NAME);
                String url = data.getStringExtra(AddChangeSourceDialog.EXTRA_URL);
                adapter.getItem(lastChoseSource).setName(name);
                adapter.getItem(lastChoseSource).setUrl(url);
                adapter.notifyDataSetChanged();
                HELPER.updateSource(adapter.getItem(lastChoseSource));
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        HELPER.removeListener(sourcesChangedListener);
    }
}
