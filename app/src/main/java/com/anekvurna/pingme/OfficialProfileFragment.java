package com.anekvurna.pingme;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import static com.anekvurna.pingme.SanathUtilities.currentUser;


/**
 * A simple {@link Fragment} subclass.
 */
public class OfficialProfileFragment extends Fragment {

    private View inflatedView;


    public OfficialProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        inflatedView =  inflater.inflate(R.layout.fragment_official_profile, container, false);
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
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("driverProfiles").child(currentUser.getUid()).child("official");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                OfficialProfile officialProfile = dataSnapshot.getValue(OfficialProfile.class);
                setProfileDetails(officialProfile);
                loadImages();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                showToast("Error loading data");
            }
        });
    }

    private void loadImages() {
        if(getContext() == null) {showToast("Failed to load image"); return;}
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("driverProfiles")
                .child(currentUser.getUid());
        Glide.with(getContext()).using(new FirebaseImageLoader()).load(storageReference.child("driverLicence.jpg"))
                .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                .into(getImageView(R.id.view_licence_image));
        Glide.with(getContext()).using(new FirebaseImageLoader()).load(storageReference.child(getString(R.string.back_licence_filename)))
                .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                .into(getImageView(R.id.view_licence_back_image));
        Glide.with(getContext()).using(new FirebaseImageLoader()).load(storageReference.child("driverVoterId.jpg"))
                .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                .into(getImageView(R.id.view_voterid_image));
        Glide.with(getContext()).using(new FirebaseImageLoader()).load(storageReference.child(getString(R.string.back_voter_filename)))
                .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                .into(getImageView(R.id.view_voterid_back_image));

    }

    private void setProfileDetails(OfficialProfile officialProfile) {
       getTextView(R.id.view_licence).setText(officialProfile.getLicenceNumber());
       getTextView(R.id.view_voterid).setText(officialProfile.getVoterId());
    }

    void showToast(String message)
    {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

}
