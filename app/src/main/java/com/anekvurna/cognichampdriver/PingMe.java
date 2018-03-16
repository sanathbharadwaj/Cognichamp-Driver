package com.anekvurna.cognichampdriver;


/**
 * Created by Sanath on 1/3/2018.
 */

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;


public class PingMe extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        // Enable Local Datastore.

    }
}

