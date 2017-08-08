package com.project.group.projectga.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
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
import java.util.Locale;

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

    private final int REQ_CODE_SPEECH_INPUT_SHORT = 100;
    private final int REQ_CODE_SPEECH_INPUT_LONG = 200;

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
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                CharSequence options[];

                if(position == 0) {
                    options = new CharSequence[]{"Add a Memory", "Fullscreen Slideshow"};
                }else{
                    options = new CharSequence[]{"Add a Memory"};
                }

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
                                    datePickerDialog.show();
                                }
                            });

                            playName.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Toast.makeText(PhotosActivity.this, "asd", Toast.LENGTH_SHORT).show();
                                    String event = eventName.getText().toString().trim();
                                    Toast.makeText(PhotosActivity.this, event, Toast.LENGTH_SHORT).show();
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
                            Intent intent = new Intent(PhotosActivity.this, FullScreenViewActivity.class);
                            intent.putExtra("position", int_position);
                            startActivity(intent);

                        }else{

                        }
                    }
                });
                builder.show();
            }
        });
    }

    private void writeMemory() {
        showProgressDialog("Saving memory details...Please wait");

        memory.setName(eventName.getText().toString().trim());
        memory.setDate(eventDate.getText().toString().trim());
        memory.setShortDescription(shortDescription.getText().toString().trim());
        memory.setLongDescription(longDescription.getText().toString().trim());

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

    public void recordDescription(View v) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));


        if (v.getId() == R.id.recordEventName) {
            try {
                startActivityForResult(intent, REQ_CODE_SPEECH_INPUT_SHORT);
            } catch (ActivityNotFoundException a) {
                Toast.makeText(PhotosActivity.this,
                        getString(R.string.speech_not_supported),
                        Toast.LENGTH_SHORT).show();
            }
        } else if (v.getId() == R.id.recordShortDescription) {
            try {
                startActivityForResult(intent, REQ_CODE_SPEECH_INPUT_LONG);
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
