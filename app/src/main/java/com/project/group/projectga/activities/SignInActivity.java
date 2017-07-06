package com.project.group.projectga.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
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

import static java.lang.Thread.sleep;

public class SignInActivity extends CoreActivity implements View.OnFocusChangeListener {


    @BindView(R.id.emailLoginTextInputLayout)
    protected TextInputLayout emailLoginTextInputLayout;
    @BindView(R.id.emailLoginTextInputEditText)
    protected TextInputEditText emailLoginTextInputEditText;
    @BindView(R.id.passwordLoginInputLayout)
    protected TextInputLayout passwordLoginInputLayout;
    @BindView(R.id.passwordLoginEditText)
    protected TextInputEditText passwordLoginEditText;
    @BindView(R.id.signupTV)
    protected TextView signUpTextView;
    @BindView(R.id.loginButton)
    protected TextView loginButton;
    @BindView(R.id.forgotPasswordTextView)
    protected TextView forgotPasswordTextView;

    //Code for Google Sign In - Start

    @BindView(R.id.signInWithGoogle)
    protected Button signInWithGoogleButton;

    private final static int RC_SIGN_IN = 2;
    private static final String TAG = "MainActivity";
    FirebaseAuth.AuthStateListener firebaseAuthListener;
    GoogleApiClient mGoogleApiClient;

    @Override
    public void onStart() {
        super.onStart();

        firebaseAuth.addAuthStateListener(firebaseAuthListener);
    }

    //Code for Google Sign In - End

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        ButterKnife.bind(this);

        firebaseAuth = FirebaseAuth.getInstance();

        // Configure Google Sign In - Start

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() != null){
                    Intent mainMenuIntent = new Intent(SignInActivity.this, ContactDetailsActivity.class);
                    mainMenuIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(mainMenuIntent);
                }
            }
        };

        signInWithGoogleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(SignInActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Configure Google Sign In - End

        emailLoginTextInputEditText.setOnFocusChangeListener(this);
        passwordLoginEditText.setOnFocusChangeListener(this);
        emailLoginTextInputEditText.addTextChangedListener(new MyTextWatcher(emailLoginTextInputEditText));
        passwordLoginEditText.addTextChangedListener(new MyTextWatcher(passwordLoginEditText));

        signUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signUpIntent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(signUpIntent);
                finish();
            }
        });

        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this,ResetPasswordActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInUser();
            }
        });

    }

    // Google Sign In Integration - Start
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(SignInActivity.this, "Authentication Failed. Please try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }

    // Google Sign In Integration - End


    private void signInUser(){
        String email = emailLoginTextInputEditText.getText().toString();
        String password = passwordLoginEditText.getText().toString();

        if (!validateEmail(email)) {
            return;
        }
        if (!validateSetPass(password)) {
            return;
        }
        showProgressDialog("Signing in...");
        firebaseAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            hideProgressDialog();
                            Toast.makeText(SignInActivity.this, task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();

                        }else if(task.isSuccessful()){
                            hideProgressDialog();
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(getUid());
                            databaseReference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Profile profile = dataSnapshot.getValue(Profile.class);
                                    Log.e("key", dataSnapshot.getKey());
                                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SignInActivity.this);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    if(profile !=null){
                                        editor.putString(Preferences.EMAIL, profile.getEmail());
                                        editor.putString(Preferences.NAME, profile.getFullName());
                                        editor.putString(Preferences.USER_TYPE, profile.getUserType());
                                    }
                                    editor.putString(Preferences.USERID, getUid());
                                    editor.apply();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                            Intent loginIntent = new Intent(SignInActivity.this,MainMenuActivity.class);
                            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            try {
                                sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            startActivity(loginIntent);
                            finish();
                        }
                    }
                });
    }

    private boolean validateEmail(String emailText) {

        if (emailText.isEmpty() || !isValidEmail(emailText)) {
            emailLoginTextInputLayout.setError("Please enter a valid Email address");
            return false;
        } else {
            emailLoginTextInputLayout.setError(null);
        }

        return true;
    }

    private boolean validateSetPass(String setPass) {
        if (TextUtils.isEmpty(setPass)) {
            passwordLoginInputLayout.setError("Password cannot be empty");
            return false;
        } else {
            passwordLoginInputLayout.setError(null);
        }
        return true;
    }

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    @Override
    public void onFocusChange(View v, boolean hasFocus) {

        switch (v.getId()) {

            case R.id.emailTextEditText:
                if (!hasFocus) {
                    validateEmail(emailLoginTextInputEditText.getText().toString().trim());
                } else {
                    emailLoginTextInputLayout.setError(null);
                }

                break;
            case R.id.passwordTextEditText:
                if (!hasFocus) {
                    validateSetPass(passwordLoginEditText.getText().toString().trim());
                } else {
                    passwordLoginInputLayout.setError(null);
                }

                break;
        }
    }

    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.emailTextEditText:
                    emailLoginTextInputLayout.setError(null);
                    break;
                case R.id.passwordTextEditText:
                    passwordLoginInputLayout.setError(null);
                    break;
            }
        }
    }

}