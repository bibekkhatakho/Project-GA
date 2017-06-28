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

public class AddGuardianActivity extends AppCompatActivity implements View.OnClickListener{

    @BindView(R.id.doneButton)
    protected FloatingActionButton doneButton;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_guardian);
        ButterKnife.bind(this);

        doneButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view == doneButton){
            Intent mainMenuIntent = new Intent(AddGuardianActivity.this, MainMenuActivity.class);
            startActivity(mainMenuIntent);
            finish();
        }
    }
}
