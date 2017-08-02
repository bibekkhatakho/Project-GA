package com.project.group.projectga.adapters;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.group.projectga.R;
import com.project.group.projectga.activities.CoreActivity;
import com.project.group.projectga.activities.FullScreenActivity;
import com.project.group.projectga.activities.MainMenuActivity;
import com.project.group.projectga.fragments.GalleryFragment;
import com.project.group.projectga.models.Memory;

/**
 * Created by ramjiseetharaman on 7/31/17.
 */


public class FullScreenImageAdapter extends PagerAdapter {

    private Activity _activity;
    private ArrayList<String> _imagePaths;
    private LayoutInflater inflater;

    String userId;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;

    ArrayList<Memory> memoryList;

    private ProgressDialog mProgressDialog;

    protected ImageView playName;
    protected ImageView recordName;
    protected ImageView playLong;
    protected ImageView recordLong;

    protected EditText eventName;
    protected EditText eventDate;
    protected EditText longDescription;
    protected TextView title;
    protected Dialog dialog;

    FloatingActionButton cancelButton;
    FloatingActionButton okButton;

    String memoryKey = null;
    Memory memory;

    Voice voice;

    private final int REQ_CODE_SPEECH_INPUT_SHORT = 100;
    private final int REQ_CODE_SPEECH_INPUT_LONG = 200;

    // constructor
    public FullScreenImageAdapter(Activity activity,
                                  ArrayList<String> imagePaths) {
        this._activity = activity;
        this._imagePaths = imagePaths;
    }

    @Override
    public int getCount() {
        return this._imagePaths.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((RelativeLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        ImageView imgDisplay;
        Button btnClose;

        voice = new Voice(_activity);

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

        inflater = (LayoutInflater) _activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.layout_fullscreen_image, container,
                false);

        imgDisplay = (ImageView) viewLayout.findViewById(R.id.imgDisplay);
        btnClose = (Button) viewLayout.findViewById(R.id.btnClose);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(_imagePaths.get(position), options);
        imgDisplay.setImageBitmap(bitmap);

        imgDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new Dialog(_activity);
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

                        DatePickerDialog datePickerDialog = new DatePickerDialog(_activity,
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
                        Toast.makeText(_activity, "asd", Toast.LENGTH_SHORT).show();
                        String event = eventName.getText().toString().trim();
                        Toast.makeText(_activity, event, Toast.LENGTH_SHORT).show();
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

                //title = (TextView) dialog.findViewById(R.id.title);
                //title.setText("");

                String path = _imagePaths.get(position);
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
                    //title.setText(memory.getName());
                }

                cancelButton = (FloatingActionButton) dialog.findViewById(R.id.cancelButton);
                okButton = (FloatingActionButton) dialog.findViewById(R.id.okButton);


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

        // close button click event
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _activity.finish();
            }
        });

        ((ViewPager) container).addView(viewLayout);

        return viewLayout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((RelativeLayout) object);

    }

    private void writeMemory() {
        showProgressDialog("Saving memory details...Please wait");

        memory.setName(eventName.getText().toString());
        memory.setDate(eventDate.getText().toString());
        memory.setDescription(longDescription.getText().toString());

        if (memoryKey.equals("")) {
            databaseReference.push().setValue(memory);
            hideProgressDialog();
            Toast.makeText(_activity, "Memory information Saved!", Toast.LENGTH_SHORT).show();
        } else {
            databaseReference.child(memoryKey).setValue(memory);
            hideProgressDialog();
            Toast.makeText(_activity, "Memory Information Updated!", Toast.LENGTH_SHORT).show();
        }
    }

    private void onAuthFailure() {
        // Write new user
        Intent intent = new Intent(_activity, MainMenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        _activity.startActivity(intent);
        _activity.finish();
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
                _activity.getString(R.string.speech_prompt));


        if (v.getId() == R.id.recordName) {
            try {
                _activity.startActivityForResult(intent, REQ_CODE_SPEECH_INPUT_SHORT);
            } catch (ActivityNotFoundException a) {
                Toast.makeText(_activity,
                        _activity.getString(R.string.speech_not_supported),
                        Toast.LENGTH_SHORT).show();
            }
        } else if (v.getId() == R.id.recordLong) {
            try {
                _activity.startActivityForResult(intent, REQ_CODE_SPEECH_INPUT_LONG);
            } catch (ActivityNotFoundException a) {
                Toast.makeText(_activity,
                        _activity.getString(R.string.speech_not_supported),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
    public void showProgressDialog(String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(_activity);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage(message);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

}
