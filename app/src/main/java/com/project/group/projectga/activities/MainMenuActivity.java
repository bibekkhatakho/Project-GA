package com.project.group.projectga.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

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
import com.project.group.projectga.fragments.BackupFragment;
import com.project.group.projectga.fragments.GalleryHomeFragment;
import com.project.group.projectga.fragments.GamesPuzzlesFragment;
import com.project.group.projectga.fragments.HomeFragment;
import com.project.group.projectga.fragments.MapsFragment;
import com.project.group.projectga.fragments.ProfileFragment;
import com.project.group.projectga.fragments.ImportantPeopleFragment;
import com.project.group.projectga.fragments.RecognitionFragment;
import com.project.group.projectga.fragments.TagLocateFragment;
import com.project.group.projectga.models.Profile;
import com.project.group.projectga.preferences.Preferences;
import com.squareup.picasso.Picasso;

import java.util.Stack;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainMenuActivity extends CoreActivity {

    @BindView(R.id.toolbar)
    protected Toolbar toolbar;

    private Drawer result = null;
    AccountHeader headerResult;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;

    Stack<PrimaryDrawerItem> gaFragmentStack;

    boolean profileFlag, homeFlag = true, importantPeopleFlag=false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        ButterKnife.bind(this);

        firebaseAuth = FirebaseAuth.getInstance();

        setSupportActionBar(toolbar);

        gaFragmentStack = new Stack<>();

        Fragment home_fragment = new HomeFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container_gaFragments, home_fragment);
        transaction.addToBackStack("home");
        transaction.commit();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            profileFlag = extras.getBoolean("profileFlag");
            homeFlag = extras.getBoolean("homeFlag");
            importantPeopleFlag = extras.getBoolean("importantPeopleFlag");
        }

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainMenuActivity.this);
        final String userType = preferences.getString(Preferences.USER_TYPE, "");
        Toast.makeText(this, userType, Toast.LENGTH_LONG).show();

        Log.d("userTypeMain", userType);

        if (getUid() != null) {
            String userId = getUid();
            firebaseAuth = FirebaseAuth.getInstance();
            databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userId);

        } else {
            onAuthFailure();
        }

        final PrimaryDrawerItem home = new PrimaryDrawerItem().withName("Home").withIdentifier(1).withIcon(GoogleMaterial.Icon.gmd_home);
        final PrimaryDrawerItem profile = new PrimaryDrawerItem().withName("Profile").withIdentifier(2).withIcon(GoogleMaterial.Icon.gmd_account);
        final PrimaryDrawerItem gallery = new PrimaryDrawerItem().withName("Gallery").withIdentifier(3).withIcon(R.drawable.ic_perm_media_black_24dp);
        final PrimaryDrawerItem recognition = new PrimaryDrawerItem().withName("Recognition").withIdentifier(4).withIcon(GoogleMaterial.Icon.gmd_face);
        final PrimaryDrawerItem maps = new PrimaryDrawerItem().withName("Maps").withIdentifier(5).withIcon(R.drawable.ic_place_black_24dp);
        final PrimaryDrawerItem tagAndLocate = new PrimaryDrawerItem().withName("Tag & Locate").withIdentifier(6).withIcon(R.drawable.ic_remove_red_eye_black_24dp);
        final PrimaryDrawerItem gamesAndPuzzle = new PrimaryDrawerItem().withName("Games & Puzzles").withIdentifier(7).withIcon(R.drawable.ic_casino_black_24dp);
        final PrimaryDrawerItem backup = new PrimaryDrawerItem().withName("Backup").withIdentifier(8).withIcon(GoogleMaterial.Icon.gmd_save);
        final PrimaryDrawerItem logout = new PrimaryDrawerItem().withName("Logout").withIdentifier(9).withIcon(FontAwesome.Icon.faw_sign_out);

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
        final ProfileDrawerItem userProfile = new ProfileDrawerItem().withName(name).withEmail(email).withIcon(R.mipmap.ic_account_circle_white_24dp);

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


        if(userType.equalsIgnoreCase("Standard")) {
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
                    .addDrawerItems(backup)
                    .addDrawerItems(new DividerDrawerItem())
                    .addDrawerItems(logout)
                    .buildForFragment();
        }else if(userType.equalsIgnoreCase("Guardian")){
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
                    .addDrawerItems(backup)
                    .addDrawerItems(new DividerDrawerItem())
                    .addDrawerItems(logout)
                    .buildForFragment();
        }
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Profile profile = dataSnapshot.getValue(Profile.class);
                String profilePic = profile.getProfile();
                if (profilePic != null && !profilePic.equals("")) {
                    userProfile.withIcon(profilePic);
                    headerResult.updateProfile(userProfile);
                } else {
                    userProfile.withIcon(R.mipmap.ic_account_circle_white_24dp);
                    headerResult.updateProfile(userProfile);
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

        result.setOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                @Override
                public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                    int drawItemId = (int) drawerItem.getIdentifier();
                    Intent intent;
                    Fragment fragment;
                    if(userType.equalsIgnoreCase("Standard")) {
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
                                gaFragmentStack.add(gallery);
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
                            case 8:
                                fragment = new BackupFragment();
                                gaFragmentStack.add(backup);
                                break;
                            default:
                                fragment = new HomeFragment();
                                break;
                        }
                    }else {
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
                                fragment = new MapsFragment();
                                gaFragmentStack.add(maps);
                                break;
                            case 8:
                                fragment = new BackupFragment();
                                gaFragmentStack.add(backup);
                                break;
                            default:
                                fragment = new HomeFragment();
                                break;
                        }
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
        }
        else if (getFragmentManager().getBackStackEntryCount() > 0 ){
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        // Check auth on Activity start
        if (firebaseAuth.getCurrentUser() == null) {
            onAuthFailure();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


}