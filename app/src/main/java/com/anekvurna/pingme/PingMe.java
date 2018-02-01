package com.anekvurna.pingme;


/**
 * Created by Sanath on 1/3/2018.
 */

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseUser;


public class PingMe extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                .applicationId("pingMeId")
                .clientKey("thisIsNotSecret")
                .server("http://52.14.72.244:1338/parse")
                .enableLocalDataStore()
                .build()
        );

        ParsePush.subscribeInBackground("");

        ParseInstallation.getCurrentInstallation().saveInBackground();
        ParseUser.enableAutomaticUser();

        ParseACL defaultACL = new ParseACL();
        defaultACL.setPublicReadAccess(true);
        defaultACL.setPublicWriteAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);
    }
}

