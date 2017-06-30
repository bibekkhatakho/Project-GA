package com.project.group.projectga.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.project.group.projectga.R;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.PendingIntent.getActivity;

public class AddGuardianActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int Request_Code = 100;
    private static final String TAG = AddGuardianActivity.class.getSimpleName();
    private String emailAddress;
    @BindView(R.id.doneButton)
    protected FloatingActionButton doneButton;
    @BindView(R.id.emailTextEditText)
    protected TextInputEditText emailtextEditText;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_guardian);
        ButterKnife.bind(this);

        emailtextEditText = (TextInputEditText) findViewById(R.id.emailTextEditText);

        doneButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if(view == doneButton){
            onDoneClicked();
        }
    }

    private void onDoneClicked()
    {
        emailAddress = emailtextEditText.getText().toString().trim();
        startActivity(new Intent(Intent.ACTION_SENDTO,Uri.parse("mailto:" + emailAddress)));
        /**Intent intent = new AppInviteInvitation.IntentBuilder("Add a Guardian")
                .setMessage("You have been invited to become a guardian user!")
                .setDeepLink(Uri.parse("http://www.google.com"))
                .setCallToActionText("Install")
                .build();

        startActivityForResult(intent,Request_Code);**/

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);
        if(requestCode == Request_Code)
        {
            if(resultCode == RESULT_OK)
            {
                String[] ids = AppInviteInvitation.getInvitationIds(requestCode,data);

                for(String id : ids)
                {
                    System.out.println("AddGuardianActivity.onActivityResult:" + id);
                }
            }
            else
            {
                Toast.makeText(this,"Error with Firebase Invites",Toast.LENGTH_SHORT).show();
            }
            Intent mainMenuIntent = new Intent(AddGuardianActivity.this, MainMenuActivity.class);
            startActivity(mainMenuIntent);
            finish();
        }
    }
}
