package com.hankarun.gevrek.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessagesFragment extends Fragment implements LoginDialogReturn {
    private VolleyHelper volleyHelper;
    private ProgressBar mProgressBar;
    private RecyclerView vies;
    private String groupName;
    private String link;
    private String reply;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private MyBaseAdapter adapters;
    private MyAdapter mAdapter;
    private ArrayList<MessageHeader> array;

    private boolean loading = true;


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

    private View rootView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_messages, container, false);
        vies = (RecyclerView) rootView.findViewById(R.id.mMessagesListView);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar4);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });

        array = new ArrayList<MessageHeader>();
        adapters = new MyBaseAdapter(getActivity().getApplicationContext(), array);
        mAdapter = new MyAdapter(array);

        vies.setHasFixedSize(true);

        // use a linear layout manager
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        vies.setLayoutManager(mLayoutManager);

        vies.setAdapter(mAdapter);



        vies.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int pastVisiblesItems, visibleItemCount, totalItemCount;
                visibleItemCount = mLayoutManager.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();

                if (loading) {
                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        loading = false;

                        if(currentPage<totalPage){
                            link = stringMap.get(currentPage+1);
                            loadPages(false);
                        }
                    }
                }
            }
        });

        //vies.setAdapter(adapters);


        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentPage = 1;
                loadPages(true);
            }
        });

        return rootView;
    }

    public void link(String link, String grouName){
        this.groupName = grouName;
        this.link = link;
        loadPages(true);
    }

    public void loadPages(final boolean clear){
        mSwipeRefreshLayout.setRefreshing(true);
        volleyHelper = new VolleyHelper(getActivity());
        volleyHelper.postStringRequest(StaticTexts.MESSAGE_LIST_REQUEST, HttpPages.group_page + link, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                mProgressBar.setVisibility(View.GONE);
                mSwipeRefreshLayout.setRefreshing(false);
                onTaskComplete(response, clear);
                loading = true;
            }
        });
        Snackbar.make(rootView, "New messages loading...", Snackbar.LENGTH_SHORT)
                .setAction("CLOSE", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                })
                .setActionTextColor(getResources().getColor(android.R.color.holo_red_light))
                .show();
    }

    private void onTaskComplete(String html, boolean clear){
        if(!html.equals("")){
            if(clear){
                array.clear();
            }
            Document doc = Jsoup.parse(html);

            setPages(doc);

            Elements table = doc.select("table.np_thread_table").select("tr");
            reply = doc.select("a.np_button").get(0).attr("href");
            table.remove(0);
            for(Element s : table){
                MessageHeader tmp = new MessageHeader();
                Elements trs = s.select("td");
                if (s.select("font").size()>0)
                    tmp.color = s.select("font").attr("color");
                tmp.date = trs.get(0).text();
                tmp.read = trs.get(1).select("a").attr("class").equals("read");
                for (Element dd:trs.get(1).select("img"))
                    tmp.images.add(dd.attr("alt"));
                tmp.header = trs.get(1).text();
                tmp.href = trs.get(1).select("a").attr("href");
                tmp.author = trs.get(3).text();
                array.add(tmp);
            }

            //vies.notify();

            //vies.deferNotifyDataSetChanged();

            /*vies.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
            });*/

        }else{
            Toast.makeText(getActivity().getApplicationContext(), R.string.network_problem,Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
    }

    private int currentPage;
    private int totalPage;
    private Map<Integer,String> stringMap;

    private void setPages(Document doc){
        stringMap = new HashMap<>();
        Elements inner = doc.select("span.np_pages");
        Element selected = inner.select("span.np_pages_selected").first();
        Elements unselected = inner.select("a.np_pages_unselected");

        currentPage = Integer.parseInt(selected.text());
        int x = 1;
        for(Element e:unselected){
            x = x + 1;
            stringMap.put(Integer.parseInt(e.text()), e.attr("href"));
        }
        totalPage = x;
    }

    @Override
    public void dialogFinished() {
        loadPages(true);
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


    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private ArrayList<MessageHeader> mDataset;

        public void add(int position, MessageHeader item) {
            mDataset.add(position, item);
            notifyItemInserted(position);
        }

        public void remove(MessageHeader item) {
            int position = mDataset.indexOf(item);
            mDataset.remove(position);
            notifyItemRemoved(position);
        }

        public MyAdapter(ArrayList<MessageHeader> myDataset) {
            mDataset = myDataset;
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final MessageHeader cHeader = mDataset.get(position);
            holder.txtDate.setText(Html.fromHtml("<font color=\"" + cHeader.color + "\">" + cHeader.date + "</font>"));

            String header = "<?xml version=\"1.0\" encoding=\"iso-8859-9\" ?>";

            String reads;
            if(cHeader.read)
                reads = "<font color=\"#999900\">"+header + cHeader.header+"</font>";
            else
                reads = "<font color=\"#26598F\">"+header + cHeader.header+"</font>";


            String firstPart = "";
            for (int x = 1; x < cHeader.getImg().length(); x++) {
                firstPart = firstPart + "   ";
            }

            holder.txtBody.setText(Html.fromHtml(firstPart+reads));

            holder.total.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ArrayList<CharSequence> tmp = new ArrayList<CharSequence>();
                    ArrayList<CharSequence> tmp1 = new ArrayList<>();

                    for (int x = 0; x < mDataset.size(); x++) {
                        MessageHeader tmps = mDataset.get(x);
                        tmp.add(tmps.href);
                        tmp1.add(tmps.header);
                    }

                    Intent intent = new Intent(getActivity(), ReadMessageActivity.class);

                    intent.putCharSequenceArrayListExtra("list",tmp);
                    intent.putCharSequenceArrayListExtra("headers",tmp1);
                    intent.putExtra("groupname",groupName);

                    intent.putExtra("message", mDataset.indexOf(cHeader));
                    getActivity().startActivityForResult(intent, 1);
                }
            });


            holder.txtAuthor.setText(cHeader.author);


        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView txtBody;
            public TextView txtAuthor;
            public TextView txtDate;
            public LinearLayout lines;
            public View total;

            public ViewHolder(View v) {
                super(v);
                txtBody = (TextView) v.findViewById(R.id.body);
                txtAuthor = (TextView) v.findViewById(R.id.author);
                txtDate = (TextView) v.findViewById(R.id.date);
                lines = (LinearLayout) v.findViewById(R.id.imageLayout);
                total = (View) v.findViewById(R.id.messagelayout);
            }
        }
    }

}

