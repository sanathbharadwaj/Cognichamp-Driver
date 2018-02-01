package com.anekvurna.pingme;

/**
 * Created by Admin on 1/18/2018.
 */

public class CarProfile {
    private String vehicleName, vehicleNumber;

    public CarProfile(String vehicleName, String vehicleNumber) {
        this.vehicleName = vehicleName;
        this.vehicleNumber = vehicleNumber;
    }

    public CarProfile(){}

    public String getVehicleName() {
        return vehicleName;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }
}
