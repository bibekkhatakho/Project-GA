package com.project.group.projectga.activities;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.project.group.projectga.R;
import com.project.group.projectga.adapters.FullScreenImageAdapter;
import com.project.group.projectga.adapters.GridViewAdapter;
import com.project.group.projectga.fragments.GalleryFragment;
import com.project.group.projectga.models.Model_images;

import java.util.ArrayList;

public class FullScreenViewActivity extends AppCompatActivity {

    private FullScreenImageAdapter adapter;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_view);

        viewPager = (ViewPager) findViewById(R.id.pager);


        Intent i = getIntent();
        int position = i.getIntExtra("position", 0);

        adapter = new FullScreenImageAdapter(this, GalleryFragment.al_images.get(position).getAl_imagepath());

        viewPager.setAdapter(adapter);

        // displaying selected image first
        viewPager.setCurrentItem(position);
    }
}
