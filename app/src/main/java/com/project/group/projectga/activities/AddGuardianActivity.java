package com.project.group.projectga.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.creativityapps.gmailbackgroundlibrary.BackgroundMail;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.project.group.projectga.R;
import com.project.group.projectga.preferences.Preferences;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddGuardianActivity extends CoreActivity{

    @BindView(R.id.addGuardianEmailTextInputLayout)
    protected TextInputLayout addGuardianEmailTextInputLayout;
    @BindView(R.id.addGuardianEmailTextInputEditText)
    protected TextInputEditText addGuardianEmailTextInputEditText;

    @BindView(R.id.doneButton)
    protected FloatingActionButton doneButton;
    @BindView(R.id.toolbar)
    protected Toolbar toolbar;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    DatabaseReference databaseReferenceGuardian;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_guardian);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        if (getUid() != null) {
            String userId = getUid();
            firebaseAuth = FirebaseAuth.getInstance();
            databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
            databaseReferenceGuardian = FirebaseDatabase.getInstance().getReference().child("guardians").child(userId);

        } else {
            onAuthFailure();
        }

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUserProfile();
            }
        });
    }

    private void createUserProfile(){
        showProgressDialog("Saving...");

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(AddGuardianActivity.this);

        String fullName = sharedPreferences.getString(Preferences.NAME, "");
        String userEmailAddress = sharedPreferences.getString(Preferences.EMAIL, "");
        Log.d("fullName", fullName);
        Log.d("userEmailAddress", userEmailAddress);

        String guardianUserEmail = addGuardianEmailTextInputEditText.getText().toString().trim();

        if (!validateForm(guardianUserEmail, userEmailAddress)) {
            hideProgressDialog();
            return;
        }

        databaseReference.child("guardianEmail").setValue(guardianUserEmail);
        databaseReferenceGuardian.child("guardianEmail").setValue(guardianUserEmail);

                String subject = "Welcome to the GA Family";
                String body = "Hi,\nThis email is to inform you that Mr." + fullName + " has invited you to be his/her “Alzheimer’s Guardian”. As such, if you accept his invitation you will be able to track his/her."
                        + " whereabouts, to reach out to him/her in case of emergency and to fulfill other roles in order to protect your loved one”. Please check our GA app at Google Play store . \n APP NAME: Google Alzheimer \n\nBest Regards,\nGA Team.";
                if (!TextUtils.isEmpty(guardianUserEmail)) {
                    BackgroundMail.newBuilder(AddGuardianActivity.this)
                            .withUsername("projectgateam@gmail.com")
                            .withPassword("projectga1234")
                            .withMailto(guardianUserEmail)
                            .withType(BackgroundMail.TYPE_PLAIN)
                            .withSubject(subject)
                            .withBody(body)
                            .withOnSuccessCallback(new BackgroundMail.OnSuccessCallback() {
                                @Override
                                public void onSuccess() {
                                    //do some magic
                                    Log.d("Email", "Sent Success");
                                    hideProgressDialog();
                                    Intent intent = new Intent(AddGuardianActivity.this, MainMenuActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .withOnFailCallback(new BackgroundMail.OnFailCallback() {
                                @Override
                                public void onFail() {
                                    //do some magic
                                    Toast.makeText(AddGuardianActivity.this, "Email was not sent due to some issues. Please try again later", Toast.LENGTH_LONG).show();
                                }
                            })
                            .send();
                }
    }

    private boolean validateForm(String guardianEmailAddress, String userEmailAddress) {
        boolean valid = true;

        if (TextUtils.isEmpty(guardianEmailAddress)){
            Toast.makeText(this, "Please enter Email Address", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (guardianEmailAddress.isEmpty() || !isValidEmail(guardianEmailAddress)) {
            addGuardianEmailTextInputLayout.setError("Please enter a valid Email address");
            valid = false;
        } else {
            addGuardianEmailTextInputLayout.setError(null);
        }

        if(guardianEmailAddress.equalsIgnoreCase(userEmailAddress)){
            addGuardianEmailTextInputLayout.setError("Guardian Email address can't be same as your Email Address");
            valid = false;
        }else{
            addGuardianEmailTextInputLayout.setError(null);
        }

        return valid;
    }

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void onAuthFailure() {
        // Write new user
        Intent intent = new Intent(AddGuardianActivity.this, SplashScreen.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check auth on Activity start
        if (firebaseAuth.getCurrentUser() == null) {
            onAuthFailure();
        }
    }

}
