package com.hankarun.gevrek.fragments;


import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.hankarun.gevrek.R;
import com.hankarun.gevrek.helpers.NNTPHelper;
import com.hankarun.gevrek.helpers.PostDialogHelper;
import com.hankarun.gevrek.helpers.SharedPrefHelper;
import com.hankarun.gevrek.helpers.VolleyHelper;
import com.hankarun.gevrek.helpers.WaitDialogHelper;
import com.hankarun.gevrek.interfaces.AsyncResponse;
import com.hankarun.gevrek.interfaces.LoginDialogReturn;
import com.hankarun.gevrek.libs.ConnectionChecker;
import com.hankarun.gevrek.libs.HttpPages;
import com.hankarun.gevrek.libs.StaticTexts;
import com.hankarun.gevrek.libs.VolleySingleton;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import static com.hankarun.gevrek.helpers.SharedPrefHelper.readPreferences;

public class ReadMessageFragment extends Fragment implements LoginDialogReturn,AsyncResponse {
    private TextView from;
    private TextView date;
    private WebView body;
    private NetworkImageView avatar;
    private VolleyHelper volleyHelper;
    private String reply;
    private PostDialogHelper postDialogHelper;

    public int link;
    public ArrayList<CharSequence> link_list;
    public ArrayList<CharSequence> header_list;
    public String mid;
    private Menu menu;
    private String groupname;
    private Dialog waitDialog;

    public ReadMessageFragment() {
        // Required empty public constructor
    }


    public void loadMessage(int _link){
        waitDialog.show();
        mid = link_list.get(link).subSequence(link_list.get(link).toString().indexOf("#")+1,link_list.get(link).length()).toString();
        volleyHelper = new VolleyHelper(getActivity());
        volleyHelper.postStringRequest(StaticTexts.READMESSAGES, HttpPages.group_page + link_list.get(_link), new Response.Listener<String>() {
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

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private boolean showDelete = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString("a");
            mParam2 = getArguments().getString("b");
        }
        setHasOptionsMenu(true);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_read_message, container, false);
        Bundle bundle = getActivity().getIntent().getExtras();
        if(bundle != null)
        {
            link = bundle.getInt("message");
            link_list = bundle.getCharSequenceArrayList("list");
            header_list = bundle.getCharSequenceArrayList("headers");
            groupname = bundle.getString("groupname");
        }
        from = (TextView) rootView.findViewById(R.id.from_text);
        date = (TextView) rootView.findViewById(R.id.date_text);
        body = (WebView) rootView.findViewById(R.id.body_view);
        avatar = (NetworkImageView) rootView.findViewById(R.id.authoravatar);

        waitDialog = new WaitDialogHelper(getActivity());
        waitDialog.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    getActivity().finish();
                }
                return true;
            }
        });
        loadMessage(link);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_read_message, menu);
        this.menu = menu;
        MenuItem item = menu.findItem(R.id.action_delete);
        item.setVisible(showDelete);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            //Burada cevaplama dialogu gözükecek.
            postDialogHelper = new PostDialogHelper(getActivity(),getActivity());
            postDialogHelper.answer = this;
            Bundle args = new Bundle();
            args.putInt("type", StaticTexts.REPLY_MESSAGE_DIALOG);
            args.putString("link", HttpPages.group_page + reply);
            postDialogHelper.dialogShow(args, getActivity());
            return true;
        }

        if(id == R.id.action_delete){
            //Show waiting screen.
            waitDialog.show();

            //NNTP ile delete yapılacak.
            NNTPHelper nntpHelper = new NNTPHelper(
                    readPreferences(getActivity(), StaticTexts.SHARED_PREF_LOGINNAME, "").toString(),
                    readPreferences(getActivity(), StaticTexts.SHARED_PREF_PASSWORD, "").toString()
            );
            nntpHelper.asyncResponse = this;
            nntpHelper.deleteArticle(mid, s, "metu.ceng." + groupname, s);
        }
        return super.onOptionsItemSelected(item);
    }

    private String s;

    public void onTaskComplete(String html) {
        if (!html.equals("")) {
            Document doc = Jsoup.parse(html);

            avatarCheck(doc);

            reply = doc.select("a.np_button").attr("href");

            String tmp = doc.select("div.np_article_header").text();
            int sbb = tmp.indexOf("Subject:");
            int fbb = tmp.indexOf("From:");
            int dbb = tmp.indexOf("Date:");
            String attach = "";
            if (tmp.indexOf("Attachments:") > 0) {
                attach = "Ekler" + doc.select("div.np_article_header").select("a").get(1).toString();
            }

            //Buraya mesaj baslığı gelecek
            String title;
            try {
                title = tmp.substring(sbb + 9, fbb - 1);
            } catch (Exception e) {
                title = "";
            }

            ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(title);

            String fpps = tmp.substring(fbb, tmp.length());
            try {
                from.setText(fpps.substring(6, fpps.indexOf("(") - 1)); //author
            } catch (Exception e) {
                from.setText("");
            }

            Elements es = doc.select("div.np_article_header");
            s = es.select("a").attr("href");
            s = s.replace("mailto:","");

            date.setText(tmp.substring(dbb + 6, dbb + 20)); //date

            Elements bod = doc.select("div.np_article_body");
            String start = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" + "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html\" charset=\"UTF-8\" /></head><body>";
            String end = "</body></html>";
            body.loadData(start + attach + bod.toString() + end, "text/html; charset=UTF-8", "UTF-8");
            body.setBackgroundColor(0x00000000);

            String username = s.substring(0,s.indexOf("@"));
            if(username.equals(SharedPrefHelper.readPreferences(getActivity(), StaticTexts.SHARED_PREF_LOGINNAME, "").toString())){
                showDelete = true;
                if(menu != null){
                    menu.findItem(R.id.action_delete).setVisible(showDelete);
                }
            }else{
                showDelete = false;
                if(menu != null){
                    menu.findItem(R.id.action_delete).setVisible(showDelete);
                }
            }


            waitDialog.dismiss();
        } else {
            Toast.makeText(getActivity().getApplicationContext(), R.string.network_problem, Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

    }

    private void avatarCheck(Document doc) {
        //Check for options
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        ImageLoader mImageLoader = VolleySingleton.getInstance().getImageLoader();
        Elements heads = doc.select("tbody").select("td");
        //new GetAvatar().execute(heads.get(0).select("a").attr("href"));
        avatar.setImageUrl(heads.get(0).select("a").attr("href"),mImageLoader);
            switch (settings.getString(StaticTexts.AVATAR_METHOD,"0")){
                case "2":
                    if(ConnectionChecker.isConnected(getActivity().getApplicationContext())){
                        avatar.setImageUrl(heads.get(0).select("a").attr("href"),mImageLoader);
                    }
                    break;
                case "1":
                    if(ConnectionChecker.isConnectedFast(getActivity().getApplicationContext())){
                        avatar.setImageUrl(heads.get(0).select("a").attr("href"),mImageLoader);
                    }
                    break;
                case "0":
                    if(ConnectionChecker.isConnectedWifi(getActivity().getApplicationContext())){
                        avatar.setImageUrl(heads.get(0).select("a").attr("href"),mImageLoader);
                    }
                    break;
            }

    }

    @Override
    public void dialogFinished() {
        postDialogHelper.mDialog.dismiss();
        getActivity().setResult(1);
        getActivity().finish();
        //Bu activiteyi bitir ve öncekini yenile.
    }

    @Override
    public void onResponse(int feed) {
        waitDialog.dismiss();
        getActivity().setResult(1);
        getActivity().finish();
    }
}
