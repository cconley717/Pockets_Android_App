package com.pocketschatapp._Utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Chris on 9/10/2015.
 */
public class SharedPreferencesManager {

    public static void saveToSharedPreferences(Context context, String key, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key,value);
        editor.commit();
    }

    public static String getFromSharedPreferences(Context context, String key, String defaultValue) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            if(sharedPrefs.contains(key))
                return sharedPrefs.getString(key, defaultValue);
            else
                return "";
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    public static void saveToSharedPreferences(Context context, String key, boolean value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key,value);
        editor.commit();
    }

    public static boolean getFromSharedPreferences(Context context, String key, boolean defaultValue) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            if(sharedPrefs.contains(key))
                return sharedPrefs.getBoolean(key, defaultValue);
            else
                return false;
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    public static void saveToSharedPreferences(Context context, String key, long value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(key,value);
        editor.commit();
    }

    public static long getFromSharedPreferences(Context context, String key, int defaultValue) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            if(sharedPrefs.contains(key))
                return sharedPrefs.getLong(key, defaultValue);
            else
                return defaultValue;
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }



    public static void removeFromSharedPreferences(Context context, String key)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.remove(key);
        editor.commit();
    }
}