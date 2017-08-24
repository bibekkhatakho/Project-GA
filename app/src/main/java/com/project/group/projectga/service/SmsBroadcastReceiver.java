package com.project.group.projectga.service;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.NotificationCompat;
import android.telephony.SmsMessage;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.project.group.projectga.Manifest;
import com.project.group.projectga.R;
import com.project.group.projectga.activities.MainMenuActivity;
import com.project.group.projectga.fragments.MessagesFragment;
import com.project.group.projectga.fragments.TagLocateFragment;
import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by billybrown on 8/19/17.
 */

public class SmsBroadcastReceiver extends BroadcastReceiver
{

    public static final String SMS_BUNDLE = "pdus";
    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;
    private int notification_id;
    private String smsBody,address;
    private static final int request_code = 150;
    private RemoteViews remoteViews;
    SmsMessage smsMessage = null;

    public void onReceive(Context context, Intent intent)
    {
        //Toast.makeText(context, "Message Received!", Toast.LENGTH_SHORT).show();

        Bundle intentExtras = intent.getExtras();

        if (intentExtras != null) {
            Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
            String smsMessageStr = "";
            if(sms != null) {
                for (int i = 0; i < sms.length; ++i) {
                    String format = intentExtras.getString("format");
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        smsMessage = SmsMessage.createFromPdu((byte[]) sms[i], format);
                    } else {
                        smsMessage = SmsMessage.createFromPdu((byte[]) sms[i]);
                    }

                    smsBody = smsMessage.getDisplayMessageBody();
                    address = smsMessage.getDisplayOriginatingAddress();

                    smsMessageStr += "SMS From: " + address + "\n";
                    smsMessageStr += smsBody + "\n";
                }

                MessagesFragment inst = MessagesFragment.instance();
                inst.updateInbox(smsMessageStr);

                createNotification(context);
                sendNotification();
            }
        }
    }

    public void createNotification(Context context)
    {
        notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notification_id = (int) System.currentTimeMillis();
        remoteViews = new RemoteViews(context.getPackageName(), R.layout.custom_notification);
        remoteViews.setTextViewText(R.id.notif_address,address);
        remoteViews.setTextViewText(R.id.notif_body,smsBody);
        Intent resultIntent = new Intent(context, MainMenuActivity.class);
        resultIntent.putExtra("menuFragment", "messagesMenuItem");
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setCustomContentView(remoteViews);
        builder.setContentIntent(resultPendingIntent);
    }

    public void sendNotification()
    {
        notificationManager.notify(notification_id,builder.build());
    }
}

