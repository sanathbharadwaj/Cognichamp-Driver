package com.anekvurna.pingme;

/**
 * Created by Admin on 1/18/2018.
 */

public class BasicProfile {
    String name;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String pinCode;
    private String email;
    private String alternateNumber;
    private String landline;
    private String stdCode;
    private int state;

    public BasicProfile(String name, String addressLine1, String addressLine2, String city, String pinCode, String email, String alternateNumber, String landline, String stdCode, int state) {
        this.name = name;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.pinCode = pinCode;
        this.email = email;
        this.alternateNumber = alternateNumber;
        this.landline = landline;
        this.stdCode = stdCode;
        this.state = state;
    }



    public BasicProfile(){}

    public String getName() {
        return name;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public String getCity() {
        return city;
    }

    public String getPinCode() {
        return pinCode;
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

    public int getState() {
        return state;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
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

    public void setState(int state) {
        this.state = state;
    }
}
