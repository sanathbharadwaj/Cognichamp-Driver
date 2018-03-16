package com.anekvurna.cognichampdriver;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.UUID;

public class ProfileParentActivity extends AppCompatActivity {

    protected String photo_file_name;
    protected static final int PHOTO_REQUEST_GALLERY = 4;
    protected static final int PHOTO_REQUEST_CUT = 5;
    protected static final int PHOTO_REQUEST_CAMERA = 6;
    protected int buttonId;
    final int DESIRED_WIDTH = 170, DESIRED_HEIGHT = 170;
    protected FirebaseAuth auth;
    protected FirebaseUser currentUser;
    protected Uri cropUri;
    final int PIC_CROP = 1;
    protected File dir;
    boolean isPreviousProfile = false;
    protected int totalImagesUploaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        isEnabledRuntimePermission();
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
    protected void onResume() {
        super.onResume();
        totalImagesUploaded = 0;
    }

    public void clickImageFromCamera() {

        if(!isEnabledRuntimePermission())
            return;

        /*Intent camIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        File file = new File(Environment.getExternalStorageDirectory(),
                "file" + String.valueOf(System.currentTimeMillis()) + ".jpg");
        uri = FileProvider.getUriForFile(this,
                BuildConfig.APPLICATION_ID + ".provider",
                file);

        camIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);

        camIntent.putExtra("return-data", true);

        startActivityForResult(camIntent, 3);
*/
        photo_file_name = UUID.randomUUID().toString()+".jpg";
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        //set file location to DreamGo/Image
        File path = Environment.getExternalStorageDirectory();
        File dir = new File(path, "Anekvurna/Cognichamp");
        if(!dir.exists())
            dir.mkdirs();
        //Android N need use FileProvider get file
        //uri because StrictMode System
        //getUriForFile(content,provider author,file)
        File file = new File(dir.getAbsolutePath(), photo_file_name);
        Uri photoURI = FileProvider.getUriForFile(this,
                BuildConfig.APPLICATION_ID + ".provider",
                file);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        startActivityForResult(intent, PHOTO_REQUEST_CAMERA);
    }

    void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void chooseImage(View view) {
        buttonId = view.getId();
        showAlertDialogButtonClicked(view);
    }

    public EditText getEditText(int id) {
        return (EditText) findViewById(id);
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
                    case 0: /*Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);*/
                        //set UUID to filename
                        photo_file_name = UUID.randomUUID().toString()+".jpg";
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType("image/*");
                        startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
                        break;
                    case 1: clickImageFromCamera(); break;
                }
            }
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    Bitmap getImageBitmap(File file)
    {
        if(file.exists()){
            Bitmap selectedBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            if(selectedBitmap == null)
            {
                return null;
            }

            return Bitmap.createScaledBitmap(selectedBitmap, DESIRED_WIDTH, DESIRED_HEIGHT, true);

        }

        return null;
    }

    public void showEditTextsAsMandatory ( EditText... ets )
    {
        for ( EditText et : ets )
        {
            String hint = et.getHint ().toString ();

            et.setHint ( Html.fromHtml ( "<font color=\"#ff0000\">" + "* " + "</font>" + hint ) );
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

    TextView getTextView(int id)
    {
        return (TextView)findViewById(id);
    }

    void setProfileStatus(int i)
    {
        DatabaseReference profileStatusReference = FirebaseDatabase.getInstance().getReference("users")
                .child(currentUser.getUid()).child("profileStatus");
        profileStatusReference.setValue(i);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        /*if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {

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
                *//*if(extras == null) return;
                Bitmap selectedBitmap = extras.getParcelable("data");*//*
                Bitmap selectedBitmap = null;
                if(cropUri != null) {
                    try {
                        selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), cropUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(selectedBitmap == null)
                    return;
                Bitmap resized = Bitmap.createScaledBitmap(selectedBitmap, DESIRED_WIDTH, DESIRED_HEIGHT, true);
                assignBitmap(resized);
            }
        }

        if(requestCode == 3)
        {
            startCropIntent(uri);
        }*/

        File path = Environment.getExternalStorageDirectory();
        dir = new File(path, "Anekvurna/Cognichamp");
        if(!dir.exists())
            dir.mkdirs();
        switch (requestCode)
        {
            case PHOTO_REQUEST_GALLERY:
                if (data != null){
                    //file from gallery
                    File sourceFile = new File(getRealPathFromURI(data.getData()));
                    //blank file DreamGo/Image/uuid.jpg
                    File destFile = new File(dir.getAbsolutePath(), photo_file_name);
                    Log.e("photo",data.getData().getPath());
                    try {
                        //copy file from gallery to DreamGo/Image/uuid.jpg
                        // otherwise crop method can't cut image without write permission
                        copyFile(sourceFile,destFile);
                        //Android N need use FileProvider to get file uri
                        Uri photoURI =  FileProvider.getUriForFile(this,
                                BuildConfig.APPLICATION_ID + ".provider",
                                destFile);
                        //cut image
                        crop(photoURI);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case PHOTO_REQUEST_CAMERA:
                //whether sdcard is usable has been checked before use camera
                File tempFile = new File(dir.getAbsolutePath(),photo_file_name);
                Uri photoURI = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        tempFile);
                crop(photoURI);
                break;
           /* case PHOTO_REQUEST_CUT:
                try {
                    if(data!=null) {
                        File file = new File(dir.getAbsolutePath(), photo_file_name);
                        getImageBitmap(file);
                    }else {
                        showToast("An error occurred when cropping image");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;*/
            default:
                break;
        }
    }

    public void crop(Uri uri) {
        this.grantUriPermission("com.android.camera",uri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        //Android N need set permission to uri otherwise system camera don't has permission to access file wait crop
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.putExtra("crop", "true");
        //The proportion of the crop box is 1:1
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        //Crop the output image size
        intent.putExtra("outputX", 250);
        intent.putExtra("outputY", 250);
        //image type
        intent.putExtra("outputFormat", "JPEG");
        intent.putExtra("noFaceDetection", true);
        //true - don't return uri |  false - return uri
        intent.putExtra("return-data", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, PHOTO_REQUEST_CUT);
    }

    public void copyFile(File sourceFile, File destFile) throws IOException {
        if (!sourceFile.exists()) {
            return;
        }
        FileChannel source = new FileInputStream(sourceFile).getChannel();
        FileChannel destination = new FileOutputStream(destFile).getChannel();
        if (destination != null && source != null) {
            destination.transferFrom(source, 0, source.size());
        }
        if (source != null) {
            source.close();
        }
        if (destination != null) {
            destination.close();
        }
    }

    //file uri to real location in filesystem
    public String getRealPathFromURI(Uri contentURI) {
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            // Source is Dropbox or other similar local file path
            return contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }
    }


    void startCropIntent(Uri picUri)
    {
        File file = new File(Environment.getExternalStorageDirectory(),
                "file" + String.valueOf(System.currentTimeMillis()) + ".jpg");

        cropUri = FileProvider.getUriForFile(this,
                BuildConfig.APPLICATION_ID + ".provider",
                file);

        this.grantUriPermission("com.android.camera",cropUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

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

            cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            cropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, cropUri);
            // start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, PIC_CROP);
        }
        catch (ActivityNotFoundException notFound) {

            String errorMessage = "Whoops - your device doesn't support the crop action!";
            showToast(errorMessage);
        }
        /*catch (Exception e)
        {
            e.printStackTrace();
        }*/
    }

}
