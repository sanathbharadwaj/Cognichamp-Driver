package com.anekvurna.pingme;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static com.anekvurna.pingme.SanathUtilities.*;


public class UserChoiceActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_choice);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null)
            loadActivityAndFinish(this, ViewTabbedActivity.class);

    }

    public void onRegister(View view)
    {
        loadActivityAndFinish(this, RegistrationActivity.class);
    }

    public void onLogIn(View view)
    {
        loadActivityAndFinish(this, LogInActivity.class);
    }
}
