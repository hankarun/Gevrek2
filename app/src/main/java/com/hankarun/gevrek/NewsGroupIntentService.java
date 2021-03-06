package com.hankarun.gevrek;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Response;
import com.hankarun.gevrek.helpers.VolleyHelper;
import com.hankarun.gevrek.libs.CourseItem;
import com.hankarun.gevrek.libs.HttpPages;
import com.hankarun.gevrek.libs.StaticTexts;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NewsGroupIntentService extends IntentService {

    public NewsGroupIntentService() {
        super("NewsGroupFetch");
    }

    public NewsGroupIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            putVariables(downloadUrl(HttpPages.left_page));
            putCourseVariables(downloadUrl(HttpPages.courses_page));
        } catch (Exception e) {
            Log.d("ServiceNetwork", e.getLocalizedMessage());
        }
    }

    private void putCourseVariables(String html) {
        getApplicationContext().getContentResolver().delete(NewsContentProvider.CONTENT_URI1, null, null);
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
                    getApplicationContext().getContentResolver().insert(NewsContentProvider.CONTENT_URI1, new CourseItem(lcodes.get(t.text()), t.text(), t.attr("abs:href")).toContentCalues());
                }
            } else {
                //This is not a solution
            }
            publishResults(0, "courses");
        } else {
            publishResults(1, "courses");
        }
    }

    private void putVariables(String html) {
        getApplicationContext().getContentResolver().delete(NewsContentProvider.CONTENT_URI, null, null);
        if (!html.equals("")) {
            Document doc = Jsoup.parse(html);
            Elements groupblock = doc.select(".np_index_groupblock:not(:has(div))");
            Elements grouphead = doc.select("div.np_index_grouphead");
            int a = 0;
            for (Element div : groupblock) {
                Url temp;
                Elements rews = div.select("a");
                Elements smalls = div.select("small");
                int b = 0;
                String groupName = grouphead.get(a++).text();
                for (Element link : rews) {
                    String color = "";
                    if (smalls.get(b).select("font").size() > 0)
                        color = smalls.get(b).select("font").attr("color");
                    temp = new Url(link.text(), link.attr("href"), smalls.get(b++).text(), color, groupName);
                    getApplicationContext().getContentResolver().insert(NewsContentProvider.CONTENT_URI, temp.toContentValues());
                }
            }
            publishResults(0, "fetch");
        } else {
            publishResults(1, "fetch");
        }
    }

    private void publishResults(int result, String s) {
        Intent intent = new Intent(s);
        intent.putExtra("result", result);
        sendBroadcast(intent);
    }

    private String downloadUrl(String myurl) throws IOException {
        VolleyHelper volleyHelper;
        volleyHelper = new VolleyHelper(getApplicationContext());
        return volleyHelper.syncStringRequest(StaticTexts.REPLY_MESSAGE_GET, myurl);
    }
}
