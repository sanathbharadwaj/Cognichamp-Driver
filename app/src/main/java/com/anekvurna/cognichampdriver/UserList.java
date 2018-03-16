package com.anekvurna.cognichampdriver;

/**
 * Created by Admin on 1/11/2018.
 */

public class UserList {
    private String username, mobile, elementId, userId;
    boolean isRegistered;

    public UserList(String username, String mobile, String elementId, String userId, Boolean isRegistered) {
        this.username = username;
        this.mobile = mobile;
        this.elementId = elementId;
        this.userId = userId;
        this.isRegistered = isRegistered;
    }

    public UserList()
    {
    }

    public String getUsername() {
        return username;
    }

    public String getMobile() {
        return mobile;
    }

    public String getElementId() {
        return elementId;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isRegistered() {
        return isRegistered;
    }
}
