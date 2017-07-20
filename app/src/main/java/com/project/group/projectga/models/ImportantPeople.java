package com.project.group.projectga.models;

import android.net.Uri;

/**
 * Created by ramjiseetharaman on 7/13/17.
 */

public class ImportantPeople {

    private String name;
    private String relation;
    private String shortDescription;
    private String longDescription;
    private String profile;
    private String key;

    public ImportantPeople() {
    }

    public ImportantPeople(String name, String relation, String shortDescription, String longDescription, String profile, String key) {
        this.name = name;
        this.relation = relation;
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
        this.profile = profile;
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }
}
