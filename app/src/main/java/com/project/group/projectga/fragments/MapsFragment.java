package com.project.group.projectga.fragments;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
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
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
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
import com.project.group.projectga.activities.MapMarkerActivity;
import com.project.group.projectga.models.GeolocationModel;
import com.project.group.projectga.models.MapMarkers;
import com.project.group.projectga.R;
import com.project.group.projectga.service.GeofenceTransitionService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsFragment extends Fragment  implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,LocationListener, OnMapReadyCallback,ResultCallback<Status> {

    Toolbar toolbar;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
 	private Context context;
    private Marker geoFenceMarker;
    ArrayList<MapMarkers> mapMarkerList;
    private Marker locationMarker;
    private String[] markerIcons = {
            "home",
            "neighborhood",
            "work",
            "firestation",
            "hospital",
            "police",
            "pharmacy",
            "airport",
            "gas",
            "location",
            "bank"
    };
    private final int REQ_PERMISSION = 999;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private TextView textLat, textLong;
	
	String inString[] = new String[]{"a","b","c"};
	int count = 0;

    private final int UPDATE_INTERVAL =  3 * 60 * 1000; // 3 minutes
    private final int FASTEST_INTERVAL = 30 * 1000;  // 30 secs

    private static final LatLng testLatlang = new LatLng(32.7314336,-97.1113121);


     private static final long GEO_DURATION = Geofence.NEVER_EXPIRE;
    private static final String GEOFENCE_REQ_ID = "My Geofence";
    double geoRadius[] = new double[]{5,10,15};
    private final int GEOFENCE_REQ_CODE = 0;
	private List<Geofence> mGeoList;

    private double currentLatitude;
    private double currentLongitude;


    private PendingIntent geoFencePendingIntent1;
    private PendingIntent geoFencePendingIntent2;
    private PendingIntent geoFencePendingIntent3;

    private final String KEY_GEOFENCE_LAT = "GEOFENCE LATITUDE";
    private final String KEY_GEOFENCE_LON = "GEOFENCE LONGITUDE";

	public static final int SEND_SMS = 101;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    DatabaseReference databaseReferenceGeo;

    String userId;
    private Circle geoFenceLimits;

    private static final String TAG = MapsFragment.class.getSimpleName();

    public static MapsFragment newInstance() {
        MapsFragment mapsFragment = new MapsFragment();
        return mapsFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, null, false);
		initGMaps();
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        createGoogleApi();
		if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.SEND_SMS},SEND_SMS);

            Toast.makeText(getContext(), "SMS not Sent!",
                    Toast.LENGTH_LONG).show();
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

        textLat = (TextView) view.findViewById(R.id.lat);
        textLong = (TextView) view.findViewById(R.id.lon);

        firebaseAuth = FirebaseAuth.getInstance();

        if (getUid() != null) {
            userId = getUid();
            firebaseAuth = FirebaseAuth.getInstance();
			databaseReferenceGeo = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("Geofence Location");
            databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("Map Markers");
        } else {
        }

        databaseReferenceGeo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GeolocationModel geolocationModel = dataSnapshot.getValue(GeolocationModel.class);
                if(geolocationModel !=null){
                    currentLatitude = geolocationModel.getLatitude();
                    currentLongitude = geolocationModel.getLongitude();
                }
               // Toast.makeText(getContext(), "The latitude from Guardiann" + currentLatitude, Toast.LENGTH_SHORT).show();
                //Toast.makeText(getContext(), "The longitude from Guardiann" + currentLongitude, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        // enable the option menu



        return view;
    }

    private void createGoogleApi() {
        if ( mGoogleApiClient == null ) {
            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
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
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
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

        /*
        LatLng myLocation = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
        CameraUpdateFactory.zoomTo(15.0f);
        */
        //Not able to get current location yet

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
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
                    String iconNumber = m.getMarkerIcon();
                    Integer iconNum = Integer.parseInt(iconNumber);
                    int resID = getResources().getIdentifier("com.project.group.projectga:drawable/" + markerIcons[iconNum], null, null);
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

    // Get last known location
    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation()");
        if ( checkPermission() ) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if ( mLastLocation != null ) {
                Log.i(TAG, "LasKnown location. " +
                        "Long: " + mLastLocation.getLongitude() +
                        " | Lat: " + mLastLocation.getLatitude());
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
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        if ( checkPermission() )
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged ["+location+"]");
        mLastLocation = location;
        writeActualLocation(location);
    }

    // Write location coordinates on UI
    private void writeActualLocation(Location location) {
        textLat.setText( "Lat: " + location.getLatitude() );
        textLong.setText( "Long: " + location.getLongitude() );

        markerLocation(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    private void writeLastLocation() {
        writeActualLocation(mLastLocation);
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
        //Toast.makeText(getContext(),"geo created",Toast.LENGTH_SHORT).show();
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .title(title);




        // startGeofence();

        if (mMap != null) {
            // Remove last geoFenceMarker
            if (geoFenceMarker != null)
               // geoFenceMarker.remove();
            Toast.makeText(this.getContext(), "checking marker", Toast.LENGTH_SHORT).show();
            geoFenceMarker = mMap.addMarker(markerOptions);
        }
        startGeofence();
    }

    // Create a Geofence
    private Geofence createGeofence(LatLng latLng, float radius ) {
        Log.d(TAG, "createGeofence");
        //Toast.makeText(getContext(),"geo building",Toast.LENGTH_SHORT).show();

        return new Geofence.Builder()
                .setRequestId(GEOFENCE_REQ_ID)
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
            Toast.makeText(getContext(), "creating intent 3", Toast.LENGTH_SHORT).show();
            Intent intent3 = new Intent(getContext(), GeofenceTransitionService.class);
            intent3.putExtra("region", inString[2]);
            return geoFencePendingIntent3 = PendingIntent.getService(
                    getContext(), 1, intent3, PendingIntent.FLAG_UPDATE_CURRENT);

        }
        if(myRange == 1) {
            Toast.makeText(getContext(), "creating intent 1", Toast.LENGTH_SHORT).show();
            Intent intent1 = new Intent(getContext(), GeofenceTransitionService.class);
            intent1.putExtra("region", inString[0]);
            return geoFencePendingIntent1 = PendingIntent.getService(
                    getContext(), 2, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

        }

        if(myRange == 2) {
            Toast.makeText(getContext(), "creating intent 2", Toast.LENGTH_SHORT).show();
            Intent intent2 = new Intent(getContext(), GeofenceTransitionService.class);
            intent2.putExtra("region", inString[1]);
            return geoFencePendingIntent2 = PendingIntent.getService(
                    getContext(), 3, intent2, PendingIntent.FLAG_UPDATE_CURRENT);

        }
       // Toast.makeText(getContext(), "leaving my geofence", Toast.LENGTH_SHORT).show();
return null;
    }

    // Add the created GeofenceRequest to the device's monitoring list
    private void addGeofence(GeofencingRequest request,int range) {
        Log.d(TAG, "addGeofence");
        Toast.makeText(this.getContext(), "adding geo" + range, Toast.LENGTH_SHORT).show();
        if (checkPermission())
            Toast.makeText(this.getContext(), "checing permision", Toast.LENGTH_SHORT).show();
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    request,
                    createGeofencePendingIntent(range)
            );
       // Toast.makeText(this.getContext(), "adding geo", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResult(@NonNull Status status) {
        //Toast.makeText(getContext(),"inside onresult",Toast.LENGTH_SHORT).show();
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
       // Toast.makeText(this.getContext(), "drawing geo", Toast.LENGTH_SHORT).show();
        CircleOptions circleOptions = new CircleOptions()
                .center( geoFenceMarker.getPosition())
                .strokeColor(Color.argb(50, 70,70,70))
                .fillColor( Color.argb(200,00,150,00) )
                .radius( geoRadius[0]);
        CircleOptions circleOptions2 = new CircleOptions()
                .center( geoFenceMarker.getPosition())
                .strokeColor(Color.argb(50, 70,70,70))
                .fillColor( Color.argb(50, 100, 00, 00) )
                .radius( geoRadius[1] );
        CircleOptions circleOptions3 = new CircleOptions()
                .center( geoFenceMarker.getPosition())
                .strokeColor(Color.argb(50, 70,70,70))
                .fillColor(Color.argb(50, 255, 0, 0) )
                .radius( geoRadius[2] );
        geoFenceLimits = mMap.addCircle( circleOptions );
        geoFenceLimits = mMap.addCircle( circleOptions2 );
        geoFenceLimits = mMap.addCircle( circleOptions3 );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(testLatlang, 5));
        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17), 1000, null);
    }

    // Start Geofence creation process
    private void startGeofence() {
        Log.i(TAG, "startGeofence()");
      //  Toast.makeText(this.getContext(), "start geofence" + geoFenceMarker.getPosition().toString(), Toast.LENGTH_SHORT).show();
        if( geoFenceMarker != null ) {
            Geofence geofence = createGeofence( geoFenceMarker.getPosition(),5);
            GeofencingRequest geofenceRequest = createGeofenceRequest( geofence );
            addGeofence( geofenceRequest,1 );

            Geofence geofence2 = createGeofence( geoFenceMarker.getPosition(), 10);
            GeofencingRequest geofenceRequest2 = createGeofenceRequest( geofence2 );
            addGeofence( geofenceRequest2 ,2);

            Geofence geofence3 = createGeofence( geoFenceMarker.getPosition(), 15 );
            GeofencingRequest geofenceRequest3 = createGeofenceRequest( geofence3 );
            addGeofence( geofenceRequest3 ,3);
        } else {
            Log.e(TAG, "Geofence marker is null");
            Toast.makeText(this.getContext(), "marker is null", Toast.LENGTH_SHORT).show();
        }
    }

    // Saving GeoFence marker with prefs mng
    private void saveGeofence() {
        //Toast.makeText(getContext(),"saving",Toast.LENGTH_SHORT).show();
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
       // Toast.makeText(getContext(),"recovering",Toast.LENGTH_SHORT).show();
        SharedPreferences sharedPref = getActivity().getPreferences( Context.MODE_PRIVATE );

        if ( sharedPref.contains( KEY_GEOFENCE_LAT ) && sharedPref.contains( KEY_GEOFENCE_LON )) {
            double lat = Double.longBitsToDouble( sharedPref.getLong( KEY_GEOFENCE_LAT, -1 ));
            double lon = Double.longBitsToDouble( sharedPref.getLong( KEY_GEOFENCE_LON, -1 ));
            LatLng latLng = new LatLng( lat, lon );
          //  markerForGeofence(testLL);
          //  drawGeofence();
           // markerForGeofence(testLL2);

        }
    }

    // Clear Geofence
    private void clearGeofence() {
        Log.d(TAG, "clearGeofence()");
        LocationServices.GeofencingApi.removeGeofences(
                mGoogleApiClient,
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



//
//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        // add blank icon to the toolbar for integrity of constraints
//        menu.add(null).setIcon(R.drawable.ic_android_trans_24dp).setShowAsActionFlags(1);
//
//
//        super.onCreateOptionsMenu(menu, inflater);
//    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
    
            mGoogleApiClient.disconnect();
        
    }

    private String getAddressFromLatLng(LatLng latLng) {
//        Toast.makeText(getContext(), "Inside getAddress", Toast.LENGTH_SHORT).show();
        Geocoder geocoder = new Geocoder(getActivity());
//        StringBuilder strReturnedAddress=null;
//        try {
//            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
//            if(addresses != null){
//                Address returnedAddress = addresses.get(0);
//                Toast.makeText(getContext(), "Returned address " + returnedAddress, Toast.LENGTH_SHORT).show();
//                strReturnedAddress = new StringBuilder();
//                    for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
//                        strReturnedAddress.append(returnedAddress.getAddressLine(i)).append(",");
//                    }
//                }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
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
    public void onConnected(@Nullable Bundle bundle) {
       //Log.i(TAG, "onConnected()");
        getLastKnownLocation();
        markerForGeofence(testLatlang);
        drawGeofence();
        recoverGeofenceMarker();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate( R.menu.main_menu, menu );

    }
	
	public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch ( item.getItemId() ) {
            case R.id.geofence: {
                startGeofence();
                return true;
            }
            case R.id.clear: {
                clearGeofence();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

}