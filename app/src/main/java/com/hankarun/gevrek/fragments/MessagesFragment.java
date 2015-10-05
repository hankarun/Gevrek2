package com.hankarun.gevrek.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.hankarun.gevrek.R;
import com.hankarun.gevrek.activities.ReadMessageActivity;
import com.hankarun.gevrek.helpers.PostDialogHelper;
import com.hankarun.gevrek.helpers.VolleyHelper;
import com.hankarun.gevrek.interfaces.LoginDialogReturn;
import com.hankarun.gevrek.libs.HttpPages;
import com.hankarun.gevrek.libs.StaticTexts;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class MessagesFragment extends Fragment implements LoginDialogReturn {
    private VolleyHelper volleyHelper;
    private ProgressBar mProgressBar;
    private ListView vies;
    private String groupName;
    private String link;
    private String reply;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public MessagesFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String mParam1 = getArguments().getString("a");
            String mParam2 = getArguments().getString("b");
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_messages, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            //Burada cevaplama dialogu gözükecek.
            PostDialogHelper postDialogHelper = new PostDialogHelper(getActivity(),getActivity());
            postDialogHelper.answer = this;
            Bundle args = new Bundle();
            args.putInt("type", StaticTexts.REPLY_MESSAGE_GET);
            args.putString("link", HttpPages.group_page + reply);
            postDialogHelper.dialogShow(args,getActivity());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);
        vies = (ListView) view.findViewById(R.id.mMessagesListView);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar4);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadPages();
            }
        });

        return view;
    }

    public void link(String link, String grouName){
        this.groupName = grouName;
        this.link = link;
        loadPages();
    }

    public void loadPages(){
        //mProgressBar.setVisibility(View.VISIBLE);
        mSwipeRefreshLayout.setRefreshing(true);
        volleyHelper = new VolleyHelper(getActivity());
        volleyHelper.postStringRequest(StaticTexts.MESSAGE_LIST_REQUEST, HttpPages.group_page+link, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                mProgressBar.setVisibility(View.GONE);
                mSwipeRefreshLayout.setRefreshing(false);
                onTaskComplete(response);
            }
        });
    }

    private void onTaskComplete(String html){
        if(!html.equals("")){
            List<MessageHeader> array = new ArrayList<MessageHeader>();
            Document doc = Jsoup.parse(html);
            Elements table = doc.select("table.np_thread_table").select("tr");
            reply = doc.select("a.np_button").get(0).attr("href");
            table.remove(0);
            for(Element s : table){
                MessageHeader tmp = new MessageHeader();
                Elements trs = s.select("td");
                if(s.select("font").size()>0)
                    tmp.color = s.select("font").attr("color");
                tmp.date = trs.get(0).text();
                tmp.read = trs.get(1).select("a").attr("class").equals("read");
                for(Element dd:trs.get(1).select("img"))
                    tmp.images.add(dd.attr("alt"));
                tmp.header = trs.get(1).text();
                tmp.href = trs.get(1).select("a").attr("href");
                tmp.author = trs.get(3).text();
                array.add(tmp);
            }
            MyBaseAdapter adapters = new MyBaseAdapter(getActivity().getApplicationContext(), array);
            vies.setAdapter(adapters);
            vies.deferNotifyDataSetChanged();

            vies.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    ArrayList<CharSequence> tmp = new ArrayList<CharSequence>();
                    ArrayList<CharSequence> tmp1 = new ArrayList<>();

                    for (int x = 0; x < vies.getAdapter().getCount(); x++) {
                        Object o = vies.getItemAtPosition(x);
                        MessageHeader tmps = (MessageHeader) o;
                        tmp.add(tmps.href);
                        tmp1.add(tmps.header);
                    }

                    Intent intent = new Intent(getActivity(), ReadMessageActivity.class);

                    intent.putCharSequenceArrayListExtra("list",tmp);
                    intent.putCharSequenceArrayListExtra("headers",tmp1);
                    intent.putExtra("groupname",groupName);

                    intent.putExtra("message", i);
                    getActivity().startActivityForResult(intent, 1);
                }
            });

        }else{
            Toast.makeText(getActivity().getApplicationContext(), R.string.network_problem,Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
    }

    @Override
    public void dialogFinished() {
        loadPages();
    }


    public class MessageHeader{
        public String header;
        public String date;
        public boolean read;
        public String color;
        public String href;
        public final List<String> images = new ArrayList<String>();
        public String author;
        public String reply;

        public String getImg(){
            String tmp = "";
            for(String a: images)
                tmp += a;
            return tmp;
        }
    }

    public class MyBaseAdapter extends BaseAdapter {
        final List<MessageHeader> headers;
        final Context context;

        public MyBaseAdapter(Context _context, List<MessageHeader> _headers){
            context = _context;
            headers = _headers;
        }

        @Override
        public int getCount() {
            return headers.size();
        }

        @Override
        public Object getItem(int i) {
            return headers.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        private class ImageHolder{
            public LinearLayout images;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ImageHolder tmps = new ImageHolder();
            if (view == null) {
                LayoutInflater infalInflater = (LayoutInflater) this.context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = infalInflater.inflate(R.layout.message_item, null);

                tmps.images = (LinearLayout) view.findViewById(R.id.imageLayout);

                view.setTag(tmps);

            }else{
                tmps = (ImageHolder) view.getTag();
            }
            TextView body = (TextView) view.findViewById(R.id.body);
            TextView date = (TextView) view.findViewById(R.id.date);
            TextView author = (TextView) view.findViewById(R.id.author);
            LinearLayout layout = (LinearLayout) view.findViewById(R.id.messagelayout);

            String header = "<?xml version=\"1.0\" encoding=\"iso-8859-9\" ?>";
            String imgs = headers.get(i).getImg();

            tmps.images.removeAllViews();

            for(int x=0; x<imgs.length(); x++){
                ImageView tmp = new ImageView(context);
                if(imgs.charAt(x) == ' ')
                    tmp.setImageDrawable(view.getResources().getDrawable(R.mipmap.b));
                else
                if(imgs.charAt(x) == '*')
                    tmp.setImageDrawable(view.getResources().getDrawable(R.mipmap.s));
                else
                if(imgs.charAt(x) == 'o')
                    tmp.setImageDrawable(view.getResources().getDrawable(R.mipmap.n));
                else
                if(imgs.charAt(x) == '+')
                    tmp.setImageDrawable(view.getResources().getDrawable(R.mipmap.tt));
                else
                if(imgs.charAt(x) == '-')
                    tmp.setImageDrawable(view.getResources().getDrawable(R.mipmap.g));
                else
                if(imgs.charAt(x) == '|')
                    tmp.setImageDrawable(view.getResources().getDrawable(R.mipmap.l));
                else
                if(imgs.charAt(x) == '`')
                    tmp.setImageDrawable(view.getResources().getDrawable(R.mipmap.k));

                Toolbar.LayoutParams params = new Toolbar.LayoutParams(40, ViewGroup.LayoutParams.MATCH_PARENT);
                params.height = ViewGroup.LayoutParams.MATCH_PARENT;

                tmp.setLayoutParams(params);
                tmp.setFocusable(false);
                tmp.setFocusableInTouchMode(false);
                tmps.images.addView(tmp, x);
            }

            String reads;
            if(headers.get(i).read)
                reads = "<font color=\"#999900\">"+header + headers.get(i).header+"</font>";
            else
                reads = "<font color=\"#26598F\">"+header + headers.get(i).header+"</font>";

            if(i % 2 == 0)
                layout.setBackgroundColor(Color.parseColor("#EEEEEE"));
            else
                layout.setBackgroundColor(Color.parseColor("#ffffff"));
            body.setText(Html.fromHtml(reads));
            date.setText(Html.fromHtml("<font color=\"" + headers.get(i).color + "\">" + headers.get(i).date + "</font>"));
            author.setText(headers.get(i).author);

            return view;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if( volleyHelper != null)
            volleyHelper.cancelRequest();
    }

}

