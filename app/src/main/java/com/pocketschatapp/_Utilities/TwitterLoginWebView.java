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
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by Chris on 1/20/2016.
 */
public class TwitterLoginWebView extends WebView {

    private OnResultReceived onResultReceived;
    public interface OnResultReceived {
        public void result(boolean success, JSONObject jsonObject, String reason);
    }

    private static Context context;
    private TwitterLoginWebView instance;

    public TwitterLoginWebView(Context context, AttributeSet attrs) {
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
        builder.setOAuthConsumerKey(getResources().getString(R.string.twitter_consumer_key));
        builder.setOAuthConsumerSecret(getResources().getString(R.string.twitter_consumer_secret));

        final Configuration configuration = builder.build();
        final TwitterFactory factory = new TwitterFactory(configuration);
        final Twitter twitter = factory.getInstance();

        loadTwitterSignInPage(twitter);
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

    private void loadTwitterSignInPage(final Twitter twitter)
    {
        getRequestToken(twitter);
    }

    private void getRequestToken(final Twitter twitter)
    {
        new AsyncTask<Void, Integer, RequestToken>()
        {
            @Override
            protected RequestToken doInBackground(Void... params)
            {
                try {
                    RequestToken requestToken = twitter.getOAuthRequestToken(context.getResources().getString(R.string.twitter_callback));
                    return requestToken;
                } catch (TwitterException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(final RequestToken requestToken)
            {
                if(requestToken == null)
                    onResultReceived.result(false, null, "error logging in");
                else
                    getAccessToken(twitter, requestToken);
            }
        }.execute(null, null, null);
    }

    private void getAccessToken(final Twitter twitter, final RequestToken requestToken)
    {
        setWebViewClient(new TwitterLoginWebViewClient(new TwitterLoginWebViewClient.OnResultReceived() {
            @Override
            public void result(final String verifier) {

                new AsyncTask<Void, Integer, AccessToken>()
                {
                    @Override
                    protected AccessToken doInBackground(Void... params)
                    {
                        try {
                            if(verifier == null)
                                return null;

                            AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
                            return accessToken;
                        } catch (TwitterException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }

                    @Override
                    protected void onPostExecute(final AccessToken accessToken)
                    {
                        if(accessToken == null)
                            onResultReceived.result(false, null, "error logging in");
                        else
                        {
                            final String accessTokenString = accessToken.getToken();
                            final String secretTokenString = accessToken.getTokenSecret();
                            final long userID = accessToken.getUserId();

                            HTTP_Communications.echoTwitterAuthenticationToServer(context, accessTokenString, secretTokenString, userID, new HTTP_Communications.OnServerRespond() {
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

        loadUrl(requestToken.getAuthenticationURL());
    }

    static class TwitterLoginWebViewClient extends WebViewClient {

        private OnResultReceived onResultReceived;
        public interface OnResultReceived {
            public void result(String verifier);
        }

        public TwitterLoginWebViewClient(OnResultReceived listener) {
            onResultReceived = listener;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Uri uri = Uri.parse(url);
            Log.d("testing", "URL: " + url);
            Log.d("testing", "uri: " + uri.toString());

            String verifier = uri.getQueryParameter(context.getResources().getString(R.string.twitter_oauth_verifier));

            if (verifier != null) {

                onResultReceived.result(verifier);

                return true;
            }
            return false;
        }
    }
}