package com.hankarun.gevrek.fragments;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Response;
import com.hankarun.gevrek.NewsContentProvider;
import com.hankarun.gevrek.NewsGroupIntentService;
import com.hankarun.gevrek.R;
import com.hankarun.gevrek.activities.MessagesActivity;
import com.hankarun.gevrek.activities.NewsGropuEditActivity;
import com.hankarun.gevrek.database.NewsGroupTable;
import com.hankarun.gevrek.helpers.SharedPrefHelper;
import com.hankarun.gevrek.helpers.VolleyHelper;
import com.hankarun.gevrek.libs.HttpPages;
import com.hankarun.gevrek.libs.StaticTexts;
import com.hankarun.gevrek.nlibs.NewsGroupListCursorAdapter;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import butterknife.Bind;
import butterknife.ButterKnife;

public class NewsGroupFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        NewsGroupListCursorAdapter.OnItemClickListener {
    @Bind(R.id.progressBar3)
    ProgressBar mProgressBar;
    @Bind(R.id.swipe_refresh_layout1)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.group_recycler)
    RecyclerView mRecyclerView;

    private NewsGroupListCursorAdapter mAdapter;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                mProgressBar.setVisibility(View.GONE);
                mSwipeRefreshLayout.setRefreshing(false);
                getLoaderManager().restartLoader(0,null,NewsGroupFragment.this);
            }
        }
    };

    public NewsGroupFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new NewsGroupListCursorAdapter(getContext());
        mAdapter.setOnItemClickListener(this);

        setHasOptionsMenu(true);
        getActivity().registerReceiver(receiver, new IntentFilter("fetch"));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(getActivity(), NewsGropuEditActivity.class);
            getActivity().startActivityForResult(intent, 1);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news_group, container, false);

        ButterKnife.bind(this,view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);
        mProgressBar.setVisibility(isMyServiceRunning(NewsGroupIntentService.class) ? View.VISIBLE : View.GONE);
        mSwipeRefreshLayout.setRefreshing(isMyServiceRunning(NewsGroupIntentService.class));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadGroup();
            }
        });

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                //mSwipeRefreshLayout.setRefreshing(true);
                mProgressBar.setVisibility(isMyServiceRunning(NewsGroupIntentService.class) ? View.VISIBLE : View.GONE);
            }
        });
        loadGroup();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(receiver, new IntentFilter("fetch"));
        mProgressBar.setVisibility(isMyServiceRunning(NewsGroupIntentService.class) ? View.VISIBLE : View.GONE);
        //mSwipeRefreshLayout.setRefreshing(isMyServiceRunning(NewsGroupIntentService.class));
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        if(getActivity()!=null) {
            ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case 0:
                return new CursorLoader(getActivity(),
                        NewsContentProvider.CONTENT_URI,
                        NewsGroupTable.projection,
                        null,
                        null,
                        null
                );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    //Recyclerview item clicked.
    @Override
    public void onItemClicked(Cursor cursor, View view) {
        if (!cursor.getString(cursor.getColumnIndex(NewsGroupTable.NEWSGROUP_COUNT)).equals("(0)")) {
            Intent intent = new Intent(getActivity(), MessagesActivity.class);
            intent.putExtra("name", cursor.getString(cursor.getColumnIndex(NewsGroupTable.NEWSGROUP_NAME)));
            intent.putExtra("link", cursor.getString(cursor.getColumnIndex(NewsGroupTable.NEWSGROUP_URL)));

            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), view, "title");
            ActivityCompat.startActivity(getActivity(),intent, options.toBundle());
        }
    }

    public void loadGroup() {
        Intent mServiceIntent = new Intent(getActivity(), NewsGroupIntentService.class);
        mServiceIntent.setData(Uri.parse(HttpPages.left_page));
        mServiceIntent.putExtra("type", "0");

        getActivity().startService(mServiceIntent);
        mSwipeRefreshLayout.setRefreshing(true);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(receiver);
    }
}