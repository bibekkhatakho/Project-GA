package com.project.group.projectga.fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.group.projectga.R;
import com.project.group.projectga.activities.MainMenuActivity;
import com.project.group.projectga.activities.PhotosActivity;
import com.project.group.projectga.activities.SettingsPrefActivity;
import com.project.group.projectga.models.LocationModel;
import com.project.group.projectga.models.Profile;
import java.util.ArrayList;

public class GuardianMapsFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, ResultCallback<Status> {

    Toolbar toolbar;
    private GoogleMap mMap;
    private static final int REQUEST_LOCATION = 137;
    private LatLng latlangForGeo;
    private Circle mCircle1;
    private Circle mCircle2;
    private Circle mCircle3;
    double geofenceradius[] = new double[]{1609.344, 3218.688, 8046.72};
    ArrayList<LatLng> arrayPoints;
    boolean geoSetAlready;
    private GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
	String thisEmail;

    double lat,lon;
    boolean geoFenceLocationExists = false;
    boolean geoFenceExistsInFirebase;
    boolean checkroute = false;
    private LatLng testLL;

    private static final int REQUEST_PERMISSIONS = 138;

    String userId;

    FloatingActionsMenu floatingActionsMenu;
    FloatingActionButton addGeoFence;
    FloatingActionButton removeGeoFence;
    TextView clickMap;
    SupportMapFragment mapFragment;
    private Marker geofenceMarker;
	
	String guardianEmail;
    String currentLat;
    String currentLong;
	Marker patientMarker;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    DatabaseReference databaseReferenceGuardian;
    DatabaseReference databaseReferenceEmail;

//    private Dialog dialogAddRadius;
//    TextInputEditText radiusSafe;
//    TextInputEditText radiusCaution;
//    TextInputEditText radiusDanger;
//
//    String radiusSafeString;
//    String radiusCautionString;
//    String radiusDangerString;
//
//    android.support.design.widget.FloatingActionButton okButton;
//    android.support.design.widget.FloatingActionButton cancelButton;


    public GuardianMapsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        thisEmail = getEmail();
        if (getUid() != null) {
            userId = getUid();
            firebaseAuth = FirebaseAuth.getInstance();
            databaseReferenceGuardian = FirebaseDatabase.getInstance().getReference().child("guardians").child("guardianEmails");
            databaseReferenceEmail = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
            databaseReference = FirebaseDatabase.getInstance().getReference().child("guardians").child("guardianEmails").child(thisEmail);
        }

        getGeofenceLocation();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_guardian_maps, null, false);
        mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.mapGuard);
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

        floatingActionsMenu = (FloatingActionsMenu) view.findViewById(R.id.fab_menu);
        addGeoFence = (FloatingActionButton) view.findViewById(R.id.fab_add_geofence);
        removeGeoFence = (FloatingActionButton) view.findViewById(R.id.fab_remove_geofence);
        clickMap = (TextView) view.findViewById(R.id.clickMap);

        arrayPoints = new ArrayList<LatLng>();

        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // enable the option menu

        addGeoFence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkroute = true;
                floatingActionsMenu.collapse();
                mapFragment.getView().setBackgroundColor(getResources().getColor(android.R.color.transparent));
                //mapFragment.getView().setAlpha(0.5f);
                clickMap.setVisibility(View.VISIBLE);
                addGeofenceGuardian();
            }
        });

        removeGeoFence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatingActionsMenu.collapse();
                removeGeoFences();
            }
        });

        setHasOptionsMenu(true);

        return view;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }else{
            Log.d("MapsFragment", "Permission Granted");
            mMap.setMyLocationEnabled(true);
        }
        mMap.setMyLocationEnabled(true);
        if(geoFenceExistsInFirebase){
            setMap(testLL);
        }

        databaseReferenceEmail.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Profile profile = dataSnapshot.getValue(Profile.class);
                    if (profile != null) {
                        guardianEmail = profile.getEmail();
                    }
                    guardianEmail = guardianEmail.replace(".", ",");
                    databaseReferenceGuardian = databaseReferenceGuardian.child(guardianEmail).child("Current Location");

                    databaseReferenceGuardian.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            LocationModel locationModel = dataSnapshot.getValue(LocationModel.class);
                            if (dataSnapshot.exists()) {
                                if (patientMarker != null) {
                                    patientMarker.remove();
                                }
                                if (locationModel != null && !locationModel.toString().isEmpty() && !locationModel.getCurrentLat().isEmpty() && !locationModel.getCurrentLong().isEmpty() && locationModel.getCurrentLat() != null && locationModel.getCurrentLong() != null) {
                                    currentLat = locationModel.getCurrentLat();
                                    currentLong = locationModel.getCurrentLong();


                                    Double currentLatDouble = Double.parseDouble(currentLat);
                                    Double currentLongDouble = Double.parseDouble(currentLong);
                                    LatLng newLocation = new LatLng(currentLatDouble, currentLongDouble);
                                    MarkerOptions markerOptions = new MarkerOptions()
                                            .position(newLocation)
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                                            .title("Patient");
                                    patientMarker = mMap.addMarker(markerOptions);
                                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(newLocation, 11.5f);
                                    mMap.animateCamera(cameraUpdate);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }

    public void addGeofenceGuardian(){
        if (geoFenceLocationExists) {
            if(!geoSetAlready) {
                setMap(testLL);
            }
        } else {
            mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    if(checkroute) {
                        mapFragment.getView().setBackgroundColor(getResources().getColor(R.color.loginbackground));
                        clickMap.setVisibility(View.GONE);
                        databaseReference.child("latitude").setValue(latLng.latitude);
                        databaseReference.child("longitude").setValue(latLng.longitude);
                        databaseReference.child("geofenceLocationExists").setValue(true);

                        if (!geoSetAlready) {
                            setMap(latLng);
                        }
                        checkroute = false;
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults.length > 0 && grantResults[i] == PackageManager.PERMISSION_GRANTED) {

                    } else {
                        Toast.makeText(getContext(), "The app was not allowed to access your location. Hence, it cannot function properly. Please consider granting it this permission", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

    public void setMap(LatLng latLng) {

        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.markericon);
        Bitmap b = bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, 200, 200, false);
        latlangForGeo = latLng;
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title("Geofence for Patient")
                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
    CircleOptions circleOptions1 = new CircleOptions()
            .center(latLng)
            .strokeColor(Color.argb(25,239,69,56))
            .fillColor(Color.argb(40,239,69,56))
            .radius(geofenceradius[0]);
    CircleOptions circleOptions2 = new CircleOptions()
            .center(latLng)
            .strokeColor(Color.argb(25,239,69,56))
            .fillColor(Color.argb(50, 239, 69, 56))
            .radius(geofenceradius[1]);
    CircleOptions circleOptions3 = new CircleOptions()
            .center(latLng)
            .strokeColor(Color.argb(25,239,69,56))
            .fillColor(Color.argb(75, 239, 69, 56))
            .radius(geofenceradius[2]);
        geofenceMarker = mMap.addMarker(markerOptions);
        mCircle1 = mMap.addCircle(circleOptions1);
        mCircle2 = mMap.addCircle(circleOptions2);
        mCircle3 = mMap.addCircle(circleOptions3);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5));
        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 1000, null);
        geoSetAlready = true;
    }

    public void removeGeoFences(){
        if(mCircle1 !=null && mCircle2 != null && mCircle3 !=null){
            mCircle1.remove();
            mCircle2.remove();
            mCircle3.remove();
            if(geofenceMarker != null){
                geofenceMarker.remove();
            }
            mMap.animateCamera(CameraUpdateFactory.zoomOut());
            mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 1000, null);
            geoSetAlready = false;
        }

        databaseReference.child("latitude").removeValue();
        databaseReference.child("longitude").removeValue();
        databaseReference.child("geofenceLocationExists").setValue(false);
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
    public void onResult(@NonNull Status status) {

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
        LatLng current = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        mMap.addMarker(new MarkerOptions().position(current).title("Your Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(current));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
	public String getEmail() {
        return FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");
    }

    public void getGeofenceLocation(){

                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        LocationModel lModel = dataSnapshot.getValue(LocationModel.class);
                        if(lModel !=null){
                            lat = lModel.getLatitude();
                            lon = lModel.getLongitude();
                            geoFenceExistsInFirebase = lModel.isGeofenceLocationExists();
                            testLL = new LatLng(lat,lon);
                            if(geoFenceExistsInFirebase) {
                                geoFenceLocationExists = true;
                            }else{
                                geoFenceLocationExists = false;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
}
