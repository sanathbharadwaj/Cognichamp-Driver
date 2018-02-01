package com.anekvurna.pingme;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.anekvurna.pingme.SanathUtilities.loadActivityAndFinish;

public class ProfileBasicActivity extends AppCompatActivity {

    DatabaseReference databaseReference;
    FirebaseAuth auth;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_basic);
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        initializeEditTexts();
        checkForPreviousProfile();
    }

    private void initializeEditTexts() {
        //TODO: store edit texts to temporary
    }

    private void checkForPreviousProfile() {
        DatabaseReference checkRef = FirebaseDatabase.getInstance().getReference("driverProfiles").child(currentUser.getUid());
        checkRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("basic"))
                {
                    populateData(dataSnapshot.child("basic").getValue(BasicProfile.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void populateData(BasicProfile basicProfile) {
        String[] states = getResources().getStringArray(R.array.india_states);
        getEditText(R.id.profile_name).setText(basicProfile.getName());
        getEditText(R.id.profile_address_line1).setText(basicProfile.getAddressLine1());
        getEditText(R.id.profile_address_line2).setText(basicProfile.getAddressLine2());
        getEditText(R.id.profile_address_city).setText(basicProfile.getCity());
        Spinner spinner = findViewById(R.id.profile_state);
        spinner.setSelection(basicProfile.getState());
        getEditText(R.id.profile_address_pinCode).setText(basicProfile.getPinCode());
        getEditText(R.id.profile_email).setText(basicProfile.getEmail());
        getEditText(R.id.profile_alternate_mobile).setText(basicProfile.getAlternateNumber());
        getEditText(R.id.profile_std_code).setText(basicProfile.getStdCode());
        getEditText(R.id.profile_landline).setText(basicProfile.getLandline());

        StorageReference storageReference = FirebaseStorage.getInstance().getReference("driverProfiles")
                .child(currentUser.getUid());


    }

    public void onSaveBasic(View view)
    {
        if(!isValid()) return;
        final Context context = this;
        Spinner spinner = findViewById(R.id.profile_state);
        BasicProfile basicProfile = new BasicProfile();
        basicProfile.setName(getEditText(R.id.profile_name).getText().toString());
        basicProfile.setAddressLine1(getEditText(R.id.profile_address_line1).getText().toString());
        basicProfile.setAddressLine2(getEditText(R.id.profile_address_line2).getText().toString());
        basicProfile.setCity(getEditText(R.id.profile_address_city).getText().toString());
        basicProfile.setState(spinner.getSelectedItemPosition());
        basicProfile.setPinCode(getEditText(R.id.profile_address_pinCode).getText().toString());
        basicProfile.setEmail(getEditText(R.id.profile_email).getText().toString());
        basicProfile.setAlternateNumber(getEditText(R.id.profile_alternate_mobile).getText().toString());
        basicProfile.setLandline(getEditText(R.id.profile_landline).getText().toString());
        basicProfile.setStdCode(getEditText(R.id.profile_std_code).getText().toString());

        databaseReference = FirebaseDatabase.getInstance().getReference("driverProfiles").child(currentUser.getUid()).child("basic");
        databaseReference.setValue(basicProfile, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(databaseError!=null){
                    showToast("Error saving data!");
                    showToast(databaseError.getMessage());
                    return;
                }
                showToast("Save successful!");
                SharedPreferences.Editor editor = getSharedPreferences("com.anekvurna.pingme", MODE_PRIVATE).edit();
                Intent intent  = getIntent();
                if(!intent.getBooleanExtra(getString(R.string.is_editing), false)) {
                    editor.putInt("profileStatus", 1);
                    editor.apply();
                    loadActivityAndFinish(context, ProfileOfficialActivity.class);
                }
                else
                {
                    finish();
                }
            }
        });
    }

    boolean isValid()
    {
        final int MOBILE_LIMIT = 10;
        boolean valid = true;
        if(getEditText(R.id.profile_name).getText().toString().equals(""))
        {
            showToast("Please enter valid name");
            valid = false;
        }
        if(getEditText(R.id.profile_address_line1).getText().toString().equals("") )
        {
            showToast("Please enter valid address line 1");
            valid = false;
        }

        if(getEditText(R.id.profile_address_line2).getText().toString().equals("") )
        {
            showToast("Please enter valid address line 2");
            valid = false;
        }

        if(getEditText(R.id.profile_address_city).getText().toString().equals("") )
        {
            showToast("Please enter valid address city");
            valid = false;
        }

        Spinner spinner = findViewById(R.id.profile_state);
        if(spinner.getSelectedItemPosition() == -1 )
        {
            showToast("Please select a state");
            valid = false;
        }

        String pinCode = getEditText(R.id.profile_address_pinCode).getText().toString();
        if( pinCode.equals("")|| pinCode.length() < 6)
        {
            showToast("Please enter valid pin code");
            valid = false;
        }

        String email = getEditText(R.id.profile_email).getText().toString();
        if(email.equals("") || !emailValidator(email))
        {
            showToast("Please enter valid email");
            valid = false;
        }
        String alternateMobile = getEditText(R.id.profile_alternate_mobile).getText().toString();
        if(alternateMobile.equals("") || alternateMobile.length() != MOBILE_LIMIT )
        {
            showToast("Please enter valid alternate mobile");
            valid = false;
        }
        String stdCode, landline;
        stdCode = getEditText(R.id.profile_std_code).getText().toString();
        landline = getEditText(R.id.profile_landline).getText().toString();
        if(stdCode.equals("") || landline.equals("") || stdCode.length() + landline.length() != MOBILE_LIMIT)
        {
            showToast("Please enter valid landline");
            valid = false;
        }

        return valid;
    }

    public EditText getEditText(int id) {
        return (EditText) findViewById(id);
    }

    void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public boolean emailValidator(String email)
    {
        Pattern pattern;
        Matcher matcher;
        final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();
    }

}
