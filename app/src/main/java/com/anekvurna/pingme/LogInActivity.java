package com.anekvurna.pingme;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.concurrent.TimeUnit;

import static com.anekvurna.pingme.SanathUtilities.loadActivityAndFinish;

public class LogInActivity extends AppCompatActivity {

    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        setTitle("Log In");
    }

    public void onLogIn(View view)
    {
        phoneNumber = getEditText(R.id.mobile_number).getText().toString();
        showToast("Automatically detecting SMS sent to your mobile");
        phoneAuthenticate();
    }

    void logInUser()
    {
        String password = getEditText(R.id.password).getText().toString();
        ParseUser.logInInBackground(phoneNumber, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if(user == null || e != null)
                {
                    showToast(e.getMessage());
                    return;
                }
                showToast("LogIn successful");
                loadToProfileActivity();
            }
        });
    }

    void phoneAuthenticate()
    {
        PhoneAuthProvider.OnVerificationStateChangedCallbacks myCallBacks;
        myCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                showToast("Successfully verified mobile number");
                logInUser();
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    showToast("Verification failed");
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    showToast("Verification failed");
                }

            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
            }
        };

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                20,
                TimeUnit.SECONDS,
                this,
                myCallBacks
        );
    }

    void showToast(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    public EditText getEditText(int id)
    {
        return (EditText)findViewById(id);
    }

    public void loadToProfileActivity()
    {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }

    public void onOrRegister(View view)
    {
        loadActivityAndFinish(this, RegistrationActivity.class);
    }
}
