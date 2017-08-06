package com.project.group.projectga.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.project.group.projectga.R;
import com.project.group.projectga.adapters.Voice;
import com.project.group.projectga.models.ImportantPeople;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ramjiseetharaman on 7/13/17.
 */

@SuppressWarnings("VisibleForTests")
public class ImportantPeoplesActivity extends CoreActivity implements View.OnFocusChangeListener {

    @BindView(R.id.personNameTextInputLayout)
    protected TextInputLayout personNameTextInputLayout;
    @BindView(R.id.personNameTextInputEditText)
    protected TextInputEditText personNameTextInputEditText;
    @BindView(R.id.personRelationTextInputLayout)
    protected TextInputLayout personRelationTextInputLayout;
    @BindView(R.id.personRelationTextInputEditText)
    protected TextInputEditText personRelationTextInputEditText;
    @BindView(R.id.shortDescTextView)
    protected TextView shortDescTextView;
    @BindView(R.id.shortDescriptionTextInputLayout)
    protected TextInputLayout shortDescriptionTextInputLayout;
    @BindView(R.id.shortDescriptionTextInputEditText)
    protected TextInputEditText shortDescrptionTextInputEditText;
    @BindView(R.id.longDescTextView)
    protected TextView longDescTextView;
    @BindView(R.id.longDescriptionTextInputLayout)
    protected TextInputLayout longDescriptionTextInputLayout;
    @BindView(R.id.longDescriptionTextInputEditText)
    protected TextInputEditText longDescriptionTextInputEditText;
    @BindView(R.id.personImage)
    protected CircularImageView personImage;
    @BindView(R.id.peoples_key)
    protected TextView peoples_key;
    @BindView(R.id.doneButton)
    protected FloatingActionButton doneButton;
    @BindView(R.id.toolbar)
    protected Toolbar toolbar;

    String imgURL;
    Voice voice;
    boolean isPlaying;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    private StorageReference storageReference;

    ImportantPeople importantPeople;
    ValueEventListener valueEventListener;

    String importantPeoplesKey = null;
    String userId;

    public static final int RC_CAMERA_CODE = 123;
    private final int MAX_WORD_LIMIT_SHORT = 10;
    private final int MAX_WORD_LIMIT_LONG = 50;
    private final int REQ_CODE_SPEECH_INPUT_SHORT = 100;
    private final int REQ_CODE_SPEECH_INPUT_LONG = 200;


    private InputFilter mInputFilter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_important_people);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        isPlaying = false;
        voice  = new Voice(getApplicationContext());
        personNameTextInputEditText.setOnFocusChangeListener(this);
        personRelationTextInputEditText.setOnFocusChangeListener(this);
        shortDescrptionTextInputEditText.setOnFocusChangeListener(this);
        longDescriptionTextInputEditText.setOnFocusChangeListener(this);

        shortDescrptionTextInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                int totalWordCount = getWordsCount(s.toString());

                // totalWordCount = 0 means a new word is going to start
                if (count == 0 && totalWordCount >= MAX_WORD_LIMIT_SHORT) {
                    forceFilter(shortDescrptionTextInputEditText, shortDescrptionTextInputEditText.getText().length());
                    shortDescTextView.setText(getString(R.string.shortDescSpecialError));
                } else {
                    removeFilter(shortDescrptionTextInputEditText);
                    shortDescTextView.setText(getString(R.string.shortDescSpecial));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        longDescriptionTextInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                int totalWordCount = getWordsCount(s.toString());

                // totalWordCount = 0 means a new word is going to start
                if (count == 0 && totalWordCount >= MAX_WORD_LIMIT_LONG) {
                    forceFilter(longDescriptionTextInputEditText, longDescriptionTextInputEditText.getText().length());
                    longDescTextView.setText(getString(R.string.longDescSpecialError));
                } else {
                    removeFilter(shortDescrptionTextInputEditText);
                    longDescTextView.setText(getString(R.string.longDescSpecial));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        Intent intent = getIntent();

        personImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, RC_CAMERA_CODE);
            }
        });

        if (getUid() != null) {
            userId = getUid();
            firebaseAuth = FirebaseAuth.getInstance();
            databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("Important Peoples");
            storageReference = FirebaseStorage.getInstance().getReference();

        } else {

            onAuthFailure();
        }

        importantPeoplesKey = null;

        if(intent!=null) {
            importantPeoplesKey = intent.getStringExtra("Key");
        }


        if (importantPeoplesKey==null)
        {
            peoples_key.setText("");
        }

        else
        {
            final String importantPeoplesKeyValue = importantPeoplesKey;
            peoples_key.setText(importantPeoplesKey);
            valueEventListener = new ValueEventListener() {
                public void onDataChange(DataSnapshot dataSnapshot) {
                    DataSnapshot snapshot=dataSnapshot.child(importantPeoplesKeyValue);
                    importantPeople = snapshot.getValue(ImportantPeople.class);
                    if(importantPeople !=null) {
                        personNameTextInputEditText.setText(importantPeople.getName());
                        personRelationTextInputEditText.setText(importantPeople.getRelation());
                        shortDescrptionTextInputEditText.setText(importantPeople.getShortDescription());
                        longDescriptionTextInputEditText.setText(importantPeople.getLongDescription());
                        Picasso.with(getApplicationContext()).load(importantPeople.getProfile()).placeholder(R.drawable.ic_account_circle_white_24dp).rotate(270.0f).error(R.drawable.ic_error_outline_black_24dp).into(personImage);
                        imgURL = importantPeople.getProfile();
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };

            databaseReference.addValueEventListener(valueEventListener);
        }

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writePersonList();
            }
        });

    }

    private void writePersonList() {
        showProgressDialog("Saving person's details...Please wait");

        String personName = personNameTextInputEditText.getText().toString().trim();
        String personRelation = personRelationTextInputEditText.getText().toString().trim();
        String shortDescription = shortDescrptionTextInputEditText.getText().toString().trim();
        String longDescription = longDescriptionTextInputEditText.getText().toString().trim();

        if (!validatePersonName(personName)) {
            hideProgressDialog();
            return;
        }

        if (!validatePersonRelation(personRelation)) {
            hideProgressDialog();
            return;
        }
        if (!validateShortDescription(shortDescription)) {
            hideProgressDialog();
            return;
        }
        if (!validateLongDescription(longDescription)) {
            hideProgressDialog();
            return;
        }

        HashMap<String, String> personsListMap = new HashMap<>();
        personsListMap.put("name", personName);
        personsListMap.put("relation", personRelation);
        personsListMap.put("shortDescription", shortDescription);
        personsListMap.put("longDescription", longDescription);
        personsListMap.put("profile",imgURL);

        //personsListMap.put("profile", String.valueOf(personImage));
        if(peoples_key.getText().toString().equals("")) {

            databaseReference.push().setValue(personsListMap);
            hideProgressDialog();
            Toast.makeText(this, "Person information Saved!", Toast.LENGTH_SHORT).show();
        }
        else
        {
            databaseReference.child(peoples_key.getText().toString()).updateChildren((java.util.HashMap)personsListMap);
            hideProgressDialog();
            Toast.makeText(this, "Person Information Updated!", Toast.LENGTH_SHORT).show();
        }

        Intent intent = new Intent(ImportantPeoplesActivity.this,MainMenuActivity.class);
        intent.putExtra("importantPeopleFlag", true);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_CAMERA_CODE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            personImage.setImageBitmap(imageBitmap);
            personImage.setDrawingCacheEnabled(true);
            personImage.buildDrawingCache();
            Bitmap bitmap = personImage.getDrawingCache();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] d = baos.toByteArray();

            final UploadTask uploadTask = storageReference.child(userId).putBytes(d);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(ImportantPeoplesActivity.this,taskSnapshot.getDownloadUrl().toString(), Toast.LENGTH_SHORT).show();
                    Toast.makeText(ImportantPeoplesActivity.this, "Person image set", Toast.LENGTH_SHORT).show();
                    imgURL = taskSnapshot.getDownloadUrl().toString();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("fail", "fail");
                }
            });

        }

        if(requestCode == REQ_CODE_SPEECH_INPUT_SHORT && data!=null ){
            ArrayList<String> result = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            shortDescrptionTextInputEditText.setText(result.get(0));
        }

        if(requestCode == REQ_CODE_SPEECH_INPUT_LONG && data!=null ){
            ArrayList<String> result = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            longDescriptionTextInputEditText.setText(result.get(0));
        }
    }

    private boolean validatePersonName(String personName) {

        String textOnlyRegex = "^[\\p{L} .'-]+$";

        if (personName.isEmpty()){
            personNameTextInputLayout.setError("Person Name cannot be empty. Please enter a value");
            return false;
        }else{
            personNameTextInputLayout.setError(null);
        }
        if (TextUtils.isEmpty(personName) || !Pattern.matches(textOnlyRegex, personName)) {
            personNameTextInputLayout.setError("Enter a valid Person name");
            return false;
        } else {
            personNameTextInputLayout.setError(null);
        }
        return true;
    }


    private boolean validatePersonRelation(String personRelation) {

        String textOnly = "^[A-Za-z -]+$";

        if (personRelation.isEmpty()) {
            personRelationTextInputLayout.setError("Relation cannot be empty. Please enter a value");
            return false;
        } else {
            personRelationTextInputLayout.setError(null);
        }
        if(!Pattern.matches(textOnly, personRelation)){
            personRelationTextInputLayout.setError("Please enter a valid relation");
            return false;
        } else{
            personRelationTextInputLayout.setError(null);
        }

        return true;
    }


    private boolean validateShortDescription(String shortDescription) {
        if (shortDescription.isEmpty()) {
            shortDescriptionTextInputLayout.setError("Quick Description cannot be empty. Please enter a valid Quick Descriprtion");
            return false;
        } else {
            shortDescriptionTextInputLayout.setError(null);
        }
        return true;
    }

    private boolean validateLongDescription(String longDescription) {
        if (longDescription.isEmpty()) {
            longDescriptionTextInputLayout.setError("Detailed Description cannot be empty. Please enter a valid Detailed Description");
            return false;
        } else {
            longDescriptionTextInputLayout.setError(null);
        }
        return true;
    }


    private void onAuthFailure() {
        // Write new user
        Intent intent = new Intent(ImportantPeoplesActivity.this, MainMenuActivity.class);
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
    public void onFocusChange(View v, boolean hasFocus) {

        switch (v.getId()) {

            case R.id.personNameTextInputEditText:
                if (!hasFocus) {
                    validatePersonName(personNameTextInputEditText.getText().toString().trim());
                } else {
                    personNameTextInputLayout.setError(null);
                }

                break;

            case R.id.personRelationTextInputEditText:
                if (!hasFocus) {
                    validatePersonRelation(personRelationTextInputEditText.getText().toString().trim());
                } else {
                    personRelationTextInputLayout.setError(null);
                }

                break;

            case R.id.shortDescriptionTextInputEditText:
                if (!hasFocus) {
                    validateShortDescription(shortDescrptionTextInputEditText.getText().toString().trim());
                } else {
                    shortDescriptionTextInputLayout.setError(null);
                }

                break;

            case R.id.longDescriptionTextInputEditText:
                if (!hasFocus) {
                    validateLongDescription(longDescriptionTextInputEditText.getText().toString().trim());
                } else {
                    longDescriptionTextInputLayout.setError(null);
                }

                break;
        }

    }

    private int getWordsCount(String input) {
        String trim = input.trim();
        if (trim.isEmpty())
            return 0;
        return trim.split("\\s+").length; // Separate string by spaces
    }

    // Helping functions to dynamically add or remove filters from your EditText
    private void forceFilter(EditText mEditText, int charCount) {
        mInputFilter = new InputFilter.LengthFilter(charCount);
        mEditText.setFilters(new InputFilter[] { mInputFilter });
    }

    private void removeFilter(EditText mEditText) {
        if (mEditText != null) {
            mEditText.setFilters(new InputFilter[0]);
            mInputFilter = null;
        }
    }

    public void onClick(View v){
        if (v.getId() == R.id.playShortDescription){

                String personName = personNameTextInputEditText.getText().toString().trim();
                String personRelation = personRelationTextInputEditText.getText().toString().trim();
                String shortDescription = shortDescrptionTextInputEditText.getText().toString().trim();
                String shortStr = personName + personRelation + shortDescription;
                voice.say(shortStr);


        }
        else if (v.getId() == R.id.playLongDescription){
            String longStr = longDescriptionTextInputEditText.getText().toString().trim();
            voice.say(longStr);
        }


    }

    public void recordDescription (View v){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));


        if(v.getId() == R.id.recordShortDescription){
            try {
                startActivityForResult(intent, REQ_CODE_SPEECH_INPUT_SHORT);
            } catch (ActivityNotFoundException a) {
                Toast.makeText(getApplicationContext(),
                        getString(R.string.speech_not_supported),
                        Toast.LENGTH_SHORT).show();
            }
        }
        else if (v.getId() == R.id.recordLongDescription){
            try {
                startActivityForResult(intent, REQ_CODE_SPEECH_INPUT_LONG);
            } catch (ActivityNotFoundException a) {
                Toast.makeText(getApplicationContext(),
                        getString(R.string.speech_not_supported),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }



}