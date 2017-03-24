package com.pocketschatapp._Utilities;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.pocketschatapp.R;

import org.json.JSONException;
import org.json.JSONObject;
import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.auth.AccessToken;
import facebook4j.auth.DialogAuthOption;
import facebook4j.auth.Display;
import facebook4j.conf.Configuration;
import facebook4j.conf.ConfigurationBuilder;

/**
 * Created by Chris on 1/27/2016.
 */
public class FacebookLoginWebView extends WebView {

    private OnResultReceived onResultReceived;
    public interface OnResultReceived {
        public void result(boolean success, JSONObject jsonObject, String reason);
    }

    private static Context context;
    private FacebookLoginWebView instance;

    public FacebookLoginWebView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.instance = this;
        this.context = context;

        getSettings().setJavaScriptEnabled(true);
        //getSettings().setAppCacheEnabled(true);
        //getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        //getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        //setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
    }

    public void initializeLoginInterface()
    {
        clearCacheAndCookies();

        final ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthAppId(getResources().getString(R.string.facebook_app_id));
        builder.setOAuthAppSecret(getResources().getString(R.string.facebook_app_secret));

        final Configuration configuration = builder.build();
        final FacebookFactory factory = new FacebookFactory(configuration);
        final Facebook facebook = factory.getInstance();

        loadFacebookSignInPage(facebook);
    }

    public void setOnResultReceivedListener(OnResultReceived listener)
    {
        onResultReceived = listener;
    }

    private void clearCacheAndCookies()
    {
        clearCache(true);
        clearHistory();
        WebSettings webSettings = getSettings();
        webSettings.setSaveFormData(false);
        webSettings.setSavePassword(false); // Not needed for API level 18 or greater (deprecated)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(context);
            cookieSyncMngr.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }

    private void loadFacebookSignInPage(final Facebook facebook)
    {
        DialogAuthOption authOption = new DialogAuthOption().display(Display.POPUP);
        String url = facebook.getOAuthAuthorizationURL(context.getString(R.string.facebook_callback), authOption);

        Log.d("testing", "authorization url: " + url);

        setWebViewClient(new FacebookLoginWebViewClient(new FacebookLoginWebViewClient.OnResultReceived() {
            @Override
            public void result(final String code) {
                new AsyncTask<Void, Integer, AccessToken>() {
                    @Override
                    protected AccessToken doInBackground(Void... params) {
                        try {
                            AccessToken accessToken = facebook.getOAuthAccessToken(code);

                            return accessToken;
                        } catch (FacebookException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }

                    @Override
                    protected void onPostExecute(final AccessToken accessToken) {

                        if(accessToken == null) {
                            onResultReceived.result(false, null, "error logging in");
                        }
                        else {
                            String token = accessToken.getToken();

                            HTTP_Communications.echoFacebookAuthenticationToServer(context, token, new HTTP_Communications.OnServerRespond() {
                                @Override
                                public void onServerRespond(JSONObject response, boolean success, String errorReason) {
                                    if(success)
                                    {
                                        try {
                                            boolean isError = response.getBoolean("isError");

                                            if (isError)
                                                onResultReceived.result(false, null, errorReason);
                                            else
                                                onResultReceived.result(true, response, "");
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    else
                                    {
                                        Log.d("testing", "error 1");
                                        onResultReceived.result(false, null, errorReason);
                                    }
                                }
                            });
                        }
                    }
                }.execute(null, null, null);
            }
        }));

        loadUrl(url);
    }

    static class FacebookLoginWebViewClient extends WebViewClient {

        private OnResultReceived onResultReceived;
        public interface OnResultReceived {
            public void result(String code);
        }

        public FacebookLoginWebViewClient(OnResultReceived listener)
        {
            onResultReceived = listener;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Uri uri = Uri.parse(url);
            Log.d("testing", "url: " + url.toString());
            Log.d("testing", "uri: " + uri.toString());

            String redirect = uri.getQueryParameter("redirect_uri");
            Log.d("testing", "redirect: " + redirect);

            String code = uri.getQueryParameter(context.getResources().getString(R.string.facebook_oauth_code));
            Log.d("testing", "code: " + code);

            if (code != null) {

                onResultReceived.result(code);

                return true;
            }

            return false;
        }
    }
}

