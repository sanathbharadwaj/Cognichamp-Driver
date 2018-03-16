package com.anekvurna.cognichampdriver;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import static com.anekvurna.cognichampdriver.SanathUtilities.*;

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
        getTextView(R.id.view_mobile).setText(currentUser.getPhoneNumber());
        getTextView(R.id.view_email).setText(basicProfile.getEmail());
        if(basicProfile.getAlternateNumber() == null || basicProfile.getAlternateNumber().equals(""))
            getTextView(R.id.view_alternate).setText(R.string.empty_dash);
        else
            getTextView(R.id.view_alternate).setText(basicProfile.getAlternateNumber());
        String landLineText = basicProfile.getStdCode()+ "-" + basicProfile.getLandline();
        if(basicProfile.getStdCode().equals("") && basicProfile.getLandline().equals(""))
            getTextView(R.id.view_landline).setText(R.string.empty_dash);
        else
            getTextView(R.id.view_landline).setText(landLineText);
    }

    void showToast(String message)
    {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

}
