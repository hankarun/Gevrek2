package com.hankarun.gevrek.helpers;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.hankarun.gevrek.MyApplication;
import com.hankarun.gevrek.libs.StaticTexts;
import com.hankarun.gevrek.libs.VolleySingleton;

import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;


public class VolleyHelper {
    private RequestQueue queue;
    private int requestType = -1;
    private Context context;
    public final Map<String, String> params;

    public VolleyHelper(Context context) {
        this.context = context;

        params = new HashMap<>();
    }

    public void cancelRequest() {
        if (requestType != -1)
            queue.cancelAll(requestType);
    }

    public String syncStringRequest(final int requestType, String url) {
        RequestFuture<String> future = RequestFuture.newFuture();
        queue = VolleySingleton.getInstance().getRequestQueue();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, future,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Volley Hata", error.toString());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                java.net.CookieManager msCookieManager = MyApplication.msCookieManager;
                if (msCookieManager.getCookieStore().getCookies().size() > 0) {
                    List<HttpCookie> cookies = msCookieManager.getCookieStore().getCookies();

                    params.put("Cookie", cookies.toString().substring(1, cookies.toString().length() - 1).replace(',', ';'));
                } else {
                    params.put("cow_username", SharedPrefHelper.readPreferences(context, StaticTexts.SHARED_PREF_LOGINNAME, ""));
                    params.put("cow_password", SharedPrefHelper.readPreferences(context, StaticTexts.SHARED_PREF_PASSWORD, ""));
                    params.put("cow_login", "login");
                }
                return params;
            }

            @Override
            protected Response parseNetworkResponse(NetworkResponse response) {
                Map headers = response.headers;
                String cookie = (String) headers.get("Set-Cookie");
                java.net.CookieManager msCookieManager = MyApplication.msCookieManager;
                msCookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                return super.parseNetworkResponse(response);
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
                1000,
                3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
        try {
            return future.get();
        } catch (InterruptedException e) {
            // exception handling
        } catch (ExecutionException e) {
            // exception handling
        }
        return "";
    }

    public void postStringRequest(final int requestType, String url, Response.Listener<String> responseListener) {
        queue = VolleySingleton.getInstance().getRequestQueue();
        this.requestType = requestType;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, responseListener,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Volley Hata", error.toString());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                java.net.CookieManager msCookieManager = MyApplication.msCookieManager;
                if (msCookieManager.getCookieStore().getCookies().size() > 0) {
                    List<HttpCookie> cookies = msCookieManager.getCookieStore().getCookies();
                    params.put("Cookie", cookies.toString().substring(1, cookies.toString().length() - 1).replace(',', ';'));
                } else {
                    params.put("cow_username", SharedPrefHelper.readPreferences(context, StaticTexts.SHARED_PREF_LOGINNAME, ""));
                    params.put("cow_password", SharedPrefHelper.readPreferences(context, StaticTexts.SHARED_PREF_PASSWORD, ""));
                    params.put("cow_login", "login");
                }
                return params;
            }

            @Override
            protected Response parseNetworkResponse(NetworkResponse response) {
                Map headers = response.headers;

                String cookie = (String) headers.get("Set-Cookie");
                java.net.CookieManager msCookieManager = MyApplication.msCookieManager;
                msCookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));

                return super.parseNetworkResponse(response);
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
                1000,
                3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        //stringRequest.setShouldCache(false);
        if (requestType == StaticTexts.READMESSAGES)
            stringRequest.setShouldCache(true);
        else
            stringRequest.setShouldCache(false);

        queue.add(stringRequest);
    }
}
