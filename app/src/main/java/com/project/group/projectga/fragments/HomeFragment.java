package com.project.group.projectga.fragments;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
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
import com.project.group.projectga.service.ActivityRecognizedService;

public class HomeFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    Toolbar toolbar;
    private static final int REQUEST_LOCATION = 1;

    FloatingActionButton smsButton;
	GoogleApiClient mApiClient;

    int counter=0;

    public HomeFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
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

        smsButton = (FloatingActionButton) getActivity().findViewById(R.id.messageActionButton);

        smsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment messagesFragment = new MessagesFragment();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.container_gaFragments, messagesFragment);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        initControls(view, userType);
        setHasOptionsMenu(true);
        if (ActivityCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(),
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.SET_ALARM) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            android.Manifest.permission.SET_ALARM,
                            android.Manifest.permission.RECEIVE_SMS,
                            android.Manifest.permission.READ_SMS,
                            android.Manifest.permission.SEND_SMS},
                    REQUEST_LOCATION);
        } else {
            Log.e("DB", "PERMISSION GRANTED");
        }
        mApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mApiClient.connect();



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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Intent intent;
        intent = new Intent(getContext(),ActivityRecognizedService.class);
        PendingIntent pendingIntent = PendingIntent.getService( getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mApiClient, 1000, pendingIntent);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

}