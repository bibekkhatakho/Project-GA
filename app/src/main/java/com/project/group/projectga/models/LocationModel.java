package com.project.group.projectga.models;

/**
 * Created by MitchelSmith on 8/15/2017.
 */

public class LocationModel {

    private String currentLat;
    private String currentLong;
    private String patientEmail;
    private String patientName;
    private String patientPicture;
    private String patientNumber;
    private String guardianName;
    private double latitude;
    private double longitude;
    private String guardianPicture;
    private String guardianNumber;
    private boolean geofenceLocationExists;

    public LocationModel() {
    }

    public LocationModel(boolean geofenceLocationExists, String currentLat, String currentLong, String patientEmail, String patientName, String guardianName, double latitude, double longitude, String patientPicture, String guardianPicture, String patientNumber, String guardianNumber) {
        this.currentLat = currentLat;
        this.currentLong = currentLong;
        this.patientEmail = patientEmail;
        this.geofenceLocationExists = geofenceLocationExists;
        this.patientName = patientName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.patientNumber = patientNumber;
        this.guardianNumber = guardianNumber;
        this.patientPicture = patientPicture;
        this.guardianName = guardianName;
        this.guardianPicture = guardianPicture;
    }

    public boolean isGeofenceLocationExists() {
        return geofenceLocationExists;
    }

    public void setGeofenceLocationExists(boolean geofenceLocationExists) {
        this.geofenceLocationExists = geofenceLocationExists;
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

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
	
	 public void setPatientPicture(String patientPicture) {this.patientPicture = patientPicture; }

    public String getPatientPicture() { return patientPicture; }

    public void setGuardianPicture(String guardianPicture) { this.guardianPicture = guardianPicture; }

    public String getGuardianPicture() { return guardianPicture; }

    public void setGuardianNumber(String guardianNumber) { this.guardianNumber = guardianNumber; }

    public String getGuardianNumber() { return guardianNumber; }

    public void setPatientNumber(String patientNumber) {this.patientNumber = patientNumber; }

    public String getPatientNumber() { return patientNumber; }
}
