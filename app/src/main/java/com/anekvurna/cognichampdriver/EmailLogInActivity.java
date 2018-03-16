package com.anekvurna.cognichampdriver;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import static com.anekvurna.cognichampdriver.SanathUtilities.*;

public class EmailLogInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_log_in);
    }

    public void registerUser(View view)
    {
        setProgressBar(this, true, "Signing in...");
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String email = getStringFromEditText(R.id.email);
        String password = getStringFromEditText(R.id.password);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            setProgressBar(EmailLogInActivity.this,false, "");
                            // Sign in success, update UI with the signed-in user's information
                            if(!checkIfEmailVerified()) return;
                            loadActivityAndClearStack(EmailLogInActivity.this,
                                    MobileVerificationActivity.class);
                           // saveProfileStatus();
                        } else {
                            // If sign in fails, display a message to the user.
                            setProgressBar(EmailLogInActivity.this,false, "");
                            Toast.makeText(EmailLogInActivity.this, task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

   /* private void saveProfileStatus() {
        initializeSharedPrefs(this);
        editor.putInt("profileStatus", 4);
        editor.apply();
    }*/

    public String getStringFromEditText(int id)
    {
        EditText et = findViewById(id);
        return et.getText().toString();
    }

    private boolean checkIfEmailVerified()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user.isEmailVerified())
        {
            // user is verified, so you can finish this activity or send user to activity which you want.
            Toast.makeText(this, "Successfully logged in", Toast.LENGTH_SHORT).show();
            return true;
        }
        else
        {
            // email is not verified, so just prompt the message to the user and restart this activity.
            // NOTE: don't forget to log out the user.
            FirebaseAuth.getInstance().signOut();
            showToast(this, "Email not verified");
            Toast.makeText(this, "Kindly verify email sent to your registered email id", Toast.LENGTH_LONG)
                    .show();
            return false;
            //restart this activity

        }
    }

    public void onForgotPassword(View view)
    {
        loadActivity(this, ForgotPasswordActivity.class);
    }

}
