package com.anekvurna.cognichampdriver;

/**
 * Created by Admin on 1/14/2018.
 */

public class MyLocation {
    public double latitude, longitude;

    public MyLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public MyLocation() {}

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

}
