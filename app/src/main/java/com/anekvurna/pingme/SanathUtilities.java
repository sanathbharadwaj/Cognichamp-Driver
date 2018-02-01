package com.anekvurna.pingme;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Admin on 1/3/2018.
 */

public class SanathUtilities {

    public static Profile currentProfile;
    public static FirebaseUser currentUser;
    public static SharedPreferences.Editor editor;
    public static SharedPreferences preferences;


    public static void initializeSharedPrefs(Context context)
    {
        editor = context.getSharedPreferences("com.anekvurna.pingme", MODE_PRIVATE).edit();
        preferences = context.getSharedPreferences("com.anekvurna.pingme", MODE_PRIVATE);
    }

    public static void showToast(Context context, String message)
    {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void initializeCurrentUser()
    {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
    }

    public static void loadActivity(Context context , Class myClass )
    {
        Intent intent = new Intent(context, myClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        context.startActivity(intent);
    }

    public static void loadActivityAndFinish(Context context , Class myClass )
    {
        Intent intent = new Intent(context, myClass);
        context.startActivity(intent);
        Activity activity = (Activity) context;
        activity.finish();
    }

    public static void getFirebaseProfile()
    {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser==null) return;
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("profiles").child(currentUser.getUid());
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentProfile = dataSnapshot.getValue(Profile.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static Bitmap fromImage64ToBitmap(String image64)
    {
        byte[] data = Base64.decode(image64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    public static byte[] fromBitmapToByteArray(Bitmap bitmap)
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

   public static Bitmap fromByteArrayToBitmap(byte[] data)
   {
       return BitmapFactory.decodeByteArray(data, 0, data.length);
   }

   /*public static EditText getEditText(Context context, int id)
   {
       return (EditText)
   }*/

}
