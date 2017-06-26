package com.project.group.projectga.activities;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.project.group.projectga.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import mehdi.sakout.fancybuttons.FancyButton;

public class SplashScreen extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.signInButton)
    protected FancyButton signInButton;
    @BindView(R.id.signUpButton)
    protected FancyButton signUpButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        ButterKnife.bind(this);

        signInButton.setOnClickListener(this);
        signUpButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view == signInButton){
            Intent signInIntent = new Intent(SplashScreen.this, SignInActivity.class);
            startActivity(signInIntent);
            finish();
        }

        if(view == signUpButton){
            Intent signUpIntent = new Intent(SplashScreen.this, SignUpActivity.class);
            startActivity(signUpIntent);
            finish();
        }
    }
}
