package com.anekvurna.cognichampdriver;

import android.content.Intent;
import android.support.design.widget.TabLayout;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import static com.anekvurna.cognichampdriver.SanathUtilities.*;

public class ViewTabbedActivity extends DrawerActivity {
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_tabbed);

        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
*/

        mSectionsPagerAdapter = new ViewTabbedActivity.SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager){
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                super.onTabSelected(tab);
            }
        });



        initializeSharedPrefs(this);

        initializeCurrentUser();

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

    public void onEditAddress(View view)
    {
        Intent intent = new Intent(this, ProfileAddressActivity.class);
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
                        return new AddressProfileFragment();

                    case 2:
                        return new OfficialProfileFragment();

                    case 3:
                        return new CarProfileFragment();

                    default:
                        return null;

                }
            }

            @Override
            public int getCount() {
                return 4;
            }

            @Override
            public CharSequence getPageTitle(int position)
            {
                switch (position)
                {
                    case 0: return "BASIC";
                    case 1: return "ADDRESS";
                    case 2: return "OFFICIAL";
                    case 3: return "VEHICLE";
                    default:return null;
                }
            }
        }
    }

