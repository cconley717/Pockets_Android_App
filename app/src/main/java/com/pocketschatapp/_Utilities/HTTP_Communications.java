package com.pocketschatapp._Utilities;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.pocketschatapp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Chris on 6/29/2016.
 */
public class HTTP_Communications {

    public interface OnServerRespond {
        public void onServerRespond(JSONObject response, boolean success, String errorReason);
    }

    public static void echoTwitterAuthenticationToServer(Context context, String accessTokenString, String secretTokenString, long userID, OnServerRespond listener)
    {
        String url = context.getResources().getString(R.string.pockets_server_address) + "/auth/twitter/token?oauth_token=" + accessTokenString + "&oauth_token_secret=" + secretTokenString + "&user_id=" + userID;

        Request request = buildGetRequest(url);

        sendRequestToServer(request, listener);
    }

    public static void echoFacebookAuthenticationToServer(Context context, String token, OnServerRespond listener)
    {
        String url = context.getResources().getString(R.string.pockets_server_address) + "/auth/facebook/token?access_token=" + token;

        Request request = buildGetRequest(url);

        sendRequestToServer(request, listener);
    }

    public static void logout(Context context, OnServerRespond listener)
    {
        String url = context.getResources().getString(R.string.pockets_server_address) + "/logout";

        Request request = buildGetRequest(url);

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
