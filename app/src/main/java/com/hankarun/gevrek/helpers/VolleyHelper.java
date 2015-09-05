package com.hankarun.gevrek.helpers;

import android.app.Activity;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.hankarun.gevrek.libs.VolleySingleton;
import com.hankarun.gevrek.libs.StaticTexts;

import java.util.HashMap;
import java.util.Map;

import static com.hankarun.gevrek.libs.StaticTexts.COURSES_REQUEST;
import static com.hankarun.gevrek.libs.StaticTexts.COURSE_DETAIL_REQUEST;
import static com.hankarun.gevrek.libs.StaticTexts.LOGIN_REQUEST;
import static com.hankarun.gevrek.libs.StaticTexts.NEWSGROUP_EDIT_POST_REQUEST;
import static com.hankarun.gevrek.libs.StaticTexts.NEWSGROUP_EDIT_REQUEST;
import static com.hankarun.gevrek.libs.StaticTexts.NEWSGROUP_REQUEST;


public class VolleyHelper{
    private RequestQueue queue;
    private int requestType = -1;
    private Activity activity;
    public Map<String, String> params;

    public VolleyHelper(Activity _activity){
        activity = _activity;

        params = new HashMap<>();
    }

    public void cancelRequest(){
        if(requestType != -1)
            queue.cancelAll(requestType);
    }

    public void postStringRequest(final int requestType, String url, Response.Listener<String> responseListener){
        queue = VolleySingleton.getInstance().getRequestQueue();
        this.requestType = requestType;


        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, responseListener,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(error.getMessage(), "That didn't work!");
                    }
        }) {
            @Override
            protected Map<String, String> getParams() {
                params.put("cow_username", SharedPrefHelper.readPreferences(activity, StaticTexts.SHARED_PREF_LOGINNAME, ""));
                params.put("cow_password", SharedPrefHelper.readPreferences(activity, StaticTexts.SHARED_PREF_PASSWORD, ""));
                params.put("cow_login", "login");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };
        stringRequest.setTag(requestType);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                12000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        //stringRequest.setShouldCache(false);

        queue.add(stringRequest);
    }
}
