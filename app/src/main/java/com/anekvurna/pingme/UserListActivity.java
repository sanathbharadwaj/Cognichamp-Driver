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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
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

import java.util.ArrayList;
import java.util.List;


public class UserListActivity extends AppCompatActivity {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    List<LocalUser> localUsers;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        intent = getIntent();
        listName = intent.getStringExtra(getString(R.string.list_name));
        listId = intent.getStringExtra(getString(R.string.list_id));
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        localUsers = new ArrayList<>();
        initializeDatabaseReference();
        myUsersAdapter = new MyUsersAdapter(localUsers, this);
        recyclerView.setAdapter(myUsersAdapter);
        setTitle(listName);
        loadUsers();

    }

    void initializeDatabaseReference()
    {

        FirebaseAuth auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        listRef = FirebaseDatabase.getInstance().getReference("lists")
                .child(currentUser.getUid()).child(listId).child(listName);
        //TODO: change to user profiles
        userRef = FirebaseDatabase.getInstance().getReference("users");
    }

    void loadUsers()
    {
       localUsers.clear();
       listRef.addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(DataSnapshot dataSnapshot) {
               for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
               {
                   UserList userList = dataSnapshot1.getValue(UserList.class);
                   getUserImage(userList);
               }
           }

           @Override
           public void onCancelled(DatabaseError databaseError) {
                showToast("Failed to load data");
           }
       });


    }

    void getUserImage(final UserList userList) {
        if (userList == null) return;
        //TODO: change to user profiles
        StorageReference reference = FirebaseStorage.getInstance().getReference("driverProfiles")
                .child(userList.getUserId()).child("driverImage.jpg");
        LocalUser localUser = new LocalUser(userList.getUsername(), userList.getMobile(), userList.getUserId(), userList.getElementId(), reference);
        localUsers.add(localUser);
        myUsersAdapter.notifyItemInserted(localUsers.size() - 1);
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

    public void onAdd(View view)
    {
        showToast("Adding user...");

        final String userName = getPopUpEditText(R.id.popup_name).getText().toString();
        final String mobile = getPopUpEditText(R.id.popup_mobile).getText().toString();
        popupWindow.dismiss();
        //TODO: change to user profiles
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference("driverProfiles");

        userRef.orderByChild("mobile").equalTo(mobile).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean isEmpty = true;
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    User user = dataSnapshot1.getValue(User.class);
                    if (user == null) {
                        showToast("User not found");
                        return;
                    }
                    isEmpty = false;

                    String userId = dataSnapshot1.getKey();
                    //TODO: change it to user image
                    StorageReference reference = storageReference.child(userId).child("driverImage.jpg");
                    String elementId;
                    if(isEditing)
                    {
                        elementId = localUsers.get(editPosition).getElementId();
                    }
                    else
                        elementId = listRef.push().getKey();
                    UserList userList = new UserList(userName, mobile, elementId, userId);
                    listRef.child(elementId).setValue(userList);

                    LocalUser localUser = new LocalUser(userName, mobile, userId, elementId, reference);
                    if(isEditing)
                    {
                        editUser(localUser);
                        return;
                    }
                    localUsers.add(localUser);
                    myUsersAdapter.notifyItemInserted(localUsers.size() - 1);


                }
                if(isEmpty)showToast("User not found");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    void editUser(LocalUser localUser)
    {
        localUsers.set(editPosition, localUser);
        myUsersAdapter.notifyItemChanged(editPosition);
        isEditing = false;
    }

    public void startTrip(View view)
    {
        Button button = findViewById(R.id.start_trip_button);
       if(startedTrip)button.setText(R.string.end_trip);
       else
           button.setText(R.string.start_trip);
       startedTrip = !startedTrip;
        myUsersAdapter.notifyDataSetChanged();
    }




    public void addToMessageHistory(String message)
    {
        DatabaseReference messageReference = FirebaseDatabase.getInstance().getReference("messages").child(currentUser.getUid());
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
