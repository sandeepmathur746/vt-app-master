package com.veertrip.android.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class StorageHelper {

    private SharedPreferences sharedPreferences;

    public StorageHelper(Context context) {
        String KEY = "VEERTRIP";
        this.sharedPreferences = context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
    }

    public void write(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String read(String key) {
        return sharedPreferences.getString(key, "");
    }

}
