package com.anekvurna.cognichampdriver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.content.res.AppCompatResources;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.anekvurna.cognichampdriver.SanathUtilities.editor;
import static com.anekvurna.cognichampdriver.SanathUtilities.initializeSharedPrefs;
import static com.anekvurna.cognichampdriver.SanathUtilities.loadActivity;
import static com.anekvurna.cognichampdriver.SanathUtilities.setProgressBar;

public class ProfileAddressActivity extends AppCompatActivity {

    DatabaseReference databaseReference;
    FirebaseAuth auth;
    FirebaseUser currentUser;
    EditText line1,line2, city, pincode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_address);
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        checkForPreviousProfile();
        line1 = getEditText(R.id.profile_address_line1);
        line2 = getEditText(R.id.profile_address_line2);
        city = getEditText(R.id.profile_address_city);
        pincode = getEditText(R.id.profile_address_pinCode);
        showEditTextsAsMandatory(line1, city, pincode);
        setDrawableLefts();
    }


    private void setDrawableLefts() {
        line1.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources
                .getDrawable(this, R.drawable.ic_my_location_black_24dp), null, null, null);
        line2.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources
                .getDrawable(this, R.drawable.ic_location_on_black_24dp), null, null, null);
        city.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources
                .getDrawable(this, R.drawable.ic_location_city_black_24dp), null, null, null);
        pincode.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources
                .getDrawable(this, R.drawable.ic_fiber_pin_black_24dp), null, null, null);
    }

    public void showEditTextsAsMandatory ( EditText... ets )
    {
        for ( EditText et : ets )
        {
            String hint = et.getHint ().toString ();

            et.setHint ( Html.fromHtml ( "<font color=\"#ff0000\">" + "* " + "</font>" + hint ) );
        }
    }

    private void checkForPreviousProfile() {
        DatabaseReference checkRef = FirebaseDatabase.getInstance().getReference("driverProfiles").child(currentUser.getUid());
        checkRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("address"))
                {
                    populateData(dataSnapshot.child("address").getValue(AddressProfile.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void populateData(AddressProfile addressProfile) {
       // String[] states = getResources().getStringArray(R.array.india_states);
        getEditText(R.id.profile_address_line1).setText(addressProfile.getAddressLine1());
        getEditText(R.id.profile_address_line2).setText(addressProfile.getAddressLine2());
        getEditText(R.id.profile_address_city).setText(addressProfile.getCity());
        Spinner spinner = findViewById(R.id.profile_state);
        spinner.setSelection(addressProfile.getState());
        getEditText(R.id.profile_address_pinCode).setText(addressProfile.getPinCode());



    }

    public void onSaveAddress(View view)
    {
        setProgressBar(this,true, "Saving...");
        if(!isValid()) return;
        final Context context = this;
        Spinner spinner = findViewById(R.id.profile_state);
        AddressProfile addressProfile = new AddressProfile();
        addressProfile.setAddressLine1(getEditText(R.id.profile_address_line1).getText().toString());
        addressProfile.setAddressLine2(getEditText(R.id.profile_address_line2).getText().toString());
        addressProfile.setCity(getEditText(R.id.profile_address_city).getText().toString());
        addressProfile.setState(spinner.getSelectedItemPosition());
        addressProfile.setPinCode(getEditText(R.id.profile_address_pinCode).getText().toString());

        String line2Text = addressProfile.getAddressLine2();
        initializeSharedPrefs(this);
        if(line2Text == null || line2Text.equals(""))
        {
            editor.putBoolean("line2Gone", true);
        }
        else
        {
            editor.remove("line2Gone");
        }
        editor.apply();
        databaseReference = FirebaseDatabase.getInstance().getReference("driverProfiles").child(currentUser.getUid()).child("address");
        databaseReference.setValue(addressProfile, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                setProgressBar(ProfileAddressActivity.this,false, "dummy");
                if(databaseError!=null){
                    showToast("Error saving data!");
                    showToast(databaseError.getMessage());
                    return;
                }
                showToast("Save successful!");
                initializeSharedPrefs(ProfileAddressActivity.this);
                Intent intent  = getIntent();
                if(!intent.getBooleanExtra(getString(R.string.is_editing), false)) {
                    editor.putInt("profileStatus", 2);
                    editor.commit();
                    setProfileStatus(2);
                    loadActivity(context, ProfileOfficialActivity.class);
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
        boolean valid = true;
        if(getEditText(R.id.profile_address_line1).getText().toString().equals("") )
        {
            showToast("Please enter valid address line 1");
            valid = false;
        }

        if(getEditText(R.id.profile_address_city).getText().toString().equals("") )
        {
            showToast("Please enter valid address city");
            valid = false;
        }

        Spinner spinner = findViewById(R.id.profile_state);
        if(spinner.getSelectedItemPosition() == 0 )
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

        return valid;
    }

    void setProfileStatus(int i)
    {
        DatabaseReference profileStatusReference = FirebaseDatabase.getInstance().getReference("users")
                .child(currentUser.getUid()).child("profileStatus");
        profileStatusReference.setValue(i);
    }

    public EditText getEditText(int id) {
        return (EditText) findViewById(id);
    }

    void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}
