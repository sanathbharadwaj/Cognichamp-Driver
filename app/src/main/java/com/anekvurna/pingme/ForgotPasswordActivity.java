package com.anekvurna.pingme;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import static com.anekvurna.pingme.SanathUtilities.*;

public class ForgotPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
    }

    public void sendResetEmail(View view)
    {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        EditText editText = findViewById(R.id.reset_email);
        String emailAddress = editText.getText().toString();
        setProgressBar(this, true, "Sending password reset mail...");

        auth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        setProgressBar(ForgotPasswordActivity.this, false, "dummy");
                        if (task.isSuccessful()) {
                            showToast(ForgotPasswordActivity.this, "A password reset email has been sent");
                        }
                        else
                        {
                            showToast(ForgotPasswordActivity.this, task.getException().getMessage());
                        }
                    }
                });
    }
}
