package com.anekvurna.cognichampdriver;

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
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.content.res.AppCompatResources;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import static com.anekvurna.cognichampdriver.SanathUtilities.*;

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

public class ProfileCarActivity extends ProfileParentActivity {

    Context context;
    static final int PICK_IMAGE = 2;
    private DatabaseReference databaseReference;
    final int PIC_CROP = 1;
    //final int DESIRED_WIDTH = 170, DESIRED_HEIGHT = 170;
    //private int buttonId;
    Bitmap carFront, carBack, carNumberPlate;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
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
        setDrawablesLeft();
        isEnabledRuntimePermission();
    }

    private void setDrawablesLeft() {
        name.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources
                .getDrawable(this, R.drawable.ic_directions_car_black_24dp), null, null, null);
        number.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources
                .getDrawable(this, R.drawable.ic_call_to_action_black_24dp), null, null, null);
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
                initializeSharedPrefs(ProfileCarActivity.this);
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

    void setSelectedText(int id)
    {
        getTextView(id).setText(R.string.image_selected);
    }

    /*void startCropIntent(Uri picUri)
    {
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            // indicate image type and Uri
            cropIntent.setDataAndType(picUri, "image*//*");
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
    }*/

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
