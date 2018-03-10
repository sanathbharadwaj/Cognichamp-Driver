package com.anekvurna.pingme;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;
import static com.anekvurna.pingme.SanathUtilities.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class UserListActivity extends AppCompatActivity {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    ArrayList<LocalUser> localUsers;
    RecyclerView recyclerView;
    MyUsersAdapter myUsersAdapter;
    private PopupWindow popupWindow;
    private View popupView;
    public FirebaseUser currentUser;
    private DatabaseReference listRef;
    private DatabaseReference userRef;
    private Intent intent;
    private boolean isEditing;
    private int editPosition;
    public boolean startedTrip = false;
    private String listName;
    private String listId;
    static final int PICK_CONTACT=1;
    public ArrayList<Integer> states;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        intent = getIntent();
        listName = intent.getStringExtra(getString(R.string.list_name));
        listId = intent.getStringExtra(getString(R.string.list_id));
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        localUsers = new ArrayList<>();
        states = new ArrayList<>();
        initializeDatabaseReference();
        initializeSharedPrefs(this);
        myUsersAdapter = new MyUsersAdapter(localUsers, this);
        recyclerView.setAdapter(myUsersAdapter);
        setTitle(listName);
        checkForPreviousRunningTrip();
    }

    private void checkForPreviousRunningTrip() {
        if(preferences.getBoolean(getString(R.string.local_trip_running), false))
        {
            Gson gson = new Gson();
            String jsonText = preferences.getString(getString(R.string.local_users_json), "");
            if(jsonText.equals("") && gson.fromJson(jsonText, new TypeToken<ArrayList<LocalUser>>(){}.getType()) == null)
            {
                showToast("Could not load previous trip");
                loadUsers();
                return;
            }
            localUsers = gson.fromJson(jsonText, new TypeToken<ArrayList<LocalUser>>(){}.getType());
            myUsersAdapter = new MyUsersAdapter(localUsers, this);
            recyclerView.setAdapter(myUsersAdapter);
            startedTrip = true;
        }
    }

    void initializeDatabaseReference()
    {

        FirebaseAuth auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        listRef = FirebaseDatabase.getInstance().getReference("lists")
                .child(currentUser.getUid()).child(listId).child("elements");
        userRef = FirebaseDatabase.getInstance().getReference("customers");
    }

    void loadUsers()
    {
        setProgressBar(this,true, "Loading...");
       localUsers.clear();
       listRef.addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(DataSnapshot dataSnapshot) {
               setProgressBar(UserListActivity.this,false, "dummy");
               for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
               {
                   UserList userList = dataSnapshot1.getValue(UserList.class);
                   getUserData(userList);
               }
           }

           @Override
           public void onCancelled(DatabaseError databaseError) {
                showToast("Failed to load data");
           }
       });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.edit_list_name:
                showNameEditDialog();return true;
            case R.id.delete_list:
                showListDeleteAlert();return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    void changeListName(String name)
    {
        DatabaseReference listNameRef = FirebaseDatabase.getInstance().getReference("lists")
                .child(currentUser.getUid()).child(listId).child("name");
        listNameRef.setValue(name);
        setTitle(name);
    }

    void showNameEditDialog()
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.custom_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText editText = (EditText) dialogView.findViewById(R.id.list_name_et);

        dialogBuilder.setTitle("List Name");
        dialogBuilder.setMessage("Enter name below");
        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                String name = editText.getText().toString();
                if(name.equals(""))
                {
                    showToast("Please fill the name field");
                    return;
                }
               changeListName(name);
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    void showListDeleteAlert()
    {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle("Delete trip list?")
                .setMessage("This cannot be undone. Are you sure?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteList();
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

    private void deleteList() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("lists").child(currentUser.getUid());
        reference.child(listId).removeValue();
        loadActivityAndFinish(this, TripListActivity.class);
    }

    void getUserData(final UserList userList) {
        if (userList == null) return;
       /*
        StorageReference reference = FirebaseStorage.getInstance().getReference("driverProfiles")
                .child(userList.getUserId()).child("driverImage.jpg");*/
        myUsersAdapter.notifyItemInserted(localUsers.size() - 1);
        checkUserRegistrationStatus(userList);
    }

    private void checkUserRegistrationStatus(final UserList userList) {
        DatabaseReference registeredReference = FirebaseDatabase.getInstance().getReference("customers");
        registeredReference.orderByChild("mobile").equalTo(userList.getMobile()).limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = null;
                        for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                        {
                            user = dataSnapshot1.getValue(User.class);
                        }
                        LocalUser localUser;
                        if(user == null)
                            localUser = new LocalUser(userList.getUsername(), userList.getMobile(), userList.getUserId(),
                                    userList.getElementId(), false);
                        else
                            localUser = new LocalUser(userList.getUsername(), userList.getMobile(), userList.getUserId(),
                                    userList.getElementId(), true);

                        localUsers.add(localUser);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    void showToast(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void onAddNew(View view) {
        showPopup();
    }

    void showPopup()
    {
        isEditing = false;
        // get a reference to the already created main layout
        ConstraintLayout mainLayout = (ConstraintLayout)
                findViewById(R.id.user_list_layout);

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        popupView = inflater.inflate(R.layout.popup_window, null);

        // create the popup window
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        popupWindow.showAtLocation(mainLayout, Gravity.BOTTOM, 0, 0);

    }

    public void pickContact(View view)
    {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT);
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode) {
            case (PICK_CONTACT) :
                if (resultCode == Activity.RESULT_OK) {

                    Uri contactData = data.getData();
                    Cursor c =  managedQuery(contactData, null, null, null, null);
                    if (c.moveToFirst()) {


                        String id =c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

                        String hasPhone =c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                        if (hasPhone.equalsIgnoreCase("1")) {
                            Cursor phones = getContentResolver().query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id,
                                    null, null);
                            phones.moveToFirst();
                            String cNumber = phones.getString(phones.getColumnIndex("data1"));
                            cNumber = cNumber.replaceAll("\\s","");
                            int l = cNumber.length();
                            if(l<10){
                                showToast("Invalid phone number");
                                break;
                            }
                            String tenDigit = cNumber.substring(l-10, l);
                            getPopUpEditText(R.id.popup_mobile).setText(tenDigit);

                        }
                        String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        getPopUpEditText(R.id.popup_name).setText(name);
                    }
                }
                break;
        }
    }

    public EditText getEditText(int id) {
        return (EditText) findViewById(id);
    }


    public void onAdd(View view)
    {
        setProgressBar(this,true, "Adding user...");

        final String userName = getPopUpEditText(R.id.popup_name).getText().toString();
        final String mobile = getPopUpEditText(R.id.popup_mobile).getText().toString();
        popupWindow.dismiss();
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference("customerProfiles");

        userRef.orderByChild("mobile").equalTo(mobile).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                setProgressBar(UserListActivity.this,false, "dummy");
                boolean isEmpty = true, isRegistered = true;
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    User user = dataSnapshot1.getValue(User.class);
                    if (user == null) {
                        showToast("User not found");
                        isRegistered = false;
                    }
                    isEmpty = false;

                    String userId;
                    if(isRegistered)
                        userId = dataSnapshot1.getKey();
                    else
                        userId = "unregistered";
                    String elementId;
                    if(isEditing)
                    {
                        elementId = localUsers.get(editPosition).getElementId();
                    }
                    else
                        elementId = listRef.push().getKey();
                    UserList userList = new UserList(userName, mobile, elementId, userId, isRegistered);
                    listRef.child(elementId).setValue(userList);

                    LocalUser localUser = new LocalUser(userName, mobile, userId, elementId, isRegistered);
                    if(isEditing)
                    {
                        editUser(localUser);
                        return;
                    }
                    localUsers.add(localUser);
                    myUsersAdapter.notifyItemInserted(localUsers.size() - 1);


                }
                if(isEmpty){
                    showToast("User not found");
                    addUnregisteredUser(userName, mobile);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void addUnregisteredUser(String userName, String mobile) {
        String userId = "unregistered";
        String elementId;
        if(isEditing)
        {
            elementId = localUsers.get(editPosition).getElementId();
        }
        else
            elementId = listRef.push().getKey();
        UserList userList = new UserList(userName, mobile, elementId, userId, false);
        listRef.child(elementId).setValue(userList);

        LocalUser localUser = new LocalUser(userName, mobile, userId, elementId, false);
        if(isEditing)
        {
            editUser(localUser);
            return;
        }
        localUsers.add(localUser);
        myUsersAdapter.notifyItemInserted(localUsers.size() - 1);
    }

    void editUser(LocalUser localUser)
    {
        localUsers.set(editPosition, localUser);
        myUsersAdapter.notifyItemChanged(editPosition);
        isEditing = false;
    }

    public void showStartTripAlert(View view)
    {
        String title, alert;

        if(startedTrip)
        {
            title = "End trip";
            alert = "Are you sure you want to end trip?";
        }
        else
        {
            title = "Start trip";
            alert = "Are you sure you want to start trip?";
        }
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle(title)
                .setMessage(alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        startTrip();
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

    void startTrip()
    {
        if(localUsers.size() == 0)
        {
            showToast("Cannot start trip with no users");
            return;
        }

        if(!startedTrip) {
            updateStartStatus(true);
            return;
        }
        /*updateTrackables(false);
        listRef.getParent().child("tripRunning").setValue(false);
        Button button = findViewById(R.id.start_trip_button);
        startedTrip = false;
        button.setText(R.string.start_trip);
        myUsersAdapter.notifyDataSetChanged();*/
        updateStartStatus(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(startedTrip)
        {
            editor.putBoolean(getString(R.string.local_trip_running), true);
            Gson gson = new Gson();
            String localUsersJSON = gson.toJson(localUsers);
            editor.putString(getString(R.string.local_users_json), localUsersJSON);
            editor.commit();
        }
        else
        {
            editor.putBoolean(getString(R.string.local_trip_running), false);
            editor.remove(getString(R.string.local_users_json));
            editor.commit();
        }

    }

    private void updateTrackables(boolean status) {
        for(LocalUser localUser : localUsers)
        {
            DatabaseReference trackReference = FirebaseDatabase.getInstance().getReference("trackables")
                    .child(localUser.getUserId()).child(currentUser.getUid());
            trackReference.setValue(status);
        }
    }

    private void updateStartStatus(boolean value) {
        updateTrackables(value);
        listRef.getParent().child("tripRunning").setValue(value);
        Button button = findViewById(R.id.start_trip_button);
        startedTrip = value;
        if(value)
            button.setText(R.string.end_trip);
        else
            button.setText(R.string.start_trip);
        myUsersAdapter.notifyDataSetChanged();
    }


    public void addToMessageHistory(String message)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String dateString = dateFormat.format(new Date());
        DatabaseReference messageReference = FirebaseDatabase.getInstance().getReference("messages").child(currentUser.getUid())
                .child(dateString);
        String id = messageReference.push().getKey();
        messageReference.child(id).setValue(message);
    }

    public void deleteUser(final int position) {
        //TODO: change to user profiles
        final DatabaseReference listReference = FirebaseDatabase.getInstance().getReference("lists")
                .child(currentUser.getUid()).child(listId);
        listReference.child(listName).child(localUsers.get(position).getElementId()).removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(databaseError != null)
                    databaseError.getMessage();
                else
                {
                    if(localUsers.size() == 1)
                        addBlank(listReference);
                        localUsers.remove(position);
                        myUsersAdapter.notifyItemRemoved(position);
                }
            }
        });
    }

    void addBlank(DatabaseReference reference)
    {
        reference.setValue(listName);
    }

    public void showEditingPopup(LocalUser localUser, int position)
    {
        showPopup();
        getPopUpEditText(R.id.popup_mobile).setText(localUser.getMobile());
        getPopUpEditText(R.id.popup_name).setText(localUser.getName());
        isEditing = true;
        editPosition = position;
    }


    @Override
    public void onBackPressed()
    {
        loadActivityAndFinish(this, TripListActivity.class);
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
