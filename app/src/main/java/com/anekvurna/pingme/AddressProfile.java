package com.anekvurna.pingme;

/**
 * Created by Admin on 2/1/2018.
 */

public class AddressProfile {
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String pinCode;
    private int state;

    public AddressProfile(String addressLine1, String addressLine2, String city, String pinCode, int state) {
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.pinCode = pinCode;
        this.state = state;
    }

    public AddressProfile() {
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

    public int getState() {
        return state;
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

    public void setState(int state) {
        this.state = state;
    }
}
