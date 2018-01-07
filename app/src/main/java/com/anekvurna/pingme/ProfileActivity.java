package com.anekvurna.pingme;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import static com.anekvurna.pingme.SanathUtilities.*;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ProfileActivity extends AppCompatActivity {

    Context context;
    static final int PICK_IMAGE = 1;
    byte[] byteArray;
    ParseFile file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        context = this;
        SharedPreferences prefs = getSharedPreferences("com.anekvurna.pingme", MODE_PRIVATE);
        if(prefs.getBoolean("isFilled", false))
        {
            loadActivityAndFinish(this, AllListsActivity.class);
        }
    }

    public void onSave(View view)
    {
        if(!isValid())
        {
            showToast("Please fill valid data");
            return;
        }
        file = new ParseFile("displayPic.png", byteArray);
        showToast("Uploading image");
        file.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null)
                    showToast("Upload failed please try again");
                else {
                    register();
                }
            }
        });
    }

    boolean isValid()
    {
        return (!getEditText(R.id.profile_name).getText().toString().equals("") &&
                !getEditText(R.id.profile_address).getText().toString().equals("") &&
                !getEditText(R.id.profile_mobile).getText().toString().equals("") &&
                !getEditText(R.id.profile_email).getText().toString().equals("") &&
                !getEditText(R.id.profile_alternate_mobile).getText().toString().equals("") &&
                !getEditText(R.id.profile_std_code).getText().toString().equals("") &&
                !getEditText(R.id.profile_landline).getText().toString().equals("") &&
                !getEditText(R.id.profile_field1).getText().toString().equals(""));
    }

    void register() {
        showToast("Saving profile");
        ParseObject profile = new ParseObject("Profile");
        profile.put("name", getEditText(R.id.profile_name).getText().toString());
        profile.put("address", getEditText(R.id.profile_address).getText().toString());
        profile.put("mobile", getEditText(R.id.profile_mobile).getText().toString());
        profile.put("email", getEditText(R.id.profile_email).getText().toString());
        profile.put("alternateMobile", getEditText(R.id.profile_alternate_mobile).getText().toString());
        profile.put("landline", getEditText(R.id.profile_std_code).getText().toString() +
                getEditText(R.id.profile_landline).getText().toString());
        profile.put("alphaNumeric", getEditText(R.id.profile_field1).getText().toString());
        profile.put("profilePic", file);
        profile.put("userMobile", ParseUser.getCurrentUser().getUsername());
        profile.put("insId", ParseInstallation.getCurrentInstallation().getInstallationId());
        profile.saveEventually();
        profile.pinInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    showToast(e.getMessage());
                    return;
                }
                showToast("Save successful!");
                SharedPreferences.Editor editor = getSharedPreferences("com.anekvurna.pingme", MODE_PRIVATE).edit();
                editor.putBoolean("isFilled", true);
                editor.apply();
                loadActivityAndFinish(context, AllListsActivity.class);

            }
        });
        ParseUser.getCurrentUser().put("nameOfUser", getEditText(R.id.profile_name).getText().toString());
        ParseUser.getCurrentUser().saveInBackground();
        ParseUser.getCurrentUser().pinInBackground();
    }

    public EditText getEditText(int id) {
        return (EditText) findViewById(id);
    }

    void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void chooseImage(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri pickedImage = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(pickedImage);
                byteArray = getBytes(inputStream);
                Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray,0, byteArray.length);
                if(bitmap.getWidth() != 170 || bitmap.getHeight() != 170)
                {
                    showToast("Please choose 170*170 image");
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                showToast("File not found");
                return;
            } catch (IOException e) {
                showToast("IO Exception");
                return;
            }
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
}
