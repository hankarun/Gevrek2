package com.hankarun.gevrek;

import android.app.Application;
import android.content.Context;

import java.net.CookieHandler;
import java.net.CookieManager;

public class MyApplication extends Application {
    private static MyApplication mInstance;
    private static Context mAppContext;
    public static java.net.CookieManager msCookieManager = new java.net.CookieManager();

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        CookieHandler.setDefault(new CookieManager());


        this.setAppContext(getApplicationContext());
    }

    public static MyApplication getInstance(){
        return mInstance;
    }
    public static Context getAppContext() {
        return mAppContext;
    }
    private void setAppContext(Context mAppContext) {
        this.mAppContext = mAppContext;
    }
}