package com.project.group.projectga.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.storage.FirebaseStorage;
import com.project.group.projectga.R;
import com.project.group.projectga.models.LocationModel;
import com.project.group.projectga.models.Profile;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Date;

import static android.content.Context.LOCATION_SERVICE;

public class GuardianMapsFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, ResultCallback<Status> {

    Toolbar toolbar;
    private GoogleMap mMap;
    private static final int REQUEST_LOCATION = 1;
    private LatLng latlangForGeo;
    private Circle mCircle1;
    private Circle mCircle2;
    private Circle mCircle3;
    LocationManager locationManager;
    double geofenceradius[] = new double[]{10000, 20000, 25000};
    ArrayList<LatLng> arrayPoints;
    boolean geoSetAlready;
    private GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
	String thisEmail;

    private static final int REQUEST_PERMISSIONS = 100;

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

    public GuardianMapsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_guardian_maps, null, false);
        mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        thisEmail = getEmail();
        if (getUid() != null) {
            userId = getUid();
            firebaseAuth = FirebaseAuth.getInstance();
            //databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("Geofence Location");
            databaseReferenceGuardian = FirebaseDatabase.getInstance().getReference().child("guardians").child("guardianEmails");
            databaseReferenceEmail = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
			databaseReference = FirebaseDatabase.getInstance().getReference().child("guardians").child("guardianEmails").child(thisEmail);
        }

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
                floatingActionsMenu.collapse();
                mapFragment.getView().setBackgroundColor(getResources().getColor(android.R.color.transparent));
                //mapFragment.getView().setAlpha(0.5f);
                clickMap.setVisibility(View.VISIBLE);
            }
        });

        removeGeoFence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatingActionsMenu.collapse();
                removeGeoFences();
            }
        });

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
        }
        mMap.setMyLocationEnabled(true);

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

                //mapFragment.getView().setAlpha(1.0f);
                mapFragment.getView().setBackgroundColor(getResources().getColor(R.color.loginbackground));
                clickMap.setVisibility(View.GONE);
                databaseReference.child("latitude").setValue(latLng.latitude);
                databaseReference.child("longitude").setValue(latLng.longitude);


                if (!geoSetAlready) {

                    setMap(latLng);
                }

            }
        });

        databaseReferenceEmail.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Profile profile = dataSnapshot.getValue(Profile.class);
                if(profile !=null) {
                    guardianEmail = profile.getEmail();
                }
                guardianEmail = guardianEmail.replace(".", ",");
                databaseReferenceGuardian = databaseReferenceGuardian.child(guardianEmail).child("Current Location");

                databaseReferenceGuardian.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        LocationModel locationModel = dataSnapshot.getValue(LocationModel.class);
                        if(dataSnapshot.exists()) {
                            if (patientMarker != null)
                            {
                                patientMarker.remove();
                            }
                            currentLat = locationModel.getCurrentLat();
                            currentLong = locationModel.getCurrentLong();
                        }
                        Double currentLatDouble = Double.parseDouble(currentLat);
                        Double currentLongDouble = Double.parseDouble(currentLong);
                        LatLng newLocation = new LatLng(currentLatDouble, currentLongDouble);
                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(newLocation)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                                .title("Patient");
                        patientMarker = mMap.addMarker(markerOptions);
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(newLocation, 15);
                        mMap.animateCamera(cameraUpdate);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults.length > 0 && grantResults[i] == PackageManager.PERMISSION_GRANTED) {

                    } else {
                        Toast.makeText(getContext(), "The app was not allowed to read or write to your storage. Hence, it cannot function properly. Please consider granting it this permission", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

    public void setMap(LatLng latLng) {
        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.logoga);
        Bitmap b = bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, 200, 200, false);
        latlangForGeo = latLng;
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title("name display")
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
}
