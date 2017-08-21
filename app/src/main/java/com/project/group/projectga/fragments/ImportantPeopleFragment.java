package com.project.group.projectga.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.group.projectga.R;
import com.project.group.projectga.activities.ImportantPeoplesActivity;
import com.project.group.projectga.adapters.ImportantPeopleAdapter;
import com.project.group.projectga.adapters.Voice;
import com.project.group.projectga.models.ImportantPeople;
import com.project.group.projectga.preferences.Preferences;

import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by ramjiseetharaman on 7/13/17.
 */

public class ImportantPeopleFragment extends Fragment  {

    RecyclerView recyclerView;
    Toolbar toolbar;
    FloatingActionButton addButton;
    DatabaseReference databaseReference;
    ArrayList<ImportantPeople> importantPeoplesList;
	MenuItem search;
    Voice voice;

    public ImportantPeopleFragment(){

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_important_people, container, false);
        importantPeoplesList = new ArrayList<>();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String userId = sharedPreferences.getString(Preferences.USERID,null);
        if (userId != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("Important Peoples");
        }
        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
		toolbar.setBackground(getResources().getDrawable(R.drawable.tile_green));
		toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_menu_green_24dp));
        // change the icon on the toolbar
        ImageView icon = (ImageView) getActivity().findViewById(R.id.toolbarIcon);
        icon.setImageResource(R.drawable.ic_group_add_black_24dp);
        icon.setColorFilter(getResources().getColor(R.color.Gallery));
        // change the title on the toolbar
        TextView title = (TextView) getActivity().findViewById(R.id.toolbarTitle);
        title.setText(R.string.importantPeople);
        title.setTextColor(getResources().getColor(R.color.textInputEditTextColor));
        toolbar.setVisibility(View.VISIBLE);

        voice = new Voice(getActivity().getApplicationContext());

        addButton = (FloatingActionButton) view.findViewById(R.id.addButton);
        recyclerView = (RecyclerView) view.findViewById(R.id.people_recycler);
        recyclerView.setNestedScrollingEnabled(false);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
//        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getContext()));
        final RecyclerView.Adapter mAdapter = new ImportantPeopleAdapter(getContext(),importantPeoplesList, voice);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ImportantPeople importantPeople = snapshot.getValue(ImportantPeople.class);
                    String key=snapshot.getKey();
                    importantPeople.setKey(key);
                    Log.d("Important People", "Invoked");
                    importantPeoplesList.add(importantPeople);
                }
                recyclerView.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        registerForContextMenu(recyclerView);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ImportantPeoplesActivity.class);
                startActivity(intent);
            }
        });
		
		setHasOptionsMenu(true);
        return view;
    }
	
	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // add the search icon to the toolbar
        search = menu.add("search").setIcon(R.drawable.ic_search_green_24dp).setShowAsActionFlags(1);

        // implement search functionality here

        super.onCreateOptionsMenu(menu, inflater);
    }


}