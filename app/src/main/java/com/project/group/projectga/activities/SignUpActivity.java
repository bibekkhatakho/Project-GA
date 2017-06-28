package com.project.group.projectga.activities;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.project.group.projectga.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener{

    @BindView(R.id.fullNameTextInputLayout)
    protected TextInputLayout fullNameTextInputLayout;
    @BindView(R.id.fullNameTextInputEditText)
    protected TextInputEditText fullNameTextInputEditText;
    @BindView(R.id.emailTextInputLayout)
    protected TextInputLayout emailTextInputLayout;
    @BindView(R.id.emailTextEditText)
    protected TextInputEditText emailTextEditText;
    @BindView(R.id.passwordTextInputLayout)
    protected TextInputLayout passwordTextInputLayout;
    @BindView(R.id.passwordTextEditText)
    protected TextInputEditText passwordTextEditText;
    @BindView(R.id.confirmpasswordTextInputLayout)
    protected TextInputLayout confirmpasswordTextInputLayout;
    @BindView(R.id.confirmpasswordTextEditText)
    protected TextInputEditText confirmpasswordTextEditText;
    @BindView(R.id.nextButton)
    protected FloatingActionButton nextButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);

        nextButton.setOnClickListener(this);

    }


    @Override
    public void onClick(View view) {

        if(view == nextButton){
            Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show();
            Intent contactIntent = new Intent(SignUpActivity.this, ContactDetailsActivity.class);
            startActivity(contactIntent);
            finish();
        }

    }
}
