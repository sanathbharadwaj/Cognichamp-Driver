package com.anekvurna.cognichampdriver;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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

import java.util.ArrayList;
import java.util.List;

import static com.anekvurna.cognichampdriver.SanathUtilities.setProgressBar;

public class HistoryDatesActivity extends DrawerActivity {

    private List<String> listTexts;
    private ArrayAdapter adapter;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = auth.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_dates);
        setTitle("Notifications history");
        initializeListView();
        getNotifications();
    }

    void getNotifications()
    {
        setProgressBar(this, true, "Fetching...");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("messages").child(currentUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listTexts.clear();
                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                {
                    String date = dataSnapshot1.getKey();
                    listTexts.add(date);
                }
                adapter.notifyDataSetChanged();
                setProgressBar(HistoryDatesActivity.this, false, "dummy");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                setProgressBar(HistoryDatesActivity.this, false, "dummy");
            }
        });

    }


    void initializeListView()
    {
        ListView listView = findViewById(R.id.notifs_list);
        listTexts = new ArrayList<>();
        showToast("Fetching lists");
        adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.list_view_layout, listTexts);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(HistoryDatesActivity.this, NotificationsHistoryActivity.class);
                intent.putExtra("date", listTexts.get(position));
                startActivity(intent);
            }
        });
    }

    void showToast(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}
