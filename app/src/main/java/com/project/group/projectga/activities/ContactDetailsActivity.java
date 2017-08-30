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
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.project.group.projectga.R;
import com.project.group.projectga.models.Profile;
import com.project.group.projectga.preferences.Preferences;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ContactDetailsActivity extends CoreActivity implements View.OnClickListener {

    @BindView(R.id.userTypeSpinner)
    protected Spinner userTypeSpinner;
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
    @BindView(R.id.toolbar)
    protected Toolbar toolbar;

    private FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    DatabaseReference databaseReferenceGuardian;
    DatabaseReference guardianPhoneNumber;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_details);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        // Initializing a String Array
        final String[] userType = new String[]{
                "Standard User",
                "Guardian User"
        };

        final List<String> userTypeList = new ArrayList<>(Arrays.asList(userType));

        // Initializing an ArrayAdapter
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(
                this,R.layout.spinner_item,userTypeList);

        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        userTypeSpinner.setAdapter(spinnerArrayAdapter);

        Intent intent = getIntent();
        final String userEmailAddress = intent.getStringExtra("userEmailAddress");

        databaseReferenceGuardian = FirebaseDatabase.getInstance().getReference().child("guardians");
        Query query = databaseReferenceGuardian.orderByChild("guardianEmail").equalTo(userEmailAddress);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    userTypeList.remove(0);
                    spinnerArrayAdapter.notifyDataSetChanged();
                }else{
                    userTypeList.remove(1);
                    spinnerArrayAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw databaseError.toException();
            }
        });

        phoneNumberTextInputEditText.addTextChangedListener(new PhoneNumberFormattingTextWatcher() {
            //we need to know if the user is erasing or inputing some new character
            private boolean backspacingFlag = false;
            //we need to block the :afterTextChanges method to be called again after we just replaced the EditText text
            private boolean editedFlag = false;
            //we need to mark the cursor position and restore it after the edition
            private int cursorComplement;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //we store the cursor local relative to the end of the string in the EditText before the edition
                cursorComplement = s.length()-phoneNumberTextInputEditText.getSelectionStart();
                //we check if the user ir inputing or erasing a character
                if (count > after) {
                    backspacingFlag = true;
                } else {
                    backspacingFlag = false;
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // nothing to do here =D
            }

            @Override
            public void afterTextChanged(Editable s) {
                String string = s.toString();
                //what matters are the phone digits beneath the mask, so we always work with a raw string with only digits
                String phone = string.replaceAll("[^\\d]", "");

                //if the text was just edited, :afterTextChanged is called another time... so we need to verify the flag of edition
                //if the flag is false, this is a original user-typed entry. so we go on and do some magic
                if (!editedFlag) {

                    //we start verifying the worst case, many characters mask need to be added
                    //example: 999999999 <- 6+ digits already typed
                    // masked: (999) 999-999
                    if (phone.length() >= 6 && !backspacingFlag) {
                        //we will edit. next call on this textWatcher will be ignored
                        editedFlag = true;
                        //here is the core. we substring the raw digits and add the mask as convenient
                        String ans = "(" + phone.substring(0, 3) + ") " + phone.substring(3,6) + "-" + phone.substring(6);
                        phoneNumberTextInputEditText.setText(ans);
                        //we deliver the cursor to its original position relative to the end of the string
                        phoneNumberTextInputEditText.setSelection(phoneNumberTextInputEditText.getText().length()-cursorComplement);

                        //we end at the most simple case, when just one character mask is needed
                        //example: 99999 <- 3+ digits already typed
                        // masked: (999) 99
                    } else if (phone.length() >= 3 && !backspacingFlag) {
                        editedFlag = true;
                        String ans = "(" +phone.substring(0, 3) + ") " + phone.substring(3);
                        phoneNumberTextInputEditText.setText(ans);
                        phoneNumberTextInputEditText.setSelection(phoneNumberTextInputEditText.getText().length()-cursorComplement);
                    }
                    // We just edited the field, ignoring this cicle of the watcher and getting ready for the next
                } else {
                    editedFlag = false;
                }
            }
        });


        if (getUid() != null) {
            String userId = getUid();
            firebaseAuth = FirebaseAuth.getInstance();
            databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userId);

        } else {
            onAuthFailure();
        }

        nextButtonGuardian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeUserProfile();
            }
        });

        dateofBirthTextInputEditText.setOnClickListener(this);
    }

    private void writeUserProfile() {

        showProgressDialog("Saving...");

        String phoneNumber = phoneNumberTextInputEditText.getText().toString().trim();
        String dateOfBirth = dateofBirthTextInputEditText.getText().toString().trim();
        String userType = userTypeSpinner.getSelectedItem().toString();


        if (!validateForm(phoneNumber, dateOfBirth)) {
            hideProgressDialog();
            return;
        }
        databaseReference.child("phoneNumber").setValue(phoneNumber);
        databaseReference.child("dateOfBirth").setValue(dateOfBirth);
        databaseReference.child("userType").setValue(userType);

        if(userType.equalsIgnoreCase("Guardian User")) {
            guardianPhoneNumber = FirebaseDatabase.getInstance().getReference().child("guardians").child("guardianEmails").child("guardianNumber");
            guardianPhoneNumber.setValue(phoneNumber);
        }


        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Profile profile = dataSnapshot.getValue(Profile.class);
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ContactDetailsActivity.this);
                SharedPreferences.Editor editor = preferences.edit();
                if (profile != null) {
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
        Toast.makeText(this, "User Profile Created.. Redirecting", Toast.LENGTH_SHORT).show();
        if(userType.equalsIgnoreCase("Standard User")) {
            Intent addGuardianIntent = new Intent(ContactDetailsActivity.this, AddGuardianActivity.class);
            addGuardianIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(addGuardianIntent);
            finish();
        }else{
            Intent mainMenuIntent = new Intent(ContactDetailsActivity.this, IntroDialogGuardianActivity.class);
            mainMenuIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainMenuIntent);
            finish();
        }

    }

    private boolean validateForm(String phoneNumber, String dateOfBirth) {

        boolean valid = true;

        if (TextUtils.isEmpty(dateOfBirth)) {
            dateOfBirthTextInputLayout.setError("Please enter your Date of Birth");
            valid = false;
        } else {
            dateOfBirthTextInputLayout.setError(null);
        }
        if (TextUtils.isEmpty(phoneNumber)) {
            phoneNumberTextInputLayout.setError("Please enter your phone number");
            valid = false;
        } else {
            phoneNumberTextInputLayout.setError(null);
        }
        if(!isValidPhoneNumber(phoneNumber)){
            phoneNumberTextInputLayout.setError("Please enter a valid Phone Number");
            valid = false;
        }else{
            phoneNumberTextInputLayout.setError(null);
        }

        if(phoneNumber.length() <14){
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

    @Override
    public void onClick(View v) {
        if (v == dateofBirthTextInputEditText) {
            Calendar c = Calendar.getInstance();
            int mYear = c.get(Calendar.YEAR);
            int mMonth = c.get(Calendar.MONTH);
            int mDay = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {
                            Calendar calendar = new GregorianCalendar(year, monthOfYear, dayOfMonth);
                            Calendar minAdultAge = new GregorianCalendar();
                            minAdultAge.add(Calendar.YEAR, -10);
                            if (minAdultAge.before(calendar)) {
                                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM-dd-yyyy", Locale.US);
                                String startDate = dateFormat.format(calendar.getTime());
                                dateOfBirthTextInputLayout.setError("You must be atleast 10 years old to use this app.");
                                dateofBirthTextInputEditText.setText(startDate);
                                nextButtonGuardian.setEnabled(false);
                                nextButtonGuardian.setAlpha(0.5f);
                            } else {
                                calendar.set(year, monthOfYear, dayOfMonth);
                                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM-dd-yyyy", Locale.US);
                                String startDate = dateFormat.format(calendar.getTime());
                                dateOfBirthTextInputLayout.setError(null);
                                dateofBirthTextInputEditText.setText(startDate);
                                nextButtonGuardian.setEnabled(true);
                                nextButtonGuardian.setAlpha(1.0f);
                            }
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.getDatePicker().setMaxDate(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000).getTime());
            datePickerDialog.show();
        }
    }

    @Override
    public void onBackPressed() {
        return;
    }

}