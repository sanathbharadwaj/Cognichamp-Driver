package com.anekvurna.pingme;

import android.*;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
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

import static com.anekvurna.pingme.SanathUtilities.fromBitmapToByteArray;
import static com.anekvurna.pingme.SanathUtilities.loadActivity;
import static com.anekvurna.pingme.SanathUtilities.loadActivityAndFinish;
import static com.anekvurna.pingme.SanathUtilities.setProgressBar;

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
    Bitmap driverLicence, licenceBack, voterBack, driverVoterId;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    private int totalImagesUploaded;
    boolean uVoterId = true, uLicence = true, uBackVoterId = true, uBackLicence = false, isPreviousProfile = false;
    private Uri uri;
    EditText voterId, licence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_official_profile);
        testingFunction();

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("driverProfiles").child(currentUser.getUid()).child("official");
        context = this;
        checkForPreviousProfile();
        voterId = getEditText(R.id.profile_number_voter_id);
        licence = getEditText(R.id.profile_number_driving_licence);
        showEditTextsAsMandatory(voterId, licence);
    }

    private void testingFunction() {
        TextView test = getTextView(R.id.choose_voterid);

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
        builder.setTitle("Choose action");

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

        if(!isEnabledRuntimePermission())
            return;

        Intent camIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        File file = new File(Environment.getExternalStorageDirectory(),
                "file" + String.valueOf(System.currentTimeMillis()) + ".jpg");
        uri = Uri.fromFile(file);

        camIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);

        camIntent.putExtra("return-data", true);

        startActivityForResult(camIntent, 3);

    }

    boolean isEnabledRuntimePermission()
    {
        if(Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        2);
                return false;
            }
        }

        return true;
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {

            Uri pickedImage = data.getData();
            startCropIntent(pickedImage);
            return;
        }
        //TODO: Remove asserts
        if (requestCode == PIC_CROP && resultCode == RESULT_OK) {
            if (data != null) {
                // get the returned data
                Bundle extras = data.getExtras();
                // get the cropped bitmap
                if(extras == null) return;
                Bitmap selectedBitmap = extras.getParcelable("data");
                if(selectedBitmap == null)
                    return;
                Bitmap resized = Bitmap.createScaledBitmap(selectedBitmap, DESIRED_WIDTH, DESIRED_HEIGHT, true);
                assignBitmap(resized);
            }
        }

        if(requestCode == 3)
        {
            startCropIntent(uri);
        }
    }

    public void showEditTextsAsMandatory ( EditText... ets )
    {
        for ( EditText et : ets )
        {
            String hint = et.getHint ().toString ();

            et.setHint ( Html.fromHtml ( "<font color=\"#ff0000\">" + "* " + "</font>" + hint ) );
        }
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
                SharedPreferences.Editor editor = getSharedPreferences("com.anekvurna.pingme", MODE_PRIVATE).edit();
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

    void setProfileStatus(int i)
    {
        DatabaseReference profileStatusReference = FirebaseDatabase.getInstance().getReference("users")
                .child(currentUser.getUid()).child("profileStatus");
        profileStatusReference.setValue(i);
    }


    void setSelectedText(int id)
    {
        getTextView(id).setText(R.string.image_selected);
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
