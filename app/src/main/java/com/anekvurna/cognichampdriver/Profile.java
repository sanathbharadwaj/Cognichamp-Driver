package com.anekvurna.cognichampdriver;

/**
 * Created by Admin on 1/11/2018.
 */

public class Profile {

    private String profileId, userId, name, address, mobile, email, alternateMobile, landline, vehicleNumber, voterId, drivingLicence;


    public Profile(){}

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    void setAddress(String address) {
        this.address = address;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    void setEmail(String email) {
        this.email = email;
    }

    void setAlternateMobile(String alternateMobile) {
        this.alternateMobile = alternateMobile;
    }

    void setLandline(String landline) {
        this.landline = landline;
    }


    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public void setVoterId(String voterId) {
        this.voterId = voterId;
    }

    public void setDrivingLicence(String drivingLicence) {
        this.drivingLicence = drivingLicence;
    }

    public String getProfileId() {

        return profileId;

    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public String getVoterId() {
        return voterId;
    }

    public String getDrivingLicence() {
        return drivingLicence;
    }

    String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    String getAddress() {
        return address;
    }

    public String getMobile() {
        return mobile;
    }

    public String getEmail() {
        return email;
    }

    public String getAlternateMobile() {
        return alternateMobile;
    }

    public String getLandline() {
        return landline;
    }

}
