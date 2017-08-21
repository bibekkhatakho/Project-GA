package com.project.group.projectga.models;

/**
 * Created by MitchelSmith on 8/18/2017.
 */

public class Photos {

    private String folder;
    private String key;

    public Photos() {
    }

    public Photos(String folder, String key) {
        this.folder = folder;
        this.key = key;
    }

    public String getFolder() { return folder; }

    public void setFolder(String folder) { this.folder = folder; }

    public String getKey() { return key; }

    public void setKey(String key) { this.key = key; }
}
