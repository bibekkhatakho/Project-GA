package com.project.group.projectga.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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
import com.project.group.projectga.models.Memory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FullScreenActivity extends CoreActivity {

    int int_position;
    @BindView(R.id.toolbar)
    protected Toolbar toolbar;

    protected ImageView playName;
    protected ImageView recordName;
    protected ImageView playLong;
    protected ImageView recordLong;

    protected EditText eventName;
    protected EditText eventDate;
    protected EditText longDescription;
    protected TextView title;
    protected Dialog dialog;

    GridViewAdapter adapter;

    Button cancelButton;
    Button okButton;

    Memory memory;
    ArrayList<Memory> memoryList;

    Voice voice;
    boolean isPlaying;

    String memoryKey = null;
    String userId;
    int position;

    private final int REQ_CODE_SPEECH_INPUT_SHORT = 100;
    private final int REQ_CODE_SPEECH_INPUT_LONG = 200;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen);
        ButterKnife.bind(this);

        voice = new Voice(getApplicationContext());
        setSupportActionBar(toolbar);

        if (getUid() != null) {
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

        ImageView fullScreenImageView = (ImageView) findViewById(R.id.fullScreenImageView);

        Intent callingActivityIntent = getIntent();
        if (callingActivityIntent != null) {
            String imageUri = callingActivityIntent.getStringExtra("imagePath");
            position = callingActivityIntent.getIntExtra("position", 0);
            if (imageUri != null && fullScreenImageView != null) {
                Glide.with(this).load("file://" + imageUri)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(fullScreenImageView);
            }
        }

        fullScreenImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new Dialog(FullScreenActivity.this);
                dialog.setContentView(R.layout.dialog_gallery_info);
                dialog.show();

                eventName = (EditText) dialog.findViewById(R.id.eventName);
                eventDate = (EditText) dialog.findViewById(R.id.eventDate);
                longDescription = (EditText) dialog.findViewById(R.id.longDescription);
                playName = (ImageView) dialog.findViewById(R.id.playShortDescription);
                recordName = (ImageView) dialog.findViewById(R.id.recordName);
                playLong = (ImageView) dialog.findViewById(R.id.playLong);
                recordLong = (ImageView) dialog.findViewById(R.id.recordLong);

                eventDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Calendar c = Calendar.getInstance();
                        int mYear = c.get(Calendar.YEAR);
                        int mMonth = c.get(Calendar.MONTH);
                        int mDay = c.get(Calendar.DAY_OF_MONTH);

                        DatePickerDialog datePickerDialog = new DatePickerDialog(FullScreenActivity.this,
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
                        Toast.makeText(FullScreenActivity.this, "asd", Toast.LENGTH_SHORT).show();
                        String event = eventName.getText().toString().trim();
                        Toast.makeText(FullScreenActivity.this, event, Toast.LENGTH_SHORT).show();
                        voice.say(event);
                    }
                });

                playLong.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String description = longDescription.getText().toString().trim();
                        voice.say(description);
                    }
                });

                title = (TextView) dialog.findViewById(R.id.title);
                title.setText("");

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
                    longDescription.setText(memory.getDescription());
                    title.setText(memory.getName());
                }

                cancelButton = (Button) dialog.findViewById(R.id.cancelButton);
                okButton = (Button) dialog.findViewById(R.id.okButton);


                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        writeMemory();

                        dialog.dismiss();
                    }
                });
            }
        });
    }

    private void writeMemory() {
        showProgressDialog("Saving memory details...Please wait");

        memory.setName(eventName.getText().toString());
        memory.setDate(eventDate.getText().toString());
        memory.setDescription(longDescription.getText().toString());

        if (memoryKey.equals("")) {
            databaseReference.push().setValue(memory);
            hideProgressDialog();
            Toast.makeText(this, "Memory information Saved!", Toast.LENGTH_SHORT).show();
        } else {
            databaseReference.child(memoryKey).setValue(memory);
            hideProgressDialog();
            Toast.makeText(this, "Memory Information Updated!", Toast.LENGTH_SHORT).show();
        }
    }

    private void onAuthFailure() {
        // Write new user
        Intent intent = new Intent(this, MainMenuActivity.class);
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


        if (v.getId() == R.id.recordName) {
            try {
                startActivityForResult(intent, REQ_CODE_SPEECH_INPUT_SHORT);
            } catch (ActivityNotFoundException a) {
                Toast.makeText(getApplicationContext(),
                        getString(R.string.speech_not_supported),
                        Toast.LENGTH_SHORT).show();
            }
        } else if (v.getId() == R.id.recordLong) {
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