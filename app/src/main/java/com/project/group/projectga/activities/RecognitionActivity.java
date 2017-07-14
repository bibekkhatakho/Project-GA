package com.project.group.projectga.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import com.project.group.projectga.models.Recognition;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ramjiseetharaman on 7/13/17.
 */

public class RecognitionActivity extends CoreActivity {

    @BindView(R.id.personNameTextInputLayout)
    protected TextInputLayout personNameTextInputLayout;
    @BindView(R.id.personNameTextInputEditText)
    protected TextInputEditText personNameTextInputEditText;
    @BindView(R.id.personRelationTextInputLayout)
    protected TextInputLayout personRelationTextInputLayout;
    @BindView(R.id.personRelationTextInputEditText)
    protected TextInputEditText personRelationTextInputEditText;
    @BindView(R.id.shortDescTextInputLayout)
    protected TextInputLayout shortDescrptionTextInputLayout;
    @BindView(R.id.shortDescTextInputEditText)
    protected TextInputEditText shortDescrptionTextInputEditText;
    @BindView(R.id.longDescTextInputLayout)
    protected TextInputLayout longDescriptionTextInputLayout;
    @BindView(R.id.longDescTextInputEditText)
    protected TextInputEditText longDescriptionTextInputEditText;
    @BindView(R.id.personImage)
    protected CircularImageView personImage;
    @BindView(R.id.recog_key)
    protected TextView recog_key;
    @BindView(R.id.doneButton)
    protected FloatingActionButton doneButton;
    @BindView(R.id.toolbar)
    protected Toolbar toolbar;

    Uri imgURL;
    String imageData;


    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    private StorageReference storageReference;

    Recognition recognition;
    ValueEventListener valueEventListener;

    String recognitionKey = null;
    String userId;

    public static final int RC_CAMERA_CODE = 123;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognition);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
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
            databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("personsList");
            storageReference = FirebaseStorage.getInstance().getReference().child("users").child(userId);

        } else {

            onAuthFailure();
        }

        recognitionKey = null;

        if(intent!=null) {
            recognitionKey = intent.getStringExtra("Key");
            //tempURL = intent.getStringExtra("tempUrl");
        }


        if (recognitionKey==null)
        {
            recog_key.setText("");
        }

        else
        {
            //Toast.makeText(this, "TempUrl" + tempURL , Toast.LENGTH_SHORT).show();
            final String recognitionKeyValue = recognitionKey;
            recog_key.setText(recognitionKey);
            valueEventListener = new ValueEventListener() {
                public void onDataChange(DataSnapshot dataSnapshot) {
                    DataSnapshot snapshot=dataSnapshot.child(recognitionKeyValue);
                    recognition = snapshot.getValue(Recognition.class);
                    if(recognition!=null) {
                        personNameTextInputEditText.setText(recognition.getName());
                        personRelationTextInputEditText.setText(recognition.getRelation());
                        shortDescrptionTextInputEditText.setText(recognition.getShortDescription());
                        longDescriptionTextInputEditText.setText(recognition.getLongDescription());
                        storageReference = FirebaseStorage.getInstance().getReference().child("users").child(userId);
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Glide.with(RecognitionActivity.this).load(uri.toString()).into(personImage);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle any errors
                            }
                        });

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

        if (!validateForm(personName, personRelation, shortDescription, longDescription)) {
            hideProgressDialog();
            return;
        }

//        if(imgURL == null){
//            imgURL = tempURL;
//        }

        HashMap<String, String> personsListMap = new HashMap<>();
        personsListMap.put("name", personName);
        personsListMap.put("relation", personRelation);
        personsListMap.put("shortDescription", shortDescription);
        personsListMap.put("longDescription", longDescription);
        personsListMap.put("profile",imgURL);

        //personsListMap.put("profile", String.valueOf(personImage));
        if(recog_key.getText().toString().equals("")) {

            databaseReference.push().setValue(personsListMap);
            hideProgressDialog();
            Toast.makeText(this, "Person information Saved!", Toast.LENGTH_SHORT).show();
        }
        else
        {
            databaseReference.child(recog_key.getText().toString()).updateChildren((java.util.HashMap)personsListMap);
            hideProgressDialog();
            Toast.makeText(this, "Person Information Updated!", Toast.LENGTH_SHORT).show();
        }

        Intent intent = new Intent(RecognitionActivity.this,MainMenuActivity.class);
        intent.putExtra("recognitionFlag", true);
        startActivity(intent);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_CAMERA_CODE && resultCode == RESULT_OK) {

            imgURL = data.getData();
                      Picasso.with(RecognitionActivity.this).load(imgURL).resize(600,550).centerCrop().into(personImage);
        }
    }

    private boolean validateForm(String personName, String personRelation, String shortDesc, String longDesc) {

        boolean valid = true;
        if (TextUtils.isEmpty(personName) || TextUtils.isEmpty(personRelation) || TextUtils.isEmpty(shortDesc) || TextUtils.isEmpty(longDesc)) {
            Toast.makeText(this, "Please enter all mandatory values.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return valid;
    }

    private void onAuthFailure() {
        // Write new user
        Intent intent = new Intent(RecognitionActivity.this, MainMenuActivity.class);
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