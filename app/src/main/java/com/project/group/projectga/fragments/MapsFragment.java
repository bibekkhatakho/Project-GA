package com.project.group.projectga.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.project.group.projectga.R;

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    Toolbar toolbar;
    private GoogleMap mMap;

    public static MapsFragment newInstance(){
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

        // enable the option menu
        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }




    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // add blank icon to the toolbar for integrity of constraints
        menu.add(null).setIcon(R.drawable.ic_android_trans_24dp).setShowAsActionFlags(1);


        super.onCreateOptionsMenu(menu, inflater);
    }
}