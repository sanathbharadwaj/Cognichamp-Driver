package com.anekvurna.cognichampdriver;

/**
 * Created by Admin on 1/18/2018.
 */

public class BasicProfile {
    String name;
    private String alternateNumber;
    private String email;
    private String landline;
    private String stdCode;
    private String mobile;

    public BasicProfile(String name,String email, String alternateNumber, String landline, String stdCode) {
        this.name = name;
        this.alternateNumber = alternateNumber;
        this.landline = landline;
        this.stdCode = stdCode;
        this.email = email;

    }



    public BasicProfile(){}

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getAlternateNumber() {
        return alternateNumber;
    }

    public String getLandline() {
        return landline;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAlternateNumber(String alternateNumber) {
        this.alternateNumber = alternateNumber;
    }

    public String getStdCode() {
        return stdCode;
    }

    public void setStdCode(String stdCode) {
        this.stdCode = stdCode;
    }

    public void setLandline(String landline) {
        this.landline = landline;
    }

}
