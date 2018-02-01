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
public class CarProfileFragment extends Fragment {


    private View inflatedView;

    public CarProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        inflatedView = inflater.inflate(R.layout.fragment_car_profile, container, false);
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
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("driverProfiles").child(currentUser.getUid()).child("vehicle");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                CarProfile carProfile = dataSnapshot.getValue(CarProfile.class);
                setProfileDetails(carProfile);
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
        Glide.with(getContext()).using(new FirebaseImageLoader()).load(storageReference.child("backImage.jpg"))
                .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                .into(getImageView(R.id.view_car_back));
        Glide.with(getContext()).using(new FirebaseImageLoader()).load(storageReference.child("frontImage.jpg"))
                .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                .into(getImageView(R.id.view_car_front));
        Glide.with(getContext()).using(new FirebaseImageLoader()).load(storageReference.child("numberPlate.jpg"))
                .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                .into(getImageView(R.id.view_number_plate));

    }

    private void setProfileDetails(CarProfile carProfile) {
        getTextView(R.id.view_vehicle_name).setText(carProfile.getVehicleName());
        getTextView(R.id.view_vehicle_number).setText(carProfile.getVehicleNumber());
    }

    void showToast(String message)
    {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

}
