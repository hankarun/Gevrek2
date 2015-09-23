package com.hankarun.gevrek.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.hankarun.gevrek.R;
import com.hankarun.gevrek.helpers.VolleyHelper;
import com.hankarun.gevrek.libs.StaticTexts;
import com.hankarun.gevrek.libs.HttpPages;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class HomewroksFragment extends Fragment {
    private String lname;
    private ListView listView;
    private String course;
    private VolleyHelper volleyHelper;
    private ProgressBar progressBar;

    public void start(String _lname) {
        lname = _lname;
        course = "test";
    }

    public HomewroksFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStop() {
        super.onStop();
        if (volleyHelper != null) {
            volleyHelper.cancelRequest();
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_homewroks, container, false);
        listView = (ListView) view.findViewById(R.id.hmwlist);
        progressBar = (ProgressBar) view.findViewById(R.id.homeworkBar);
        startTask();
        return view;
    }

    private void startTask(){
        volleyHelper = new VolleyHelper(getActivity());
        volleyHelper.params.put("task_homeworks","list");
        volleyHelper.params.put("selector_homeworks_course",lname);
        volleyHelper.postStringRequest(StaticTexts.HOMEWORK_PAGE, HttpPages.homeworks_page, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                onTaskComplete(response);
            }
        });
    }


    private void onTaskComplete(String html) {
        progressBar.setVisibility(View.GONE);
        if(!html.equals("")){
            Document doc = Jsoup.parse(html);
            ArrayList<HomeWorks> hmws = new ArrayList<HomeWorks>();

            Elements table = doc.select("table[class=cow]");
            Elements others = table.select("tr");

            if(!others.toString().contains("The list is empty....")){
                for(int x=3; x < others.size(); x++){
                    HomeWorks tmp = new HomeWorks();
                    tmp.id = others.get(x).select("td").get(0).text();
                    tmp.name = others.get(x).select("td").get(1).text();
                    tmp.deadline = others.get(x).select("td").get(2).text();
                    tmp.greaded = others.get(x).select("td").get(3).text();
                    tmp.link = others.get(x).select("td").get(4).select("a").attr("href");
                    if(others.get(x).select("td").get(5).text().equals(""))
                        tmp.greade = "-";
                    else
                        tmp.greade = others.get(x).select("td").get(5).text();
                    tmp.avarage = others.get(x).select("td").get(6).text();
                    tmp.course = course;
                    hmws.add(tmp);
                }
            }else{
                //Show no homework screen
            }

            MyOtherAdapter adapter = new MyOtherAdapter(getActivity().getApplicationContext(),hmws);
            listView.setAdapter(adapter);
        }else{
            Toast.makeText(getActivity().getApplicationContext(), R.string.network_problem, Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
    }


    public class HomeWorks{
        String course;
        String id;
        String name;
        String deadline;
        String greaded;
        String greade;
        String avarage;
        public String link;
    }

    public class MyOtherAdapter extends BaseAdapter {
        final ArrayList<HomeWorks> hmw;
        final Context context;

        public MyOtherAdapter(Context context, ArrayList<HomeWorks> _hmw){
            this.context = context;
            hmw = _hmw;
        }

        @Override
        public int getCount() {
            return hmw.size();
        }

        @Override
        public Object getItem(int i) {
            return hmw.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                LayoutInflater infalInflater = (LayoutInflater) this.context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = infalInflater.inflate(R.layout.hmw_item, null);
            }

            TextView hname = (TextView) view.findViewById(R.id.hmwname);
            TextView grade = (TextView) view.findViewById(R.id.grade);
            TextView agrade = (TextView) view.findViewById(R.id.avarage);

            hname.setText(hmw.get(i).name);
            grade.setText(hmw.get(i).greade);
            agrade.setText(hmw.get(i).avarage);

            return view;
        }
    }
}
