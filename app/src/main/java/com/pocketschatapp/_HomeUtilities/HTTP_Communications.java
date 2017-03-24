package com.pocketschatapp._HomeUtilities;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import com.pocketschatapp.R;
import com.pocketschatapp._Utilities.GlobalVariables;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Chris on 6/27/2016.
 */
public class HTTP_Communications {

    public interface OnServerRespond {
        public void onServerRespond(JSONObject response, boolean success, String errorReason);
    }

    public static void createPocket(Context context, String pocketName, String pocketPassword, double topLatitude, double bottomLatitude, double leftLongitude,
                                    double rightLongitude, double centerLatitude, double centerLongitude,
                                    float zoomLevel, int pocketCost, OnServerRespond listener)
    {
        RequestBody requestBody = buildCreatePocketRequestBody(pocketName, pocketPassword, topLatitude, bottomLatitude,
                leftLongitude, rightLongitude, centerLatitude, centerLongitude, zoomLevel, pocketCost);

        String url = context.getResources().getString(R.string.pockets_server_address) + "/createPocket";

        Request request = buildPostRequest(requestBody, url);

        sendRequestToServer(request, listener);
    }

    public static void getPocketsWithinGeographicArea(Context context, double topLatitude, double bottomLatitude,
                                                   double leftLongitude, double rightLongitude,
                                                   OnServerRespond listener)
    {
        RequestBody requestBody = buildGetPocketsRequestBody(topLatitude, bottomLatitude,
                leftLongitude, rightLongitude);

        String url = context.getResources().getString(R.string.pockets_server_address) + "/getPocketsWithinGeographicArea";

        Request request = buildPostRequest(requestBody, url);

        sendRequestToServer(request, listener);
    }

    public static void joinPocket(Context context, String pocketName, OnServerRespond listener)
    {
        RequestBody requestBody = buildJoinPocketRequestBody(pocketName);

        String url = context.getResources().getString(R.string.pockets_server_address) + "/joinPocket";

        Request request = buildPostRequest(requestBody, url);

        sendRequestToServer(request, listener);
    }

    public static void joinPasswordedPocket(Context context, String pocketName, String pocketPassword, OnServerRespond listener)
    {
        RequestBody requestBody = buildJoinPasswordedPocketRequestBody(pocketName, pocketPassword);

        String url = context.getResources().getString(R.string.pockets_server_address) + "/joinPasswordedPocket";

        Request request = buildPostRequest(requestBody, url);

        sendRequestToServer(request, listener);
    }

    public static void showPocketOnMap(Context context, String pocketName, OnServerRespond listener)
    {
        RequestBody requestBody = buildShowPocketOnMapRequestBody(pocketName);

        String url = context.getResources().getString(R.string.pockets_server_address) + "/showPocketOnMap";

        Request request = buildPostRequest(requestBody, url);

        sendRequestToServer(request, listener);
    }

    public static void deletePocket(Context context, String pocketName, String pocketPassword, OnServerRespond listener)
    {
        RequestBody requestBody = buildDeletePocketOnMapRequestBody(pocketName, pocketPassword);

        String url = context.getResources().getString(R.string.pockets_server_address) + "/deletePocket";

        Request request = buildPostRequest(requestBody, url);

        sendRequestToServer(request, listener);
    }

    public static void getPocketsForCategory(Context context, String pocketCategory, OnServerRespond listener)
    {
        RequestBody requestBody = buildGetPocketsForCategoryRequestBody(pocketCategory);

        String url = context.getResources().getString(R.string.pockets_server_address) + "/pocketsForCategory";

        Request request = buildPostRequest(requestBody, url);

        sendRequestToServer(request, listener);
    }

    public static void myLint(Context context, OnServerRespond listener)
    {
        String url = context.getResources().getString(R.string.pockets_server_address) + "/myLint";

        Request request = buildGetRequest(url);

        sendRequestToServer(request, listener);
    }

    public static void sendLint(Context context, String username, int amount, OnServerRespond listener)
    {
        RequestBody requestBody = buildSendLintRequestBody(username, amount);

        String url = context.getResources().getString(R.string.pockets_server_address) + "/sendLint";

        Request request = buildPostRequest(requestBody, url);

        sendRequestToServer(request, listener);
    }

    public static void removePocketFromCategory(Context context, String pocketsCategory, String pocketName, OnServerRespond listener)
    {
        RequestBody requestBody = buildRemovePocketFromCategoryRequestBody(pocketsCategory, pocketName);

        String url = context.getResources().getString(R.string.pockets_server_address) + "/removePocketFromCategory";

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






    private static RequestBody buildCreatePocketRequestBody(String pocketName, String pocketPassword, double topLatitude, double bottomLatitude, double leftLongitude,
                                                            double rightLongitude, double centerLatitude, double centerLongitude,
                                                            float zoomLevel, int pocketCost)
    {
        RequestBody requestBody = new FormBody.Builder()
                .add("pocketName", pocketName)
                .add("pocketPassword", pocketPassword)
                .add("topLatitude", String.valueOf(topLatitude))
                .add("bottomLatitude", String.valueOf(bottomLatitude))
                .add("leftLongitude", String.valueOf(leftLongitude))
                .add("rightLongitude", String.valueOf(rightLongitude))
                .add("centerLatitude", String.valueOf(centerLatitude))
                .add("centerLongitude", String.valueOf(centerLongitude))
                .add("zoomLevel", String.valueOf(zoomLevel))
                .add("pocketCost", String.valueOf(pocketCost))
                .build();

        return requestBody;
    }

    private static RequestBody buildGetPocketsRequestBody(double topLatitude, double bottomLatitude, double leftLongitude,
                                                   double rightLongitude)
    {
        RequestBody requestBody = new FormBody.Builder()
                .add("topLatitude", String.valueOf(topLatitude))
                .add("bottomLatitude", String.valueOf(bottomLatitude))
                .add("leftLongitude", String.valueOf(leftLongitude))
                .add("rightLongitude", String.valueOf(rightLongitude))
                .build();

        return requestBody;
    }

    private static RequestBody buildCheckForNewPocketContentRequestBody(JSONArray pocketsArray)
    {
        RequestBody requestBody = new FormBody.Builder()
                .add("pocketsArray", pocketsArray.toString())
                .build();

        return requestBody;
    }

    private static RequestBody buildJoinPocketRequestBody(String pocketName)
    {
        RequestBody requestBody = new FormBody.Builder()
                .add("pocketName", pocketName)
                .build();

        return requestBody;
    }

    private static RequestBody buildJoinPasswordedPocketRequestBody(String pocketName, String pocketPassword)
    {
        RequestBody requestBody = new FormBody.Builder()
                .add("pocketName", pocketName)
                .add("pocketPassword", pocketPassword)
                .build();

        return requestBody;
    }

    private static RequestBody buildShowPocketOnMapRequestBody(String pocketName)
    {
        RequestBody requestBody = new FormBody.Builder()
                .add("pocketName", pocketName)
                .build();

        return requestBody;
    }

    private static RequestBody buildLeavePocketOnMapRequestBody(String pocketName)
    {
        RequestBody requestBody = new FormBody.Builder()
                .add("pocketName", pocketName)
                .build();

        return requestBody;
    }

    private static RequestBody buildDeletePocketOnMapRequestBody(String pocketName, String pocketPassword)
    {
        RequestBody requestBody = new FormBody.Builder()
                .add("pocketName", pocketName)
                .add("pocketPassword", pocketPassword)
                .build();

        return requestBody;
    }

    private static RequestBody buildGetPocketsForCategoryRequestBody(String pocketCategory)
    {
        RequestBody requestBody = new FormBody.Builder()
                .add("pocketCategory", pocketCategory)
                .build();

        return requestBody;
    }

    private static RequestBody buildSendLintRequestBody(String username, int amount)
    {
        RequestBody requestBody = new FormBody.Builder()
                .add("username", username)
                .add("amount", String.valueOf(amount))
                .build();

        return requestBody;
    }

    private static RequestBody buildRemovePocketFromCategoryRequestBody(String pocketsCategory, String pocketName)
    {
        RequestBody requestBody = new FormBody.Builder()
                .add("pocketsCategory", pocketsCategory)
                .add("pocketName", pocketName)
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
