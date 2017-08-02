package com.project.group.projectga.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.View;
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

    protected TextView title;

    GridViewAdapter adapter;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        gridView = (GridView)findViewById(R.id.gv_folder);
        int_position = getIntent().getIntExtra("value", 0);
        adapter = new GridViewAdapter(this, GalleryFragment.al_images,int_position);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //Intent intent = new Intent(PhotosActivity.this, FullScreenActivity.class);
                Intent intent = new Intent(PhotosActivity.this, FullScreenViewActivity.class);
                //intent.putExtra("imagePath", GalleryFragment.al_images.get(int_position).getAl_imagepath().get(position));
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });
    }
}
