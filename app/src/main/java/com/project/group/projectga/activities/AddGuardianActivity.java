package com.project.group.projectga.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.creativityapps.gmailbackgroundlibrary.BackgroundMail;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.group.projectga.R;
import com.project.group.projectga.models.Profile;
import com.project.group.projectga.preferences.Preferences;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddGuardianActivity extends CoreActivity implements View.OnClickListener, View.OnFocusChangeListener{

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

    String userEmailAddress;
    String guardianUserEmail;
    String fullName;

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

        } else {
            onAuthFailure();
        }

        addGuardianEmailTextInputEditText.setOnFocusChangeListener(this);

        doneButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view == doneButton){
            createUserProfile();
        }
    }

    private void createUserProfile(){
        showProgressDialog("Saving...");

        Intent intent = getIntent();
        fullName = intent.getStringExtra("fullName");
        userEmailAddress = intent.getStringExtra("userEmailAddress");
        String phoneNumber = intent.getStringExtra("phoneNumber");
        String dateOfBirth = intent.getStringExtra("dateofBirth");
        String userType = intent.getStringExtra("userType");
        guardianUserEmail = addGuardianEmailTextInputEditText.getText().toString().trim();

        if (!validateForm(guardianUserEmail, userEmailAddress)) {
            hideProgressDialog();
            return;
        }
        databaseReference.child("phoneNumber").setValue(phoneNumber);
        databaseReference.child("dateOfBirth").setValue(dateOfBirth);
        databaseReference.child("guardianEmail").setValue(guardianUserEmail);
        databaseReference.child("userType").setValue(userType);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Profile profile = dataSnapshot.getValue(Profile.class);
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = preferences.edit();
                if(profile != null) {
                    editor.putString(Preferences.NAME, profile.getFullName());
                    editor.putString(Preferences.EMAIL, profile.getEmail());
                    editor.putString(Preferences.USER_TYPE, profile.getUserType());
                }
                editor.putString(Preferences.USERID, getUid());
                editor.apply();

                String subject = "App Invitation!";
                String body = "Hi,\nThis email is to inform you that Mr." + fullName + " has invited you to be the guardian, as he/she is suffering from Alzheimer's."
                        + " Check out the Google Play store to find our application to protect your loved one. \n APP NAME: Google Alzheimer \n\nRegards,\nGA Team.";
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
                                    Intent intent = new Intent(AddGuardianActivity.this, MainMenuActivity.class);
//                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
//                                    finish();
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

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        hideProgressDialog();
        Toast.makeText(this, "Profile Created!", Toast.LENGTH_SHORT).show();
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
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {

            case R.id.emailTextEditText:
                if (!hasFocus) {
                    validateForm(addGuardianEmailTextInputEditText.getText().toString().trim(), userEmailAddress);
                } else {
                    addGuardianEmailTextInputLayout.setError(null);
                }
                break;
        }
    }
}
