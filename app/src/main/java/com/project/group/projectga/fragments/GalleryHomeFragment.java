package com.project.group.projectga.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;

import com.project.group.projectga.R;

import mehdi.sakout.fancybuttons.FancyButton;

public class GalleryHomeFragment extends Fragment {

    Toolbar toolbar;
    MenuItem search;

    public GalleryHomeFragment(){

    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery_home, container, false);
        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        ImageView icon = (ImageView) getActivity().findViewById(R.id.toolbarIcon);
        icon.setImageResource(R.drawable.ic_perm_media_black_24dp);
        icon.setColorFilter(getResources().getColor(R.color.Gallery));
        TextView title = (TextView) getActivity().findViewById(R.id.toolbarTitle);
        title.setText(getString(R.string.galleryLabel));
        title.setTextColor(getResources().getColor(R.color.textInputEditTextColor));
        toolbar.setBackground(getResources().getDrawable(R.drawable.tile_green));
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_menu_green_24dp));

        setHasOptionsMenu(true);

        initControls(view);
        return view;
    }

    public void initControls(View view) {

        CardView galleryCardView = (CardView) view.findViewById(R.id.galleryCard);
        CardView importantPeopleCardView = (CardView) view.findViewById(R.id.peopleCard);

        galleryCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment galleryFragment = new GalleryFragment();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.container_gaFragments, galleryFragment);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        importantPeopleCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment recognitionFragment = new ImportantPeopleFragment();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.container_gaFragments, recognitionFragment);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.addToBackStack(null);
                ft.commit();
            }
        });

    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        search = menu.add("search").setIcon(R.drawable.ic_search_green_24dp).setShowAsActionFlags(1);

        super.onCreateOptionsMenu(menu, inflater);
    }
}