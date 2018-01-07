package com.anekvurna.pingme;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import static com.anekvurna.pingme.SanathUtilities.*;

import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.concurrent.TimeUnit;


public class RegistrationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        if(ParseUser.getCurrentUser() != null)
            loadActivityAndFinish(this, ProfileActivity.class);
    }

    public void onRegister(View view)
    {
        String phoneNumber = getEditText(R.id.mobile_number).getText().toString();
        showToast("Automatically detecting SMS sent to your mobile");
        phoneAuthenticate(phoneNumber);
    }

    void registerUser()
    {
        String phoneNumber = getEditText(R.id.mobile_number).getText().toString();
        ParseUser user = new ParseUser();
        user.setUsername(phoneNumber);
        user.setPassword(getEditText(R.id.password).getText().toString());
        user.put("phone", phoneNumber);
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if(e!=null)
                {
                    showToast(e.getMessage());
                    return;
                }
                showToast("Registration successful");
                loadToProfileActivity();
            }
        });
    }


    void phoneAuthenticate(String phoneNumber)
    {
        PhoneAuthProvider.OnVerificationStateChangedCallbacks myCallBacks;
        myCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
               showToast("Successfully verified mobile number");
                registerUser();
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

    public void onOrLogin(View view)
    {
        loadActivityAndFinish(this, LogInActivity.class);
    }
}
