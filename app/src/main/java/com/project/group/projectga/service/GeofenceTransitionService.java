package com.project.group.projectga.service;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.project.group.projectga.R;
import com.project.group.projectga.activities.MainMenuActivity;
import com.project.group.projectga.fragments.GuardianMapsFragment;
import com.project.group.projectga.fragments.MapsFragment;

import java.util.ArrayList;
import java.util.List;


public class GeofenceTransitionService extends IntentService {

    private static final String TAG = GeofenceTransitionService.class.getSimpleName();

    public static final int GEOFENCE_NOTIFICATION_ID = 149;

    int counter = 0;
    String geoFencingRegion;
    public GeofenceTransitionService() {
        super(TAG);
    }
    String str;
    boolean appNotifications;

    @Override
    protected void onHandleIntent(Intent intent) {



        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        appNotifications = sharedPreferences.getBoolean(getString(R.string.title_app_notifications_key), false);

        // Handling

        str = intent.getStringExtra("region");
        if ( geofencingEvent.hasError() ) {
            String errorMsg = getErrorString(geofencingEvent.getErrorCode() );
            Log.e( TAG, errorMsg );
            return;
        }
        if(str.matches("a")) {
            counter = 1;
            geoFencingRegion = " zone 1";
        }
        else if(str.matches("b")) {
            counter = 2;
            geoFencingRegion = " zone 2";
        }

        else if (str.matches("c")) {
            counter = 3;
            geoFencingRegion = " zone 3";
        }
        int geoFenceTransition = geofencingEvent.getGeofenceTransition();
        // Check if the transition type is of interest
        if ( geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT ) {
            // Get the geofence that were triggered
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            String geofenceTransitionDetails = getGeofenceTrasitionDetails(geoFenceTransition, triggeringGeofences );

            // Send notification details as a String
            if(appNotifications) {
                sendNotification(geofenceTransitionDetails);
            }
        }
    }


    private String getGeofenceTrasitionDetails(int geoFenceTransition, List<Geofence> triggeringGeofences) {
        // get the ID of each geofence triggered
        ArrayList<String> triggeringGeofencesList = new ArrayList<>();
        for ( Geofence geofence : triggeringGeofences ) {
            triggeringGeofencesList.add( geofence.getRequestId() );
        }

        String status = null;
        if ( geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ) {
            status = "Entering " + geoFencingRegion;




        }
        else if ( geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT )
            status = "Exiting " + geoFencingRegion;

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage("14692589637", null, "The standard user  is " + status, null, null);

           // Toast.makeText(getApplicationContext(), "SMS Sent!",
                 //   Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),
                    "SMS faild, please try again later!",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        return status + TextUtils.join( ", ", triggeringGeofencesList);
    }

    private void sendNotification( String msg ) {
        Log.i(TAG, "sendNotification: " + msg );

        // Intent to start the main Activity
        Intent notificationIntent = MainMenuActivity.makeNotificationIntent(getApplicationContext(), msg);
        notificationIntent.putExtra("Maps", "geofenceCall");

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainMenuActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(counter, PendingIntent.FLAG_UPDATE_CURRENT);

        // Creating and sending Notification
        NotificationManager notificatioMng =
                (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
        notificatioMng.notify(
                counter,
                createNotification(msg, notificationPendingIntent));
    }

    // Create notification
    private Notification createNotification(String msg, PendingIntent notificationPendingIntent) {

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder
                .setSmallIcon(R.drawable.ic_contact_phone_black_24dp)
                .setColor(Color.WHITE)
                .setAutoCancel(true)
                .setContentTitle("Geofence Notification!")
                .setContentText(msg + "Do you want to call the user?")
                .addAction(R.drawable.ic_call_black_24dp,"Call",notificationPendingIntent)
                .setContentIntent(notificationPendingIntent)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND);

        return notificationBuilder.build();
    }


    private static String getErrorString(int errorCode) {
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "GeoFence not available";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "Too many GeoFences";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "Too many pending intents";
            default:
                return "Unknown error.";
        }
    }
}