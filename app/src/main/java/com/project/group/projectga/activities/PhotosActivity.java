package com.project.group.projectga.activities;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.project.group.projectga.R;
import com.project.group.projectga.adapters.GridViewAdapter;
import com.project.group.projectga.fragments.GalleryFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ramjiseetharaman on 7/12/17.
 */

public class PhotosActivity extends AppCompatActivity {
    int int_position;
    @BindView(R.id.gv_folder)
    protected GridView gridView;
    @BindView(R.id.toolbar)
    protected Toolbar toolbar;

    MenuItem search;

    GridViewAdapter adapter;

    Button cancelButton;
    Button okButton;

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

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final Dialog dialog = new Dialog(PhotosActivity.this);
                dialog.setTitle("Gallery Information");
                dialog.setContentView(R.layout.dialog_gallery_info);
                dialog.show();

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
                        dialog.dismiss();
                    }
                });
                return false;
            }
        });



    }

}
