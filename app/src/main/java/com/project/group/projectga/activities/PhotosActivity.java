package com.project.group.projectga.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.group.projectga.R;
import com.project.group.projectga.adapters.GridViewAdapter;
import com.project.group.projectga.adapters.Voice;
import com.project.group.projectga.fragments.GalleryFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.project.group.projectga.models.Memory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Created by ramjiseetharaman on 7/12/17.
 */

public class PhotosActivity extends CoreActivity{
    int int_position;
    @BindView(R.id.gv_folder)
    protected GridView gridView;
    @BindView(R.id.toolbar)
    protected Toolbar toolbar;

    GridViewAdapter adapter;

    String userId;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;

    ArrayList<Memory> memoryList;

    private ImageView playName;
    private ImageView recordName;
    private ImageView playLong;
    private ImageView recordLong;
    private ImageView playLonger;
    private ImageView recordLonger;

    private TextInputLayout eventNameLayout;
    private TextInputLayout eventDateLayout;
    private TextInputLayout shortDescriptionLayout;
    private TextInputLayout longDescriptionLayout;

    private TextInputEditText eventName;
    private TextInputEditText eventDate;
    private TextInputEditText shortDescription;
    private TextInputEditText longDescription;
    private TextView title;
    private Dialog dialogAddMemory;

    private FloatingActionButton cancelButton;
    private FloatingActionButton okButton;

    String memoryKey = null;
    Memory memory;

    Voice voice;

    private final int REQ_CODE_SPEECH_INPUT_NAME = 131;
    private final int REQ_CODE_SPEECH_INPUT_SHORT = 132;
    private final int REQ_CODE_SPEECH_INPUT_LONG = 133;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        voice = new Voice(getApplicationContext());

        gridView = (GridView)findViewById(R.id.gv_folder);
        int_position = getIntent().getIntExtra("value", 0);
        adapter = new GridViewAdapter(this, GalleryFragment.al_images,int_position);
        gridView.setAdapter(adapter);

        if(getUid() != null) {
            userId = getUid();
            firebaseAuth = FirebaseAuth.getInstance();
            databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("Memories");
        } else {
            onAuthFailure();
        }

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                memoryList = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Memory m = snapshot.getValue(Memory.class);
                    String key = snapshot.getKey();
                    m.setKey(key);
                    memoryList.add(m);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, long id) {

                CharSequence options[] = new CharSequence[]{"Add a Memory", "Listen Short Memory?", "Listen Long Memory?", "Fullscreen Slideshow"};
                AlertDialog.Builder builder = new AlertDialog.Builder(PhotosActivity.this);
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            dialogAddMemory = new Dialog(PhotosActivity.this, R.style.custom_theme_dialog);
                            dialogAddMemory.setTitle(getString(R.string.memoryInfo));
                            dialogAddMemory.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                            dialogAddMemory.setContentView(R.layout.dialog_gallery_info);
                            dialogAddMemory.show();

                            eventName = (TextInputEditText) dialogAddMemory.findViewById(R.id.eventNameTextInputEditText);
                            eventNameLayout = (TextInputLayout) dialogAddMemory.findViewById(R.id.eventNameTextInputLayout);
                            eventDate = (TextInputEditText) dialogAddMemory.findViewById(R.id.eventDateTextEditText);
                            eventDateLayout = (TextInputLayout) dialogAddMemory.findViewById(R.id.eventDateTextInputLayout);
                            shortDescription = (TextInputEditText) dialogAddMemory.findViewById(R.id.shortDescriptionDialogTextInputEditText);
                            shortDescriptionLayout = (TextInputLayout) dialogAddMemory.findViewById(R.id.shortDescriptionDialogTextInputLayout);
                            longDescription = (TextInputEditText) dialogAddMemory.findViewById(R.id.longDescriptionDialogTextInputEditText);
                            longDescriptionLayout = (TextInputLayout) dialogAddMemory.findViewById(R.id.longDescriptionDialogTextInputLayout);
                            playName = (ImageView) dialogAddMemory.findViewById(R.id.playEventName);
                            recordName = (ImageView) dialogAddMemory.findViewById(R.id.recordEventName);
                            playLong = (ImageView) dialogAddMemory.findViewById(R.id.playShortDescription);
                            recordLong = (ImageView) dialogAddMemory.findViewById(R.id.recordShortDescription);
                            playLonger = (ImageView) dialogAddMemory.findViewById(R.id.playLongDescription);
                            recordLonger = (ImageView) dialogAddMemory.findViewById(R.id.recordLongDescription);

                            title = (TextView) dialogAddMemory.findViewById(R.id.title);
                            title.setText("");


                            eventDate.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Calendar c = Calendar.getInstance();
                                    int mYear = c.get(Calendar.YEAR);
                                    int mMonth = c.get(Calendar.MONTH);
                                    int mDay = c.get(Calendar.DAY_OF_MONTH);

                                    DatePickerDialog datePickerDialog = new DatePickerDialog(PhotosActivity.this,
                                            new DatePickerDialog.OnDateSetListener() {
                                                @Override
                                                public void onDateSet(DatePicker view, int year,
                                                                      int monthOfYear, int dayOfMonth) {
                                                    Calendar calendar = Calendar.getInstance();
                                                    calendar.set(year, monthOfYear, dayOfMonth);
                                                    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM-dd-yyyy", Locale.US);
                                                    String startDate = dateFormat.format(calendar.getTime());
                                                    eventDate.setText(startDate);
                                                }
                                            }, mYear, mMonth, mDay);
                                    datePickerDialog.getDatePicker().setMaxDate(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000).getTime());
                                    datePickerDialog.show();
                                }
                            });

                            playName.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String event = eventName.getText().toString().trim();
                                    voice.say(event);
                                }
                            });

                            playLong.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String description = shortDescription.getText().toString().trim();
                                    voice.say(description);
                                }
                            });

                            playLonger.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String description = longDescription.getText().toString().trim();
                                    voice.say(description);
                                }
                            });

                            recordName.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    recordDescription(v);
                                }
                            });

                            recordLong.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    recordDescription(v);
                                }
                            });

                            recordLonger.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    recordDescription(v);
                                }
                            });

                            String path = GalleryFragment.al_images.get(int_position).getAl_imagepath().get(position);
                            memory = getMemoryByPath(path);

                            if (memory == null) {
                                memory = new Memory();
                                memoryKey = "";
                                memory.setPath(path);
                            } else {
                                memoryKey = memory.getKey();
                                eventName.setText(memory.getName());
                                eventDate.setText(memory.getDate());
                                shortDescription.setText(memory.getShortDescription());
                                longDescription.setText(memory.getLongDescription());
                                title.setText(memory.getName());
                            }

                            cancelButton = (FloatingActionButton) dialogAddMemory.findViewById(R.id.cancelButton);
                            okButton = (FloatingActionButton) dialogAddMemory.findViewById(R.id.okButton);


                            cancelButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialogAddMemory.dismiss();
                                }
                            });

                            okButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    writeMemory();

                                    dialogAddMemory.dismiss();
                                }
                            });

                        }else if (which == 1) {
                            String path = GalleryFragment.al_images.get(int_position).getAl_imagepath().get(position);
                            memory = getMemoryByPath(path);
                            if(memory != null) {
                                memoryKey = memory.getKey();
                                String shortName = memory.getName();
                                String eventDateMem = memory.getDate();
                                String shortDes = memory.getShortDescription();
                                voice.say(shortName);
                                voice.playSilence();
                                voice.say(eventDateMem);
                                voice.playSilence();
                                voice.say(shortDes);
                            }
                        }
                        else if(which == 2){
                            String path = GalleryFragment.al_images.get(int_position).getAl_imagepath().get(position);
                            memory = getMemoryByPath(path);
                            if(memory != null) {
                                memoryKey = memory.getKey();
                                String shortName = memory.getName();
                                String eventNameMem = memory.getDate();
                                String shortDesc = memory.getShortDescription();
                                String longDes = memory.getLongDescription();
                                voice.say(shortName);
                                voice.playSilence();
                                voice.say(eventNameMem);
                                voice.playSilence();
                                voice.say(shortDesc);
                                voice.playSilence();
                                voice.say(longDes);
                            }
                        }else if(which == 3){
                            Intent intent = new Intent(PhotosActivity.this, FullScreenViewActivity.class);
                            intent.putExtra("position", int_position);
                            intent.putExtra("positionPicture", position);
                            startActivity(intent);
                        }
                    }
                });
                builder.show();
            }
        });
    }

    private void writeMemory() {
        showProgressDialog("Saving memory details...Please wait");

        String eventNameString = eventName.getText().toString().trim();
        String eventDateString = eventDate.getText().toString().trim();
        String shortDescriptionString = shortDescription.getText().toString().trim();
        String longDescriptionString = longDescription.getText().toString().trim();

        if (!validateForm(eventNameString, eventDateString, shortDescriptionString, longDescriptionString)) {
            hideProgressDialog();
            return;
        }

        memory.setName(eventNameString);
        memory.setDate(eventDateString);
        memory.setShortDescription(shortDescriptionString);
        memory.setLongDescription(longDescriptionString);

        if (memoryKey.equals("")) {
            databaseReference.push().setValue(memory);
            hideProgressDialog();
            Toast.makeText(PhotosActivity.this, "Memory information Saved!", Toast.LENGTH_SHORT).show();
        } else {
            databaseReference.child(memoryKey).setValue(memory);
            hideProgressDialog();
            Toast.makeText(PhotosActivity.this, "Memory Information Updated!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateForm(String eventName, String eventDate, String shortDescription, String longDescription) {
        boolean valid = true;

        String textOnlyRegex = "^[\\p{L} .'-]+$";

        if (TextUtils.isEmpty(eventName)) {
            eventNameLayout.setError("Name cannot be empty");
            valid = false;
        } else {
            eventNameLayout.setError(null);
        }
        if (TextUtils.isEmpty(eventName) || !Pattern.matches(textOnlyRegex, eventName)) {
            eventNameLayout.setError("Please enter a valid name");
            valid = false;
        } else {
            eventNameLayout.setError(null);
        }
        if (TextUtils.isEmpty(shortDescription)) {
            shortDescriptionLayout.setError("Short Description cannot be empty");
            valid = false;
        } else {
            shortDescriptionLayout.setError(null);
        }
        if(TextUtils.isEmpty(eventDate)){
            eventDateLayout.setError("Event Date cannot be empty");
            valid = false;
        }else{
            eventDateLayout.setError(null);
        }

        if(TextUtils.isEmpty(longDescription)){
            longDescriptionLayout.setError("Long Description cannot be empty");
            valid = false;
        }else{
            longDescriptionLayout.setError(null);
        }

        return valid;
    }

    private void onAuthFailure() {
        // Write new user
        Intent intent = new Intent(PhotosActivity.this, MainMenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    Memory getMemoryByPath(String path) {
        for (Memory m : memoryList) {
            if (m.getPath() == null) continue;
            if (m.getPath().equals(path)) return m;
        }
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQ_CODE_SPEECH_INPUT_SHORT && data!=null )
        {
            ArrayList<String> result = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            shortDescription.setText(result.get(0));
        }

        if(requestCode == REQ_CODE_SPEECH_INPUT_LONG && data!=null )
        {
            ArrayList<String> result = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            longDescription.setText(result.get(0));
        }
        if(requestCode == REQ_CODE_SPEECH_INPUT_NAME && data != null){
            ArrayList<String> result = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            eventName.setText(result.get(0));
        }
    }

    public void recordDescription(View v) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));


        if (v.getId() == R.id.recordEventName) {
            try {
                startActivityForResult(intent, REQ_CODE_SPEECH_INPUT_NAME);
            } catch (ActivityNotFoundException a) {
                Toast.makeText(PhotosActivity.this,
                        getString(R.string.speech_not_supported),
                        Toast.LENGTH_SHORT).show();
            }
        } else if (v.getId() == R.id.recordShortDescription) {
            try {
                startActivityForResult(intent, REQ_CODE_SPEECH_INPUT_SHORT);
            } catch (ActivityNotFoundException a) {
                Toast.makeText(PhotosActivity.this,
                        getString(R.string.speech_not_supported),
                        Toast.LENGTH_SHORT).show();
            }
        } else if (v.getId() == R.id.recordLongDescription) {
            try {
                startActivityForResult(intent, REQ_CODE_SPEECH_INPUT_LONG);
            } catch (ActivityNotFoundException a) {
                Toast.makeText(PhotosActivity.this,
                        getString(R.string.speech_not_supported),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
