package com.project.group.projectga.models;

public class Profile {

    private String fullName;
    private String userId;
    private String email;

    private String phoneNumber;
    private String dateOfBirth;

    private String userType;
    private String guardianEmail;
    private String profile;

    public Profile() {
    }

    public Profile(String fullName,String profile, String userType, String userId, String email, String phoneNumber, String dateOfBirth, String guardianEmail) {
        this.fullName = fullName;
        this.userId = userId;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.userType = userType;
        this.dateOfBirth = dateOfBirth;
        this.guardianEmail = guardianEmail;
        this.profile = profile;
    }

    public String getProfile() {
        return profile;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGuardianEmail() {
        return guardianEmail;
    }

    public void setGuardianEmail(String guardianEmail) {
        this.guardianEmail = guardianEmail;
    }

}


