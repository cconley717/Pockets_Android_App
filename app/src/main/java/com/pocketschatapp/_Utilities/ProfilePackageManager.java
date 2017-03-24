package com.pocketschatapp._Utilities;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by Chris on 2/3/2015.
 */
public class ProfilePackageManager extends Application
{
    private static ProfilePackage profilePackage = null;

    public static boolean profilePackageExists(Context context)
    {
        return (new File(context.getFilesDir().toString() + File.separator + "profilePackage").exists());
    }

    public static boolean profilePackageIsLoaded()
    {
        if(profilePackage == null)
            return false;
        else
            return true;
    }

    public static boolean loadProfilePackage(Context context)
    {
        boolean loadSuccessful = false;

        if(profilePackageExists(context))
        {
            try
            {
                ObjectInputStream input = new ObjectInputStream(new FileInputStream(new File(context.getFilesDir().toString() + File.separator + "profilePackage")));
                profilePackage = (ProfilePackage) input.readObject();
                input.close();

                loadSuccessful = true;
            }
            catch (IOException e)
            {
                loadSuccessful = false;
                e.printStackTrace();
            }
            catch (ClassNotFoundException e)
            {
                loadSuccessful = false;
                e.printStackTrace();
            }
        }
        else
        {
            profilePackage = new ProfilePackage();
            loadSuccessful = true;
        }

        return loadSuccessful;
    }

    public static void saveProfilePackage(Context context)
    {
        if(context == null)
            Log.d("testing", "context is null");

        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(new FileOutputStream(new File(context.getFilesDir().getAbsolutePath() + File.separator + "profilePackage")));
            out.writeObject(getProfilePackage(context));
            out.close();
        } catch (IOException e) {
            Log.d("testing", e.toString());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            Log.d("testing", e.toString());
            e.printStackTrace();
        }
    }

    public static ProfilePackage getProfilePackage(Context context) throws IOException, ClassNotFoundException
    {
        if(!profilePackageIsLoaded() && profilePackageExists(context))
        {
            loadProfilePackage(context);
        }

        return profilePackage;
    }
}
