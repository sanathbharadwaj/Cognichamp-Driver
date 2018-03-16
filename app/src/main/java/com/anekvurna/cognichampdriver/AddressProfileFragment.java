package com.anekvurna.cognichampdriver;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import static com.anekvurna.cognichampdriver.SanathUtilities.*;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

//TODO: Implement in DRY fashion
public class AddressProfileFragment extends Fragment {

    private View inflatedView;
    private AddressProfile addressProfile;
    public View rootView;

    public AddressProfileFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        inflatedView = inflater.inflate(R.layout.fragment_address_profile, container, false);
        //TextView textView = inflatedView.findViewById(R.id.view_name);
        //textView.setText("My Profile");
        return inflatedView;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeSharedPrefs(getContext());
        boolean makeGone = preferences.getBoolean("line2Gone", false);
        rootView = getView();
        if(makeGone)
            rootView.findViewById(R.id.view_line2).setVisibility(View.GONE);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getProfileDetails();
    }

    TextView getTextView(int id) {
        return inflatedView.findViewById(id);
    }

    void getProfileDetails() {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("driverProfiles").child(currentUser.getUid()).child("address");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                addressProfile = dataSnapshot.getValue(AddressProfile.class);
                setProfileDetails(addressProfile);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                showToast("Error loading data");
            }
        });
    }

   /* private void loadUserImage() {
        if (getContext() == null) {
            showToast("Failed to load image");
            return;
        }
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("driverProfiles").child(currentUser.getUid()).child("driverImage.jpg");
        Glide.with(getContext()).using(new FirebaseImageLoader()).load(storageReference).signature(new StringSignature(String.valueOf(System.currentTimeMillis()))).into(getImageView(R.id.view_image));
    }
*/
    private void setProfileDetails(AddressProfile addressProfile) {
        getTextView(R.id.view_line1).setText(addressProfile.getAddressLine1());
        getTextView(R.id.view_line2).setText(addressProfile.getAddressLine2());
        TextView line2 = getTextView(R.id.view_line2);
        if(addressProfile.getAddressLine2() == null || addressProfile.getAddressLine2().equals("")) {
            rootView.findViewById(R.id.view_line2).setVisibility(View.GONE);
            editor.putBoolean("line2Gone", true);
        }
        else
        {
            rootView.findViewById(R.id.view_line2).setVisibility(View.VISIBLE);
            line2.setText(addressProfile.getAddressLine2());
            editor.remove("line2Gone");
        }
        editor.apply();
        getTextView(R.id.view_city).setText(addressProfile.getCity());
        String[] states = getResources().getStringArray(R.array.india_states);
        getTextView(R.id.view_state).setText(states[addressProfile.getState()]);
        getTextView(R.id.view_pin_code).setText(addressProfile.getPinCode());
    }

    void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

}
