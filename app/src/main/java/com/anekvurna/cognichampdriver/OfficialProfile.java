package com.anekvurna.cognichampdriver;

/**
 * Created by Admin on 1/18/2018.
 */

public class OfficialProfile {
    String licenceNumber, voterId;

    public OfficialProfile(String licenceNumber, String voterId) {
        this.licenceNumber = licenceNumber;
        this.voterId = voterId;
    }

    public OfficialProfile() {}

    public String getLicenceNumber() {
        return licenceNumber;
    }

    public String getVoterId() {
        return voterId;
    }
}
