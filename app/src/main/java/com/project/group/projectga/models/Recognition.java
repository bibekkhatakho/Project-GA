package com.project.group.projectga.models;

/**
 * Created by ramjiseetharaman on 7/13/17.
 */

public class Recognition {

    private String name;
    private String relation;
    private String shortDescription;
    private String longDescription;
    private String imageName;
    private String key;

    public Recognition() {
    }

    public Recognition(String name, String relation, String shortDescription, String longDescription, String imageName, String key) {
        this.name = name;
        this.relation = relation;
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
        this.imageName = imageName;
        this.key = key;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
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

}
