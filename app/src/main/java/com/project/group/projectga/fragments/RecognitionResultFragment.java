package com.project.group.projectga.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.group.projectga.R;
import com.project.group.projectga.adapters.RecognitionResultAdapter;
import com.project.group.projectga.adapters.Voice;
import com.project.group.projectga.models.ImportantPeople;
import com.project.group.projectga.models.Recognition;
import com.project.group.projectga.preferences.Preferences;

import java.util.ArrayList;

import static com.mikepenz.iconics.Iconics.TAG;

public class RecognitionResultFragment extends Fragment {

    public static final double THRESHOLD = 100;

    Toolbar toolbar;
    ArrayList<ImportantPeople> importantPeoplesList;
    RecyclerView recyclerView;
    DatabaseReference databaseReference;
    Voice voice;

    public RecognitionResultFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recognition_result, container, false);

        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setBackground(getResources().getDrawable(R.drawable.tile_red));
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_menu_red_24dp));
        // change the icon on the toolbar
        ImageView icon = (ImageView) getActivity().findViewById(R.id.toolbarIcon);
        icon.setImageResource(R.drawable.ic_face_black_24dp);
        icon.setColorFilter(getResources().getColor(R.color.Recognition));
        // change the title on the toolbar
        TextView title = (TextView) getActivity().findViewById(R.id.toolbarTitle);
        title.setText(R.string.recognitionLabel);
        title.setTextColor(getResources().getColor(R.color.textInputEditTextColor));
        toolbar.setVisibility(View.VISIBLE);

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        ImageView img = (ImageView) getView().findViewById(R.id.testPhoto);
        img.setImageBitmap(Recognition.testImage);

//        TextView txt = (TextView) getView().findViewById(R.id.suggestionText);
//        txt.setText("Distance: " + Recognition.distance);
        Log.v(TAG, "onStart: recognition result: " + Recognition.result);
        Log.v(TAG, "onStart: recognition result distance: " + Recognition.distance);

        getView().findViewById(R.id.retakeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        if (Recognition.distance <= THRESHOLD) {
            getView().findViewById(R.id.noMatch).setVisibility(View.GONE);

            importantPeoplesList = new ArrayList<>();
            voice = new Voice(getActivity().getApplicationContext());

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            String userId = sharedPreferences.getString(Preferences.USERID, null);
            if (userId != null) {
                databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("Important Peoples");
            }
            recyclerView = (RecyclerView) getView().findViewById(R.id.recognition_people_recycler);
            recyclerView.setNestedScrollingEnabled(false);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
            recyclerView.setLayoutManager(mLayoutManager);
            final RecyclerView.Adapter mAdapter = new RecognitionResultAdapter(getContext(), importantPeoplesList, voice);
            databaseReference.orderByChild("name").equalTo(Recognition.result).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        ImportantPeople importantPeople = snapshot.getValue(ImportantPeople.class);
//                    String key=snapshot.getKey();
//                    importantPeople.setKey(key);
                        Log.d("Important People", "Invoked");
                        importantPeoplesList.add(importantPeople);
                    }
                    recyclerView.setAdapter(mAdapter);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // add the search icon to the toolbar
        menu.add(null).setIcon(R.drawable.ic_android_trans_24dp).setShowAsActionFlags(1);

        // implement search functionality here

        super.onCreateOptionsMenu(menu, inflater);
    }
}
