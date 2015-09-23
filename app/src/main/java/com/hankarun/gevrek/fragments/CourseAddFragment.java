package com.hankarun.gevrek.fragments;


import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.hankarun.gevrek.R;
import com.hankarun.gevrek.helpers.VolleyHelper;
import com.hankarun.gevrek.libs.HttpPages;
import com.hankarun.gevrek.libs.StaticTexts;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;

public class CourseAddFragment extends Fragment {
    private final Map<String,String> corses = new HashMap<String,String>();

    public CourseAddFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_course_add, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadCourseList();
    }

    private void loadCourseList(){
        VolleyHelper volleyHelper = new VolleyHelper(getActivity());
        volleyHelper.postStringRequest(StaticTexts.COURSELIST, HttpPages.courses_page, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                onTaskComplete(response);
            }
        });
    }

    private void onTaskComplete(String response){
        if(!response.equals("")){
            Document doc = Jsoup.parse(response);
            doc.setBaseUri("https://cow.ceng.metu.edu.tr");


            Map<String,String> lcodes = new HashMap<String,String>();

            Elements names = doc.select("div");
            Element divs = null;
            for(Element e:names){
                if(e.attr("id").equals("mtm_menu_horizontal"))
                    divs = e;
            }
            Elements rnamesd = doc.select("td.content").select("tr").select("td");
            if(divs!=null){
                int x = 0;
                while(x<rnamesd.size()){
                    lcodes.put(rnamesd.get(x).text(), rnamesd.get(x+1).text());
                    x += 2;
                }
                Elements courses = divs.select("a");
                courses.remove(0);
                for(Element t: courses){
                    Log.d("codes", lcodes.get(t.text()));
                    Log.d("names",t.text());
                    Log.d("page", t.attr("abs:href"));
                }
            }else{
                //This is not a solution
            }
        }
        //Compute other course list do show different to remove
        if(!response.equals("")){
            Document doc1 = Jsoup.parse(response);
            doc1.setBaseUri("https://cow.ceng.metu.edu.tr");
            Elements table = doc1.select("table.cow");
            Elements tds = table.select("td");
            int x = 0;
            while(x<tds.size()){
                corses.put(tds.get(x).text(), tds.get(x+1).text());
                x += 2;
            }
        }
    }

}
