package com.anekvurna.pingme;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import static com.anekvurna.pingme.SanathUtilities.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by Sanath on 1/5/2018.
 */

public class MyUsersAdapter extends RecyclerView.Adapter<MyUsersAdapter.MyHolder> {

    private List<LocalUser> localUsers;
    private Context context;
    private UserListActivity userListActivity;


    MyUsersAdapter(List<LocalUser> users, Context context) {
        this.localUsers = users;
        this.context = context;
        userListActivity = (UserListActivity)context;
    }



    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.user_element, null);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyHolder holder, int position) {
        LocalUser myLocalUser = localUsers.get(position);
        String name = myLocalUser.getName();
        holder.userName.setText(name);

        holder.callButton.setTag(position);
        holder.deleteButton.setTag(position);
        holder.editButton.setTag(position);

        holder.inFiveMinsButton.setTag(position);
        holder.pickedButton.setTag(position);
        holder.droppedButton.setTag(position);

        Glide.with(context /* context */)
                .using(new FirebaseImageLoader())
                .load(myLocalUser.getStorageReference())
                .into(holder.profilePic);


        //notifyButton = (ImageButton) itemView.findViewById(R.id.notify_button);
        holder.callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int tag = (int) view.getTag();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                String mobile = localUsers.get(tag).getMobile();
                intent.setData(Uri.parse("tel:" + mobile));
                context.startActivity(intent);
            }
        });


        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int tag = (int) view.getTag();
                alertDelete(tag);
            }
        });

        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int tag = (int) view.getTag();
                userListActivity.showEditingPopup(localUsers.get(tag), tag);
            }
        });

        if(!userListActivity.startedTrip)
        {
            holder.inFiveMinsButton.setEnabled(false);
            holder.pickedButton.setEnabled(false);
            holder.droppedButton.setEnabled(false);
            return;
        }
        else
        {
            holder.inFiveMinsButton.setEnabled(true);
            holder.pickedButton.setEnabled(true);
            holder.droppedButton.setEnabled(false);
        }

        holder.inFiveMinsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: notify the user
                Button thisButton = holder.inFiveMinsButton;
                thisButton.setEnabled(false);
                notifyStatus("Arriving in 5 mins", "Driver arriving in 5 mins", (Integer)thisButton.getTag());
            }
        });

        holder.pickedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: notify the user
                holder.droppedButton.setEnabled(true);
                holder.inFiveMinsButton.setEnabled(false);
                Button thisButton = holder.pickedButton;
                thisButton.setEnabled(false);
                notifyStatus("Ride started", "Driver picked the user", (Integer)thisButton.getTag());
            }
        });

        holder.droppedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: notify the user
                Button thisButton = holder.droppedButton;
                thisButton.setEnabled(false);
                notifyStatus("Ride ended", "Driver dropped user", (Integer)thisButton.getTag());
            }
        });
    }

    void alertDelete(final int tag)
    {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(context);
        }
        builder.setTitle("Delete entry")
                .setMessage("Are you sure you want to delete this entry?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        userListActivity.deleteUser(tag);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void notifyStatus(String title, String message, final int tag)
    {
        HashMap<String, String> params = new HashMap<String, String>();
        //params.put("mobileNo", mobile);
        //params.put("installation", insId);
        params.put("title", title);
        params.put("message", message);
        ParseCloud.callFunctionInBackground("notificationToAll", params, new FunctionCallback<Integer>() {
            public void done(Integer res, ParseException e) {
                if (e == null) {
                    showToast("Notification sent");
                    addToHistory(localUsers.get(tag).getName());
                }
                else
                    showToast("Sending failed");
            }
        });
    }

    private void addToHistory(final String receiver)
    {
        final Date date = new Date();
        final SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss 'on' MM/dd/yyyy", Locale.getDefault());
        String message = "You messaged " + receiver + " at " + format.format(date);


        userListActivity.addToMessageHistory(message);

    }


    @Override
    public int getItemCount() {
        return localUsers.size();
    }


    class MyHolder extends RecyclerView.ViewHolder{
        ImageView profilePic;
        TextView userName;
        ImageButton callButton, deleteButton, editButton;
        Button inFiveMinsButton, pickedButton, droppedButton;
       // private String receiverName;

        MyHolder(View itemView) {
            super(itemView);
            profilePic = (ImageView) itemView.findViewById(R.id.profile_pic);
            userName = (TextView) itemView.findViewById(R.id.user_name);

            inFiveMinsButton = itemView.findViewById(R.id.in_5_mins_button);
            pickedButton = itemView.findViewById(R.id.picked_button);
            droppedButton = itemView.findViewById(R.id.dropped_button);

            deleteButton = itemView.findViewById(R.id.delete_button);
            editButton = itemView.findViewById(R.id.edit_button);
            callButton = (ImageButton) itemView.findViewById(R.id.call_button);



           /* notifyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int tag = (int) view.getTag();
                    String userId = localUsers.get(tag).getUserId();
                    notifyUser(userId, tag);
                    *//*final ParseQuery query = new ParseQuery("LocalUser");
                    query.fromLocalDatastore();
                    query.whereEqualTo("mobile", localUsers.get(tag).getMobile());
                    query.setLimit(1);
                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, ParseException e) {
                            notifyUser(objects.get(0).getString("insId"), ParseUser.getCurrentUser().getString("nameOfUser"));
                            String receiver = objects.get(0).getString("mobile");
                            addToHistory(receiver);
                        }
                    });*//*
                }
            });*/
        }


            void notifyUser(String userId, final int tag)
            {
                HashMap<String, String> params = new HashMap<String, String>();
               //params.put("mobileNo", mobile);
                //params.put("installation", insId);
                params.put("name", SanathUtilities.currentProfile.getName());
                params.put("userId", userId);
                ParseCloud.callFunctionInBackground("sendNotification", params, new FunctionCallback<Integer>() {
                    public void done(Integer res, ParseException e) {
                        if (e == null) {
                            showToast("Notification sent");
                            //addToHistory(localUsers.get(tag).getName());
                        }
                        else
                            showToast("Sending failed");
                    }
                });
            }
    }
    void showToast(String message)
    {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }



}
