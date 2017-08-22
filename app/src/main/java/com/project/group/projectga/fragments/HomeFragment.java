package com.project.group.projectga.fragments;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.pm.ActivityInfoCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.group.projectga.R;
import com.project.group.projectga.models.LocationModel;
import com.project.group.projectga.models.Profile;
import com.project.group.projectga.preferences.Preferences;

public class HomeFragment extends Fragment {
    Toolbar toolbar;
    private static final int REQUEST_LOCATION = 1;

    double lat, lon;
    boolean geoFenceLocationExists = false;
    private LatLng testLL;

    DatabaseReference gEmail;
    String guardianEmail;
    DatabaseReference databaseReferenceGeo;

    public HomeFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
//        if (getUid() != null) {
//            String userId = getUid();
//            gEmail = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
//        }
//        getGeofenceLocation();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String userType = sharedPreferences.getString(Preferences.USER_TYPE, "");
        View view;
        if (userType.equalsIgnoreCase("Standard User")) {
            view = inflater.inflate(R.layout.fragment_home, container, false);
        } else {
            view = inflater.inflate(R.layout.fragment_home_guardian, container, false);
        }
        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        ImageView icon = (ImageView) getActivity().findViewById(R.id.toolbarIcon);
        icon.setImageResource(R.drawable.logoga);
        icon.setColorFilter(null);
        TextView title = (TextView) getActivity().findViewById(R.id.toolbarTitle);
        title.setText(getString(R.string.googleAlzheimer));
        title.setTextColor(getResources().getColor(R.color.textInputEditTextColor));
        toolbar.setBackground(null);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_menu_red_24dp));

        initControls(view, userType);
        setHasOptionsMenu(true);
        if (ActivityCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(),
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.SET_ALARM) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            android.Manifest.permission.SET_ALARM},
                    REQUEST_LOCATION);
        } else {
            Log.e("DB", "PERMISSION GRANTED");
        }
        return view;
    }


    public void initControls(View view, String userType) {

        if (userType.equalsIgnoreCase("Standard User")) {
            CardView galleryCard = (CardView) view.findViewById(R.id.galleryCard);
            CardView mapsCard = (CardView) view.findViewById(R.id.mapCard);
            CardView recognitionCard = (CardView) view.findViewById(R.id.recognitionCard);
            CardView tagAndLocateCard = (CardView) view.findViewById(R.id.locateCard);
            CardView gamesAndPuzzlesCard = (CardView) view.findViewById(R.id.gamesCard);

            galleryCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Fragment galleryFragment = new GalleryHomeFragment();
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.container_gaFragments, galleryFragment);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    ft.addToBackStack(null);
                    ft.commit();
                }
            });
            mapsCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Fragment mapsFragment = new MapsFragment();
//                    Bundle arguments = new Bundle();
//                    arguments.putDouble("testLat", testLL.latitude);
//                    arguments.putDouble("testLong", testLL.longitude);
//                    arguments.putBoolean("geoExists", geoFenceLocationExists);
//                    mapsFragment.setArguments(arguments);
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.container_gaFragments, mapsFragment);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    ft.addToBackStack(null);
                    ft.commit();
                }
            });
            recognitionCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Fragment recognitionFragment = new RecognitionFragment();
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.container_gaFragments, recognitionFragment);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    ft.addToBackStack(null);
                    ft.commit();
                }
            });
            tagAndLocateCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Fragment tagAndLocateFragment = new TagLocateFragment();
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.container_gaFragments, tagAndLocateFragment);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    ft.addToBackStack(null);
                    ft.commit();
                }
            });
            gamesAndPuzzlesCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Fragment gamesAndPuzzlesFragment = new GamesPuzzlesFragment();
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.container_gaFragments, gamesAndPuzzlesFragment);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    ft.addToBackStack(null);
                    ft.commit();
                }
            });

        } else if (userType.equalsIgnoreCase("Guardian User")) {

            CardView mapsCard = (CardView) view.findViewById(R.id.mapCard);

            mapsCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Fragment mapsFragment = new GuardianMapsFragment();
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.container_gaFragments, mapsFragment);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    ft.addToBackStack(null);
                    ft.commit();
                }
            });

        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(null).setIcon(R.drawable.ic_android_trans_24dp).setShowAsActionFlags(1);

        super.onCreateOptionsMenu(menu, inflater);
    }

//    public void getGeofenceLocation() {
//        gEmail.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                Profile profile = dataSnapshot.getValue(Profile.class);
//                if (profile != null) {
//                    guardianEmail = profile.getGuardianEmail().replace(".", ",");
//                }
//                databaseReferenceGeo = FirebaseDatabase.getInstance().getReference().child("guardians").child("guardianEmails").child(guardianEmail);
//                databaseReferenceGeo.addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        LocationModel lModel = dataSnapshot.getValue(LocationModel.class);
//                        if (lModel != null) {
//                            lat = lModel.getLatitude();
//                            lon = lModel.getLongitude();
//                            testLL = new LatLng(lat, lon);
//                            if (lat != 0.0 && lon != 0.0 && !testLL.toString().isEmpty() && testLL.toString() != null) {
//                                geoFenceLocationExists = true;
//                                Toast.makeText(getActivity(), "geo" + String.valueOf(geoFenceLocationExists), Toast.LENGTH_SHORT).show();
//                            } else {
//                                Toast.makeText(getActivity(), "Lat Long not set", Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                           Toast.makeText(getContext(), "The latitude from Guardiann" + lat + geoFenceLocationExists, Toast.LENGTH_SHORT).show();
//                         Toast.makeText(getContext(), "The longitude from Guardiann" + lon, Toast.LENGTH_SHORT).show();
//                        Toast.makeText(getContext(), "The longitude from Guardiann" + testLL.toString(), Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
//                //  Toast.makeText(getContext(),"g email "+ guardianEmail,Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//            }
//        });
//    }
//
//    public String getUid() {
//        return FirebaseAuth.getInstance().getCurrentUser().getUid();
//    }
}