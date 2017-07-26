package com.project.group.projectga.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
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

import java.util.ArrayList;

/**
 * Created by ramjiseetharaman on 7/12/17.
 */

public class PhotosActivity extends CoreActivity {
    int int_position;
    @BindView(R.id.gv_folder)
    protected GridView gridView;
    @BindView(R.id.toolbar)
    protected Toolbar toolbar;

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

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);



        isPlaying = false;
        voice  = new Voice(getApplicationContext());


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

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Memory m = snapshot.getValue(Memory.class);
                    String key=snapshot.getKey();
                    m.setKey(key);
                    memoryList.add(m);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        gridView = (GridView)findViewById(R.id.gv_folder);
        int_position = getIntent().getIntExtra("value", 0);
        adapter = new GridViewAdapter(this, GalleryFragment.al_images,int_position);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialog = new Dialog(PhotosActivity.this);
                dialog.setContentView(R.layout.dialog_gallery_info);
                dialog.show();

                eventName = (EditText) dialog.findViewById(R.id.eventName);
                eventDate = (EditText) dialog.findViewById(R.id.eventDate);
                longDescription = (EditText) dialog.findViewById(R.id.longDescription);

                title = (TextView) dialog.findViewById(R.id.title);
                title.setText("");

                String path = GalleryFragment.al_images.get(int_position).getAl_imagepath().get(position);
                memory = getMemoryByPath(path);

                if (memory == null) {
                    memory = new Memory();
                    memoryKey = "";
                    memory.setPath(path);
                }
                else {
                    memoryKey = memory.getKey();
                    eventName.setText(memory.getName());
                    eventDate.setText(memory.getDate());
                    longDescription.setText(memory.getDescription());
                    title.setText(memory.getName());
                }

                cancelButton = (Button)dialog.findViewById(R.id.cancelButton);
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

        if(memoryKey.equals("")) {
            databaseReference.push().setValue(memory);
            hideProgressDialog();
            Toast.makeText(this, "Memory information Saved!", Toast.LENGTH_SHORT).show();
        }
        else
        {
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
        for (Memory m: memoryList) {
            if (m.getPath() == null) continue;
            if (m.getPath().equals(path)) return m;
        }
        return null;
    }
}
