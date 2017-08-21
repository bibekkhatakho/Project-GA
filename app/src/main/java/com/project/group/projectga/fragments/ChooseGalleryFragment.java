package com.project.group.projectga.fragments;

import android.os.Bundle;
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

public class ChooseGalleryFragment extends Fragment {

    Toolbar toolbar;

    public ChooseGalleryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

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

        // Inflate the layout for this fragment and retain view
        View view = inflater.inflate(R.layout.fragment_choose_gallery, container, false);

        // find cardviews and set listeners
        CardView galleryCard = (CardView) view.findViewById(R.id.galleryCard);
        CardView peopleCard = (CardView) view.findViewById(R.id.peopleCard);
        galleryCard.setOnClickListener(new View.OnClickListener() {
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
        peopleCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment peopleFragment = new PeopleFragment();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.container_gaFragments, peopleFragment);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(null).setIcon(R.drawable.ic_android_trans_24dp).setShowAsActionFlags(1);

        super.onCreateOptionsMenu(menu, inflater);
    }
}
