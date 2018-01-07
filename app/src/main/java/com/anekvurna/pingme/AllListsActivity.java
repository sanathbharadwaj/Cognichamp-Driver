package com.anekvurna.pingme;

import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class AllListsActivity extends AppCompatActivity {


    private List<String> listNames;
    private ListView listView;
    private ArrayAdapter adapter;
    private String[] elements;
    private DrawerLayout drawerLayout;
    private ListView drawerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_lists);
        setTitle("All Lists");
        initializeListView();
        setListView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.navigation_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ping_history:
                startActivity(new Intent(this, NotificationsHistoryActivity.class));
                return true;
            case R.id.profile:
                startActivity(new Intent(this, ViewProfileActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }


    void initializeListView()
    {
        listView = (ListView) findViewById(R.id.my_lists);
        listNames = new ArrayList<>();
        ParseQuery query = new ParseQuery("List");
        query.orderByDescending("createdAt");
        query.fromLocalDatastore();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e!=null) {showToast(e.getMessage()); return;}

                if(objects.size()==0)
                {
                    findViewById(R.id.first_list).setVisibility(View.VISIBLE);
                    listView.setVisibility(View.GONE);
                }
                for(ParseObject object : objects)
                {
                    listNames.add(object.getString("listName"));
                }
                adapter.notifyDataSetChanged();
            }
        });

    }

    void setListView()
    {
        adapter = new ArrayAdapter(getApplicationContext(), R.layout.list_view_layout, listNames);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                loadUserListActivity(position);
            }
        });
    }

    void loadUserListActivity(int position)
    {
        Intent intent = new Intent(this, UserListActivity.class);
        intent.putExtra("listName", listNames.get(position));
        startActivity(intent);
        finish();
    }

    public void addItem(View view)
    {
        EditText editText = (EditText) findViewById(R.id.new_list_edittext);
        final String name = editText.getText().toString();
        ParseObject object = new ParseObject("List");
        object.put("listName", name);
        object.pinInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e!=null) {showToast(e.getMessage()); return;}
                listNames.add(name);
                adapter.notifyDataSetChanged();
                if(listNames.size() == 1)
                {
                    findViewById(R.id.first_list).setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    void showToast(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
