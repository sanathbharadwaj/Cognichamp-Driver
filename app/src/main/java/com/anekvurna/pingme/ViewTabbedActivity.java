package com.anekvurna.pingme;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

import static com.anekvurna.pingme.SanathUtilities.*;

public class ViewTabbedActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_tabbed);

        checkForNetConnection();

        final Context context = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        initializeSharedPrefs(this);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);

        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        NavigationView mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem){
                switch (menuItem.getItemId()){
                    case(R.id.notification_history):
                        loadActivity(context, NotificationsHistoryActivity.class);break;
                    case(R.id.all_list):
                        loadActivity(context, AllListsActivity.class);break;
                    case(R.id.logout):
                        FirebaseAuth.getInstance().signOut();
                        editor.putInt("profileStatus", 0);
                        editor.apply();
                        loadActivityAndFinish(context, RegistrationActivity.class);break;
                }
                mDrawerLayout.closeDrawers();
                return true;
            }
        });

        initializeCurrentUser();
        storeInstantiationId();

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

    private void storeInstantiationId() {
        final String insId = ParseInstallation.getCurrentInstallation().getInstallationId();
        ParseQuery<ParseObject> query = new ParseQuery<>("FirebaseUser");
        query.whereEqualTo("userId", currentUser.getUid());
        query.setLimit(1);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e != null) return;
                if (objects.size() != 0) {
                    objects.get(0).put("insId", insId);
                    objects.get(0).saveEventually();
                } else {
                    ParseObject parseObject = new ParseObject("FirebaseUser");
                    parseObject.put("userId", currentUser.getUid());
                    parseObject.put("insId", insId);
                    parseObject.saveEventually();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        this.recreate();
    }

    public void onEditBasic(View view)
    {
        Intent intent = new Intent(this, ProfileBasicActivity.class);
        intent.putExtra(getString(R.string.is_editing), true);
        intent.putExtra(getString(R.string.only_editing), true);
        startActivity(intent);
    }

    public void onEditOfficial(View view)
    {
        Intent intent = new Intent(this, ProfileOfficialActivity.class);
        intent.putExtra(getString(R.string.is_editing), true);
        intent.putExtra(getString(R.string.only_editing), true);
        startActivity(intent);
    }

    public void onEditCar(View view)
    {
        Intent intent = new Intent(this, ProfileCarActivity.class);
        intent.putExtra(getString(R.string.is_editing), true);
        intent.putExtra(getString(R.string.only_editing), true);
        startActivity(intent);
    }



   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_tabbed, menu);
        return true;
    }*/

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



        public class SectionsPagerAdapter extends FragmentPagerAdapter {

            SectionsPagerAdapter(FragmentManager fm) {
                super(fm);
            }

            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return new BasicProfileFragment();

                    case 1:
                        return new OfficialProfileFragment();

                    case 2:
                        return new CarProfileFragment();
                    default:
                        return null;

                }
            }

            @Override
            public int getCount() {
                return 3;
            }

            @Override
            public CharSequence getPageTitle(int position)
            {
                switch (position)
                {
                    case 0: return "BASIC";
                    case 1: return "OFFICIAL";
                    case 2: return "VEHICLE";
                    default:return null;
                }
            }
        }
    }

