package com.project.group.projectga.models;

/**
 * Created by MitchelSmith on 8/15/2017.
 */

public class LocationModel {

    private String currentLat;
    private String currentLong;
    private String patientEmail;
    private String patientName;
    private String guardianName;

    public LocationModel() {
    }

    public LocationModel(String currentLat, String currentLong, String patientEmail, String patientName, String guardianName) {
        this.currentLat = currentLat;
        this.currentLong = currentLong;
        this.patientEmail = patientEmail;
        this.patientName = patientName;
        this.guardianName = guardianName;
    }

    public void setCurrentLat(String currentLat) { this.currentLat = currentLat; }

    public String getCurrentLat() {
        return currentLat;
    }

    public void setCurrentLong(String currentLong) {
        this.currentLong = currentLong;
    }

    public String getCurrentLong() {
        return currentLong;
    }

    public void setPatientEmail(String patientEmail) { this.patientEmail = patientEmail; }

    public String getPatientEmail() { return patientEmail; }

    public void setGuardianName(String guardianName) { this.guardianName = guardianName; }

    public String getGuardianName() { return guardianName; }

    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientName() { return patientName; }
}
