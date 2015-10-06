package com.hankarun.gevrek.helpers;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPrefHelper {
    public static void savePreferences(Context context, String key, String value){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String readPreferences(Context context, String key, String defaultValue){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        try{
            return sp.getString(key, defaultValue);
        }catch (Exception e){
            return "9";
        }

    }
}
