package com.project.group.projectga.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.project.group.projectga.R;
import com.project.group.projectga.adapters.Voice;
import com.project.group.projectga.helpers.MapsCustomIconSpinnerAdapter;
import com.project.group.projectga.models.CustomIcons;
import com.project.group.projectga.models.MapMarkers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by MitchelSmith on 7/25/2017.
 */

public class MapMarkerActivity extends CoreActivity implements View.OnFocusChangeListener {

    @BindView(R.id.toolbar)
    protected Toolbar toolbar;
    @BindView(R.id.mapMarker_key)
    protected TextView mapMarker_key;
    @BindView(R.id.addressTextInputEditText)
    protected TextInputEditText addressTextInputEditText;
    @BindView(R.id.addressTextInputLayout)
    protected TextInputLayout addressTextInputLayout;
    @BindView(R.id.addressDescTextInputEditText)
    protected TextInputEditText addressDescriptionTextInputEditText;
    @BindView(R.id.addressDescTextView)
    protected TextView addressDescriptionTextView;
    @BindView(R.id.chooseIconTextView)
    protected TextView chooseIconTextView;
    @BindView(R.id.markerIconSpinner)
    protected Spinner markerIconSpinner;
    @BindView(R.id.doneButton)
    protected FloatingActionButton doneButton;

    Voice voice;
    boolean isPlaying;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    private StorageReference storageReference;
    private final int REQ_CODE_SPEECH_INPUT_SHORT = 100;
    private final int REQ_CODE_SPEECH_INPUT_LONG = 200;


    MapMarkers mapMarkers;
    ValueEventListener valueEventListener;

    String mapMarkersKey = null;
    String userId;

    String address;
    String latitude;
    String longitude;

    private final int MAX_WORD_LIMIT = 10;
    private final int REQ_CODE_SPEECH_INPUT = 100;

    private InputFilter mInputFilter;

    ArrayList<CustomIcons> customIcons;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_marker);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();

        if(extras !=null) {
            address = extras.getString("ADDRESS").trim();
            latitude = extras.getString("LAT").trim();
            longitude = extras.getString("LONG").trim();
        }

        customIcons=new ArrayList<>();

        customIcons.add(new CustomIcons(R.drawable.general));
        customIcons.add(new CustomIcons(R.drawable.home));
        customIcons.add(new CustomIcons(R.drawable.work));
        customIcons.add(new CustomIcons(R.drawable.hospital));
        customIcons.add(new CustomIcons(R.drawable.airport));
        customIcons.add(new CustomIcons(R.drawable.firestation));
        customIcons.add(new CustomIcons(R.drawable.police));
        customIcons.add(new CustomIcons(R.drawable.gasstation));


        MapsCustomIconSpinnerAdapter customIconsAdapter = new MapsCustomIconSpinnerAdapter(this,R.layout.maps_icons_spinneritem,customIcons);
        markerIconSpinner.setAdapter(customIconsAdapter);

        addressTextInputEditText.setText(address);
        isPlaying = false;
        voice = new Voice(getApplicationContext());
        addressTextInputEditText.setOnFocusChangeListener(this);
        addressDescriptionTextInputEditText.setOnFocusChangeListener(this);
        markerIconSpinner.setOnFocusChangeListener(this);

        addressDescriptionTextInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                int totalWordCount = getWordsCount(s.toString());

                // totalWordCount = 0 means a new word is going to start
                if (count == 0 && totalWordCount >= MAX_WORD_LIMIT) {
                    forceFilter(addressDescriptionTextInputEditText, addressDescriptionTextInputEditText.getText().length());
                    addressDescriptionTextView.setHint("Exceeded the word limit of 10 words");
                } else {
                    removeFilter(addressDescriptionTextInputEditText);
                    addressDescriptionTextView.setHint(getString(R.string.shortDescSpecial));
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

        if (getUid() != null) {
            userId = getUid();
            firebaseAuth = FirebaseAuth.getInstance();
            databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("Map Markers");
            storageReference = FirebaseStorage.getInstance().getReference();

        } else {
            onAuthFailure();
        }

        mapMarkersKey = null;

        if(intent!=null) {
            mapMarkersKey = intent.getStringExtra("Key");
        }


        if (mapMarkersKey==null)
        {
            mapMarker_key.setText("");
        }

        else
        {
            final String mapMarkersKeyValue = mapMarkersKey;
            mapMarker_key.setText(mapMarkersKey);
            valueEventListener = new ValueEventListener() {
                public void onDataChange(DataSnapshot dataSnapshot) {
                    DataSnapshot snapshot=dataSnapshot.child(mapMarkersKeyValue);
                    mapMarkers = snapshot.getValue(MapMarkers.class);
                    if(mapMarkers !=null) {
                        addressTextInputEditText.setText(mapMarkers.getAddress());
                        addressDescriptionTextInputEditText.setText(mapMarkers.getAddressDescription());
                        markerIconSpinner.setSelection(Integer.parseInt(mapMarkers.getMarkerIcon()));
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
                writeAddressList();
            }
        });
    }

    private void writeAddressList() {
        showProgressDialog("Saving person's details...Please wait");

        String addressDescription = addressDescriptionTextInputEditText.getText().toString().trim();
        String markerIcon = String.valueOf(markerIconSpinner.getSelectedItemPosition());

        if (!validateAddress(address)) {
            hideProgressDialog();
            return;
        }
        if (!validateAddressDescription(addressDescription)) {
            hideProgressDialog();
            return;
        }

        HashMap<String, String> addressListMap = new HashMap<>();
        addressListMap.put("address", address);
        addressListMap.put("addressDescription", addressDescription);
        addressListMap.put("latitude", latitude);
        addressListMap.put("longitude", longitude);
        addressListMap.put("markerIcon", markerIcon);

        databaseReference.push().setValue(addressListMap);
        hideProgressDialog();
        Toast.makeText(this, "Address information Saved!", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(MapMarkerActivity.this,MainMenuActivity.class);
        intent.putExtra("mapMarkerFlag", true);
        startActivity(intent);
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQ_CODE_SPEECH_INPUT && data!=null ){
            ArrayList<String> result = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            addressDescriptionTextInputEditText.setText(result.get(0));
        }
    }

    private boolean validateAddress(String address) {
        if (address.isEmpty()){
            addressTextInputLayout.setError("Person Name cannot be empty. Please enter a value");
            return false;
        }else{
            addressTextInputLayout.setError(null);
        }
        return true;
    }

    private boolean validateAddressDescription(String addressDescription) {
        if (addressDescription.isEmpty()) {
            addressDescriptionTextInputEditText.setError("Address Description cannot be empty. Please enter a valid Address Description");
            return false;
        } else {
            addressDescriptionTextInputEditText.setError(null);
        }
        return true;
    }

    private void onAuthFailure() {
        // Write new user
        Intent intent = new Intent(MapMarkerActivity.this, MainMenuActivity.class);
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
                    validateAddress(addressTextInputEditText.getText().toString().trim());
                } else {
                    addressTextInputLayout.setError(null);
                }

                break;

            case R.id.addressDescTextInputEditText:
                if (!hasFocus) {
                    validateAddressDescription(addressDescriptionTextInputEditText.getText().toString().trim());
                } else {
                    addressDescriptionTextInputEditText.setError(null);
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
        if (v.getId() == R.id.playDescription){

            String addressDescription = addressDescriptionTextInputEditText.getText().toString().trim();
            voice.say(addressDescription);


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
    }
}
