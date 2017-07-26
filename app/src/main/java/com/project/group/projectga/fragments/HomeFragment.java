package com.project.group.projectga.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.project.group.projectga.R;
import com.project.group.projectga.preferences.Preferences;

public class HomeFragment extends Fragment {
    Toolbar toolbar;

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

        initControls(view, userType);
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
                    Fragment mapsFragment = new MapsFragment();
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
}