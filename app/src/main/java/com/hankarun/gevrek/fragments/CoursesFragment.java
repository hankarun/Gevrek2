package com.hankarun.gevrek.fragments;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.android.volley.Response;
import com.hankarun.gevrek.NewsContentProvider;
import com.hankarun.gevrek.NewsGroupIntentService;
import com.hankarun.gevrek.R;
import com.hankarun.gevrek.activities.CourseAddActivity;
import com.hankarun.gevrek.database.CourseTable;
import com.hankarun.gevrek.database.NewsGroupTable;
import com.hankarun.gevrek.helpers.VolleyHelper;
import com.hankarun.gevrek.helpers.AdapterRecycler;
import com.hankarun.gevrek.libs.CourseItem;
import com.hankarun.gevrek.libs.HttpPages;
import com.hankarun.gevrek.libs.StaticTexts;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CoursesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private ArrayList<CourseItem> lnames;
    private OnFragmentInteractionListener mListener;
    private ProgressBar mProgressBar;
    private VolleyHelper volleyHelper;
    private AdapterRecycler mAdapter;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                int resultCode = bundle.getInt("result");
                mProgressBar.setVisibility(View.GONE);
            }
        }
    };

    public static CoursesFragment newInstance(String param1, String param2) {
        CoursesFragment fragment = new CoursesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public CoursesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String mParam1 = getArguments().getString(ARG_PARAM1);
            String mParam2 = getArguments().getString(ARG_PARAM2);
        }
        //setHasOptionsMenu(true);
        getActivity().registerReceiver(receiver, new IntentFilter("courses"));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_courses, container, false);


        RecyclerView mRecyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar2);

        mProgressBar.setVisibility(isMyServiceRunning(NewsGroupIntentService.class) ? View.VISIBLE : View.GONE);


        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        lnames = new ArrayList<>();
        mAdapter = new AdapterRecycler(lnames, getActivity().getApplicationContext(), getActivity());
        mRecyclerView.setAdapter(mAdapter);
        startTask();

        return view;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        //if (volleyHelper != null)
        //    volleyHelper.cancelRequest();
        getActivity().unregisterReceiver(receiver);

    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(receiver, new IntentFilter("courses"));
        mProgressBar.setVisibility(isMyServiceRunning(NewsGroupIntentService.class) ? View.VISIBLE : View.GONE);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                NewsContentProvider.CONTENT_URI1,
                CourseTable.projection,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.clear();
        if (data != null && data.moveToFirst()) {
            do {
                mAdapter.add(mAdapter.getItemCount(), CourseItem.fromCursor(data));
            } while ((data.moveToNext()));
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void startTask() {
        /*Intent mServiceIntent = new Intent(getActivity(), NewsGroupIntentService.class);
        mServiceIntent.setData(Uri.parse(HttpPages.courses_page));
        mServiceIntent.putExtra("type","1");
        getActivity().startService(mServiceIntent);*/

        getLoaderManager().initLoader(0, null, this);
        /*
        volleyHelper = new VolleyHelper(getActivity());
        volleyHelper.postStringRequest(StaticTexts.MESSAGE_LIST_REQUEST, HttpPages.courses_page, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                mProgressBar.setVisibility(View.GONE);
                onTaskComplete(response);
            }
        });*/
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
            Intent intent = new Intent(getActivity(), CourseAddActivity.class);
            getActivity().startActivityForResult(intent, 3);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void onTaskComplete(String html) {
        if (!html.equals("")) {
            Document doc = Jsoup.parse(html);
            doc.setBaseUri("https://cow.ceng.metu.edu.tr");


            Map<String, String> lcodes = new HashMap<String, String>();

            Elements names = doc.select("div");
            Element divs = null;
            for (Element e : names) {
                if (e.attr("id").equals("mtm_menu_horizontal"))
                    divs = e;
            }
            Elements rnamesd = doc.select("td.content").select("tr").select("td");
            if (divs != null) {
                int x = 0;
                while (x < rnamesd.size()) {
                    lcodes.put(rnamesd.get(x).text(), rnamesd.get(x + 1).text());
                    x += 2;
                }
                Elements courses = divs.select("a");
                courses.remove(0);
                for (Element t : courses) {
                    mAdapter.add(mAdapter.getItemCount(), new CourseItem(lcodes.get(t.text()), t.text(), t.attr("abs:href")));
                }
            } else {
                //This is not a solution
            }
        } else {
            //Network Problem
        }
    }

}
