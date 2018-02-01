package com.anekvurna.pingme;

/**
 * Created by Admin on 1/10/2018.
 */

public class User {

    private String mobile, password;

    public User()
    {}

    public User(String mobile, String password) {
        this.mobile = mobile;
        this.password = password;
    }


    public String getMobile() {
        return mobile;
    }

    public String getPassword() {
        return password;
    }
}
