package com.project.group.projectga;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void onClick(View v){
        EditText email = (EditText)findViewById(R.id.emailET);
        EditText password = (EditText)findViewById(R.id.emailET);
        if(v.getId() == R.id.loginButton){


        }
    }
}
