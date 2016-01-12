package com.hankarun.gevrek;

import android.app.IntentService;
import android.content.Intent;
import android.provider.SyncStateContract;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.hankarun.gevrek.helpers.SharedPrefHelper;
import com.hankarun.gevrek.libs.HttpPages;
import com.hankarun.gevrek.libs.StaticTexts;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class NewsGroupIntentService extends IntentService {

    public NewsGroupIntentService(){
        super("NewsGroupFetch");
    }

    public NewsGroupIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            putVariables(downloadUrl(HttpPages.left_page));
        }catch (Exception e){
            Log.d("ServiceNetwork",e.getLocalizedMessage());
        }
    }

    private void putVariables(String html){
        getApplicationContext().getContentResolver().delete(NewsContentProvider.CONTENT_URI, null, null);
        if(!html.equals("")){
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
                for (Element link : rews){
                    String color = "";
                    if(smalls.get(b).select("font").size()>0)
                        color = smalls.get(b).select("font").attr("color");
                    temp = new Url(link.text(), link.attr("href"),smalls.get(b++).text(),color,groupName);
                    getApplicationContext().getContentResolver().insert(NewsContentProvider.CONTENT_URI, temp.toContentValues());
                }
            }
            publishResults(0);
        }else{
            publishResults(1);
        }
    }

    private void publishResults(int result) {
        Intent intent = new Intent("fetch");
        intent.putExtra("result", result);
        sendBroadcast(intent);
    }

    private String downloadUrl(String myurl) throws IOException {
        InputStream is = null;

        try {
            String urlParameters  = "cow_username="+SharedPrefHelper.readPreferences(getApplicationContext(), StaticTexts.SHARED_PREF_LOGINNAME, "")
                    +"&cow_password="+SharedPrefHelper.readPreferences(getApplicationContext(), StaticTexts.SHARED_PREF_PASSWORD, "")
                    +"&cow_login=login";
            byte[] postData       = urlParameters.getBytes( StandardCharsets.UTF_8 );
            int    postDataLength = postData.length;
            String request        = myurl;
            URL    url            = new URL( request );
            HttpURLConnection conn= (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty( "charset", "utf-8");
            conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
            conn.setUseCaches( false );
            try( DataOutputStream wr = new DataOutputStream( conn.getOutputStream())) {
                wr.write( postData );
            }
            is = conn.getInputStream();

            // Convert the InputStream into a string
            StringWriter writer = new StringWriter();
            IOUtils.copy(is, writer, "UTF-8");
            return writer.toString();

        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
}
