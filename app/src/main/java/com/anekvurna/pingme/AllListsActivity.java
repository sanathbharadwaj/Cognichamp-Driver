package com.anekvurna.pingme;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class AllListsActivity extends AppCompatActivity {


    private List<String> listNames;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> listIds = new ArrayList<>();
    private FirebaseUser currentUser;
    private DatabaseReference databaseReference, locationReference;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location currentLocation;
    private Handler locationSendHandler;

    public AllListsActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_lists);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("lists").child(currentUser.getUid());
        setTitle("All Lists");
        checkForNetConnection();
        initializeLocationObjects();
        initializeListView();
        setListView();
        //storeInstantiationId();
        SanathUtilities.getFirebaseProfile();
    }



    void initializeListView()
    {
        listView = findViewById(R.id.my_lists);
        listNames = new ArrayList<>();

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listNames.clear();
                listIds.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    String name = null, id;
                    id = snapshot.getKey();
                    for(DataSnapshot snapshot1 : snapshot.getChildren())
                    {
                        name = snapshot1.getKey();
                    }
                    if(name==null)
                        name = snapshot.getValue(String.class);
                    if(name !=null && id!=null) {
                        listNames.add(name);
                        listIds.add(id);
                    }
                }
                if(listNames.size()==0)
                {
                    findViewById(R.id.first_list).setVisibility(View.VISIBLE);
                    listView.setVisibility(View.GONE);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                showToast(databaseError.getMessage());
            }
        });

    }

    void setListView()
    {
        adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.list_view_layout, listNames);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                loadUserListActivity(position);
            }
        });
    }

    void checkForNetConnection()
    {
        if(!isInternetAvailable())
        {
            showNoInternetAlert();
        }
    }

    boolean isInternetAvailable()
    {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            // There are no active networks.
            return false;
        }

        return ni.isConnected();
    }

    void loadUserListActivity(int position)
    {
        Intent intent = new Intent(this, UserListActivity.class);
        intent.putExtra(getString(R.string.list_name), listNames.get(position));
        intent.putExtra(getString(R.string.list_id), listIds.get(position));
        startActivity(intent);
        finish();
    }

    void initializeLocationObjects()
    {
        final Context context = this;
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationReference = FirebaseDatabase.getInstance().getReference("drivers").child(currentUser.getUid()).child("location");
        //TODO: Call GPS functions again
        locationListener = new LocationListener() {


            @Override
            public void onLocationChanged(Location location) {
                currentLocation = location;
                sendMyLocation();
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {
                //TODO: Call GPS functions again
                checkPermissionAndStart();
            }

            @Override
            public void onProviderDisabled(String s) {
                displayLocationSettingsRequest(context);

            }
        };

        checkPermissionAndStart();
    }

    void checkPermissionAndStart()
    {
        if (Build.VERSION.SDK_INT < 23) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            startLocation();
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                startLocation();
            }
        }
    }

    void startLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 10, locationListener);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double latitude = location.getLatitude();
    }

    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            public static final String TAG = "GPSAlert";

            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(TAG, "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(AllListsActivity.this, 1);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            startLocation();
        }
    }

    void sendMyLocation()
    {
        MyLocation myLocation = new MyLocation(currentLocation.getLatitude(), currentLocation.getLongitude());
        locationReference.setValue(myLocation);
    }

    private void storeInstantiationId() {
        final String insId = ParseInstallation.getCurrentInstallation().getInstallationId();
        ParseQuery<ParseObject> query = new ParseQuery<>("FirebaseUser");
        query.whereEqualTo("userId", currentUser.getUid());
        query.setLimit(1);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e!= null) return;
                if(objects.size()!=0) {
                    objects.get(0).put("insId", insId);
                    objects.get(0).saveEventually();
                }
                else {
                    ParseObject parseObject = new ParseObject("FirebaseUser");
                    parseObject.put("userId", currentUser.getUid());
                    parseObject.put("insId", insId);
                    parseObject.saveEventually();
                }
            }
        });

    }

    public void addItem(View view)
    {
        final EditText editText = findViewById(R.id.new_list_edittext);
        final String name = editText.getText().toString();

        String id = databaseReference.child(currentUser.getUid()).push().getKey();
        listIds.add(id);
        databaseReference.child(id).setValue(name, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(databaseError != null) {showToast(databaseError.getMessage()); return;}
                listNames.add(name);
                adapter.notifyDataSetChanged();
                editText.setText("");
                if(listNames.size() == 1)
                {
                    findViewById(R.id.first_list).setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void showNoInternetAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("Network unavailable");

        // Setting Dialog Message
        alertDialog.setMessage("Internet is not available. Please connect to the internet");

        // On pressing Settings button
        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
               finish();
            }
        });

        // on pressing cancel button

        // Showing Alert Message
        alertDialog.show();
    }

    void showToast(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
