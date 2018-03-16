package com.anekvurna.cognichampdriver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.json.JSONObject;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by Admin on 1/7/2018.
 */

public class PushReceiver  {
    private static int notificationId = 0;


    Context context;

    protected void onPushReceive(Context mContext, Intent intent) {
        context = mContext;
        try {
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            final String title = json.getString("title");
            final String message = json.getString("alert");
            //ParseObject object = new ParseObject("NotificationThree");
           // object.put("sender", sender);
           // object.put("receiver", ParseUser.getCurrentUser().getString("nameOfUser"));
            //String receiver = ParseUser.getCurrentUser().getString("nameOfUser");
          /*  Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss 'on' MM/dd/yyyy", Locale.getDefault());
            final String message = sender + " messaged you at " + format.format(date);*/
            /*object.put("message",message);
            object.put("date", date);
            object.pinInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    context.sendBroadcast(new Intent("NEW"));
                    notifyUser(title, message);
                }
            });*/
            notifyUser(title, message);
            //addToMessageHistory(message);


        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void addToMessageHistory(String message)
    {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser==null) return;
        DatabaseReference messageReference = FirebaseDatabase.getInstance().getReference("messages").child(currentUser.getUid());
        String id = messageReference.push().getKey();
        messageReference.child(id).setValue(message);
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