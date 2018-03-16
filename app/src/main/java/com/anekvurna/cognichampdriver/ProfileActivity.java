package com.anekvurna.cognichampdriver;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.anekvurna.cognichampdriver.SanathUtilities.fromBitmapToByteArray;
import static com.anekvurna.cognichampdriver.SanathUtilities.loadActivityAndFinish;

public class ProfileActivity extends AppCompatActivity {

    Context context;
    static final int PICK_IMAGE = 2;
    byte[] byteArray;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseReference;
    final int PIC_CROP = 1;
    final int DESIRED_WIDTH = 170, DESIRED_HEIGHT = 170;
    private int buttonId;
    Bitmap driverImage, driverLicence, driverVoterId, carFront, carBack, carNumberPlate;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    private int totalImagesUploaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        context = this;
        SharedPreferences prefs = getSharedPreferences("com.anekvurna.cognichampdriver", MODE_PRIVATE);
        if(prefs.getBoolean("isFilled", false))
        {
            loadActivityAndFinish(this, TripListActivity.class);
        }

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("driverProfiles").child(currentUser.getUid());
    }

    public void onSave(View view)
    {
        totalImagesUploaded = 0;
        if(!isValid())
        {
            return;
        }
        uploadImage(R.id.choose_driver_image, driverImage, "driverImage");
        uploadImage(R.id.choose_voterid, driverVoterId, "driverVoterId");
        uploadImage(R.id.choose_driving_licence, driverLicence, "driverLicence");
        uploadImage(R.id.choose_front_image, carFront, "frontImage");
        uploadImage(R.id.choose_back_image, carBack, "backImage");
        uploadImage(R.id.choose_number_plate, carNumberPlate, "numberPlate");
    }

    private void uploadImage(final int textId, Bitmap imageBitmap, String name) {
        setUploadingText(textId);
        byte[] data = fromBitmapToByteArray(imageBitmap);
        storageRef = FirebaseStorage.getInstance().getReference().child("profiles").child(currentUser.getUid())
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
                if(totalImagesUploaded == 6)
                    register();
            }
        });
    }



    boolean isValid()
    {
        final int MOBILE_LIMIT = 10;
        boolean valid = true;
        if(getEditText(R.id.profile_name).getText().toString().equals(""))
        {
            showToast("Please enter valid name");
            valid = false;
        }
        if(getEditText(R.id.profile_address_line1).getText().toString().equals("") )
        {
            showToast("Please enter valid address");
            valid = false;
        }
        String email = getEditText(R.id.profile_email).getText().toString();
        if(email.equals("") || !emailValidator(email))
        {
            showToast("Please enter valid email");
            valid = false;
        }
        String alternateMobile = getEditText(R.id.profile_alternate_mobile).getText().toString();
        if(alternateMobile.equals("") || alternateMobile.length() != MOBILE_LIMIT )
        {
            showToast("Please enter valid alternate mobile");
            valid = false;
        }
        String stdCode, landline;
        stdCode = getEditText(R.id.profile_std_code).getText().toString();
        landline = getEditText(R.id.profile_landline).getText().toString();
        if(stdCode.equals("") || landline.equals("") || stdCode.length() + landline.length() != MOBILE_LIMIT)
        {
            showToast("Please enter valid landline");
            valid = false;
        }
        if(getEditText(R.id.profile_vehicle_number).getText().toString().equals(""))
        {
            showToast("Please enter valid vehicle number");
            valid = false;
        }

        if(getEditText(R.id.profile_number_voter_id).getText().toString().equals(""))
        {
            showToast("Please enter valid voter id");
            valid = false;
        }
        if(getEditText(R.id.profile_number_driving_licence).getText().toString().equals(""))
        {
            showToast("Please enter valid driving licence number");
            valid = false;
        }

        if(driverImage == null || driverLicence == null || driverVoterId == null || carFront == null ||
         carBack == null || carNumberPlate == null)
        {
            showToast("Please choose all the images");
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

    void register() {
        showToast("Saving profile");
        Profile profile = new Profile();
        profile.setName(getEditText(R.id.profile_name).getText().toString());
        profile.setAddress(getEditText(R.id.profile_address_line1).getText().toString());
        profile.setMobile(currentUser.getPhoneNumber());
        profile.setEmail(getEditText(R.id.profile_email).getText().toString());
        profile.setAlternateMobile(getEditText(R.id.profile_alternate_mobile).getText().toString());
        profile.setLandline(getEditText(R.id.profile_std_code).getText().toString() +
                getEditText(R.id.profile_landline).getText().toString());
        profile.setUserId(currentUser.getUid());
        profile.setVehicleNumber(getEditText(R.id.profile_vehicle_number).getText().toString());
        profile.setVoterId(getEditText(R.id.profile_number_voter_id).getText().toString());
        profile.setDrivingLicence(getEditText(R.id.profile_number_driving_licence).getText().toString());

        databaseReference.setValue(profile, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(databaseError!=null){
                    showToast("Error saving database!");
                    return;
                }
                showToast("Save successful!");
                SharedPreferences.Editor editor = getSharedPreferences("com.anekvurna.cognichampdriver", MODE_PRIVATE).edit();
                editor.putBoolean("isFilled", true);
                editor.apply();
                loadActivityAndFinish(context, TripListActivity.class);

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
               /* assert pickedImage != null;
                InputStream inputStream = getContentResolver().openInputStream(pickedImage);
                byteArray = getBytes(inputStream);
                Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray,0, byteArray.length);
                int width = bitmap.getWidth(), height = bitmap.getHeight();
                if(width < 170 || height < 170)
                {
                    showToast("Please choose an image with minimum size 170*170");
                    byteArray = null;
                    return;
                }
                Bitmap trimmedImage = Bitmap.createBitmap(bitmap, (width-1)/2, (height-1)/2, 170, 170);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                trimmedImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byteArray = stream.toByteArray(); */
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
            case R.id.profile_driver_pic : driverImage = resized;setNullText(R.id.choose_driver_image); break;
            case R.id.profile_driver_voter_id : driverVoterId = resized;setNullText(R.id.choose_voterid);break;
            case R.id.profile_driver_licence : driverLicence = resized;setNullText(R.id.choose_driving_licence);break;
            case R.id.profile_car_front : carFront = resized;setNullText(R.id.choose_front_image);break;
            case R.id.profile_car_back : carBack = resized;setNullText(R.id.choose_back_image);break;
            case R.id.profile_car_plate : carNumberPlate = resized;setNullText(R.id.choose_number_plate);break;
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

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    TextView getTextView(int id)
    {
        return (TextView)findViewById(id);
    }
}
