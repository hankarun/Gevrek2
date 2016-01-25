package com.hankarun.gevrek.fragments;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.hankarun.gevrek.NewsContentProvider;
import com.hankarun.gevrek.NewsGroupIntentService;
import com.hankarun.gevrek.Newsgroup;
import com.hankarun.gevrek.R;
import com.hankarun.gevrek.Url;
import com.hankarun.gevrek.activities.MessagesActivity;
import com.hankarun.gevrek.activities.NewsGropuEditActivity;
import com.hankarun.gevrek.database.NewsGroupTable;
import com.hankarun.gevrek.helpers.SharedPrefHelper;
import com.hankarun.gevrek.helpers.VolleyHelper;
import com.hankarun.gevrek.libs.HttpPages;
import com.hankarun.gevrek.libs.StaticTexts;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class NewsGroupFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private ExpandableListView listview;
    private List<Newsgroup> groups;

    private ProgressBar mProgressBar;

    private OnFragmentInteractionListener mListener;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                int resultCode = bundle.getInt("result");
                mProgressBar.setVisibility(View.GONE);
                mSwipeRefreshLayout.setRefreshing(false);
                loadusername();
            }
        }
    };


    public static NewsGroupFragment newInstance(String param1, String param2) {
        NewsGroupFragment fragment = new NewsGroupFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public void reload() {
        loadGroup();
    }

    public NewsGroupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String mParam1 = getArguments().getString(ARG_PARAM1);
            String mParam2 = getArguments().getString(ARG_PARAM2);
        }
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_news_group, container, false);
        listview = (ExpandableListView) view.findViewById(R.id.expandableListView);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar3);

        mProgressBar.setVisibility(isMyServiceRunning(NewsGroupIntentService.class) ? View.VISIBLE : View.GONE);


        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout1);
        mSwipeRefreshLayout.setRefreshing(isMyServiceRunning(NewsGroupIntentService.class));

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reload();
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

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(receiver, new IntentFilter("fetch"));
        mProgressBar.setVisibility(isMyServiceRunning(NewsGroupIntentService.class) ? View.VISIBLE : View.GONE);
        //mSwipeRefreshLayout.setRefreshing(isMyServiceRunning(NewsGroupIntentService.class));
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

    private void loadusername() {
        VolleyHelper volleyHelper;
        volleyHelper = new VolleyHelper(getActivity());
        volleyHelper.postStringRequest(StaticTexts.REPLY_MESSAGE_GET, HttpPages.login_page, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response != null) {
                    Document doc = Jsoup.parse(response);
                    Element loginform = doc.getElementById("edit_auth");

                    Elements inputElements = loginform.select("tr");

                    TextView name = (TextView) getActivity().findViewById(R.id.drawerHeaderUserName);
                    SharedPrefHelper.savePreferences(getContext(), StaticTexts.USER_REAL_NAME, inputElements.get(5).select("td").text());
                    name.setText(inputElements.get(5).select("td").text());

                    ImageView image = (ImageView) getActivity().findViewById(R.id.drawerHeaderImage);

                    Log.d("image", inputElements.get(20).select("a").attr("abs:href"));

                    String username = SharedPrefHelper.readPreferences(getContext(), StaticTexts.SHARED_PREF_LOGINNAME, "");

                    TextView detail = (TextView) getActivity().findViewById(R.id.draweDetailText);
                    detail.setText(username + "@ceng.metu.edu.tr");

                    Picasso.with(getContext())
                            .load("https://cow.ceng.metu.edu.tr/User/download_userPicture.php?username=" + username)
                                    //.placeholder(R.drawable.ic_file_big)
                                    //.error(R.drawable.ic_cloud_big)
                            .into(image);
                    //ImageLoader mImageLoader = VolleySingleton.getInstance().getImageLoader();
                    //image.setImageUrl(inputElements.get(20).select("a").attr("abs:href"),mImageLoader);
                }
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //String type = args.getString("type");
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
        public void onLoadFinished (Loader < Cursor > loader, Cursor data){
            groups = new ArrayList<>();
            String tempName = "";
            Newsgroup n = new Newsgroup();
            if (data != null && data.moveToFirst()) {
                while (data.moveToNext()) {
                    Url tempUrl = Url.fromCursor(data);
                    if(!tempName.equals(tempUrl.group)){
                        tempName = tempUrl.group;
                        n = new Newsgroup();
                        n.name = tempUrl.group;
                        groups.add(n);
                    }
                    n.addUrl(tempUrl);
                }
            }

            ExpandableListAdapter adapter = new ExpandableListAdapter(getActivity(), groups);
            listview.setAdapter(adapter);
            for (int x = 0; x < groups.size(); x++)
                listview.expandGroup(x);
            listview.setGroupIndicator(null);
            listview.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int group_position, int child_position, long id) {
                    if (!groups.get(group_position).getUrl(child_position).count.equals("(0)")) {
                        Intent intent = new Intent(getActivity(), MessagesActivity.class);
                        intent.putExtra("name", groups.get(group_position).getUrl(child_position).name);
                        intent.putExtra("link", groups.get(group_position).getUrl(child_position).url);
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                    } else {
                        Toast.makeText(getActivity().getApplicationContext(), "Group '" + groups.get(group_position).getUrl(child_position).name + "' has no message to show.", Toast.LENGTH_SHORT).show();
                    }
                    return false;
                }
            });
            adapter.notifyDataSetChanged();

        }

        @Override
        public void onLoaderReset (Loader < Cursor > loader) {

        }

        public interface OnFragmentInteractionListener {
            void onFragmentInteraction(Uri uri);
        }

    private void loadGroup() {
        Intent mServiceIntent = new Intent(getActivity(), NewsGroupIntentService.class);
        mServiceIntent.setData(Uri.parse(HttpPages.left_page));
        mServiceIntent.putExtra("type","0");
        getActivity().startService(mServiceIntent);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(receiver);
    }


    public class ExpandableListAdapter extends BaseExpandableListAdapter {

        private final Context _context;
        private final List<Newsgroup> groups;

        public ExpandableListAdapter(Context context, List<Newsgroup> _groups) {
            this._context = context;
            this.groups = _groups;
        }

        @Override
        public Object getChild(int groupPosition, int childPosititon) {
            return this.groups.get(groupPosition).getUrl(childPosititon);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public View getChildView(int groupPosition, final int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {

            final String childText = getChild(groupPosition, childPosition).toString();

            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this._context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.list_item, null);
            }

            TextView txtListChild = (TextView) convertView
                    .findViewById(R.id.lblist_item);

            txtListChild.setText(Html.fromHtml(childText));
            return convertView;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return this.groups.get(groupPosition)
                    .getSize();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return this.groups.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return this.groups.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            String headerTitle = getGroup(groupPosition).toString();
            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this._context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.list_group, null);
            }

            TextView lblListHeader = (TextView) convertView
                    .findViewById(R.id.lblistgroup);
            lblListHeader.setTypeface(null, Typeface.BOLD);
            lblListHeader.setText(headerTitle);

            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

    }
}