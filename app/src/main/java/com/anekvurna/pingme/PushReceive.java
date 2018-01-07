package com.anekvurna.pingme;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import com.anekvurna.pingme.R;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by Admin on 1/7/2018.
 */

public class PushReceive extends ParsePushBroadcastReceiver {
    private static int notificationId = 0;


    Context context;

    @Override
    protected void onPushReceive(Context mContext, Intent intent) {
        context = mContext;
        try {
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            final String title = json.getString("title");
            final String sender = json.getString("alert");
            ParseObject object = new ParseObject("NotificationThree");
           // object.put("sender", sender);
           // object.put("receiver", ParseUser.getCurrentUser().getString("nameOfUser"));
            String receiver = ParseUser.getCurrentUser().getString("nameOfUser");
            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss 'on' MM/dd/yyyy", Locale.getDefault());
            final String message = sender + " messaged " + receiver + " at " + format.format(date);
            object.put("message",message);
            object.put("date", date);
            object.pinInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    context.sendBroadcast(new Intent("NEW"));
                    notifyUser(title, message);
                }
            });

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    void notifyUser(String title, String alert)
    {
        Notification notification;

        Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Intent resultIntent = new Intent(context, NotificationsHistoryActivity.class);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        if(Build.VERSION.SDK_INT < 16) {
            notification = new Notification.Builder(context)
                    .setSound(uri)
                    .setContentTitle(title)
                    .setContentText(alert)
                    .setContentIntent(resultPendingIntent)
                    .setSmallIcon(R.mipmap.ic_launcher).getNotification();
        }
        else
        {
            notification = new Notification.Builder(context)
                    .setSound(uri)
                    .setContentTitle(title)
                    .setContentText(alert)
                    .setContentIntent(resultPendingIntent)
                    .setSmallIcon(R.mipmap.ic_launcher).build();
        }
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId,notification);
        notificationId++;
    }
}