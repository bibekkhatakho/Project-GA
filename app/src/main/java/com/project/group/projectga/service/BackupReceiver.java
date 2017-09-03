package com.project.group.projectga.service;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.widget.Toast;

/**
 * Created by MitchelSmith on 8/17/2017.
 */

public class BackupReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent intent1 = new Intent(context, BackupService.class);
        context.startService(intent1);
    }
}
