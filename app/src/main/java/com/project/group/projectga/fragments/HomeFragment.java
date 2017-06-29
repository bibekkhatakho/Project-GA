package com.project.group.projectga.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.project.group.projectga.R;

public class HomeFragment extends Fragment {
    Toolbar toolbar;

    public HomeFragment(){

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.homeScreen));
        initControls(view);
        return view;
    }


    public void initControls(View view){

        CardView galleryCard = (CardView) view.findViewById(R.id.galleryCard);
        CardView mapsCard = (CardView) view.findViewById(R.id.mapCard);
        CardView recognitionCard = (CardView) view.findViewById(R.id.recognitionCard);
        CardView tagAndLocateCard = (CardView) view.findViewById(R.id.locateCard);
        CardView gamesAndPuzzlesCard = (CardView) view.findViewById(R.id.gamesCard);
        CardView backupCard = (CardView) view.findViewById(R.id.backupCard);

        galleryCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment galleryFragment = new GalleryFragment();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.container_gaFragments, galleryFragment);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

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
        backupCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment backupFragment = new BackupFragment();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.container_gaFragments, backupFragment);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.addToBackStack(null);
                ft.commit();
            }
        });
    }
}