package com.anekvurna.cognichampdriver;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.anekvurna.cognichampdriver.SanathUtilities.*;

import java.text.SimpleDateFormat;
import java.util.Date;
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
        userListActivity = (UserListActivity) context;
        getFirebaseProfile();
        initializeCurrentUser();
    }


    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.user_element, null);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyHolder holder, int position) {
        final LocalUser myLocalUser = localUsers.get(position);
        String name = myLocalUser.getName();
        holder.userName.setText(name);

        holder.callButton.setTag(position);
        holder.deleteButton.setTag(position);
        holder.editButton.setTag(position);

        holder.inFiveMinsButton.setTag(position);
        holder.pickedButton.setTag(position);
        holder.droppedButton.setTag(position);

        if (!myLocalUser.isRegistered())
            holder.flagImage.setVisibility(View.VISIBLE);

        /*Glide.with(context *//* context *//*)
                .using(new FirebaseImageLoader())
                .load(myLocalUser.getStorageReference())
                .into(holder.profilePic);*/


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

        if (!myLocalUser.isRegistered())
            holder.navButton.setEnabled(false);
        else
            holder.navButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                            Uri.parse("http://maps.google.com/maps?&daddr=" + myLocalUser.getMyLocation().getLatitude()
                                    + "," + myLocalUser.getMyLocation().getLongitude()));
                    userListActivity.startActivity(intent);
                }
            });


        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userListActivity.startedTrip) {
                    showToast("Cannot delete when trip running");
                    return;
                }
                int tag = (int) view.getTag();
                alertDelete(tag);
            }
        });

        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userListActivity.startedTrip) {
                    showToast("Cannot edit when trip running");
                    return;
                }
                int tag = (int) view.getTag();
                userListActivity.showEditingPopup(localUsers.get(tag), tag);
            }
        });

        if (!userListActivity.startedTrip || !myLocalUser.isRegistered()) {
            holder.inFiveMinsButton.setEnabled(false);
            holder.pickedButton.setEnabled(false);
            holder.droppedButton.setEnabled(false);
            myLocalUser.setState(0);
            return;
        } else {
            setButtonState(holder, myLocalUser);
        }

        holder.inFiveMinsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: notify the user
                if (currentProfile == null) {
                    showToast("An error occurred, please try reconnecting to the internet");
                    return;
                }
                myLocalUser.setState(1);
                Button thisButton = holder.inFiveMinsButton;
                setButtonState(holder, myLocalUser);
                Date now = new Date();
                Date fiveMins = new Date(now.getTime() + 60 * 5 * 1000);
                SimpleDateFormat ft = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                String time = ft.format(fiveMins);
                String message = currentProfile.getName() + " arriving at " + time;
                // notifyStatus("Arriving shortly", message, (Integer)thisButton.getTag());
                addToCustomersHistory(message, (Integer) thisButton.getTag());
                addToHistory(message);
            }
        });

        holder.pickedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: notify the user
                myLocalUser.setState(2);
                setButtonState(holder, myLocalUser);
                Button thisButton = holder.pickedButton;
                SimpleDateFormat ft = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                Date now = new Date();
                String time = ft.format(now);
                int tag = (int) thisButton.getTag();
                String message = currentProfile.getName() + " picked " + localUsers.get(tag).getName() + " at "
                        + time;
                //notifyStatus("Rider picked", message, tag);
                addToCustomersHistory(message, tag);
                addToHistory(message);
            }
        });

        holder.droppedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: notify the user
                myLocalUser.setState(3);
                setButtonState(holder, myLocalUser);
                Button thisButton = holder.droppedButton;
                SimpleDateFormat ft = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                Date now = new Date();
                String time = ft.format(now);
                int tag = (int) thisButton.getTag();
                String message = currentProfile.getName() + " dropped " + localUsers.get(tag).getName() + " at "
                        + time;
                addToCustomersHistory(message, tag);
                addToHistory(message);
                setDropLocation(myLocalUser);
            }
        });

        holder.flagImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showToast("User not registered");
            }
        });

        DatabaseReference locationRef = FirebaseDatabase.getInstance().getReference("customerProfiles")
                .child(myLocalUser.getUserId()).child("addressTag");
        locationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                MyLocation location = dataSnapshot.getValue(MyLocation.class);
                myLocalUser.setMyLocation(location);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setDropLocation(LocalUser localUser) {
        MyLocation myLocation = new MyLocation(0,0);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("dropLocations").child(localUser.getUserId());
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location location = TripListActivity.locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            myLocation = new MyLocation(location.getLatitude(), location.getLongitude());
        }
        reference.setValue(myLocation);
    }

    private void setButtonState(MyHolder holder, LocalUser localUser)
    {
        int state = localUser.getState();
        boolean enableIn5Min = false, enablePicked = false, enableDropped = false;
        switch (state)
        {
            case 0: enableIn5Min = true; enablePicked = true; break;
            case 1: enablePicked = true; break;
            case 2: enableDropped = true;
            default: break;
        }

        holder.inFiveMinsButton.setEnabled(enableIn5Min);
        holder.pickedButton.setEnabled(enablePicked);
        holder.droppedButton.setEnabled(enableDropped);
    }

    private void addToCustomersHistory(String message, int tag) {
        DatabaseReference messageReference = FirebaseDatabase.getInstance().getReference("customerMessages")
                .child(localUsers.get(tag).getUserId());
        String id = messageReference.push().getKey();
        messageReference.child(id).setValue(message);

    }


    private void alertDelete(final int tag) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(context);
        }
        builder.setTitle("Delete Passenger")
                .setMessage("Are you sure you want to delete this passenger?")
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

    private void notifyStatus(String title, final String message, final int tag) {
       /* HashMap<String, String> params = new HashMap<String, String>();
        params.put("userId", localUsers.get(tag).getUserId());
        params.put("title", title);
        params.put("message", message);
        ParseCloud.callFunctionInBackground("sendNotification", params, new FunctionCallback<Integer>() {
            public void done(Integer res, ParseException e) {
                if (e == null) {
                    showToast("Notification sent");
                    addToHistory(message);
                }
                else
                    showToast("Sending failed");
            }
        });*/
    }

    private void addToHistory(final String message) {
        /*final Date date = new Date();
        final SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss 'on' MM/dd/yyyy", Locale.getDefault());
        String message = "You messaged " + receiver + " at " + format.format(date);*/
        userListActivity.addToMessageHistory(message);
    }


    @Override
    public int getItemCount() {
        return localUsers.size();
    }


    class MyHolder extends RecyclerView.ViewHolder {
        TextView userName;
        ImageButton callButton, deleteButton, editButton, navButton;
        Button inFiveMinsButton, pickedButton, droppedButton;
        ImageButton flagImage;
        // private String receiverName;

        MyHolder(View itemView) {
            super(itemView);
            //profilePic = (ImageView) itemView.findViewById(R.id.profile_pic);
            userName = (TextView) itemView.findViewById(R.id.user_name);

            inFiveMinsButton = itemView.findViewById(R.id.in_5_mins_button);
            pickedButton = itemView.findViewById(R.id.picked_button);
            droppedButton = itemView.findViewById(R.id.dropped_button);

            deleteButton = itemView.findViewById(R.id.delete_button);
            editButton = itemView.findViewById(R.id.edit_button);
            callButton = (ImageButton) itemView.findViewById(R.id.call_button);

            flagImage = itemView.findViewById(R.id.flag_image);

            navButton = itemView.findViewById(R.id.navigation_button);



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


        void notifyUser(String userId, final int tag) {
                /*HashMap<String, String> params = new HashMap<String, String>();
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
            }*/
        }
    }

        void showToast(String message) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }


    }
