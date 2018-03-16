package com.anekvurna.cognichampdriver;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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

import static com.anekvurna.cognichampdriver.SanathUtilities.*;

public class LogInActivity extends AppCompatActivity {

    private String phoneNumber;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        setTitle("Log In");
        initializeSharedPrefs(this);
    }

    public void onLogIn(View view)
    {
        phoneNumber = "+91" +getEditText(R.id.mobile_number).getText().toString();
        showToast("Automatically detecting SMS sent to your mobile");
        phoneAuthenticate();
    }

    void phoneAuthenticate()
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

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        showToast("Logging in user...");
        mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = task.getResult().getUser();
                            checkForPassword(user);

                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                showToast("Sign in failed. Invalid credential");
                            }
                        }
                    }
                });
    }

    private void loginUser(FirebaseUser user) {
        final Context context = this;
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("users");
        String phoneNumber = getEditText(R.id.mobile_number).getText().toString();
        String password = getEditText(R.id.login_password).getText().toString();
        User user1 = new User(phoneNumber, password, 0);//TODO:
        databaseReference.child(user.getUid()).setValue(user1, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                showToast("Login successful");
                editor.putInt(getString(R.string.profile_status), 4);
                editor.apply();
                loadActivityAndFinish(context, ViewTabbedActivity.class);
            }
        });
    }

    void checkForPassword(final FirebaseUser user)
    {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean flag = false;
                if(dataSnapshot.exists())
                {
                    User user1= dataSnapshot.getValue(User.class);
                    if(user1!=null)
                    if(user1.getEmail().equals(getEditText(R.id.login_password).getText().toString()))
                    {
                        flag = true;
                    }

                }
                if(flag)
                {
                    loginUser(user);
                }
                else
                {
                    showToast("Error invalid username and password");
                    FirebaseAuth.getInstance().signOut();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public EditText getEditText(int id)
    {
        return (EditText)findViewById(id);
    }


    public void onOrRegister(View view)
    {
        loadActivityAndFinish(this, RegistrationActivity.class);
    }
}
