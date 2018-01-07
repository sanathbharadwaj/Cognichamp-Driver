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
    List<ParseObject> parseObjects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications_history);
        setTitle("Notifications history");
        FindCallback first = new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                parseObjects = objects;
                initializeListView();
            }
        };
        getNotifications(first);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("NEW");
        registerReceiver(broadcastReceiver, intentFilter);
    }

    void getNotifications(FindCallback callback)
    {
        ParseQuery query = new ParseQuery("NotificationThree");
        query.orderByDescending("date");
        query.fromLocalDatastore();
        query.findInBackground(callback);
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
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
    };

    void initializeListView()
    {
        listView = (ListView)findViewById(R.id.notifs_list);
        listTexts = new ArrayList<>();
        putData();
        adapter = new ArrayAdapter(getApplicationContext(), R.layout.list_view_layout, listTexts);
        listView.setAdapter(adapter);
        listView.setClickable(false);
    }

    void putData()
    {
        listTexts.clear();
        for(ParseObject object : parseObjects){
            String text = object.getString("message");
            listTexts.add(text);
        }
    }

    void showToast(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


}
