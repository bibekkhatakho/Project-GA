package com.project.group.projectga.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.project.group.projectga.R;

import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SignUpActivity extends CoreActivity implements View.OnClickListener{

    @BindView(R.id.fullNameTextInputLayout)
    protected TextInputLayout fullNameTextInputLayout;
    @BindView(R.id.fullNameTextInputEditText)
    protected TextInputEditText fullNameTextInputEditText;
    @BindView(R.id.emailTextInputLayout)
    protected TextInputLayout emailTextInputLayout;
    @BindView(R.id.emailTextEditText)
    protected TextInputEditText emailTextEditText;
    @BindView(R.id.passwordTextInputLayout)
    protected TextInputLayout passwordTextInputLayout;
    @BindView(R.id.passwordTextEditText)
    protected TextInputEditText passwordTextEditText;
    @BindView(R.id.confirmpasswordTextInputLayout)
    protected TextInputLayout confirmPasswordTextInputLayout;
    @BindView(R.id.confirmpasswordTextEditText)
    protected TextInputEditText confirmPasswordTextEditText;
    @BindView(R.id.nextButton)
    protected FloatingActionButton nextButton;

    @BindView(R.id.toolbar)
    protected Toolbar toolbar;

    protected String fullName;
    //String reference of password
    protected String email;
    //String reference of password
    protected String password;
    //String reference of confirmpassword
    protected String confirmPassword;
    //Future reference for Firebase Authentication
    private FirebaseAuth firebaseAuth;

    DatabaseReference databaseReference;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        setSupportActionBar(toolbar);

        nextButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        if(view == nextButton){
            authUser();
        }

    }

    private void authUser()
    {
        fullName = fullNameTextInputEditText.getText().toString().trim();
        email = emailTextEditText.getText().toString().trim();
        password = passwordTextEditText.getText().toString().trim();
        confirmPassword = confirmPasswordTextEditText.getText().toString().trim();

        if (!validateFullName(fullName)) {
            return;
        }

        if (!validateEmail(email)) {
            return;
        }
        if (!validateSetPass(password)) {
            return;
        }
        if (!validateConfirmPass(password, confirmPassword)) {
            return;
        }

        showProgressDialog("Registering the user...");

        firebaseAuth.createUserWithEmailAndPassword(email,confirmPassword).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                if(task.isSuccessful())
                {
                    hideProgressDialog();
                    Toast.makeText(SignUpActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                    onAuthSuccess(task.getResult().getUser());
                }
                else
                    hideProgressDialog();
                Toast.makeText(SignUpActivity.this, "Could not Register user", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onAuthSuccess(FirebaseUser user) {
        // Write new user
        writeNewUser(user.getUid(), user.getEmail());
        Intent contactIntent = new Intent(SignUpActivity.this, ContactDetailsActivity.class);
        contactIntent.putExtra("fullName", fullName);
        startActivity(contactIntent);
        finish();
    }

    private void writeNewUser(String userId, String email) {

        databaseReference.child("users").child(userId);
        databaseReference.child("users").child(userId).child("email").setValue(email);
        Intent contactIntent = new Intent(SignUpActivity.this, ContactDetailsActivity.class);
        contactIntent.putExtra("fullName", fullName);
        startActivity(contactIntent);
        finish();

    }

    private boolean validateFullName(String name) {
        boolean valid = true;
        String textOnlyRegex = "^[\\p{L} .'-]+$";
        if (TextUtils.isEmpty(name) || !Pattern.matches(textOnlyRegex, name)) {
            fullNameTextInputLayout.setError("Enter a valid name");
            valid = false;
        } else {
            fullNameTextInputLayout.setError(null);
        }
        return valid;
    }

    private boolean validateEmail(String emailText) {

        if (emailText.isEmpty() || !isValidEmail(emailText)) {
            emailTextInputLayout.setError("Please enter a valid Email address");
            return false;
        } else {
            emailTextInputLayout.setError(null);
        }

        return true;
    }

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean validateSetPass(String setPass) {
        if (setPass.length() < 8) {
            passwordTextInputLayout.setError("Password should be a minimum of 8 characters");
            return false;
        } else {
            passwordTextInputLayout.setError(null);
        }
        return true;
    }

    private boolean validateConfirmPass(String setPass, String confirmPass) {
        if (confirmPass.compareTo(setPass) != 0) {
            confirmPasswordTextInputLayout.setError("Passwords don't match. Please try again");
            return false;
        } else {
            confirmPasswordTextInputLayout.setError(null);
        }
        return true;
    }



    @Override
    public void onStart() {
        super.onStart();
        // Check auth on Activity start
        if (firebaseAuth.getCurrentUser() != null) {
            onAuthSuccess(firebaseAuth.getCurrentUser());
        }
    }


}
