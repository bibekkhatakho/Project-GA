package com.project.group.projectga.fragments;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
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
import com.project.group.projectga.activities.MapMarkerActivity;
import com.project.group.projectga.models.LocationModel;
import com.project.group.projectga.models.MapMarkers;
import com.project.group.projectga.models.Profile;
import com.project.group.projectga.preferences.Preferences;
import com.project.group.projectga.service.GeofenceTransitionService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MapsFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,LocationListener, OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener,ResultCallback<Status> {

    Toolbar toolbar;

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;

    private static final String TAG = GuardianMapsFragment.class.getSimpleName();

    private Marker geoFenceMarker;
    private Marker locationMarker;
    private LocationRequest locationRequest;
    private Location lastLocation;

    private LatLng testLL;

    String inString[] = new String[]{"a","b","c"};
    int count = 0;
    private static final long GEO_DURATION = Geofence.NEVER_EXPIRE;
    double geoRadius[] = new double[]{100,200,300};
    private Circle geoFenceLimits;

    private final int UPDATE_INTERVAL =  3 * 60 * 1000;
    private final int FASTEST_INTERVAL = 30 * 1000;
    private final int REQ_PERMISSION = 999;
    double lat,lon;
    boolean geoFenceLocationExists = false;
    String guardianEmail;
    private final String KEY_GEOFENCE_LAT = "GEOFENCE LATITUDE";
    private final String KEY_GEOFENCE_LON = "GEOFENCE LONGITUDE";
    public static final int SEND_SMS = 101;

    private PendingIntent geoFencePendingIntent1;
    private PendingIntent geoFencePendingIntent2;
    private PendingIntent geoFencePendingIntent3;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    DatabaseReference databaseReferenceGeo;
    DatabaseReference gEmail;


    String userId;

    private String[] markerIcons = {
            "general",
            "home",
            "work",
            "hospital",
            "airport",
            "firestation",
            "police",
            "gasstation",
    };
    ArrayList<MapMarkers> mapMarkerList;

    public MapsFragment(){

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String userIdPref = preferences.getString(Preferences.USERID, "");
        if (userIdPref != null) {
            userId = userIdPref;
            firebaseAuth = FirebaseAuth.getInstance();
            databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("Map Markers");
            gEmail = FirebaseDatabase.getInstance().getReference().child("users").child(userId);

        }
        getGeofenceLocation();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, null, false);
        initGMaps();

        createGoogleApi();

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.SEND_SMS},SEND_SMS);

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
        return view;
    }

    private void createGoogleApi() {
        if ( googleApiClient == null ) {
            googleApiClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi( LocationServices.API )
                    .build();
        }
    }


    private void initGMaps() {
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);
        askPermission();
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        googleMap.setMyLocationEnabled(true);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
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
                    String iconNumber = m.getMarkerIcon();
                    Integer iconNum = Integer.parseInt(iconNumber);
                    int resID = getActivity().getResources().getIdentifier("com.project.group.projectga:drawable/" + markerIcons[iconNum], null, null);
                    mMap.addMarker(new MarkerOptions()
                            .position(newLocation)
                            .icon(BitmapDescriptorFactory.fromResource(resID)));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }
    @Override
    public void onMapLongClick(LatLng latLng) {
        Intent intent = new Intent(getContext(), MapMarkerActivity.class);
        Double lat = latLng.latitude;
        Double lng = latLng.longitude;
        String address = getAddressFromLatLng(latLng);
        String coordLat = lat.toString();
        String coordLng = lng.toString();
        intent.putExtra("LAT", coordLat);
        intent.putExtra("LONG", coordLng);
        intent.putExtra("ADDRESS", address);
        startActivity(intent);
    }

    // Callback called when Marker is touched
    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }



    // Get last known location
    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation()");
        if ( checkPermission() ) {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if ( lastLocation != null ) {
                Log.i(TAG, "LasKnown location. " +
                        "Long: " + lastLocation.getLongitude() +
                        " | Lat: " + lastLocation.getLatitude());
                writeLastLocation();
                startLocationUpdates();
            } else {
                Log.w(TAG, "No location retrieved yet");
                startLocationUpdates();
            }
        }
        else askPermission();
    }

    // Start location Updates
    private void startLocationUpdates(){
        Log.i(TAG, "startLocationUpdates()");
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        if ( checkPermission() )
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged ["+location+"]");
        lastLocation = location;
        writeActualLocation(location);
    }

    // Write location coordinates on UI
    private void writeActualLocation(Location location) {


        markerLocation(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    private void writeLastLocation() {
        writeActualLocation(lastLocation);
    }

    // Check for permission to access Location
    private boolean checkPermission() {
        Log.d(TAG, "checkPermission()");
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED );
    }

    // Asks for permission
    private void askPermission() {
        Log.d(TAG, "askPermission()");
        ActivityCompat.requestPermissions(
                getActivity(),
                new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                REQ_PERMISSION
        );
    }

    // Verify user's response of the permission requested
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult()");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch ( requestCode ) {
            case REQ_PERMISSION: {
                if ( grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                    // Permission granted
                    getLastKnownLocation();

                } else {
                    // Permission denied
                    permissionsDenied();
                }
                break;
            }
        }
    }

    // App cannot work without the permissions
    private void permissionsDenied() {
        Log.w(TAG, "permissionsDenied()");
    }

    // Create a Location Marker
    private void markerLocation(LatLng latLng) {
        Log.i(TAG, "markerLocation("+latLng+")");
        String title = latLng.latitude + ", " + latLng.longitude;
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(title);
        if ( mMap!=null ) {
            // Remove the anterior marker
            if ( locationMarker != null )
                locationMarker.remove();
            locationMarker = mMap.addMarker(markerOptions);
            float zoom = 14f;
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
            mMap.animateCamera(cameraUpdate);
        }
    }

    private void markerForGeofence(LatLng latLng) {
        Log.i(TAG, "markerForGeofence(" + latLng + ")");
        String title = latLng.latitude + ", " + latLng.longitude;
        // Define marker options
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .title(title);

        if (mMap != null) {
            if (geoFenceMarker == null)
                geoFenceMarker = mMap.addMarker(markerOptions);
        }
        startGeofence();
    }

    // Create a Geofence
    private Geofence createGeofence(LatLng latLng, float radius,String reqCode ) {
        Log.d(TAG, "createGeofence");

        return new Geofence.Builder()
                .setRequestId(reqCode)
                .setCircularRegion( latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration( GEO_DURATION )
                .setTransitionTypes( Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT )
                .build();
    }

    // Create a Geofence Request
    private GeofencingRequest createGeofenceRequest(Geofence geofence ) {
        Log.d(TAG, "createGeofenceRequest");
        return new GeofencingRequest.Builder()
                .setInitialTrigger( GeofencingRequest.INITIAL_TRIGGER_ENTER )
                .addGeofence( geofence )
                .build();
    }

    public PendingIntent createGeofencePendingIntent(int myRange) {
        Log.d(TAG, "createGeofencePendingIntent");

        if(myRange == 3) {
            final Intent intent3 = new Intent(getContext(), GeofenceTransitionService.class);
            intent3.putExtra("region", inString[2]);
            return geoFencePendingIntent3 = PendingIntent.getService(
                    getContext(), 1, intent3, PendingIntent.FLAG_UPDATE_CURRENT);

        }
        if(myRange == 1) {
            final Intent intent1 = new Intent(getContext(), GeofenceTransitionService.class);
            intent1.putExtra("region", inString[0]);
            return geoFencePendingIntent1 = PendingIntent.getService(
                    getContext(), 2, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

        }

        if(myRange == 2) {
            final Intent intent2 = new Intent(getContext(), GeofenceTransitionService.class);
            intent2.putExtra("region", inString[1]);
            return geoFencePendingIntent2 = PendingIntent.getService(
                    getContext(), 3, intent2, PendingIntent.FLAG_UPDATE_CURRENT);

        }
        return null;
    }

    // Add the created GeofenceRequest to the device's monitoring list
    private void addGeofence(GeofencingRequest request,int range) {
        Log.d(TAG, "addGeofence");
        if (checkPermission())
            LocationServices.GeofencingApi.addGeofences(
                    googleApiClient,
                    request,
                    createGeofencePendingIntent(range)
            );
    }

    @Override
    public void onResult(@NonNull Status status) {
        Log.i(TAG, "onResult: " + status);
        if ( status.isSuccess() ) {
            saveGeofence();
            drawGeofence();
        } else {
            // inform about fail
        }
    }

    private void drawGeofence() {
        Log.d(TAG, "drawGeofence()");

        if ( geoFenceLimits != null )
            geoFenceLimits.remove();
        CircleOptions circleOptions = new CircleOptions()
                .center( geoFenceMarker.getPosition())
                .strokeColor(Color.argb(50, 70,70,70))
                .fillColor( Color.argb(50,256,256,256) )
                .radius( geoRadius[0]);
        CircleOptions circleOptions2 = new CircleOptions()
                .center( geoFenceMarker.getPosition())
                .strokeColor(Color.argb(50, 70,70,70))
                .fillColor( Color.argb(100, 239,69,56) )
                .radius( geoRadius[1] );
        CircleOptions circleOptions3 = new CircleOptions()
                .center( geoFenceMarker.getPosition())
                .strokeColor(Color.argb(50, 70,70,70))
                .fillColor(Color.argb(150, 239,69,56) )
                .radius( geoRadius[2] );
        geoFenceLimits = mMap.addCircle( circleOptions );
        geoFenceLimits = mMap.addCircle( circleOptions2 );
        geoFenceLimits = mMap.addCircle( circleOptions3 );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(testLL, 5));
        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17), 1000, null);

    }

    // Start Geofence creation process
    private void startGeofence() {
        Log.i(TAG, "startGeofence()");
        if( geoFenceMarker != null ) {
            Geofence geofence = createGeofence( geoFenceMarker.getPosition(),100,"first geofence");
            GeofencingRequest geofenceRequest = createGeofenceRequest( geofence );
            addGeofence( geofenceRequest,1 );

            Geofence geofence2 = createGeofence( geoFenceMarker.getPosition(), 300,"second geofence");
            GeofencingRequest geofenceRequest2 = createGeofenceRequest( geofence2 );
            addGeofence( geofenceRequest2 ,2);

            Geofence geofence3 = createGeofence( geoFenceMarker.getPosition(), 500 ,"third geofence");
            GeofencingRequest geofenceRequest3 = createGeofenceRequest( geofence3 );
            addGeofence( geofenceRequest3 ,3);
        } else {
            Log.e(TAG, "Geofence marker is null");
        }
    }

    // Saving GeoFence marker with prefs mng
    private void saveGeofence() {
        Log.d(TAG, "saveGeofence()");
        SharedPreferences sharedPref = getActivity().getPreferences( Context.MODE_PRIVATE );
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putLong( KEY_GEOFENCE_LAT, Double.doubleToRawLongBits( geoFenceMarker.getPosition().latitude ));
        editor.putLong( KEY_GEOFENCE_LON, Double.doubleToRawLongBits( geoFenceMarker.getPosition().longitude ));
        editor.apply();
    }

    // Recovering last Geofence marker
    private void recoverGeofenceMarker() {
        Log.d(TAG, "recoverGeofenceMarker");
        SharedPreferences sharedPref = getActivity().getPreferences( Context.MODE_PRIVATE );

        if ( sharedPref.contains( KEY_GEOFENCE_LAT ) && sharedPref.contains( KEY_GEOFENCE_LON )) {
            double lat = Double.longBitsToDouble( sharedPref.getLong( KEY_GEOFENCE_LAT, -1 ));
            double lon = Double.longBitsToDouble( sharedPref.getLong( KEY_GEOFENCE_LON, -1 ));
            LatLng latLng = new LatLng( lat, lon );

        }
    }

    // Clear Geofence
    private void clearGeofence() {
        Log.d(TAG, "clearGeofence()");
        LocationServices.GeofencingApi.removeGeofences(
                googleApiClient,
                createGeofencePendingIntent(1)
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if ( status.isSuccess() ) {
                    // remove drawing
                    removeGeofenceDraw();
                }
            }
        });
    }

    private void removeGeofenceDraw() {
        Log.d(TAG, "removeGeofenceDraw()");
        if ( geoFenceMarker != null)
            geoFenceMarker.remove();
        if ( geoFenceLimits != null )
            geoFenceLimits.remove();
    }


    private String getAddressFromLatLng(LatLng latLng) {
        Geocoder geocoder = new Geocoder(getActivity());
        String address = "";
        try {
            address = geocoder
                    .getFromLocation(latLng.latitude, latLng.longitude, 1)
                    .get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return address;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Call GoogleApiClient connection when starting the Activity
        googleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        // Disconnect GoogleApiClient when stopping Activity
        googleApiClient.disconnect();
    }

    // GoogleApiClient.ConnectionCallbacks connected
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getLastKnownLocation();

        if(geoFenceLocationExists) {
            markerForGeofence(testLL);
            drawGeofence();
            recoverGeofenceMarker();
        }
    }


    // GoogleApiClient.ConnectionCallbacks suspended
    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG, "onConnectionSuspended()");
    }

    // GoogleApiClient.OnConnectionFailedListener fail
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed()");
    }

    public void getGeofenceLocation(){
        gEmail.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Profile profile = dataSnapshot.getValue(Profile.class);
                if(profile!=null) {
                    guardianEmail = profile.getGuardianEmail().replace(".", ",");
                }
                databaseReferenceGeo = FirebaseDatabase.getInstance().getReference().child("guardians").child("guardianEmails").child(guardianEmail);
                databaseReferenceGeo.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        LocationModel lModel = dataSnapshot.getValue(LocationModel.class);
                        if(lModel !=null){
                            lat = lModel.getLatitude();
                            lon = lModel.getLongitude();
                            testLL = new LatLng(lat,lon);
                            if(lat != 0.0 && lon != 0.0) {
                                geoFenceLocationExists = true;
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
}