package com.project.group.projectga.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
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
import com.satsuware.usefulviews.LabelledSpinner;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ContactDetailsActivity extends CoreActivity implements View.OnClickListener, View.OnFocusChangeListener{

    @BindView(R.id.userTypeSpinner)
    protected LabelledSpinner userTypeSpinner;
    @BindView(R.id.phoneNumberTextInputLayout)
    protected TextInputLayout phoneNumberTextInputLayout;
    @BindView(R.id.phoneNumberTextInputEditText)
    protected TextInputEditText phoneNumberTextInputEditText;
    @BindView(R.id.dateOfBirthTextInputLayout)
    protected TextInputLayout dateOfBirthTextInputLayout;
    @BindView(R.id.dateofBirthTextInputEditText)
    protected TextInputEditText dateofBirthTextInputEditText;
    @BindView(R.id.nextButtonContact)
    protected FloatingActionButton nextButtonGuardian;


    String userType = "Standard";
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_details);
        ButterKnife.bind(this);

        userTypeSpinner.setLabelText(R.string.userType);
        userTypeSpinner.setOnItemChosenListener(new LabelledSpinner.OnItemChosenListener() {
            @Override
            public void onItemChosen(View labelledSpinner, AdapterView<?> adapterView, View itemView, int position, long id) {
                userType = adapterView.getSelectedItem().toString();
            }

            @Override
            public void onNothingChosen(View labelledSpinner, AdapterView<?> adapterView) {
                userType = adapterView.getSelectedItem().toString();
            }
        });

        if (getUid() != null) {
            String userId = getUid();
            firebaseAuth = FirebaseAuth.getInstance();
            databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userId);

        } else {
            onAuthFailure();
        }

        phoneNumberTextInputEditText.setOnFocusChangeListener(this);

        nextButtonGuardian.setOnClickListener(this);
        dateofBirthTextInputEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                int mYear = calendar.get(Calendar.YEAR);
                int mMonth = calendar.get(Calendar.MONTH);
                int mDay = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(ContactDetailsActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                Calendar calendar = Calendar.getInstance();
                                calendar.set(year, monthOfYear, dayOfMonth);
                                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM-dd-yyyy");
                                String startDate = dateFormat.format(calendar.getTime());
                                // TODO: 4/11/17 Deleted the filter for startDate - Added in validateForm() method
                                dateofBirthTextInputEditText.setText(startDate);

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });
    }

    @Override
    public void onClick(View view) {

        if (view == nextButtonGuardian) {
            writeUserProfile();
        }else{
            onAuthFailure();
        }
    }

    private void writeUserProfile() {

        showProgressDialog("Saving...");
        Intent intent = getIntent();

        String fullName = intent.getStringExtra("fullName");
        String userEmailAddress = intent.getStringExtra("emailAddress");

        String phoneNumber = phoneNumberTextInputEditText.getText().toString().trim();
        String dateOfBirth = dateofBirthTextInputEditText.getText().toString().trim();

        if (!validateForm(phoneNumber, dateOfBirth)) {
            hideProgressDialog();
            return;
        }

        if(userType.equalsIgnoreCase("Standard")){
            hideProgressDialog();
            Toast.makeText(this, "User details saved!",Toast.LENGTH_SHORT).show();
            Intent guardianIntent = new Intent(ContactDetailsActivity.this, AddGuardianActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            guardianIntent.putExtra("fullName", fullName);
            guardianIntent.putExtra("userEmailAddress", userEmailAddress);
            guardianIntent.putExtra("phoneNumber", phoneNumber);
            guardianIntent.putExtra("dateofBirth", dateOfBirth);
            guardianIntent.putExtra("userType", userType);
            startActivity(guardianIntent);
            finish();
        }else{
            databaseReference.child("phoneNumber").setValue(phoneNumber);
            databaseReference.child("dateOfBirth").setValue(dateOfBirth);
            databaseReference.child("userType").setValue(userType);

            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Profile profile = dataSnapshot.getValue(Profile.class);
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ContactDetailsActivity.this);
                    SharedPreferences.Editor editor = preferences.edit();
                    if(profile != null) {
                        editor.putString(Preferences.NAME, profile.getFullName());
                        editor.putString(Preferences.EMAIL, profile.getEmail());
                        editor.putString(Preferences.USER_TYPE, profile.getUserType());
                    }
                    editor.putString(Preferences.USERID, getUid());
                    editor.apply();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            hideProgressDialog();
            Toast.makeText(this, "Profile Created", Toast.LENGTH_SHORT).show();
            Intent mainMenuIntent = new Intent(ContactDetailsActivity.this, MainMenuActivity.class);
//            mainMenuIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainMenuIntent);
//            finish();
        }


    }
    private boolean validateForm(String phone, String dob) {
        boolean valid = true;
        if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(dob)) {
            valid = false;
        }
        if(!isValidPhoneNumber(phone)){
            phoneNumberTextInputLayout.setError("Please enter a valid Phone Number");
            valid = false;
        }else{
            phoneNumberTextInputLayout.setError(null);
        }

        if(phone.length() <10){
            phoneNumberTextInputLayout.setError("Phone number should be atleast 10 digits");
            valid = false;
        }else{
            phoneNumberTextInputLayout.setError(null);
        }
        return valid;
    }

    private static boolean isValidPhoneNumber(String phoneNumber) {
        return !TextUtils.isEmpty(phoneNumber) && android.util.Patterns.PHONE.matcher(phoneNumber).matches();
    }



    private void onAuthFailure() {

        Intent intent = new Intent(ContactDetailsActivity.this, SplashScreen.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {

            case R.id.phoneNumberTextInputEditText:
                if (!hasFocus) {
                    validateForm(phoneNumberTextInputEditText.getText().toString().trim(), dateofBirthTextInputEditText.getText().toString().trim());
                } else {
                    phoneNumberTextInputLayout.setError(null);
                }
                break;
        }

    }
}