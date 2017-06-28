package com.project.group.projectga.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.project.group.projectga.R;
import com.satsuware.usefulviews.LabelledSpinner;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ContactDetailsActivity extends CoreActivity implements View.OnClickListener{

    @BindView(R.id.phoneNumberTextInputLayout)
    protected TextInputLayout phoneNumberTextInputLayout;
    @BindView(R.id.phoneNumberTextInputEditText)
    protected TextInputEditText phoneNumberTextInputEditText;
    @BindView(R.id.dateOfBirthTextInputLayout)
    protected TextInputLayout dateOfBirthTextInputLayout;
    @BindView(R.id.dateofBirthTextInputEditText)
    protected TextInputEditText dateofBirthTextInputEditText;
    @BindView(R.id.securityQuestionSpinner)
    protected LabelledSpinner securityQuestionSpinner;
    @BindView(R.id.securityAnswerTextInputLayout)
    protected TextInputLayout securityAnswerTextInputLayout;
    @BindView(R.id.securityAnswerTextInputEditText)
    protected TextInputEditText securityAnswerTextInputEditText;
    @BindView(R.id.nextButtonContact)
    protected FloatingActionButton nextButtonGuardian;

    String securityQuestion = "What is the name of your first pet?";

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_details);
        ButterKnife.bind(this);

        if (getUid() != null) {
            String userId = getUid();
            firebaseAuth = FirebaseAuth.getInstance();
            databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
        }else{
            onAuthFailure();
        }

        securityQuestionSpinner.setLabelText(R.string.securityQuestion);
        securityQuestionSpinner.setOnItemChosenListener(new LabelledSpinner.OnItemChosenListener() {
            @Override
            public void onItemChosen(View labelledSpinner, AdapterView<?> adapterView, View itemView, int position, long id) {

                securityQuestion = adapterView.getSelectedItem().toString();

            }

            @Override
            public void onNothingChosen(View labelledSpinner, AdapterView<?> adapterView) {
                securityQuestion = adapterView.getSelectedItem().toString();
            }
        });

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

        String phoneNumber = phoneNumberTextInputEditText.getText().toString().trim();
        String dateOfBirth = dateofBirthTextInputEditText.getText().toString().trim();
        String securityAnswer = securityAnswerTextInputEditText.getText().toString().trim();

        if (!validateForm(phoneNumber, dateOfBirth, securityAnswer)) {
            hideProgressDialog();
            return;
        }

        hideProgressDialog();
        Toast.makeText(this, "User details saved!",Toast.LENGTH_SHORT).show();
        Intent guardianIntent = new Intent(ContactDetailsActivity.this, AddGuardianActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        guardianIntent.putExtra("fullName", fullName);
        guardianIntent.putExtra("phoneNumber", phoneNumber);
        guardianIntent.putExtra("dateofBirth", dateOfBirth);
        guardianIntent.putExtra("securityAnswer", securityAnswer);
        guardianIntent.putExtra("securityQuestion", securityQuestion);
        startActivity(guardianIntent);
//        finish();

    }
    private boolean validateForm(String phoneNumber, String dateOfBirth, String securityAnswer) {
        boolean valid = true;
        String textOnlyRegex = "^[\\p{L} .'-]+$";
        if (TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(dateOfBirth) || TextUtils.isEmpty(securityAnswer)) {
            Toast.makeText(this, "Please enter all mandatory fields", Toast.LENGTH_SHORT).show();
            valid = false;
        }
        return valid;
    }

    private void onAuthFailure() {

        Intent intent = new Intent(ContactDetailsActivity.this, SplashScreen.class);
        startActivity(intent);
        finish();
    }
}
