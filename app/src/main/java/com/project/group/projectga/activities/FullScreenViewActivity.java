package com.project.group.projectga.activities;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.project.group.projectga.R;
import com.project.group.projectga.adapters.FullScreenImageAdapter;
import com.project.group.projectga.adapters.GridViewAdapter;
import com.project.group.projectga.fragments.GalleryFragment;
import com.project.group.projectga.models.Model_images;

import java.util.ArrayList;

public class FullScreenViewActivity extends AppCompatActivity {

    private FullScreenImageAdapter adapter;
    private ViewPager viewPager;
    private TextView countLabel;
    private int selectedPostion = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_view);

        viewPager = (ViewPager) findViewById(R.id.pager);
        countLabel = (TextView) findViewById(R.id.countLabel);


        Intent i = getIntent();
        selectedPostion = i.getIntExtra("position", 0);

        adapter = new FullScreenImageAdapter(this, GalleryFragment.al_images.get(selectedPostion).getAl_imagepath());

        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(pageChangeListener);

        // displaying selected image first
        //viewPager.
            setCurrentItem(selectedPostion);
    }

    private void setCurrentItem(int position) {
        viewPager.setCurrentItem(position, false);
        displayExtras(position);
    }

    //  page change listener
    ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            displayExtras(position);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    private void displayExtras(int position) {
        countLabel.setText((position + 1) + " of " + GalleryFragment.al_images.get(selectedPostion).getAl_imagepath().size());

    }
}