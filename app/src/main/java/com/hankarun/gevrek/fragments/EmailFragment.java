package com.hankarun.gevrek.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.hankarun.gevrek.R;
import com.hankarun.gevrek.helpers.SharedPrefHelper;
import com.hankarun.gevrek.libs.HttpPages;
import com.hankarun.gevrek.libs.StaticTexts;

public class EmailFragment extends Fragment {

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;


    public static EmailFragment newInstance() {
        EmailFragment fragment = new EmailFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public EmailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CookieSyncManager.createInstance(getActivity());
        CookieManager cookieManager=CookieManager.getInstance();
        cookieManager.removeAllCookie();

    }

    private WebView webView;
    private ProgressBar mProgressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_email, container, false);
        // Inflate the layout for this fragment

        webView = (WebView) view.findViewById(R.id.emailWebView);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        final Activity activity = getActivity();
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                // Activities and WebViews measure progress with different scales.
                // The progress meter will automatically disappear when we reach 100%
                activity.setProgress(progress);

                mProgressBar.setProgress(progress); //Make the bar disappear after URL is loaded

                if(progress == 100)
                    mProgressBar.setVisibility(View.INVISIBLE);
                else
                    mProgressBar.setVisibility(View.VISIBLE);

            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if(url.equals(HttpPages.EmailUrl)) {
                    webView.loadUrl("javascript: {document.getElementsByName('_user')[0].value ='" +
                            SharedPrefHelper.readPreferences(getActivity(), StaticTexts.SHARED_PREF_LOGINNAME, "") + "';" +
                            "document.getElementsByName('_pass')[0].value = '" +
                            SharedPrefHelper.readPreferences(getActivity(), StaticTexts.SHARED_PREF_PASSWORD, "") + "';" +
                            "var frms = document.getElementsByName('form');" +
                            "frms[0].submit();};");
                }
            }
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(activity, activity.getString(R.string.oh_no) + description, Toast.LENGTH_SHORT).show();
            }
        });


        webView.getSettings().setJavaScriptEnabled(true);


        webView.loadUrl(HttpPages.EmailUrl);



        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}
