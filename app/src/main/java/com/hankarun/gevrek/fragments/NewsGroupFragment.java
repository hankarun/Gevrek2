package com.hankarun.gevrek.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.hankarun.gevrek.R;
import com.hankarun.gevrek.activities.MessagesActivity;
import com.hankarun.gevrek.activities.NewsGropuEditActivity;
import com.hankarun.gevrek.helpers.VolleyHelper;
import com.hankarun.gevrek.libs.HttpPages;
import com.hankarun.gevrek.libs.StaticTexts;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class NewsGroupFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private ExpandableListView listview;
    private ExpandableListAdapter adapter;
    private List<Newsgroup> groups;

    private ProgressBar mProgressBar;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private VolleyHelper volleyHelper;


    // TODO: Rename and change types and number of parameters
    public static NewsGroupFragment newInstance(String param1, String param2) {
        NewsGroupFragment fragment = new NewsGroupFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public void reload(){
        startTask();
    }

    public NewsGroupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        setHasOptionsMenu(true);
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

        startTask();
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    public void onTaskComplete(String html) {
        if(!html.equals("")){
            groups = new ArrayList<Newsgroup>();

            Document doc = Jsoup.parse(html);
            Elements groupblock = doc.select(".np_index_groupblock:not(:has(div))");
            Elements grouphead = doc.select("div.np_index_grouphead");
            int a = 0;
            for (Element div : groupblock) {
                Newsgroup temp = new Newsgroup();
                temp.name = grouphead.get(a++).text();
                Elements rews = div.select("a");
                Elements smalls = div.select("small");
                int b = 0;
                for (Element link : rews){
                    String color = "";
                    if(smalls.get(b).select("font").size()>0)
                        color = smalls.get(b).select("font").attr("color");
                    temp.addUrl(link.text(), link.attr("href"),smalls.get(b++).text(),color);
                }
                groups.add(temp);

            }
            adapter = new ExpandableListAdapter(getActivity(),groups);
            listview.setAdapter(adapter);
            for(int x = 0; x < groups.size(); x++)
                listview.expandGroup(x);
            listview.setGroupIndicator(null);
            listview.setOnChildClickListener(new ExpandableListView.OnChildClickListener()
            {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int group_position, int child_position, long id)
                {
                    if(!groups.get(group_position).getUrl(child_position).count.equals("(0)")) {
                        Intent intent = new Intent(getActivity(), MessagesActivity.class);
                        intent.putExtra("name", groups.get(group_position).getUrl(child_position).name);
                        intent.putExtra("link", groups.get(group_position).getUrl(child_position).url);
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                    }else{
                        Toast.makeText(getActivity().getApplicationContext(), "Group '" +groups.get(group_position).getUrl(child_position).name + "' has no message to show.", Toast.LENGTH_SHORT).show();
                    }
                    return false;
                }
            });
            adapter.notifyDataSetChanged();
        }else{
            Toast.makeText(getActivity().getApplicationContext(), R.string.network_problem, Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
    }

    private void startTask(){
        mProgressBar.setVisibility(View.VISIBLE);

        volleyHelper = new VolleyHelper(getActivity());
        volleyHelper.postStringRequest(StaticTexts.COURSES_REQUEST, HttpPages.left_page, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                mProgressBar.setVisibility(View.GONE);
                onTaskComplete(response);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        if(volleyHelper != null)
            volleyHelper.cancelRequest();
    }

    public class Newsgroup {
        public String name;
        public final List<Urls> groups = new ArrayList<Urls>();

        @Override
        public String toString(){
            return name;
        }

        public int getSize() { return groups.size();}

        public void addUrl(String _name, String _url, String _count, String _color){
            groups.add(new Urls(_name,_url, _count, _color));
        }

        public Urls getUrl(int i) { return groups.get(i);}


    }
    public class Urls {
        public final String name;
        public final String url;
        public final String count;
        public final String color;

        @Override
        public String toString(){return name + " <font color=\""+ color +"\">" +count + "</font>";}

        Urls(String _name, String _url, String _count, String _color){
            name = _name;
            count = _count;
            url = _url;
            color = _color;
        }
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
