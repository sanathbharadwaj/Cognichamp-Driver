package com.anekvurna.cognichampdriver;

import android.Manifest;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.anekvurna.cognichampdriver.SanathUtilities.editor;
import static com.anekvurna.cognichampdriver.SanathUtilities.fromBitmapToByteArray;
import static com.anekvurna.cognichampdriver.SanathUtilities.initializeSharedPrefs;
import static com.anekvurna.cognichampdriver.SanathUtilities.loadActivity;
import static com.anekvurna.cognichampdriver.SanathUtilities.setProgressBar;

public class ProfileBasicActivity extends ProfileParentActivity {

    DatabaseReference databaseReference;
    private EditText name;
    private EditText email;
    private EditText alternate;
    private EditText landline;
    private EditText stdCode;
    boolean uDriverImage = true;
    private BasicProfile basicProfile;
    Bitmap driverImage;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    private Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_basic);
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        initializeEditTexts();
        checkForPreviousProfile();
        name = getEditText(R.id.profile_name);
        email = getEditText(R.id.profile_email);
        alternate = getEditText(R.id.profile_alternate_mobile);
        landline = getEditText(R.id.profile_landline);
        stdCode = getEditText(R.id.profile_std_code);
        setDrawableLefts();
        if(currentUser.getDisplayName()!=null)
        name.setText(currentUser.getDisplayName());
        email.setText(currentUser.getEmail());
        showEditTextsAsMandatory(name, email);
        isEnabledRuntimePermission();
    }

    private void setDrawableLefts() {
        name.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources
                .getDrawable(this, R.drawable.ic_account_box_black_24dp), null, null, null);
        email.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources
                .getDrawable(this, R.drawable.ic_email_black_24dp), null, null, null);
        alternate.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources
                .getDrawable(this, R.drawable.ic_phone_android_black_24dp), null, null, null);
        stdCode.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources
                .getDrawable(this, R.drawable.ic_home_black_24dp), null, null, null);
    }

    private void initializeEditTexts() {
        //TODO: store edit texts to temporary
    }

    private void checkForPreviousProfile() {
        DatabaseReference checkRef = FirebaseDatabase.getInstance().getReference("driverProfiles").child(currentUser.getUid());
        checkRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("basic"))
                {
                    uDriverImage = false;
                    isPreviousProfile = true;
                    populateData(dataSnapshot.child("basic").getValue(BasicProfile.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void populateData(BasicProfile basicProfile) {
        name.setText(basicProfile.getName());
        email.setText(basicProfile.getEmail());
        alternate.setText(basicProfile.getAlternateNumber());
        stdCode.setText(basicProfile.getStdCode());
        landline.setText(basicProfile.getLandline());

        StorageReference storageReference = FirebaseStorage.getInstance().getReference("driverProfiles")
                .child(currentUser.getUid());


    }

    public void onSaveBasic(View view)
    {
        if(!isValid()) return;
        final Context context = this;
        basicProfile = new BasicProfile();
        basicProfile.setName(name.getText().toString());
        basicProfile.setEmail(email.getText().toString());
        basicProfile.setAlternateNumber(alternate.getText().toString());
        basicProfile.setLandline(landline.getText().toString());
        basicProfile.setStdCode(stdCode.getText().toString());
        basicProfile.setMobile(currentUser.getPhoneNumber());

        if(uDriverImage)
            uploadImage(R.id.choose_driver_image, driverImage, "driverImage.jpg");

        if(numberOfUploads() == 0)
            register();
    }

    void register()
    {
        setProgressBar(this,true, "Saving...");
        databaseReference = FirebaseDatabase.getInstance().getReference("driverProfiles").child(currentUser.getUid()).child("basic");
        databaseReference.setValue(basicProfile, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                setProgressBar(ProfileBasicActivity.this,false, "dummy");
                if(databaseError!=null){
                    showToast("Error saving data!");
                    showToast(databaseError.getMessage());
                    return;
                }
                showToast("Save successful!");
                initializeSharedPrefs(ProfileBasicActivity.this);
                Intent intent  = getIntent();
                if(!intent.getBooleanExtra(getString(R.string.is_editing), false)) {
                    editor.putInt("profileStatus", 1);
                    editor.commit();
                    setProfileStatus(1);
                    loadActivity(ProfileBasicActivity.this, ProfileAddressActivity.class);
                }
                else
                {
                    finish();
                }
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



    private void assignBitmap(Bitmap resized) {
        switch (buttonId)
        {
            case R.id.profile_driver_pic : driverImage = resized;
                setSelectedText(R.id.choose_driver_image);uDriverImage = true; break;
            default: showToast("Image fetching failed try again"); break;
        }
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
        return count;
    }

    boolean isValid()
    {
        final int MOBILE_LIMIT = 10, LANDLINE_LIMIT = 11;
        boolean valid = true;
        if(getEditText(R.id.profile_name).getText().toString().equals(""))
        {
            showToast("Please enter valid name");
            valid = false;
        }

        String email = getEditText(R.id.profile_email).getText().toString();
        if(email.equals("") || !emailValidator(email))
        {
            showToast("Please enter valid email");
            valid = false;
        }
        String alternateMobile = getEditText(R.id.profile_alternate_mobile).getText().toString();
        if(!alternateMobile.equals("") && alternateMobile.length() != MOBILE_LIMIT )
        {
            showToast("Please enter valid alternate mobile");
            valid = false;
        }
        String stdCode, landline;
        stdCode = getEditText(R.id.profile_std_code).getText().toString();
        landline = getEditText(R.id.profile_landline).getText().toString();
        int length = stdCode.length() + landline.length();
        if((stdCode.equals("") || landline.equals("") || length != LANDLINE_LIMIT) && length != 0)
        {
            showToast("Please enter valid landline");
            valid = false;
        }

        if(driverImage == null && !isPreviousProfile)
        {
            showToast("Please choose driver image");
            valid = false;
        }

        return valid;
    }

    public boolean emailValidator(String email)
    {
        Pattern pattern;
        Matcher matcher;
        final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();
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

}
