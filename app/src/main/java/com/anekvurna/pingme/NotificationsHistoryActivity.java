package com.anekvurna.pingme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class NotificationsHistoryActivity extends AppCompatActivity {
    private List<String> listTexts;
    private ListView listView;
    private ArrayAdapter adapter;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = auth.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications_history);
        setTitle("Notifications history");
       /* FindCallback first = new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                parseObjects = objects;
                initializeListView();
            }
        };
        getNotifications(first);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("NEW");
        registerReceiver(broadcastReceiver, intentFilter)*/

        initializeListView();
        getNotifications();
    }

    void getNotifications()
    {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("messages").child(currentUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listTexts.clear();
                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                {
                    String message = dataSnapshot1.getValue(String.class);
                    listTexts.add(message);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        /*ParseQuery query = new ParseQuery("NotificationThree");
        query.orderByDescending("date");
        query.fromLocalDatastore();
        query.findInBackground(callback);*/
    }



    /*BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action == null) return;
            FindCallback second = new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    parseObjects = objects;
                    putData();
                    adapter.notifyDataSetChanged();
                }
            };
            getNotifications(second);
        }
    };*/

    void initializeListView()
    {
        listView = (ListView)findViewById(R.id.notifs_list);
        listTexts = new ArrayList<>();
        listTexts.add("Fetching lists...");
        adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.list_view_layout, listTexts);
        listView.setAdapter(adapter);
        listView.setClickable(false);
    }

    /*void putData()
    {
        listTexts.clear();
        for(ParseObject object : parseObjects){
            String text = object.getString("message");
            listTexts.add(text);
        }
    }*/

    void showToast(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


}
