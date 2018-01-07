package com.anekvurna.pingme;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by Sanath on 1/5/2018.
 */

public class MyUsersAdapter extends RecyclerView.Adapter<MyUsersAdapter.MyHolder> {

    public List<ParseObject> localUsers;
    private Context context;

    public MyUsersAdapter(List<ParseObject> users, Context context) {
        this.localUsers = users;
        this.context = context;
    }



    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.user_element, null);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(MyHolder holder, int position) {
        ParseObject myLocalUser = localUsers.get(position);
        String name = localUsers.get(position).getString("userName");
        holder.userName.setText(name);
        setImage(myLocalUser, holder);
        holder.callButton.setTag(position);
        holder.notifyButton.setTag(position);
        //setImage(myLocalUser, holder);
    }

    @Override
    public int getItemCount() {
        return localUsers.size();
    }

    private void setImage(final ParseObject localUser, final MyHolder holder)
    {
       /* ParseFile file = localUser.getParseFile("profilePic");
        if(file == null){
            loadUserFromServer(localUser, holder);
            return;
        }
        file.getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] data, ParseException e) {
                if(e!=null){
                    loadUserFromServer(localUser, holder);
                    return;
                }
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                holder.profilePic.setImageBitmap(bitmap);
            }
        });  */

        byte[] data = localUser.getBytes("profilePic");
        if(data==null)
        {
            loadUserFromServer(localUser, holder);
            return;

        }
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        holder.profilePic.setImageBitmap(bitmap);
    }

    void loadUserFromServer(final ParseObject localUser, final MyHolder holder)
    {
        ParseQuery query = new ParseQuery("Profile");
        query.whereEqualTo("userMobile", localUser.getString("mobile"));
        query.setLimit(1);
        query.findInBackground( new FindCallback<ParseObject>(){
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e!=null)return;
                loadImageFromServer(objects.get(0), localUser, holder);
            }
        });
    }

    void loadImageFromServer(ParseObject user, final ParseObject localUser, final MyHolder holder)
    {
        ParseFile file = user.getParseFile("profilePic");
        file.getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] data, ParseException e) {
                if(e!=null)return;
                //ParseFile parseFile = new ParseFile("displayPic.png", data);
                localUser.put("profilePic", data);
                localUser.pinInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e!=null)
                        {
                            UserListActivity activity = (UserListActivity) context;
                            activity.showToast("Failed to pin.");

                        }
                    }
                });
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                holder.profilePic.setImageBitmap(bitmap);
            }
        });
    }

    class MyHolder extends RecyclerView.ViewHolder{
        ImageView profilePic;
        TextView userName;
        ImageButton callButton, notifyButton;
        private String receiverName;

        public MyHolder(View itemView) {
            super(itemView);
            profilePic = (ImageView) itemView.findViewById(R.id.profile_pic);
            userName = (TextView) itemView.findViewById(R.id.user_name);

            callButton = (ImageButton) itemView.findViewById(R.id.call_button);
            notifyButton = (ImageButton) itemView.findViewById(R.id.notify_button);
            callButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int tag = (int) view.getTag();
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    String mobile = localUsers.get(tag).getString("mobile");
                    intent.setData(Uri.parse("tel:" + mobile));
                    context.startActivity(intent);
                }
            });
            notifyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int tag = (int) view.getTag();
                    final ParseQuery query = new ParseQuery("LocalUser");
                    query.fromLocalDatastore();
                    query.whereEqualTo("mobile", localUsers.get(tag).getString("mobile"));
                    query.setLimit(1);
                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, ParseException e) {
                            notifyUser(objects.get(0).getString("insId"), ParseUser.getCurrentUser().getString("nameOfUser"));
                            String receiver = objects.get(0).getString("mobile");
                            addToHistory(receiver);
                        }
                    });
                }
            });
        }

        void addToHistory(final String receiver)
        {
            final Date date = new Date();
            final SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss 'on' MM/dd/yyyy", Locale.getDefault());

            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereEqualTo("username", receiver);
            query.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> objects, ParseException e) {
                    if(e!=null || objects.size()==0)
                        receiverName = receiver;
                    else
                    receiverName = objects.get(0).getString("nameOfUser");
                    String message = "You messaged " + receiverName + " at " + format.format(date);
                    ParseObject object = new ParseObject("NotificationThree");
                    object.put("message",message);
                    object.put("date", date);
                    object.pinInBackground();
                }

            });

        }

            void notifyUser(String insId, String mobile)
            {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("mobileNo", mobile);
                params.put("installation", insId);
                ParseCloud.callFunctionInBackground("sendNotification", params, new FunctionCallback<Integer>() {
                    public void done(Integer res, ParseException e) {
                        if (e == null) {
                            showToast("Notification sent");
                        }
                        else
                            showToast("Sending failed");
                    }
                });
            }

        void showToast(String message)
        {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }





    }

}
