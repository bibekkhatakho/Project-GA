package com.project.group.projectga.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.project.group.projectga.R;

public class ResetPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
    }

    public void onClick(View v){
        EditText firstPassword = (EditText)findViewById(R.id.resetpassowordentryfirst);
        EditText secondPassword = (EditText)findViewById(R.id.resetpasswordentrysecond);
        if(v.getId()==R.id.nextButtonSetNewPassword){
            if(firstPassword.equals(secondPassword)){
                //update the password
                //intent to login or main menu
            }
            else{
                Toast.makeText(this, "Passwords do not match. Try again", Toast.LENGTH_SHORT).show();
            }

        }
    }
}
