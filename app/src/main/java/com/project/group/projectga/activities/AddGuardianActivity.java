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
import android.view.View;
import android.widget.Toast;

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

        String fullName = intent.getStringExtra("fullName");
        userEmailAddress = intent.getStringExtra("userEmailAddress");
        String phoneNumber = intent.getStringExtra("phoneNumber");
        String dateOfBirth = intent.getStringExtra("dateofBirth");
        String securityAnswer = intent.getStringExtra("securityAnswer");
        String securityQuestion = intent.getStringExtra("securityQuestion");
        String guardianUserEmail = addGuardianEmailTextInputEditText.getText().toString().trim();

        if (!validateForm(guardianUserEmail, userEmailAddress)) {
            hideProgressDialog();
            return;
        }

        databaseReference.child("fullName").setValue(fullName);
        databaseReference.child("phoneNumber").setValue(phoneNumber);
        databaseReference.child("dateOfBirth").setValue(dateOfBirth);
        databaseReference.child("securityAnswer").setValue(securityAnswer);
        databaseReference.child("securityQuestion").setValue(securityQuestion);
        databaseReference.child("guardianUserEmail").setValue(guardianUserEmail);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Profile profile = dataSnapshot.getValue(Profile.class);
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AddGuardianActivity.this);
                SharedPreferences.Editor editor = preferences.edit();
                if(profile != null) {
                    editor.putString(Preferences.NAME, profile.getFullName());
                    editor.putString(Preferences.EMAIL, profile.getEmail());
                }
                editor.putString(Preferences.USERID, getUid());
                editor.apply();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        hideProgressDialog();
        Toast.makeText(this, "Profile Created!", Toast.LENGTH_SHORT).show();
        Intent mainMenuIntent = new Intent(AddGuardianActivity.this, MainMenuActivity.class);
        mainMenuIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainMenuIntent);
        finish();
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
