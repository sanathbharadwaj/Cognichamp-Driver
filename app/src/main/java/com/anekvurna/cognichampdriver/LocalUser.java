package com.anekvurna.cognichampdriver;

/**
 * Created by Admin on 1/11/2018.
 */

public class LocalUser {
    private String name, mobile, userId, elementId;
    private boolean isRegistered;
    private MyLocation myLocation;
    private int state;


    public LocalUser(String name, String mobile, String userId, String elementId, boolean isRegistered) {
        this.name = name;
        this.mobile = mobile;
        this.userId = userId;
        this.elementId = elementId;
        this.isRegistered = isRegistered;
        state = 0;
    }

    public LocalUser()
    {

    }

    public MyLocation getMyLocation() {
        return myLocation;
    }

    public void setMyLocation(MyLocation myLocation) {
        this.myLocation = myLocation;
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

    public String getElementId() {
        return elementId;
    }

    public boolean isRegistered() {
        return isRegistered;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
