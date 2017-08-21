package com.project.group.projectga.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.project.group.projectga.models.Model_images;
import com.project.group.projectga.service.BackupService;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by MitchelSmith on 8/17/2017.
 */

public class BackupReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        //Toast.makeText(context, "I'm here in the receiver!", Toast.LENGTH_LONG).show();

        Intent intent1 = new Intent(context, BackupService.class);
        context.startService(intent1);
    }
}
