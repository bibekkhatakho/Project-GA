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

public class GamesPuzzlesFragment extends Fragment {

    ImageView gardenImage, pondImage, sudokuImage, wordSearchImage, quizImage,
            radioImage, luminosityImage, labyrinthImage, pianoImage, paintImage;

    ImageView playGarden, playPond, playSudoku, playWordSearch, playQuiz,
            playRadio, playLuminosity, playLabyrinth, playPiano, playPaint;

    TextView gardenText, pondText, sudokuText, wordSearchText, quizText,
            radioText, luminosityText, labyrinthText, pianoText, paintText;

    TextView gardenCompText, pondCompText, sudokuCompText, wordSearchCompText, quizCompText,
            radioCompText, luminosityCompText, labyrinthCompText, pianoCompText, paintCompText;

    TextView gardenDescText, pondDescText, sudokuDescText, wordSearchDescText, quizDescText,
            radioDescText, luminosityDescText, labyrinthDescText, pianoDescText, paintDescText;

    Toolbar toolbar;

    String desc;

    Voice voice;

    public GamesPuzzlesFragment(){

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        voice  = new Voice(getActivity().getApplicationContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_games_puzzles, container, false);

        // set the background and recolor the menu icon for the toolbar
        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setBackground(getResources().getDrawable(R.drawable.tile_purple));
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_menu_purple_24dp));
        // change the icon on the toolbar
        ImageView icon = (ImageView) getActivity().findViewById(R.id.toolbarIcon);
        icon.setImageResource(R.drawable.ic_casino_black_24dp);
        icon.setColorFilter(getResources().getColor(R.color.GamesPuzzles));
        // change the title on the toolbar
        TextView title = (TextView) getActivity().findViewById(R.id.toolbarTitle);
        title.setText(R.string.gamesLabel);
        title.setTextColor(getResources().getColor(R.color.textInputEditTextColor));

        pondImage = (ImageView) view.findViewById(R.id.pondImage);
        sudokuImage = (ImageView) view.findViewById(R.id.sudokuImage);
        wordSearchImage = (ImageView) view.findViewById(R.id.wordSearchImage);
        quizImage = (ImageView) view.findViewById(R.id.quizImage);
        radioImage = (ImageView) view.findViewById(R.id.radioImage);
        labyrinthImage = (ImageView) view.findViewById(R.id.labyrinthImage);
        gardenImage = (ImageView) view.findViewById(R.id.gardenImage);
        luminosityImage = (ImageView) view.findViewById(R.id.luminosityImage);
        pianoImage = (ImageView) view.findViewById(R.id.pianoImage);
        paintImage = (ImageView) view.findViewById(R.id.paintImage);

        playPond = (ImageView) view.findViewById(R.id.playPond);
        playSudoku = (ImageView) view.findViewById(R.id.playSudoku);
        playWordSearch = (ImageView) view.findViewById(R.id.playWordSearch);
        playQuiz = (ImageView) view.findViewById(R.id.playQuiz);
        playRadio = (ImageView) view.findViewById(R.id.playRadio);
        playLabyrinth = (ImageView) view.findViewById(R.id.playLabyrinth);
        playGarden = (ImageView) view.findViewById(R.id.playGarden);
        playLuminosity = (ImageView) view.findViewById(R.id.playLuminosity);
        playPiano = (ImageView) view.findViewById(R.id.playPiano);
        playPaint = (ImageView) view.findViewById(R.id.playPaint);

        pondText = (TextView) view.findViewById(R.id.pondText);
        sudokuText = (TextView) view.findViewById(R.id.sudokuText);
        wordSearchText = (TextView) view.findViewById(R.id.wordSearchText);
        quizText = (TextView) view.findViewById(R.id.quizText);
        radioText = (TextView) view.findViewById(R.id.radioText);
        labyrinthText = (TextView) view.findViewById(R.id.labyrinthText);
        gardenText = (TextView) view.findViewById(R.id.gardenText);
        luminosityText = (TextView) view.findViewById(R.id.luminosityText);
        pianoText = (TextView) view.findViewById(R.id.pianoText);
        paintText = (TextView) view.findViewById(R.id.paintText);

        pondCompText = (TextView) view.findViewById(R.id.pondCompText);
        sudokuCompText = (TextView) view.findViewById(R.id.sudokuCompText);
        wordSearchCompText = (TextView) view.findViewById(R.id.wordSearchCompText);
        quizCompText = (TextView) view.findViewById(R.id.quizCompText);
        radioCompText = (TextView) view.findViewById(R.id.radioCompText);
        labyrinthCompText = (TextView) view.findViewById(R.id.labyrinthCompText);
        gardenCompText = (TextView) view.findViewById(R.id.gardenCompText);
        luminosityCompText = (TextView) view.findViewById(R.id.luminosityCompText);
        pianoCompText = (TextView) view.findViewById(R.id.pianoCompText);
        paintCompText = (TextView) view.findViewById(R.id.paintCompText);

        pondDescText = (TextView) view.findViewById(R.id.pondDescText);
        sudokuDescText = (TextView) view.findViewById(R.id.sudokuDescText);
        wordSearchDescText = (TextView) view.findViewById(R.id.wordSearchDescText);
        quizDescText = (TextView) view.findViewById(R.id.quizDescText);
        radioDescText = (TextView) view.findViewById(R.id.radioDescText);
        labyrinthDescText = (TextView) view.findViewById(R.id.labyrinthDescText);
        gardenDescText = (TextView) view.findViewById(R.id.gardenDescText);
        luminosityDescText = (TextView) view.findViewById(R.id.luminosityDescText);
        pianoDescText = (TextView) view.findViewById(R.id.pianoDescText);
        paintDescText = (TextView) view.findViewById(R.id.paintDescText);

        pondImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.johnmoff.PocketPond2&hl=en")));
            }
        });

        sudokuImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.zerologicgames.minimalsudoku&hl=en")));
            }
        });

        wordSearchImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.wordloco.wordchallenge&hl=en")));
            }
        });

        quizImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=lemmingsatwork.quiz&hl=en")));
            }
        });

        radioImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.andromo.dev137436.app216769&hl=en")));
            }
        });

        labyrinthImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=se.illusionlabs.labyrinth.lite&hl=en")));
            }
        });

        gardenImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.fairyenginellc.flowergarden&hl=en")));
            }
        });

        luminosityImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.lumoslabs.lumosity&hl=en")));
            }
        });

        pianoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.smule.magicpiano&hl=en")));
            }
        });

        paintImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=meritum.app15&hl=en")));
            }
        });

        playLuminosity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                desc = luminosityDescText.getText().toString().trim();
                voice.say(desc);
            }
        });

        playPond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                desc = pondDescText.getText().toString().trim();
                voice.say(desc);
            }
        });

        playPaint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                desc = paintDescText.getText().toString().trim();
                voice.say(desc);
            }
        });

        playWordSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                desc = wordSearchDescText.getText().toString().trim();
                voice.say(desc);
            }
        });

        playQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                desc = quizDescText.getText().toString().trim();
                voice.say(desc);
            }
        });

        playSudoku.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                desc = sudokuDescText.getText().toString().trim();
                voice.say(desc);
            }
        });

        playRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                desc = radioDescText.getText().toString().trim();
                voice.say(desc);
            }
        });

        playLabyrinth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                desc = labyrinthDescText.getText().toString().trim();
                voice.say(desc);
            }
        });

        playGarden.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                desc = gardenDescText.getText().toString().trim();
                voice.say(desc);
            }
        });

        playPiano.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                desc = pianoDescText.getText().toString().trim();
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