package com.pocketschatapp._Utilities;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.text.Spanned;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.pocketschatapp.R;

/**
 * Created by Chris on 11/28/2015.
 */
public class DialogDispatcher {

    private static MaterialDialog materialDialog;


    public static void showPassiveProgressDialog(final Context context, final String title, final String content)
    {
        materialDialog = new MaterialDialog.Builder(context)
                .title(title)
                .titleColor(ContextCompat.getColor(context, R.color.dialogTitleColor))
                .positiveColor(ContextCompat.getColor(context, R.color.dialogButtonColor))
                .content(content)
                .cancelable(false)
                .progress(true, 0)
                .backgroundColor(ContextCompat.getColor(context, R.color.dialogBackgroundColor))
                .widgetColor(ContextCompat.getColor(context, R.color.dialogTitleColor))
                .show();
    }

    public static void showInformationDialog(final Context context, final String title, final String content) {
        materialDialog = new MaterialDialog.Builder(context)
                .title(title)
                .titleColor(ContextCompat.getColor(context, R.color.dialogTitleColor))
                .content(content)
                .cancelable(true)
                .positiveText("Close")
                .positiveColor(ContextCompat.getColor(context, R.color.dialogButtonColor))
                .backgroundColor(ContextCompat.getColor(context, R.color.dialogBackgroundColor))
                .show();
    }

    public static void showInformationDialog(final Context context, final String title, final Spanned content) {
        materialDialog = new MaterialDialog.Builder(context)
                .title(title)
                .content(content)
                .cancelable(true)
                .positiveText("Close")
                .show();
    }

    public static void adjustToInformationDialog(final String title, final String content, final String buttonText)
    {
        materialDialog.setTitle(title);
        materialDialog.setContent(content);
        materialDialog.getProgressBar().setVisibility(View.GONE);
        materialDialog.setActionButton(DialogAction.POSITIVE, buttonText);
    }

    public static void dismissCurrentDialog()
    {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(materialDialog != null) {
                    materialDialog.dismiss();
                    materialDialog = null;
                }
            }
        });
    }

    public static MaterialDialog getDialog()
    {
        return materialDialog;
    }
}
