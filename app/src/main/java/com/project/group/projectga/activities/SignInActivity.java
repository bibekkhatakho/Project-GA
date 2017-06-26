package com.project.group.projectga.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.project.group.projectga.R;

public class SignInActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
    }

    public void onClick(View v){
        EditText email = (EditText)findViewById(R.id.emailET);
        EditText password = (EditText)findViewById(R.id.emailET);
        if(v.getId() == R.id.loginButton){


        }
    }
}
