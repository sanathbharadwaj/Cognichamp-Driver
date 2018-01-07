package com.anekvurna.pingme;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by Admin on 1/3/2018.
 */

public class SanathUtilities {

    public static void showToast(Context context, String message)
    {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void loadActivity(Context context , Class myClass )
    {
        Intent intent = new Intent(context, myClass);
        context.startActivity(intent);
    }

    public static void loadActivityAndFinish(Context context , Class myClass )
    {
        Intent intent = new Intent(context, myClass);
        context.startActivity(intent);
        Activity activity = (Activity) context;
        activity.finish();
    }
}
