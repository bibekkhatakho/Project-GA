package com.project.group.projectga.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.project.group.projectga.R;

public class SplashActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        firebaseAuth = FirebaseAuth.getInstance();

        Thread splashThread = new Thread(){
            public void run(){
                try {
                    sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    if(firebaseAuth.getCurrentUser() != null){
                        Intent mainMenuIntent = new Intent(SplashActivity.this, MainMenuActivity.class);
                        startActivity(mainMenuIntent);
                        finish();
                    }else{
                        Intent splashScreenIntent = new Intent(SplashActivity.this, SplashScreen.class);
                        startActivity(splashScreenIntent);
                        finish();
                    }
                }
            }
        };
        splashThread.start();
    }
}
