package com.anekvurna.pingme;

import android.graphics.Bitmap;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Created by Admin on 1/11/2018.
 */

public class LocalUser {
    private String name, mobile, userId, elementId;
    StorageReference storageReference;


    public LocalUser(String name, String mobile, String userId, String elementId,  StorageReference storageReference) {
        this.name = name;
        this.mobile = mobile;
        this.userId = userId;
        this.storageReference = storageReference;
        this.elementId = elementId;
    }

    public LocalUser()
    {
    }

    public String getName() {
        return name;
    }

    public String getMobile() {
        return mobile;
    }

    public String getUserId() {
        return userId;
    }

    public StorageReference getStorageReference() {
        return storageReference;
    }

    public String getElementId() {
        return elementId;
    }
}
