package com.anekvurna.cognichampdriver;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.anekvurna.cognichampdriver.SanathUtilities.*;

public class ResetPasswordActivity extends DrawerActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        initializeCurrentUser();
    }

    public void onUpdate(View view)
    {
        showToast(this, "Verifying...");
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("users").child(currentUser.getUid()).child("password");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String oldPassword = dataSnapshot.getValue(String.class);
                String enteredOldPassword = getStringFromEditText(R.id.old_password_et);
                if(!verifyPassword(oldPassword, enteredOldPassword))
                {
                    showToast(ResetPasswordActivity.this, "Wrong old password");
                    return;
                }

                String newPassword = getStringFromEditText(R.id.new_password_et);
                String confirmedNewPassword = getStringFromEditText(R.id.confirm_new_password_et);
                if(!verifyPassword(newPassword, confirmedNewPassword))
                {
                    showToast(ResetPasswordActivity.this, "New passwords don't match");
                    return;
                }
                databaseReference.setValue(newPassword, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if(databaseError != null)
                        {
                            showToast(ResetPasswordActivity.this, databaseError.getMessage());
                            return;
                        }
                        showToast(ResetPasswordActivity.this,"Password updated successfully");
                        loadActivityAndFinish(ResetPasswordActivity.this, ViewTabbedActivity.class);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                showToast(ResetPasswordActivity.this, databaseError.getMessage());
            }
        });

    }

    private boolean verifyPassword(String p1, String p2) {
        return p1.equals(p2);
    }


    String getStringFromEditText(int id)
    {
        EditText editText = findViewById(id);
        return editText.getText().toString();
    }

}
