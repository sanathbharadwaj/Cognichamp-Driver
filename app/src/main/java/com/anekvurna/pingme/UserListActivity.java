package com.anekvurna.pingme;

import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;
import static com.anekvurna.pingme.SanathUtilities.*;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;


public class UserListActivity extends AppCompatActivity {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    List<ParseObject> localUsers;
    RecyclerView recyclerView;
    MyUsersAdapter myUsersAdapter;
    private PopupWindow popupWindow;
    private String listName;
    private View popupView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        localUsers = new ArrayList<>();
        loadUsers();
    }

    void loadUsers()
    {
        Intent intent = getIntent();
        listName = intent.getStringExtra("listName");
        ParseQuery query = new ParseQuery("LocalUser");
        query.fromLocalDatastore();
        query.whereEqualTo("listName", listName);
        query.orderByAscending("createdAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                    if(e!=null)
                    {
                        showToast("Error loading data!");
                        finish();
                    }
                    localUsers = objects;
                    myUsersAdapter = new MyUsersAdapter(localUsers, getApplicationContext());
                    recyclerView.setAdapter(myUsersAdapter);
            }
        });
    }

    void showToast(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void onAddNew(View view) {

        // get a reference to the already created main layout
        ConstraintLayout mainLayout = (ConstraintLayout)
                findViewById(R.id.user_list_layout);

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        popupView = inflater.inflate(R.layout.popup_window, null);

        // create the popup window
        int width = ConstraintLayout.LayoutParams.MATCH_PARENT;
        int height = ConstraintLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        popupWindow.showAtLocation(mainLayout, Gravity.CENTER, 0, 0);


    }

    public void onAdd(View view)
    {
        showToast("Adding user...");

        String userName = getPopUpEditText(R.id.popup_name).getText().toString();
        final String mobile = getPopUpEditText(R.id.popup_mobile).getText().toString();
        popupWindow.dismiss();
        final ParseObject object = new ParseObject("LocalUser");
        object.put("userName", userName);
        object.put("mobile", mobile);
        object.put("listName", listName);

        object.pinInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e!=null){showToast("Failed to add user"); return;}
                localUsers.add(object);
                findInsId(mobile, object);
                myUsersAdapter.notifyItemInserted(localUsers.size()-1);
            }
        });
    }

    void findInsId(String mobile, final ParseObject parseObject)
    {
        ParseQuery query = new ParseQuery("Profile");
        query.whereEqualTo("userMobile", mobile);
        query.setLimit(1);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e!=null){showToast("Internet error! You cannot send notifications to this user"); return;}
                ParseObject object = objects.get(0);
                parseObject.put("insId", object.getString("insId"));
                parseObject.pinInBackground();
            }
        });
    }


    @Override
    public void onBackPressed()
    {
        loadActivityAndFinish(this, AllListsActivity.class);
    }

    public void onCancel(View view)
    {
        popupWindow.dismiss();
    }

    EditText getPopUpEditText(int id)
    {
        return (EditText)popupView.findViewById(id);
    }

}
