package com.project.group.projectga.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.project.group.projectga.R;

public class ForgetPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
    }

    public void onClick(View v){
        EditText answer = (EditText)findViewById(R.id.SecurityAnswerInputField);
        if(v.getId() == R.id.nextButtonForgetPassword){

        }
    }
}
