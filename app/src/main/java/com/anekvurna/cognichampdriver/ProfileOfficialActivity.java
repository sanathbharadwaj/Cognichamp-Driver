package com.anekvurna.cognichampdriver;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.content.res.AppCompatResources;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
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
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.UUID;

import static com.anekvurna.cognichampdriver.SanathUtilities.fromBitmapToByteArray;
import static com.anekvurna.cognichampdriver.SanathUtilities.loadActivity;
import static com.anekvurna.cognichampdriver.SanathUtilities.setProgressBar;

public class ProfileOfficialActivity extends ProfileParentActivity {


    Context context;
    static final int PICK_IMAGE = 2;
    byte[] byteArray;
    private DatabaseReference databaseReference;
    Bitmap driverLicence, licenceBack, voterBack, driverVoterId;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    boolean uVoterId = true, uLicence = true, uBackVoterId = true, uBackLicence = false, isPreviousProfile = false;
    private Uri uri;
    EditText voterId, licence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_official_profile);
        databaseReference = FirebaseDatabase.getInstance().getReference("driverProfiles").child(currentUser.getUid()).child("official");
        context = this;
        checkForPreviousProfile();
        voterId = getEditText(R.id.profile_number_voter_id);
        licence = getEditText(R.id.profile_number_driving_licence);
        showEditTextsAsMandatory(voterId, licence);
        setDrawableLefts();
    }

    private void setDrawableLefts() {
        voterId.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources
                .getDrawable(this, R.drawable.ic_receipt_black_24dp), null, null, null);
        licence.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources
                .getDrawable(this, R.drawable.ic_local_taxi_black_24dp), null, null, null);
    }



    public void onSaveOfficial(View view)
    {
        totalImagesUploaded = 0;
        if(!isValid())
        {
            return;
        }
        if(uVoterId)
        uploadImage(R.id.choose_voterid, driverVoterId, "driverVoterId.jpg");
        if(uBackVoterId)
            uploadImage(R.id.choose_voter_back, voterBack, getString(R.string.back_voter_filename));
        if(uLicence)
            uploadImage(R.id.choose_driving_licence, driverLicence, "driverLicence.jpg");
        if(uBackLicence)
            uploadImage(R.id.choose_licence_back, licenceBack, getString(R.string.back_licence_filename));
        if(numberOfUploads() == 0)
            register();
    }

    boolean isValid()
    {
        boolean valid = true;
        final int SIZE_OF_VOTER_ID = 10;
        String voterId = getEditText(R.id.profile_number_voter_id).getText().toString();
        if(voterId.equals("") || voterId.length() != SIZE_OF_VOTER_ID)
        {
            showToast("Please enter valid voter id");
            valid = false;
        }
        if(getEditText(R.id.profile_number_driving_licence).getText().toString().equals(""))
        {
            showToast("Please enter valid driving licence number");
            valid = false;
        }

        if(( driverLicence == null || driverVoterId == null || voterBack == null) && !isPreviousProfile)
        {
            showToast("Please choose all the images");
            valid = false;
        }

        return valid;
    }




    private void assignBitmap(Bitmap resized) {
        switch (buttonId)
        {
            case R.id.profile_driver_voter_id : driverVoterId = resized;
                setSelectedText(R.id.choose_voterid, "Voter Id Front Selected");uVoterId = true;break;
            case R.id.profile_driver_licence : driverLicence = resized;
                setSelectedText(R.id.choose_driving_licence, "Driving License Front Selected");uLicence = true;break;
            case R.id.profile_voter_back : voterBack = resized;
                setSelectedText(R.id.choose_voter_back, "Voter Id Back Selected");uBackVoterId = true;break;
            case R.id.profile_licence_back : licenceBack = resized;
                setSelectedText(R.id.choose_licence_back, "Driving License Back Selected");uBackLicence = true;break;
            default: showToast("Image fetching failed try again"); break;
        }
    }

    private void setSelectedText(int id, String s) {
        getTextView(id).setText(s);
    }

    private void checkForPreviousProfile() {
        DatabaseReference checkRef = FirebaseDatabase.getInstance().getReference("driverProfiles").child(currentUser.getUid());
        checkRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("official"))
                {
                    isPreviousProfile = true;
                    uVoterId = false;
                    uLicence = false;
                    uBackLicence = false;
                    uBackVoterId = false;
                    populateData(dataSnapshot.child("official").getValue(OfficialProfile.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
         if(requestCode == PHOTO_REQUEST_CUT) {
             try {
                 if (data != null) {
                     File file = new File(dir.getAbsolutePath(), photo_file_name);
                     Bitmap imageBitmap = getImageBitmap(file);
                     if(imageBitmap == null)
                     {
                         Bundle extras = data.getExtras();
                         // get the cropped bitmap
                         if(extras != null) {
                             Bitmap selectedBitmap = extras.getParcelable("data");
                             if (selectedBitmap == null) {
                                 showToast("Error cropping image please choose another cropping tool");
                                 return;
                             }
                             Bitmap resized = Bitmap.createScaledBitmap(selectedBitmap, DESIRED_WIDTH, DESIRED_HEIGHT, true);
                             assignBitmap(resized);
                         }
                         else
                         showToast("Error cropping image please choose another cropping tool");
                     }
                     else
                     assignBitmap(imageBitmap);
                 } else {
                     showToast("An error occurred when cropping image");
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }

    }

    private void populateData(OfficialProfile officialProfile) {
        getEditText(R.id.profile_number_driving_licence).setText(officialProfile.getLicenceNumber());
        getEditText(R.id.profile_number_voter_id).setText(officialProfile.getVoterId());
    }

    private void uploadImage(final int textId, Bitmap imageBitmap, String name) {
        setUploadingText(textId);
        byte[] data = fromBitmapToByteArray(imageBitmap);
        storageRef = FirebaseStorage.getInstance().getReference().child("driverProfiles").child(currentUser.getUid())
                .child(name);
        UploadTask uploadTask = storageRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showToast(e.getMessage());
            }
        });

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                setUploadedText(textId);
                totalImagesUploaded++;
                if(totalImagesUploaded == numberOfUploads())
                    register();
            }
        });
    }

    int numberOfUploads()
    {
        int count = 0;
        if(uVoterId)count++;
        if(uLicence)count++;
        if(uBackVoterId)count++;
        if(uBackLicence)count++;
        return count;
    }

    void register()
    {
        setProgressBar(this,true, "Saving...");
        String licenceNumber = getEditText(R.id.profile_number_driving_licence).getText().toString(),
                VoterId = getEditText(R.id.profile_number_voter_id).getText().toString();
        OfficialProfile officialProfile = new OfficialProfile(licenceNumber, VoterId);

        databaseReference.setValue(officialProfile, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                setProgressBar(ProfileOfficialActivity.this,false, "dummy");
                if(databaseError!=null){
                    showToast("Error saving database!");
                    return;
                }
                showToast("Save successful!");
                SharedPreferences.Editor editor = getSharedPreferences("com.anekvurna.cognichampdriver", MODE_PRIVATE).edit();
                Intent intent = getIntent();
                if(!intent.getBooleanExtra(getString(R.string.is_editing), false)) {
                    editor.putInt("profileStatus", 3);
                    editor.commit();
                    setProfileStatus(3);
                    loadActivity(context, ProfileCarActivity.class);
                }
                else
                {
                    finish();
                }
            }
        });
    }


}
