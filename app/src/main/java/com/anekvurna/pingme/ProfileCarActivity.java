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
import static com.anekvurna.pingme.SanathUtilities.*;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
    boolean uploadBack = true, uploadFront = true, uploadNumberPLate = true, uploadCarSide = false, isPreviousProfile = false;
    EditText number, name;
    private Bitmap carSide;
    private Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_car);
        context = this;
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("driverProfiles").child(currentUser.getUid()).child("vehicle");
        checkForPreviousProfile();
        number = getEditText(R.id.profile_vehicle_number);
        name = getEditText(R.id.profile_vehicle_name);
        showEditTextsAsMandatory(number, name);
    }

    public void showEditTextsAsMandatory ( EditText... ets )
    {
        for ( EditText et : ets )
        {
            String hint = et.getHint ().toString ();

            et.setHint ( Html.fromHtml ( "<font color=\"#ff0000\">" + "* " + "</font>" + hint ) );
        }
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
        if(uploadCarSide)
            uploadImage(R.id.choose_car_side, carSide, getString(R.string.car_side));
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

    private void setSelectedText(int id, String s) {
        getTextView(id).setText(s);
    }

    private void populateData(CarProfile carProfile) {
        getEditText(R.id.profile_vehicle_name).setText(carProfile.getVehicleName());
        getEditText(R.id.profile_vehicle_number).setText(carProfile.getVehicleNumber());
        uploadBack = false; uploadFront = false; uploadNumberPLate = false; uploadCarSide = false;

    }

    int numberOfUploads()
    {
        int count = 0;
        if(uploadBack) count++;
        if(uploadFront)count++;
        if(uploadNumberPLate)count++;
        if(uploadCarSide)count++;
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
        setProgressBar(this,true, "Saving...");
        String vehicleNumber = getEditText(R.id.profile_vehicle_number).getText().toString(),
        vehicleName= getEditText(R.id.profile_vehicle_name).getText().toString();
        CarProfile carProfile = new CarProfile(vehicleName,vehicleNumber);

        databaseReference.setValue(carProfile, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                setProgressBar(ProfileCarActivity.this,false, "dummy");
                if(databaseError!=null){
                    showToast("Error saving database!");
                    return;
                }
                showToast("Save successful!");
                SharedPreferences.Editor editor = getSharedPreferences("com.anekvurna.pingme", MODE_PRIVATE).edit();
                Intent intent = getIntent();
                if(!intent.getBooleanExtra(getString(R.string.is_editing), false)) {
                    editor.putInt("profileStatus", 4);
                    editor.commit();
                    //sendVerificationEmail();
                    setProfileStatus(4);
                    loadActivityAndClearStack(context, ViewTabbedActivity.class);
                    finish();
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
        showAlertDialogButtonClicked(view);
    }

    public void showAlertDialogButtonClicked(View view) {

        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose action");

        // add a list
        String[] actions = {"Gallery", "Camera"};
        builder.setItems(actions, new DialogInterface.OnClickListener() {
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


    private void sendVerificationEmail()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //if(user== null) return;
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // email sent
                            showToast("A verification message has been sent to your email");
                            // after email is sent just logout the user and finish this activity
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(ProfileCarActivity.this, UserChoiceActivity.class));
                            finish();
                        }
                        else
                        {
                            // email not sent, so display message and restart the activity or do whatever you wish to do

                            //restart this activity
                            overridePendingTransition(0, 0);
                            finish();
                            overridePendingTransition(0, 0);
                            startActivity(getIntent());

                        }
                    }
                });
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

    private void assignBitmap(Bitmap resized) {
        switch (buttonId)
        {
            case R.id.profile_car_front : carFront = resized;
                setSelectedText(R.id.choose_front_image, "Car Front Selected");uploadFront = true;break;
            case R.id.profile_car_back : carBack = resized;
                setSelectedText(R.id.choose_back_image, "Car Back Selected");uploadBack = true;break;
            case R.id.profile_car_side : carSide = resized;
                setSelectedText(R.id.choose_car_side, "Car Side Selected");uploadCarSide = true;break;
            case R.id.profile_car_plate : carNumberPlate = resized;
                setSelectedText(R.id.choose_number_plate, "Car Number Plate Selected");uploadNumberPLate = true;break;
            default: showToast("Image fetching failed try again"); break;
        }
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

    TextView getTextView(int id)
    {
        return (TextView)findViewById(id);
    }
}
