package com.pocketschatapp._HomeUtilities;

import android.app.Activity;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Chris on 6/28/2016.
 */
public class CenteredToastMessage {

    public static void showCenteredToastMessage(Activity activity, String message)
    {
        Toast toast = Toast.makeText(activity, message, Toast.LENGTH_SHORT);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        if( v != null) v.setGravity(Gravity.CENTER);
        toast.show();
    }
}
