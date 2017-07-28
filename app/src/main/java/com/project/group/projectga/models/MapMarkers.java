package com.project.group.projectga.models;

/**
 * Created by MitchelSmith on 7/24/2017.
 */

public class MapMarkers {

    private String latitude;
    private String longitude;
    private String address;
    private String addressDescription;
    private String key;
    private String markerIcon;

    public MapMarkers() {
    }

    public MapMarkers(String latitude, String markerIcon, String longitude, String address, String addressDescription, String key) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.markerIcon = markerIcon;
        this.address = address;
        this.addressDescription = addressDescription;
        this.key = key;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddressDescription() {
        return addressDescription;
    }

    public void setAddressDescription(String addressDescription) {
        this.addressDescription = addressDescription;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getMarkerIcon() {
        return markerIcon;
    }

    public void setMarkerIcon(String markerIcon) {
        this.markerIcon = markerIcon;
    }
}
