package com.anekvurna.pingme;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import static com.anekvurna.pingme.SanathUtilities.*;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.zip.Inflater;

//TODO: Implement in DRY fashion
public class BasicProfileFragment extends Fragment {

    private View inflatedView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        inflatedView = inflater.inflate(R.layout.fragment_basic_profile, container, false);
        TextView textView = inflatedView.findViewById(R.id.view_name);
        textView.setText("sanath");
        return inflatedView;

    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        getProfileDetails();
    }

    TextView getTextView(int id)
    {
        return inflatedView.findViewById(id);
    }

    ImageView getImageView(int id)
    {
        return inflatedView.findViewById(id);
    }

    void getProfileDetails()
    {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("driverProfiles").child(currentUser.getUid()).child("basic");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                BasicProfile basicProfile = dataSnapshot.getValue(BasicProfile.class);
                setProfileDetails(basicProfile);
                loadUserImage();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                showToast("Error loading data");
            }
        });
    }

    private void loadUserImage() {
        if(getContext() == null) {showToast("Failed to load image"); return;}
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("driverProfiles").child(currentUser.getUid()).child("driverImage.jpg");
        Glide.with(getContext()).using(new FirebaseImageLoader()).load(storageReference).signature(new StringSignature(String.valueOf(System.currentTimeMillis()))).into(getImageView(R.id.view_image));
    }

    private void setProfileDetails(BasicProfile basicProfile) {
        getTextView(R.id.view_name).setText(basicProfile.getName());
       /* getTextView(R.id.view_line1).setText(basicProfile.getAddressLine1());
        getTextView(R.id.view_line2).setText(basicProfile.getAddressLine2());
        getTextView(R.id.view_city).setText(basicProfile.getCity());*/

        String[] states = getResources().getStringArray(R.array.india_states);
       /* getTextView(R.id.view_state).setText(states[basicProfile.getState()]);

        getTextView(R.id.view_pin_code).setText(basicProfile.getPinCode());*/
        getTextView(R.id.view_mobile).setText(currentUser.getPhoneNumber());
        getTextView(R.id.view_email).setText(basicProfile.getEmail());
        getTextView(R.id.view_alternate).setText(basicProfile.getAlternateNumber());
        getTextView(R.id.view_landline).setText(basicProfile.getLandline());

    }

    void showToast(String message)
    {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

}
