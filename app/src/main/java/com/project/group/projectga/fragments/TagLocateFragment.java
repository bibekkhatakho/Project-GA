package com.project.group.projectga.fragments;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.project.group.projectga.adapters.Voice;
import com.project.group.projectga.R;

public class TagLocateFragment extends Fragment {

    ImageView tileImage, chipoloImage, duetImage, trackRImage;
    ImageView playTile, playChipolo, playDuet, playTrackR;

    TextView tileText, chipoloText, duetText, trackRText;
    TextView tileCompText, chipoloCompText, duetCompText, trackRCompText;
    TextView tileDescText, chipoloDescText, duetDescText, trackRDescText;

    Toolbar toolbar;

    String company;
    String product;
    String desc;

    Voice voice;

    public TagLocateFragment(){

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        voice  = new Voice(getActivity().getApplicationContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tag_locate, container, false);

        // set the background and recolor the menu icon for the toolbar
        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setBackground(getResources().getDrawable(R.drawable.tile_blue));
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_menu_blue_24dp));
        // change the icon on the toolbar
        ImageView icon = (ImageView) getActivity().findViewById(R.id.toolbarIcon);
        icon.setImageResource(R.drawable.ic_remove_red_eye_black_24dp);
        icon.setColorFilter(getResources().getColor(R.color.TagLocate));
        // change the title on the toolbar
        TextView title = (TextView) getActivity().findViewById(R.id.toolbarTitle);
        title.setText(R.string.locateLabel);
        title.setTextColor(getResources().getColor(R.color.textInputEditTextColor));

        tileImage = (ImageView) view.findViewById(R.id.tilesImage);
        chipoloImage = (ImageView) view.findViewById(R.id.chipoloImage);
        duetImage = (ImageView) view.findViewById(R.id.duetImage);
        trackRImage = (ImageView) view.findViewById(R.id.trackRImage);


        playTile = (ImageView) view.findViewById(R.id.playTiles);
        playChipolo = (ImageView) view.findViewById(R.id.playChipolo);
        playDuet = (ImageView) view.findViewById(R.id.playDuet);
        playTrackR = (ImageView) view.findViewById(R.id.playTrackR);

        tileText = (TextView) view.findViewById(R.id.tilesText);
        chipoloText = (TextView) view.findViewById(R.id.chipoloText);
        duetText = (TextView) view.findViewById(R.id.duetText);
        trackRText = (TextView) view.findViewById(R.id.trackRText);

        tileCompText = (TextView) view.findViewById(R.id.tilesCompText);
        chipoloCompText = (TextView) view.findViewById(R.id.chipoloCompText);
        duetCompText = (TextView) view.findViewById(R.id.duetCompText);
        trackRCompText = (TextView) view.findViewById(R.id.trackRCompText);

        tileDescText = (TextView) view.findViewById(R.id.tilesDescText);
        chipoloDescText = (TextView) view.findViewById(R.id.chipoloDescText);
        duetDescText = (TextView) view.findViewById(R.id.duetDescText);
        trackRDescText = (TextView) view.findViewById(R.id.trackRDescText);

        tileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.thetileapp.tile&hl=en")));
            }
        });

        chipoloImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=chipolo.net.v3&hl=en")));
            }
        });

        duetImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.innova.protag.ui&hl=en")));
            }
        });

        trackRImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.phonehalo.itemtracker&hl=en")));
            }
        });



        playTile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                company = tileCompText.getText().toString().trim();
                product = tileText.getText().toString().trim();
                desc = tileDescText.getText().toString().trim();
                voice.say(product);
                voice.playSilence();
                voice.say(company);
                voice.playSilence();
                voice.say(desc);
            }
        });


        playChipolo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                company = chipoloCompText.getText().toString().trim();
                product = chipoloText.getText().toString().trim();
                desc = chipoloDescText.getText().toString().trim();
                voice.say(product);
                voice.playSilence();
                voice.say(company);
                voice.playSilence();
                voice.say(desc);
            }
        });


        playDuet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                company = duetCompText.getText().toString().trim();
                product = duetText.getText().toString().trim();
                desc = duetDescText.getText().toString().trim();
                voice.say(product);
                voice.playSilence();
                voice.say(company);
                voice.playSilence();
                voice.say(desc);
            }
        });


        playTrackR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                company = trackRCompText.getText().toString().trim();
                product = trackRText.getText().toString().trim();
                desc = trackRDescText.getText().toString().trim();
                voice.say(product);
                voice.playSilence();
                voice.say(company);
                voice.playSilence();
                voice.say(desc);
            }
        });

        // enable the option menu
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // add blank icon to the toolbar for integrity of constraints
        menu.add(null).setIcon(R.drawable.ic_android_trans_24dp).setShowAsActionFlags(1);

        super.onCreateOptionsMenu(menu, inflater);
    }
}