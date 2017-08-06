package com.project.group.projectga.models;

public class Memory {

    public Memory() {}

    String name;
    String date;
    String shortDescription;
    String longDescription;
    String path;
    String key;
    String title;

    public Memory(String name, String date, String shortDescription, String longDescription, String path, String key, String title) {
        this.name = name;
        this.date = date;
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
        this.path = path;
        this.key = key;
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
