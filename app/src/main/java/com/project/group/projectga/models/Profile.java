package com.project.group.projectga;

/**
 * Created by iod on 6/23/2017.
 */

public class Profile {

    private String fullName;
    private String userId;
    private String email;

    private String phone;
    private String dateOfBirth;

    private String securityQuestion;
    private String securityAnswer;

    private String guardianEmail;

    public Profile() {
    }

    public Profile(String fullName, String userId, String email, String phone, String dateOfBirth, String securityQuestion, String securityAnswer, String guardianEmail) {
        this.fullName = fullName;
        this.userId = userId;
        this.email = email;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
        this.guardianEmail = guardianEmail;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getSecurityQuestion() {
        return securityQuestion;
    }

    public void setSecurityQuestion(String securityQuestion) {
        this.securityQuestion = securityQuestion;
    }

    public String getSecurityAnswer() {
        return securityAnswer;
    }

    public void setSecurityAnswer(String securityAnswer) {
        this.securityAnswer = securityAnswer;
    }

    public String getGuardianEmail() {
        return guardianEmail;
    }

    public void setGuardianEmail(String guardianEmail) {
        this.guardianEmail = guardianEmail;
    }
}


