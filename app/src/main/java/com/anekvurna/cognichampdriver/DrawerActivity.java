package com.anekvurna.cognichampdriver;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;

import static com.anekvurna.cognichampdriver.SanathUtilities.editor;
import static com.anekvurna.cognichampdriver.SanathUtilities.loadActivity;
import static com.anekvurna.cognichampdriver.SanathUtilities.loadActivityAndClearStack;

public class DrawerActivity extends AppCompatActivity implements NetworkStateReceiver.NetworkStateReceiverListener{

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private NetworkStateReceiver networkStateReceiver;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void onCreateDrawer()
    {
        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));


        final Context context = this;

        if(getSupportActionBar() == null) {
            Toolbar toolbar = findViewById(R.id.toolbar);
            if(toolbar!=null)
            setSupportActionBar(toolbar);
        }
            if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);

        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        NavigationView mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem){
                mDrawerLayout.closeDrawers();
                switch (menuItem.getItemId()){
                    case(R.id.notification_history):
                        loadActivity(context, HistoryDatesActivity.class);break;
                    case(R.id.all_list):
                        loadActivity(context, TripListActivity.class);break;
                    case(R.id.logout):
                        editor.clear();
                        editor.commit();
                        FirebaseAuth.getInstance().signOut();
                        loadActivityAndClearStack(context, UserChoiceActivity.class);break;
                    /*case (R.id.reset_password):
                        loadActivityAndFinish(context, ResetPasswordActivity.class);break;*/
                    default:break;
                }
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDrawerLayout.closeDrawer(Gravity.START, false);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        onCreateDrawer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        networkStateReceiver.removeListener(this);
        this.unregisterReceiver(networkStateReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(mToggle.onOptionsItemSelected(item))
            return true;

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void checkForNetConnection()
    {
        if(!isInternetAvailable())
        {
            showNoInternetAlert();
        }
    }

    boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        // There are no active networks.
        return ni != null && ni.isConnected();

    }

    public void showNoInternetAlert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setCancelable(false);

        // Setting Dialog Title
        alertDialogBuilder.setTitle("Network unavailable");

        // Setting Dialog Message
        alertDialogBuilder.setMessage("Internet is not available. Please connect to the internet");

        // On pressing Settings button
        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    finishAffinity();
                }
                finish();
            }
        });

        // on pressing cancel button

        // Showing Alert Message
        dialog = alertDialogBuilder.show();
    }

    @Override
    public void networkAvailable() {
        if(dialog !=null)
            dialog.dismiss();
    }

    @Override
    public void networkUnavailable() {
        showNoInternetAlert();
    }
}
