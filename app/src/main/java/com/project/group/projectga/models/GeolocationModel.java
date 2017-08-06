package com.project.group.projectga.models;

/**
 * Created by bibek on 8/2/17.
 */

public class GeolocationModel {

    private double latitude;
    private double longitude;

    public GeolocationModel() {
    }

    public GeolocationModel(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

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
}

