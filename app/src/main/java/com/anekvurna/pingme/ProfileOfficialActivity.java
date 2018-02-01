package com.anekvurna.pingme;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.parse.ParseFile;

import java.io.File;

import static com.anekvurna.pingme.SanathUtilities.fromBitmapToByteArray;
import static com.anekvurna.pingme.SanathUtilities.loadActivityAndFinish;

public class ProfileOfficialActivity extends AppCompatActivity {


    Context context;
    static final int PICK_IMAGE = 2;
    byte[] byteArray;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseReference;
    final int PIC_CROP = 1;
    final int DESIRED_WIDTH = 170, DESIRED_HEIGHT = 170;
    private int buttonId;
    Bitmap driverImage, driverLicence, driverVoterId;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    private int totalImagesUploaded;
    boolean uDriverImage = true, uVoterId = true, uLicence = true, isPreviousProfile = false;
    private Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_official_profile);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("driverProfiles").child(currentUser.getUid()).child("official");
        context = this;
        checkForPreviousProfile();
    }


    public void onSaveOfficial(View view)
    {
        totalImagesUploaded = 0;
        if(!isValid())
        {
            return;
        }
        if(uDriverImage)
        uploadImage(R.id.choose_driver_image, driverImage, "driverImage.jpg");
        if(uVoterId)
        uploadImage(R.id.choose_voterid, driverVoterId, "driverVoterId.jpg");
        if(uLicence)
        uploadImage(R.id.choose_driving_licence, driverLicence, "driverLicence.jpg");
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

        if((driverImage == null || driverLicence == null || driverVoterId == null) && !isPreviousProfile)
        {
            showToast("Please choose all the images");
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

    public void chooseImage(View view) {
        buttonId = view.getId();
        showAlertDialogButtonClicked(view);
    }

    public void showAlertDialogButtonClicked(View view) {

        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose an animal");

        // add a list
        String[] animals = {"Gallery", "Camera"};
        builder.setItems(animals, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE); break;
                    case 1: clickImageFromCamera(); break;
                }
            }
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void clickImageFromCamera() {

        Intent camIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        File file = new File(Environment.getExternalStorageDirectory(),
                "file" + String.valueOf(System.currentTimeMillis()) + ".jpg");
        uri = Uri.fromFile(file);

        camIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);

        camIntent.putExtra("return-data", true);

        startActivityForResult(camIntent, 3);

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {

            Uri pickedImage = data.getData();
            startCropIntent(pickedImage);
            return;
        }
        //TODO: Remove asserts
        if (requestCode == PIC_CROP) {
            if (data != null) {
                // get the returned data
                Bundle extras = data.getExtras();
                // get the cropped bitmap
                assert extras != null;
                Bitmap selectedBitmap = extras.getParcelable("data");
                assert selectedBitmap != null;
                Bitmap resized = Bitmap.createScaledBitmap(selectedBitmap, DESIRED_WIDTH, DESIRED_HEIGHT, true);
                assignBitmap(resized);
            }
        }

        if(requestCode == 3)
        {
            startCropIntent(uri);
        }
    }

    private void assignBitmap(Bitmap resized) {
        switch (buttonId)
        {
            case R.id.profile_driver_pic : driverImage = resized;setNullText(R.id.choose_driver_image);uDriverImage = true; break;
            case R.id.profile_driver_voter_id : driverVoterId = resized;setNullText(R.id.choose_voterid);uVoterId = true;break;
            case R.id.profile_driver_licence : driverLicence = resized;setNullText(R.id.choose_driving_licence);uLicence = true;break;
            default: showToast("Image fetching failed try again"); break;
        }
    }

    private void checkForPreviousProfile() {
        DatabaseReference checkRef = FirebaseDatabase.getInstance().getReference("driverProfiles").child(currentUser.getUid());
        checkRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("official"))
                {
                    isPreviousProfile = true;
                    uDriverImage = false;
                    uVoterId = false;
                    uLicence = false;
                    populateData(dataSnapshot.child("official").getValue(OfficialProfile.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
        if(uDriverImage) count++;
        if(uVoterId)count++;
        if(uLicence)count++;
        return count;
    }

    void register()
    {
        showToast("Saving profile");
        String licenceNumber = getEditText(R.id.profile_number_driving_licence).getText().toString(),
                VoterId = getEditText(R.id.profile_number_voter_id).getText().toString();
        OfficialProfile officialProfile = new OfficialProfile(licenceNumber, VoterId);

        databaseReference.setValue(officialProfile, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(databaseError!=null){
                    showToast("Error saving database!");
                    return;
                }
                showToast("Save successful!");
                SharedPreferences.Editor editor = getSharedPreferences("com.anekvurna.pingme", MODE_PRIVATE).edit();
                Intent intent = getIntent();
                if(!intent.getBooleanExtra(getString(R.string.is_editing), false)) {
                    editor.putInt("profileStatus", 2);
                    editor.apply();
                    loadActivityAndFinish(context, ProfileCarActivity.class);
                }
                else
                {
                    finish();
                }
            }
        });
    }

    void startCropIntent(Uri picUri)
    {
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            // indicate image type and Uri
            cropIntent.setDataAndType(picUri, "image/*");
            // set crop properties here
            cropIntent.putExtra("crop", true);
            // indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            // indicate output X and Y
            cropIntent.putExtra("outputX", 128);
            cropIntent.putExtra("outputY", 128);
            // retrieve data on return
            cropIntent.putExtra("return-data", true);
            // start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, PIC_CROP);
        }
        catch (ActivityNotFoundException notFound) {

            String errorMessage = "Whoops - your device doesn't support the crop action!";
            showToast(errorMessage);
        }
    }


    void setNullText(int id)
    {
        getTextView(id).setText("");
    }

    void setUploadingText(int id)
    {
        getTextView(id).setText(R.string.uploading);
    }

    void setUploadedText(int id)
    {
        getTextView(id).setText(R.string.uploaded);
    }

    TextView getTextView(int id)
    {
        return (TextView)findViewById(id);
    }

}
