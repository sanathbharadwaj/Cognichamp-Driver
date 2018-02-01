package com.anekvurna.pingme;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import static com.anekvurna.pingme.SanathUtilities.*;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfileCarActivity extends AppCompatActivity {

    Context context;
    static final int PICK_IMAGE = 2;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseReference;
    final int PIC_CROP = 1;
    final int DESIRED_WIDTH = 170, DESIRED_HEIGHT = 170;
    private int buttonId;
    Bitmap carFront, carBack, carNumberPlate;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    private int totalImagesUploaded;
    boolean uploadBack = true, uploadFront = true, uploadNumberPLate = true, isPreviousProfile = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_car);
        context = this;
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("driverProfiles").child(currentUser.getUid()).child("vehicle");
        checkForPreviousProfile();
    }

    public void onSaveCar(View view)
    {
        totalImagesUploaded = 0;
        if(!isValid())
        {
            return;
        }
        if(uploadFront)
        uploadImage(R.id.choose_front_image, carFront, "frontImage.jpg");
        if(uploadBack)
        uploadImage(R.id.choose_back_image, carBack, "backImage.jpg");
        if(uploadNumberPLate)
        uploadImage(R.id.choose_number_plate, carNumberPlate, "numberPlate.jpg");
        if(numberOfUploads()==0)
            register();
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

    private void checkForPreviousProfile() {
        DatabaseReference checkRef = FirebaseDatabase.getInstance().getReference("driverProfiles").child(currentUser.getUid());
        checkRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("vehicle"))
                {
                    isPreviousProfile = true;
                    populateData(dataSnapshot.child("vehicle").getValue(CarProfile.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void populateData(CarProfile carProfile) {
        getEditText(R.id.profile_vehicle_name).setText(carProfile.getVehicleName());
        getEditText(R.id.profile_vehicle_number).setText(carProfile.getVehicleNumber());
        uploadBack = false; uploadFront = false; uploadNumberPLate = false;

    }

    int numberOfUploads()
    {
        int count = 0;
        if(uploadBack) count++;
        if(uploadFront)count++;
        if(uploadNumberPLate)count++;
        return count;
    }



    boolean isValid()
    {
        boolean valid = true;

        if(getEditText(R.id.profile_vehicle_number).getText().toString().equals(""))
        {
            showToast("Please enter valid vehicle number");
            valid = false;
        }

        if(getEditText(R.id.profile_vehicle_name).getText().toString().equals(""))
        {
            showToast("Please enter valid car name");
            valid = false;
        }

        if((carFront == null || carBack == null || carNumberPlate == null) && !isPreviousProfile)
        {
            showToast("Please choose all the images");
            valid = false;
        }

        return valid;
    }

    void register() {
        showToast("Saving profile");
        String vehicleNumber = getEditText(R.id.profile_vehicle_number).getText().toString(),
        vehicleName= getEditText(R.id.profile_vehicle_name).getText().toString();
        CarProfile carProfile = new CarProfile(vehicleName,vehicleNumber);

        databaseReference.setValue(carProfile, new DatabaseReference.CompletionListener() {
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
                    editor.putInt("profileStatus", 3);
                    editor.apply();
                    loadActivityAndFinish(context, ViewTabbedActivity.class);
                }
                else
                    finish();


            }
        });

    }

    public EditText getEditText(int id) {
        return (EditText) findViewById(id);
    }

    void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void chooseImage(View view) {
        buttonId = view.getId();
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
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
    }

    private void assignBitmap(Bitmap resized) {
        switch (buttonId)
        {
            case R.id.profile_car_front : carFront = resized;setNullText(R.id.choose_front_image);uploadFront = true;break;
            case R.id.profile_car_back : carBack = resized;setNullText(R.id.choose_back_image);uploadBack = true;break;
            case R.id.profile_car_plate : carNumberPlate = resized;setNullText(R.id.choose_number_plate);uploadNumberPLate = true;break;
            default: showToast("Image fetching failed try again"); break;
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

    TextView getTextView(int id)
    {
        return (TextView)findViewById(id);
    }
}
