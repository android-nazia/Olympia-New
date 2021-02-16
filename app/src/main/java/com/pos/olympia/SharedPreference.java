package com.pos.olympia;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreference {
    public static final String PREFS_NAME = "POS_PREFS";
    public static final String PREFS_LOGIN_STATUS = "login_status";
    public static final String PREFS_USERID = "user_id";
    public static final String PREFS_bill = "bill_size";
    public static final int PREFS_WELCOME = 0;


    public SharedPreference() {
        super();
    }

    public void setSharedPrefString(Context context, String PREFS_KEY, String text) {
        SharedPreferences settings;
        SharedPreferences.Editor editor;
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();
        editor.putString(PREFS_KEY, text);
        editor.commit();
    }

    public String getSharedPrefString(Context context, String PREFS_KEY) {
        SharedPreferences settings;
        String text;
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        text = settings.getString(PREFS_KEY, null);
        return text;
    }

    public void clearSharedPreference(Context context) {
        SharedPreferences settings;
        SharedPreferences.Editor editor;
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();
        editor.clear();
        editor.commit();
    }

    public int getSharedPrefInt(Context context, int key){
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(String.valueOf(key), 0);
    }

    public void setSharedPrefInt(Context context, int key, int value){
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(String.valueOf(key),value).apply();
    }
}
