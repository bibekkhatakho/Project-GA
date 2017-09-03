package com.project.group.projectga.activities;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.project.group.projectga.R;
import com.project.group.projectga.fragments.GalleryHomeFragment;
import com.project.group.projectga.fragments.GamesPuzzlesFragment;
import com.project.group.projectga.fragments.GuardianMapsFragment;
import com.project.group.projectga.fragments.HomeFragment;
import com.project.group.projectga.fragments.MapsFragment;
import com.project.group.projectga.fragments.ProfileFragment;
import com.project.group.projectga.fragments.ImportantPeopleFragment;
import com.project.group.projectga.fragments.RecognitionFragment;
import com.project.group.projectga.fragments.TagLocateFragment;
import com.project.group.projectga.models.LocationModel;
import com.project.group.projectga.models.Profile;
import com.project.group.projectga.preferences.Preferences;
import com.project.group.projectga.service.CurrentLocation;
import com.squareup.picasso.Picasso;

import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainMenuActivity extends CoreActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @BindView(R.id.toolbar)
    protected Toolbar toolbar;

    private Drawer result = null;
    AccountHeader headerResult;
    private static final int REQUEST_PERMISSIONS = 128;

    boolean drivingcheckPrimary = true;
    String userStatus;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    DatabaseReference databaseReferenceGuardian;


    Stack<PrimaryDrawerItem> gaFragmentStack;

    private static final String NOTIFICATION_MSG = "NOTIFICATION MSG";

    String guardianEmail;
    String myEmail;
    String myName;
    String guardianName;
    Timer timer;
    Timer timerToAskFuel;
    String guardianPicture;
    String patientPicture;
    String patientNumber;
    String guardianNumber;
    String numberPlus;

    boolean profileFlag, homeFlag = true, importantPeopleFlag = false, firstTime = false, mapMarkerFlag = false, notificationFlag = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);


        if (getUid() != null) {
            String userId = getUid();
            firebaseAuth = FirebaseAuth.getInstance();
            databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
            databaseReferenceGuardian = FirebaseDatabase.getInstance().getReference().child("guardians").child("guardianEmails");
        } else {
            onAuthFailure();
        }

        gaFragmentStack = new Stack<>();

        Fragment home_fragment = new HomeFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container_gaFragments, home_fragment);
        transaction.addToBackStack("home");
        transaction.commit();


        String type = getIntent().getStringExtra("Maps");

        if (type != null) {
            switch (type) {
                case "mapsFragment":
                    Fragment mapsFragment = new MapsFragment();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.container_gaFragments, mapsFragment).commit();
                    break;

                case "gasstation":
                    Uri gmmIntentUri = Uri.parse("geo:0,0?q=gas station");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                    break;
                case "geofenceCall":
                    databaseReferenceGuardian.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            LocationModel locationModel = dataSnapshot.getValue(LocationModel.class);
                            if(locationModel !=null){
                                guardianNumber = locationModel.getGuardianNumber();
                                guardianNumber = guardianNumber.replaceAll("[^0-9]","");
                                numberPlus = "+1" + guardianNumber;
                                String uri = "tel:" + numberPlus;
                                //Build the intent that will make the phone call
                                Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(uri));
                                callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                if (ActivityCompat.checkSelfPermission(MainMenuActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                    // TODO: Consider calling
                                    //    ActivityCompat#requestPermissions
                                    // here to request the missing permissions, and then overriding
                                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    //                                          int[] grantResults)
                                    // to handle the case where the user grants the permission. See the documentation
                                    // for ActivityCompat#requestPermissions for more details.
                                    return;
                                }
                                getApplicationContext().startActivity(callIntent);
                                finish();
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
            }
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            profileFlag = extras.getBoolean("profileFlag");
            homeFlag = extras.getBoolean("homeFlag");
            importantPeopleFlag = extras.getBoolean("importantPeopleFlag");
            mapMarkerFlag = extras.getBoolean("mapMarkerFlag");
            firstTime = extras.getBoolean("firstTime");
        }


        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainMenuActivity.this);
        final String userType = preferences.getString(Preferences.USER_TYPE, "");

        Log.d("userTypeMain", userType);

        final PrimaryDrawerItem home = new PrimaryDrawerItem().withName("Home").withIdentifier(1).withIcon(R.drawable.ic_home_black_24dp);
        final PrimaryDrawerItem profile = new PrimaryDrawerItem().withName("Profile").withIdentifier(2).withIcon(R.drawable.ic_person_red_24dp);
        final PrimaryDrawerItem gallery = new PrimaryDrawerItem().withName("Gallery").withIdentifier(3).withIcon(R.drawable.ic_perm_media_black_24dp);
        final PrimaryDrawerItem recognition = new PrimaryDrawerItem().withName("Recognition").withIdentifier(4).withIcon(R.drawable.ic_face_gray_24dp);
        final PrimaryDrawerItem maps = new PrimaryDrawerItem().withName("Maps").withIdentifier(5).withIcon(R.drawable.ic_place_black_24dp);
        final PrimaryDrawerItem tagAndLocate = new PrimaryDrawerItem().withName("Tag & Locate").withIdentifier(6).withIcon(R.drawable.ic_remove_red_eye_black_24dp);
        final PrimaryDrawerItem gamesAndPuzzle = new PrimaryDrawerItem().withName("Games & Puzzles").withIdentifier(7).withIcon(R.drawable.ic_casino_black_24dp);
        final PrimaryDrawerItem prefSettings = new PrimaryDrawerItem().withName("Preferences").withIdentifier(8).withIcon(R.drawable.ic_settings_gray_24dp);

        final PrimaryDrawerItem logout = new PrimaryDrawerItem().withName("Logout").withIdentifier(9).withIcon(R.drawable.ic_exit_to_app_gray_24dp);

        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                Picasso.with(imageView.getContext()).load(uri).placeholder(placeholder).fit().centerCrop().into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                Picasso.with(imageView.getContext()).cancelRequest(imageView);
            }
        });

        String name = preferences.getString(Preferences.NAME, "");
        String email = preferences.getString(Preferences.EMAIL, "");
        Log.d("NAMEMAIN", name);
        Log.d("emailmain", email);

        if (firstTime) {

            if (userType.equalsIgnoreCase("Standard User")) {
                AlertDialog.Builder alertDialogBuilder;
                alertDialogBuilder = new AlertDialog.Builder(MainMenuActivity.this);
                alertDialogBuilder.setTitle("Welcome " + name + "!!");
                alertDialogBuilder.setIcon(R.drawable.ic_home_red_24dp);
                alertDialogBuilder.setMessage(R.string.privacyInfo);
                alertDialogBuilder.setPositiveButton("Go to Preferences", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(MainMenuActivity.this, SettingsPrefActivity.class));
                    }
                });
                alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        hideProgressDialog();
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                alertDialog.setCancelable(false);
            }
        }

        final ProfileDrawerItem userProfile = new ProfileDrawerItem().withName(name).withEmail(email).withIcon(R.drawable.ic_account_circle_white_24dp);

        headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .withSelectionListEnabledForSingleProfile(false)
                .addProfiles(userProfile)
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        Fragment profileFragment = new ProfileFragment();
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.container_gaFragments, profileFragment);
                        transaction.addToBackStack("profile");
                        transaction.commit();
                        return false;
                    }
                })
                .build();


        if (userType.equalsIgnoreCase("Standard User")) {
            result = new DrawerBuilder()
                    .withActivity(this)
                    .withAccountHeader(headerResult)
                    .withToolbar(toolbar)
                    .withDisplayBelowStatusBar(false)
                    .withTranslucentStatusBar(true)
                    .withSavedInstance(savedInstanceState)
                    .withActionBarDrawerToggle(true)
                    .withActionBarDrawerToggleAnimated(true)
                    .addDrawerItems(home)
                    .addDrawerItems(profile)
                    .addDrawerItems(gallery)
                    .addDrawerItems(recognition)
                    .addDrawerItems(maps)
                    .addDrawerItems(tagAndLocate)
                    .addDrawerItems(gamesAndPuzzle)
                    .addDrawerItems(new DividerDrawerItem())
                    .addDrawerItems(prefSettings)
                    .addDrawerItems(new DividerDrawerItem())
                    .addDrawerItems(logout)
                    .buildForFragment();
        } else if (userType.equalsIgnoreCase("Guardian User")) {
            result = new DrawerBuilder()
                    .withActivity(this)
                    .withAccountHeader(headerResult)
                    .withToolbar(toolbar)
                    .withDisplayBelowStatusBar(false)
                    .withTranslucentStatusBar(true)
                    .withSavedInstance(savedInstanceState)
                    .withActionBarDrawerToggle(true)
                    .withActionBarDrawerToggleAnimated(true)
                    .addDrawerItems(home)
                    .addDrawerItems(profile)
                    .addDrawerItems(maps)
                    .addDrawerItems(new DividerDrawerItem())
                    .addDrawerItems(logout)
                    .buildForFragment();
        }

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Profile profile = dataSnapshot.getValue(Profile.class);
                if (profile != null) {
                    String profilePic = profile.getProfile();

                    if (userType.equalsIgnoreCase("Standard User")) {
                        guardianEmail = profile.getGuardianEmail();
                        guardianEmail = guardianEmail.replace(".", ",");
                        myEmail = profile.getEmail();
                        myName = profile.getFullName();
                        patientPicture = profile.getProfile();
                        patientNumber = profile.getPhoneNumber();
                        databaseReferenceGuardian.child(guardianEmail).child("patientEmail").setValue(myEmail);
                        databaseReferenceGuardian.child(guardianEmail).child("patientName").setValue(myName);
                        databaseReferenceGuardian.child(guardianEmail).child("patientPicture").setValue(patientPicture);
                        databaseReferenceGuardian.child(guardianEmail).child("patientNumber").setValue(patientNumber);
                    }
                    if (userType.equalsIgnoreCase("Guardian User")) {
                        guardianEmail = profile.getEmail();
                        guardianEmail = guardianEmail.replace(".", ",");
                        guardianName = profile.getFullName();
                        guardianPicture = profile.getProfile();
                        guardianNumber = profile.getPhoneNumber();
                        databaseReferenceGuardian.child(guardianEmail).child("guardianName").setValue(guardianName);
                        databaseReferenceGuardian.child(guardianEmail).child("guardianPicture").setValue(guardianPicture);
                        databaseReferenceGuardian.child(guardianEmail).child("guardianNumber").setValue(guardianNumber);
                    }
                    if (profilePic != null && !profilePic.equals("")) {
                        userProfile.withIcon(profilePic);
                        headerResult.updateProfile(userProfile);
                    } else {
                        userProfile.withIcon(R.drawable.ic_account_circle_white_24dp);
                        headerResult.updateProfile(userProfile);
                    }
                    if (userType.equalsIgnoreCase("Standard User")) {
                        Intent intent = new Intent(MainMenuActivity.this, CurrentLocation.class);
                        intent.putExtra("guardianEmail", guardianEmail);
                        startService(intent);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (importantPeopleFlag) {
            importantPeopleFlag = false;
            Fragment fragment = new ImportantPeopleFragment();
            startFragment(fragment);
            result.setSelection(recognition);
        }

        if (mapMarkerFlag) {
            mapMarkerFlag = false;
            Fragment fragment = new MapsFragment();
            startFragment(fragment);
            result.setSelection(maps);
        }
            result.setOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                int drawItemId = (int) drawerItem.getIdentifier();
                Intent intent;
                Fragment fragment;
                if (userType.equalsIgnoreCase("Standard User")) {
                    switch (drawItemId) {

                        case 1:
                            fragment = new HomeFragment();
                            gaFragmentStack.add(home);
                            break;

                        case 2:
                            fragment = new ProfileFragment();
                            gaFragmentStack.add(profile);
                            break;
                        case 3:
                            fragment = new GalleryHomeFragment();
                            break;
                        case 4:
                            fragment = new RecognitionFragment();
                            gaFragmentStack.add(recognition);
                            break;
                        case 5:
                            fragment = new MapsFragment();
                            gaFragmentStack.add(maps);
                            break;
                        case 6:
                            fragment = new TagLocateFragment();
                            gaFragmentStack.add(tagAndLocate);
                            break;
                        case 7:
                            fragment = new GamesPuzzlesFragment();
                            gaFragmentStack.add(gamesAndPuzzle);
                            break;
                        default:
                            fragment = new HomeFragment();
                            break;
                    }
                } else {
                    switch (drawItemId) {

                        case 1:
                            fragment = new HomeFragment();
                            gaFragmentStack.add(home);
                            break;

                        case 2:
                            fragment = new ProfileFragment();
                            gaFragmentStack.add(profile);
                            break;
                        case 5:
                            fragment = new GuardianMapsFragment();
                            gaFragmentStack.add(maps);
                            break;
                        default:
                            fragment = new HomeFragment();
                            break;
                    }
                }

                if (drawItemId == 8) {
                    startActivity(new Intent(MainMenuActivity.this, SettingsPrefActivity.class));
                    return true;
                }
                if (drawItemId == 9) {
                    FirebaseAuth.getInstance().signOut();
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.clear();
                    editor.apply();
                    intent = new Intent(MainMenuActivity.this, SplashActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container_gaFragments, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
                return false;
            }
        });


    }

    public static Intent makeNotificationIntent(Context context, String msg) {
        Intent intent = new Intent(context, MainMenuActivity.class);
        intent.putExtra(NOTIFICATION_MSG, msg);
        return intent;
    }

    private void startFragment(Fragment fragment) {

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container_gaFragments, fragment);
        transaction.commit();

    }


    private void onAuthFailure() {
        Intent intent = new Intent(MainMenuActivity.this, SplashScreen.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();

    }

    @Override
    public void onBackPressed() {
        if (result.isDrawerOpen()) {
            result.closeDrawer();
        } else if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }


    }

    public void startProactiveFunctionality(boolean notificationState) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainMenuActivity.this);
        String userType = sharedPreferences.getString(Preferences.USER_TYPE, "");

        if(userType.equalsIgnoreCase("Standard User")) {
            if (notificationState) {
                if (timer == null) {
                    timer = new Timer();
                }
                TimerTask hourlyTask = new TimerTask() {
                    @Override
                    public void run() {
                        Intent notificationIntent = new Intent(getApplicationContext(), MapsFragment.class);
                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
                        stackBuilder.addParentStack(MainMenuActivity.class);
                        stackBuilder.addNextIntent(notificationIntent);
                        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(1, PendingIntent.FLAG_UPDATE_CURRENT);


                        // Creating and sending Notification
                        NotificationManager notificatioMng =
                                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        notificatioMng.notify(
                                1,
                                createNotification("aaa", notificationPendingIntent));
                    }

                };

                timer.schedule(hourlyTask, 0l, 1000*60*10);
            } else {
                if (timer != null) {
                    timer.cancel();
                    timer.purge();
                    timer = null;
                }
            }
        }
    }

    public void startProactiveFunctionalityForFuel(final boolean notificationState) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainMenuActivity.this);
        String userType = sharedPreferences.getString(Preferences.USER_TYPE, "");

                if (userType.equalsIgnoreCase("Standard User")) {
                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Profile model = dataSnapshot.getValue(Profile.class);
                            if (model != null) {
                                userStatus = model.getUserStatus();

                            }
                            if (notificationState) {
                                if (timerToAskFuel == null) {
                                    timerToAskFuel = new Timer();
                                }
                                TimerTask askFuelAvailability = new TimerTask() {
                                    @Override
                                    public void run() {
                                        if (!drivingcheckPrimary) {
                                            drivingcheckPrimary = true;
                                            if (userStatus.equals("vehicle")) {
                                                Intent notificationIntentFuel = new Intent(getApplicationContext(), MapsFragment.class);
                                                TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
                                                stackBuilder.addParentStack(MainMenuActivity.class);
                                                stackBuilder.addNextIntent(notificationIntentFuel);
                                                PendingIntent notificationPendingIntentFuel = stackBuilder.getPendingIntent(2, PendingIntent.FLAG_UPDATE_CURRENT);


                                                // Creating and sending Notification
                                                NotificationManager notificatioMng =
                                                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                                notificatioMng.notify(
                                                        2,
                                                        createNotificationForFuel("bbb", notificationPendingIntentFuel));

                                            }

                                        }
                                        if (drivingcheckPrimary && userStatus.equals("vehicle")) {
                                            drivingcheckPrimary = false;
                                        }


                                    }
                                };

                                timerToAskFuel.schedule(askFuelAvailability, 0l, 1000*60*10);

                            }else{
                            if (timerToAskFuel != null) {
                                timerToAskFuel.cancel();
                                timerToAskFuel.purge();
                                timerToAskFuel = null;
                            }
                        }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check auth on Activity start
        if (firebaseAuth.getCurrentUser() == null) {
            onAuthFailure();
        }

        PreferenceManager.setDefaultValues(this, R.xml.pref_main, false);

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPreferences();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        loadPreferences();
    }

    public void loadPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        notificationFlag = preferences.getBoolean(getString(R.string.notifications_new_message), false);
        //backupFlag = preferences.getBoolean(getString(R.string.backup_key), true);

        preferences.registerOnSharedPreferenceChangeListener(MainMenuActivity.this);

        startProactiveFunctionality(notificationFlag);
        startProactiveFunctionalityForFuel(notificationFlag);
//        if ((ContextCompat.checkSelfPermission(this,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(this,
//                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
//            if ((ActivityCompat.shouldShowRequestPermissionRationale(MainMenuActivity.this,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) && (ActivityCompat.shouldShowRequestPermissionRationale(MainMenuActivity.this,
//                    Manifest.permission.READ_EXTERNAL_STORAGE))) {
//
//            } else {
//                ActivityCompat.requestPermissions(MainMenuActivity.this,
//                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
//                        REQUEST_PERMISSIONS);
//            }
//        }else {
//            Log.e("ElseMain","Else");
//            startBackupService(backupFlag);
//        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        switch (requestCode) {
//            case REQUEST_PERMISSIONS: {
//                for (int i = 0; i < grantResults.length; i++) {
//                    if (grantResults.length > 0 && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
//                        startBackupService(backupFlag);
//                    } else {
//                        Toast.makeText(getApplicationContext(), "The app was not allowed to read or write to your storage. Hence, it cannot function properly. Please consider granting it this permission", Toast.LENGTH_LONG).show();
//                    }
//                }
//            }
//        }
//    }

//    public void startBackupService(boolean backupFlag){
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainMenuActivity.this);
//        String userType = preferences.getString(Preferences.USER_TYPE, "");
//        if(userType.equalsIgnoreCase("Standard User")) {
//            if (backupFlag) {
//                scheduleBackup();
//            } else {
//                cancelBackup();
//            }
//        }
//    }
//
//    public void scheduleBackup() {
//        Intent myIntent = new Intent(MainMenuActivity.this, BackupReceiver.class);
//        myIntent.setAction("com.project.group.projectga.service.BACKUP");
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, myIntent, 0);
//        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//
//        alarmManager.setInexactRepeating(AlarmManager.RTC, SystemClock.elapsedRealtime(), 3600000 * 24, pendingIntent);
//        //Toast.makeText(this, "Backup Scheduled", Toast.LENGTH_SHORT).show();
//    }
//
//    public void cancelBackup() {
//        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        Intent myIntent = new Intent(MainMenuActivity.this, BackupReceiver.class);
//        myIntent.setAction("com.project.group.projectga.service.BACKUP");
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, myIntent, 0);
//
//        alarmManager.cancel(pendingIntent);
//    }


    public Notification createNotification(String msg, PendingIntent notificationPendingIntent) {
        Intent intent = new Intent(getApplicationContext(), MainMenuActivity.class);
        intent.putExtra("Maps", "mapsFragment");

        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext());
        notificationBuilder
                .setSmallIcon(R.drawable.ic_place_black_24dp)
                .setColor(Color.WHITE)
                .setContentTitle("Your safety")
                .setContentText("Are you in a safe place? Do you need to navigate?")
                .setContentIntent(notificationPendingIntent)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
                .addAction(R.drawable.ic_close_black_24dp, "Dismiss", pIntent)
                .addAction(R.drawable.ic_map_black_24dp, "Go to Maps", pIntent)
                .setAutoCancel(true);
        return notificationBuilder.build();

    }
	
	    public Notification createNotificationForFuel(String msg, PendingIntent notificationPendingIntent) {
        Intent intent = new Intent(getApplicationContext(), MainMenuActivity.class);
        intent.putExtra("Maps", "gasstation");

        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext());
        notificationBuilder
                .setSmallIcon(R.drawable.ic_my_location_black_24dp)
                .setColor(Color.WHITE)
                .setContentTitle("Car Fuel Check")
                .setContentText("Do you have fuel in your car to drive to your location?")
                .setContentIntent(notificationPendingIntent)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
                .addAction(R.drawable.ic_close_black_24dp,"Dismiss",pIntent)
                .addAction(R.drawable.ic_local_gas_station_black_24dp,"Find Gas Station",pIntent)
                .setAutoCancel(true);
        return notificationBuilder.build();

    }
}