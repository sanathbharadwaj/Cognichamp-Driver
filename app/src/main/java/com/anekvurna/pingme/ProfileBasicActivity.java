package com.anekvurna.pingme;

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

import static com.anekvurna.pingme.SanathUtilities.fromBitmapToByteArray;
import static com.anekvurna.pingme.SanathUtilities.loadActivity;
import static com.anekvurna.pingme.SanathUtilities.loadActivityAndFinish;
import static com.anekvurna.pingme.SanathUtilities.setProgressBar;

public class ProfileBasicActivity extends AppCompatActivity {

    DatabaseReference databaseReference;
    FirebaseAuth auth;
    FirebaseUser currentUser;
    private EditText name;
    private EditText email;
    private EditText alternate;
    private EditText landline;
    private EditText stdCode;
    boolean uDriverImage = true;
    static final int PICK_IMAGE = 2;
    final int PIC_CROP = 1;
    private BasicProfile basicProfile;
    final int DESIRED_WIDTH = 170, DESIRED_HEIGHT = 170;
    private int buttonId;
    Bitmap driverImage;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    private int totalImagesUploaded;
    boolean isPreviousProfile = false;
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

    public void showEditTextsAsMandatory ( EditText... ets )
    {
        for ( EditText et : ets )
        {
            String hint = et.getHint ().toString ();

            et.setHint ( Html.fromHtml ( "<font color=\"#ff0000\">" + "* " + "</font>" + hint ) );
        }
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
                SharedPreferences.Editor editor = getSharedPreferences("com.anekvurna.pingme", MODE_PRIVATE).edit();
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

    void setProfileStatus(int i)
    {
        DatabaseReference profileStatusReference = FirebaseDatabase.getInstance().getReference("users")
                .child(currentUser.getUid()).child("profileStatus");
        profileStatusReference.setValue(i);
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
            case R.id.profile_driver_pic : driverImage = resized;
                setSelectedText(R.id.choose_driver_image);uDriverImage = true; break;
            default: showToast("Image fetching failed try again"); break;
        }
    }

    public void chooseImage(View view) {
        buttonId = view.getId();
        showAlertDialogButtonClicked(view);
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

    public EditText getEditText(int id) {
        return (EditText) findViewById(id);
    }

    void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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

    boolean isEnabledRuntimePermission()
    {
        if(Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        2);
                return false;
            }
        }

        return true;
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
