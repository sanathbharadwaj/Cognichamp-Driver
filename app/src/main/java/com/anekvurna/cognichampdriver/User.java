package com.anekvurna.cognichampdriver;

/**
 * Created by Admin on 1/10/2018.
 */

public class User {

    private String mobile, email;
    int profileStatus;

    public User()
    {}

    public User(String mobile, String email, int profileStatus) {
        this.mobile = mobile;
        this.email = email;
        this.profileStatus = profileStatus;
    }

    public int getProfileStatus() {
        return profileStatus;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getMobile() {
        return mobile;
    }

    public String getEmail() {
        return email;
    }
}
