package com.project.group.projectga.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.group.projectga.preferences.Preferences;
import com.project.group.projectga.activities.MapMarkerActivity;
import com.project.group.projectga.models.MapMarkers;
import com.project.group.projectga.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    Toolbar toolbar;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    ArrayList<MapMarkers> mapMarkerList;
    String userId;
    String mapMarkerKey = null;
    Marker marker;

    public static MapsFragment newInstance() {
        MapsFragment mapsFragment = new MapsFragment();
        return mapsFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, null, false);
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // set the background and recolor the menu icon for the toolbar
        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setBackground(getResources().getDrawable(R.drawable.tile_yellow));
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_menu_yellow_24dp));
        // change the icon on the toolbar
        ImageView icon = (ImageView) getActivity().findViewById(R.id.toolbarIcon);
        icon.setImageResource(R.drawable.ic_place_black_24dp);
        icon.setColorFilter(getResources().getColor(R.color.Map));
        // change the title on the toolbar
        TextView title = (TextView) getActivity().findViewById(R.id.toolbarTitle);
        title.setText(R.string.mapLabel);
        title.setTextColor(getResources().getColor(R.color.textInputEditTextColor));

        firebaseAuth = FirebaseAuth.getInstance();

        if (getUid() != null) {
            userId = getUid();
            firebaseAuth = FirebaseAuth.getInstance();
            databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("Map Markers");
        } else {
        }

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        // enable the option menu

        setHasOptionsMenu(true);

        return view;
    }


    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // Add a marker for UTA and move the camera
        LatLng uta = new LatLng(32.7311, -97.1141);
        mMap.addMarker(new MarkerOptions().position(uta).title("UTA"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(uta));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Intent intent = new Intent(getContext(), MapMarkerActivity.class);
                Double lat = latLng.latitude;
                Double lng = latLng.longitude;
                String address = getAddressFromLatLng(latLng);
                Toast.makeText(getContext(), address, Toast.LENGTH_SHORT).show();
                String coordLat = lat.toString();
                String coordLng = lng.toString();
                intent.putExtra("LAT", coordLat);
                intent.putExtra("LONG", coordLng);
                intent.putExtra("ADDRESS", address);
                startActivity(intent);
            }
        });

        mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                LatLng markerLocation = marker.getPosition();
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + markerLocation.latitude + "," + markerLocation.longitude + "=w");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
                return false;
            }
        });

        if (getUid() != null) {
            userId = getUid();
            firebaseAuth = FirebaseAuth.getInstance();
            databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("Map Markers");
        } else {
            //onAuthFailure();
        }

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mapMarkerList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MapMarkers m = snapshot.getValue(MapMarkers.class);
                    String key = snapshot.getKey();
                    m.setKey(key);
                    mapMarkerList.add(m);
                    String latitude = m.getLatitude();
                    String longitude = m.getLongitude();
                    Double lat = Double.parseDouble(latitude);
                    Double lng = Double.parseDouble(longitude);
                    LatLng newLocation = new LatLng(lat, lng);
                    mMap.addMarker(new MarkerOptions()
                            .position(newLocation));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // add blank icon to the toolbar for integrity of constraints
        menu.add(null).setIcon(R.drawable.ic_android_trans_24dp).setShowAsActionFlags(1);


        super.onCreateOptionsMenu(menu, inflater);
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private String getAddressFromLatLng(LatLng latLng) {
        Geocoder geocoder = new Geocoder(getActivity());
        StringBuilder strReturnedAddress=null;
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
             if(addresses != null){
                 Address returnedAddress = addresses.get(0);
                 strReturnedAddress = new StringBuilder();
                 for(int i=0; i < returnedAddress.getMaxAddressLineIndex(); i++){
                     strReturnedAddress.append(returnedAddress.getAddressLine(i)).append(",");
                 }
             }
        } catch (IOException e) {
            e.printStackTrace();
        }
//        String address = "";
//        try {
//            address = geocoder
//                    .getFromLocation(latLng.latitude, latLng.longitude, 1)
//                    .get(0).getAddressLine(0);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        return strReturnedAddress.toString();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mCurrentLocation = LocationServices
                .FusedLocationApi
                .getLastLocation(mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}