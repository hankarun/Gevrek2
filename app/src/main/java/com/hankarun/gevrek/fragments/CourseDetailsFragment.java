package com.hankarun.gevrek.fragments;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.hankarun.gevrek.R;
import com.hankarun.gevrek.helpers.VolleyHelper;
import com.hankarun.gevrek.libs.HttpPages;
import com.hankarun.gevrek.libs.StaticTexts;
import com.hankarun.gevrek.libs.VolleySingleton;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class CourseDetailsFragment extends Fragment {
    public String lname;
    String course;
    WebView mWebview;
    private VolleyHelper volleyHelper;

    public void start(String _lname) {
        lname = _lname;
        course = "test";
    }

    public CourseDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_course_details, container, false);

        mWebview = (WebView) view.findViewById(R.id.courseDetailsWeb);

        startTask();
        return view;
    }

    private void startTask(){
        volleyHelper = new VolleyHelper(getActivity());
        volleyHelper.postStringRequest(StaticTexts.HOMEWORK_PAGE, lname, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                onTaskComplete(response);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        if (volleyHelper != null) {
            volleyHelper.cancelRequest();
        }
    }

    public void onTaskComplete(String html) {
        if(!html.equals("")){
            Document doc = Jsoup.parse(html);
            Elements tds = doc.select("td[class=content]");
            Elements tds1 = tds.select("td");

            String info = "<center>"+getString(R.string.info)+"</center>" +tds1.get(1).toString();
            String staff = "<br><HR COLOR=\"blue\" size=\"10\"><center>"+getString(R.string.staff)+"</center>" + tds1.get(2).toString();
            String announcments = "<HR COLOR=\"blue\" size=\"10\"><center>"+getString(R.string.announcments)+"</center>" + tds1.get(3).toString();
            String notes = "<HR COLOR=\"blue\" size=\"10\"><center>"+getString(R.string.lecture_notes)+"</center><br>" + tds1.get(4).toString();
            String exams = "<HR COLOR=\"blue\" size=\"10\"><center>"+getString(R.string.exams)+"</center>" + tds1.get(5).toString();


            mWebview.loadData(info+staff+announcments+notes+exams,"text/html", "UTF-8");
        }

    }

}
