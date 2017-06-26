package com.project.group.projectga.activities;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.project.group.projectga.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ContactDetailsActivity extends AppCompatActivity implements View.OnClickListener{

    @BindView(R.id.nextButtonContact)
    protected FloatingActionButton nextButtonGuardian;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_details);
        ButterKnife.bind(this);

        nextButtonGuardian.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view == nextButtonGuardian){
            Intent addGuardianIntent = new Intent(ContactDetailsActivity.this, AddGuardianActivity.class);
            startActivity(addGuardianIntent);
            finish();
        }
    }
}
