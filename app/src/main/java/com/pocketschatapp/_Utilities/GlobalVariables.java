package com.pocketschatapp._Utilities;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.pocketschatapp._HomeUtilities.HTTP_Communications;

import java.io.File;

import okhttp3.OkHttpClient;

/**
 * Created by Chris on 6/27/2016.
 */
public class GlobalVariables extends Application
{
    private static Context context;

    private static OkHttpClient client;
    private static PersistentCookieJar cookieJar;

    @Override
    public void onCreate()
    {
        super.onCreate();

        context = getApplicationContext();

        Log.d("testing", "initializing global variables");

        cookieJar = new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(getApplicationContext()));
        client = new OkHttpClient.Builder().cookieJar(cookieJar).build();

        //activity.getFilesDir().toString() + File.separator + "pockets"

        File file = new File(getFilesDir().toString() + File.separator + "pockets");
        if(!file.exists())
            file.mkdir();

        Fresco.initialize(this);
    }

    public static OkHttpClient getHttpClient()
    {
        return client;
    }

    public static void setDisplayName(String displayName)
    {
        SharedPreferencesManager.saveToSharedPreferences(context, "displayName", displayName);
    }

    public static String getDisplayName()
    {
        return SharedPreferencesManager.getFromSharedPreferences(context, "displayName", "");
    }

    public static void setAvatar(String avatar)
    {
        SharedPreferencesManager.saveToSharedPreferences(context, "avatar", avatar);
    }

    public static String getAvatar()
    {
        return SharedPreferencesManager.getFromSharedPreferences(context, "avatar", "");
    }

    public static void setUserid(String uuid)
    {
        SharedPreferencesManager.saveToSharedPreferences(context, "userid", uuid);
    }

    public static String getUserid()
    {
        return SharedPreferencesManager.getFromSharedPreferences(context, "userid", "");
    }

    public static void setLastActivityForPocket(String pocketName, long timestamp )
    {
        SharedPreferencesManager.saveToSharedPreferences(context, pocketName, timestamp);
    }

    public static long getLastActivityForPocket(String pocketName)
    {
        return SharedPreferencesManager.getFromSharedPreferences(context, pocketName, 0);
    }

    public static void setHasSeenInitialHelp()
    {
        SharedPreferencesManager.saveToSharedPreferences(context, "helped", true);
    }

    public static boolean hasSeenInitialHelp()
    {
        return SharedPreferencesManager.getFromSharedPreferences(context, "helped", false);
    }

    /*
            //listItem.setBackgroundColor(Color.parseColor("#fff6cf"));
        //listItem.setBackgroundColor(Color.TRANSPARENT);
     */
}
