package com.hankarun.gevrek.fragments;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.hankarun.gevrek.R;
import com.hankarun.gevrek.helpers.VolleyHelper;
import com.hankarun.gevrek.libs.HttpPages;
import com.hankarun.gevrek.libs.StaticTexts;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewsGroupEditFragment extends Fragment {
    private ListView listview;
    GroupListAdapter adapter;
    EditText filterEditText;
    Button send;
    Button cancel;
    private Dialog mDialog;

    public class GroupName{
        String name;
        Boolean checked;
        Boolean disabled;
    }

    public NewsGroupEditFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_news_group_edit, container, false);
        listview = (ListView) rootView.findViewById(R.id.allGroupsList);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                GroupName tmp = (GroupName) adapter.getItem(i);

                if (adapter.names.get(adapter.names.indexOf(tmp)).checked)
                    adapter.names.get(adapter.names.indexOf(tmp)).checked = false;
                else
                    adapter.names.get(adapter.names.indexOf(tmp)).checked = true;
                adapter.notifyDataSetChanged();
            }
        });

        send = (Button) rootView.findViewById(R.id.groupsave);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendGroupsviaVolley();
            }
        });
        cancel = (Button) rootView.findViewById(R.id.groupcancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                volleyHelper.cancelRequest();
                Intent returnIntent = new Intent();
                getActivity().setResult(8, returnIntent);
                getActivity().finish();
            }
        });

        filterEditText = (EditText) rootView.findViewById(R.id.groupSearchEdit);

        // Add Text Change Listener to EditText
        filterEditText.addTextChangedListener(new TextWatcher()
        {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                // Call back the Adapter with current character to Filter
                adapter.getFilter().filter(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,int after)
            {
            }

            @Override
            public void afterTextChanged(Editable s)
            {
            }
        });

        filterEditText.setOnTouchListener(new View.OnTouchListener() {
            final Drawable imgX = getResources().getDrawable(R.mipmap.ic_action_remove );
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Is there an X showing?
                if (filterEditText.getCompoundDrawables()[2] == null) return false;
                // Only do this for up touches
                if (event.getAction() != MotionEvent.ACTION_UP) return false;
                // Is touch on our clear button?
                if (event.getX() > filterEditText.getWidth() - filterEditText.getPaddingRight() - imgX.getIntrinsicWidth()) {
                    filterEditText.setText("");

                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                }
                return false;
            }
        });

        Context mContext = getActivity();
        mDialog = new Dialog(mContext);


        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        mDialog.setContentView(R.layout.custom_dialog);
        mDialog.setCancelable(false);

        final Window window = mDialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        //window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        mDialog.show();

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadGroupsViaVolley();
    }

    private VolleyHelper volleyHelper;

    public void sendGroupsviaVolley(){
        volleyHelper = new VolleyHelper(getActivity());

        volleyHelper.params.put("submitOptions","save Options");

        String collecs = "";
        for(GroupName a: adapter.names){
            if(a.checked)
                volleyHelper.params.put("mygroups["+adapter.names.indexOf(a)+"]",a.name);
            //collecs = collecs + a.name + ",";
        }
        volleyHelper.postStringRequest(StaticTexts.READMESSAGES, HttpPages.group_edit_page, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                mDialog.dismiss();
                Intent returnIntent = new Intent();
                returnIntent.putExtra("type","group");
                getActivity().setResult(1, returnIntent);
                getActivity().finish();
            }
        });
        mDialog.show();
    }

    private void parse_loadAdapter(String response){
        mDialog.dismiss();
        if(!response.equals("")){
            Document doc = Jsoup.parse(response);
            Elements test = doc.select("input");
            ArrayList<GroupName> groups = new ArrayList<GroupName>();

            for(Element e: test){
                if(e.attr("name").equals("mygroups[]")){
                    GroupName tmp = new GroupName();

                    tmp.name = e.attr("value");
                    tmp.checked = e.hasAttr("checked");
                    tmp.disabled = e.hasAttr("disabled");

                    groups.add(tmp);
                }
            }
            adapter = new GroupListAdapter(getActivity().getApplicationContext(), groups);
            listview.setAdapter(adapter);
        }else{
            Toast.makeText(getActivity().getApplicationContext(), R.string.network_problem, Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

    }

    public void loadGroupsViaVolley(){
        volleyHelper = new VolleyHelper(getActivity());
        volleyHelper.postStringRequest(StaticTexts.READMESSAGES, HttpPages.group_edit_page, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parse_loadAdapter(response);
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

    public class GroupListAdapter extends BaseAdapter implements Filterable {
        Context context;
        public ArrayList<GroupName> names;
        private ArrayList<GroupName> filteredModelItemsArray;
        CustomFilter filter;

        public GroupListAdapter(Context _context, ArrayList<GroupName> _names){
            context = _context;
            names = _names;
            filteredModelItemsArray = new ArrayList<GroupName>();
            filteredModelItemsArray.addAll(names);
        }

        @Override
        public int getCount() {
            return filteredModelItemsArray.size();
        }

        @Override
        public Object getItem(int i) {
            return filteredModelItemsArray.get(i);
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
                view = infalInflater.inflate(R.layout.group_edit_item, null);
            }

            TextView groupName = (TextView) view.findViewById(R.id.groupNameText);
            CheckBox groupCheck = (CheckBox) view.findViewById(R.id.groupCheckBox);

            GroupName tmp = (GroupName) filteredModelItemsArray.get(i);

            groupName.setText(tmp.name);
            groupCheck.setChecked(tmp.checked);
            groupCheck.setEnabled(!tmp.disabled);

            return view;
        }

        @Override
        public Filter getFilter() {
            if (filter == null){
                filter = new CustomFilter();
            }
            return filter;
        }


        private class CustomFilter extends Filter {

            @SuppressLint("DefaultLocale")
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                constraint = constraint.toString().toLowerCase();
                FilterResults result = new FilterResults();
                if(constraint != null && constraint.toString().length() > 0)
                {
                    ArrayList<GroupName> filteredItems = new ArrayList<GroupName>();

                    for(int i = 0, l = names.size(); i < l; i++)
                    {
                        String m = names.get(i).name;
                        if(m.contains(constraint))
                            filteredItems.add(names.get(i));
                    }
                    result.count = filteredItems.size();
                    result.values = filteredItems;
                }
                else
                {
                    synchronized(this)
                    {
                        result.values = names;
                        result.count = names.size();
                    }
                }
                return result;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredModelItemsArray.clear();
                filteredModelItemsArray.addAll((ArrayList<GroupName>) results.values);
                notifyDataSetChanged();
            }

        }
    }
}

