package com.anekvurna.pingme;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import static com.anekvurna.pingme.SanathUtilities.*;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;


public class RegistrationActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String vid;
    private String mPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        mAuth = FirebaseAuth.getInstance();
        //FirebaseUser currentUser = mAuth.getCurrentUser();
    }




    public void onRegister(View view)
    {
        mPhoneNumber = getEditText(R.id.mobile_number).getText().toString();
        showToast("Automatically detecting SMS sent to your mobile");
        phoneAuthenticate("+91" + mPhoneNumber);
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        showToast("Registering user...");
        mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = task.getResult().getUser();
                            seeProfileExists(user);

                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                showToast("Sign in failed. Invalid credential");
                            }
                        }
                    }
                });
    }

    private void saveUser(FirebaseUser user) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("users");
        String phoneNumber = mPhoneNumber;
        String password = getEditText(R.id.password).getText().toString();
        User user1 = new User(phoneNumber, password, 0);
        databaseReference.child(user.getUid()).setValue(user1, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                showToast("Registration successful");
                loadToProfileActivity();
            }
        });
    }


    void seeProfileExists(final FirebaseUser user)
    {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        final FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser==null) return;
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("driverProfiles");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               if(dataSnapshot.hasChild(currentUser.getUid()))
               {
                  /* showToast("User already exists");
                   FirebaseAuth.getInstance().signOut();*/
                   showToast("Login successful");
                   initializeSharedPrefs(RegistrationActivity.this);
                   editor.putInt("profileStatus", 4);
                   editor.apply();
                   loadActivityAndFinish(RegistrationActivity.this, ViewTabbedActivity.class);
               }
               else
                   saveUser(user);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    showToast("Verification failed invalid credential");
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    showToast("Verification failed too many requests");
                }

            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
               // vid = verificationId;

            }
        };

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                myCallBacks
                );
    }

    public void verifyManually(View view)
    {
        String code = getEditText(R.id.manual).getText().toString();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(vid, code);
        signInWithPhoneAuthCredential(credential);
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
        Intent intent = new Intent(this, ProfileBasicActivity.class);
        startActivity(intent);
        finish();
    }

    public void onOrLogin(View view)
    {
        loadActivityAndFinish(this, LogInActivity.class);
    }
}
