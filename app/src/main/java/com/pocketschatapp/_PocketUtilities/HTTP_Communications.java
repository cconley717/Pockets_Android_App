package com.pocketschatapp._PocketUtilities;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.pocketschatapp.R;
import com.pocketschatapp._Utilities.GlobalVariables;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Chris on 7/2/2016.
 */
public class HTTP_Communications {
    public interface OnServerRespond {
        public void onServerRespond(JSONObject response, boolean success, String errorReason);
    }

    public static void getRecentChatActivity(Context context, String pocketName, String pocketPassword, long mostRecentTimestamp, OnServerRespond listener)
    {
        RequestBody requestBody = buildRecentChatActivityRequestBody(pocketName, pocketPassword, mostRecentTimestamp);

        String url = context.getResources().getString(R.string.pockets_server_address) + "/getRecentChatActivity";

        Request request = buildPostRequest(requestBody, url);

        sendRequestToServer(request, listener);
    }

    public static void addToFavoritePockets(Context context, String pocketName, OnServerRespond listener)
    {
        RequestBody requestBody = buildFavoritePocketRequestBody(pocketName);

        String url = context.getResources().getString(R.string.pockets_server_address) + "/favoritePocket";

        Request request = buildPostRequest(requestBody, url);

        sendRequestToServer(request, listener);
    }

    public static void changePocketPassword(Context context, String pocketName, String oldPocketPassword, String newPocketPassword, OnServerRespond listener)
    {
        RequestBody requestBody = buildChangePocketPasswordRequestBody(pocketName, oldPocketPassword, newPocketPassword);

        String url = context.getResources().getString(R.string.pockets_server_address) + "/changePocketPassword";

        Request request = buildPostRequest(requestBody, url);

        sendRequestToServer(request, listener);
    }



    private static Request buildPostRequest(RequestBody requestBody, String url)
    {
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        return request;
    }

    private static Request buildGetRequest(String url)
    {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        return request;
    }


    private static RequestBody buildRecentChatActivityRequestBody(String pocketName, String pocketPassword, long mostRecentTimestamp)
    {
        RequestBody requestBody = new FormBody.Builder()
                .add("pocketName", pocketName)
                .add("pocketPassword", pocketPassword)
                .add("mostRecentTimestamp", String.valueOf(mostRecentTimestamp))
                .build();

        return requestBody;
    }

    private static RequestBody buildFavoritePocketRequestBody(String pocketName)
    {
        RequestBody requestBody = new FormBody.Builder()
                .add("pocketName", pocketName)
                .build();

        return requestBody;
    }

    private static RequestBody buildChangePocketPasswordRequestBody(String pocketName, String oldPocketPassword, String newPocketPassword)
    {
        RequestBody requestBody = new FormBody.Builder()
                .add("pocketName", pocketName)
                .add("oldPocketPassword", oldPocketPassword)
                .add("newPocketPassword", newPocketPassword)
                .build();

        return requestBody;
    }



    private static void sendRequestToServer(final Request request, final OnServerRespond listener)
    {
        GlobalVariables.getHttpClient().newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("testing", "HTTP_CLIENT: " + e.getMessage());

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onServerRespond(null, false, "An error occurred. Please try again.");
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException
            {
                final String temp = response.body().string();
                Log.d("testing", "BODY: " + temp);

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run()
                    {
                        if (!response.isSuccessful()) {
                            Log.d("testing", "HTTP_CLIENT: " + response.message());
                            listener.onServerRespond(null, false, "An error occurred. Please try again.");
                        }
                        else
                        {
                            try
                            {
                                JSONObject jsonObject = new JSONObject(temp);

                                boolean isError = jsonObject.getBoolean("isError");

                                if(isError)
                                {
                                    String errorReason = jsonObject.getString("errorReason");
                                    listener.onServerRespond(jsonObject, false, errorReason);
                                }
                                else
                                    listener.onServerRespond(jsonObject, true, "");
                            }
                            catch (JSONException e)
                            {
                                listener.onServerRespond(null, false, "An error occurred. Please try again.");
                                e.printStackTrace();
                            }
                        }

                        response.body().close();
                    }
                });
            }
        });
    }

}
