package com.pocketschatapp._Utilities;

import android.app.Activity;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.pocketschatapp.R;
import com.pocketschatapp._HomeUtilities.CenteredToastMessage;
import org.json.JSONObject;


/**
 * Created by Chris on 6/29/2016.
 */
public class SocialMediaLoginSlidingMenu extends SlidingMenu {

    private OnLoginResultReceived onLoginResultReceivedListener;
    public interface OnLoginResultReceived {
        public void result(boolean success, JSONObject jsonObject, String reason);
    }

    private Activity activity;

    private RelativeLayout socialMediaLoginWebViewContainer;
    private FacebookLoginWebView facebookLoginWebView;
    private TwitterLoginWebView twitterLoginWebView;

    private Snackbar snackbarExitInterface;


    public SocialMediaLoginSlidingMenu(Activity activity) {
        super(activity);

        this.activity = activity;

        registerResources();
        registerListeners();

        configureSnackbarExitInterface();
        initializeSlidingWindow();
    }

    private void registerResources()
    {
        socialMediaLoginWebViewContainer = (RelativeLayout) View.inflate(activity, R.layout.socialmedia_login_webview_container, null);

        facebookLoginWebView = (FacebookLoginWebView) socialMediaLoginWebViewContainer.findViewById(R.id.facebookWebView);
        twitterLoginWebView = (TwitterLoginWebView) socialMediaLoginWebViewContainer.findViewById(R.id.twitterWebView);
    }

    private void configureSnackbarExitInterface()
    {
        snackbarExitInterface = Snackbar.make(activity.findViewById(android.R.id.content), "", Snackbar.LENGTH_INDEFINITE)
                .setAction("return to pockets", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("testing", "exiting webview");

                        toggle(true);
                    }
                });
    }

    private void registerListeners() {
        setOnOpenedListener(new OnOpenedListener() {
            @Override
            public void onOpened() {
                Log.d("testing", "sliding menu opened");

                snackbarExitInterface.show();
            }
        });

        setOnClosedListener(new OnClosedListener() {
            @Override
            public void onClosed() {
                Log.d("testing", "sliding menu closed");

                snackbarExitInterface.dismiss();
                ((AppCompatActivity) activity).getSupportActionBar().show();
            }
        });

        facebookLoginWebView.setOnResultReceivedListener(new FacebookLoginWebView.OnResultReceived() {
            @Override
            public void result(boolean success, JSONObject jsonObject, String errorReason) {
                toggle(true);
                onLoginResultReceivedListener.result(success, jsonObject, errorReason);
            }
        });

        twitterLoginWebView.setOnResultReceivedListener(new TwitterLoginWebView.OnResultReceived() {
            @Override
            public void result(boolean success, JSONObject jsonObject, String errorReason) {

                toggle(true);
                onLoginResultReceivedListener.result(success, jsonObject, errorReason);
            }
        });
    }

    private void initializeSlidingWindow() {
        setMode(SlidingMenu.LEFT);
        setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        setFadeDegree(0.35f);
        attachToActivity(activity, SlidingMenu.SLIDING_CONTENT);

        setMenu(socialMediaLoginWebViewContainer);
    }

    public void initializeFacebookLoginWebView(OnLoginResultReceived listener)
    {
        facebookLoginWebView.setVisibility(View.VISIBLE);
        twitterLoginWebView.setVisibility(View.GONE);

        ((AppCompatActivity) activity).getSupportActionBar().hide();

        onLoginResultReceivedListener = listener;
        facebookLoginWebView.initializeLoginInterface();

        toggle(true);
    }

    public void initializeTwitterLoginWebView(OnLoginResultReceived listener)
    {
        facebookLoginWebView.setVisibility(View.GONE);
        twitterLoginWebView.setVisibility(View.VISIBLE);

        ((AppCompatActivity) activity).getSupportActionBar().hide();

        onLoginResultReceivedListener = listener;
        twitterLoginWebView.initializeLoginInterface();

        toggle(true);
    }
}
