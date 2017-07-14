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
import android.view.View;
import android.view.ViewGroup;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.group.projectga.R;
import com.project.group.projectga.activities.RecognitionActivity;
import com.project.group.projectga.adapters.RecognitionAdapter;
import com.project.group.projectga.models.Recognition;
import com.project.group.projectga.preferences.Preferences;

import java.util.ArrayList;

/**
 * Created by ramjiseetharaman on 7/13/17.
 */

public class RecognitionFragment extends Fragment  {

    RecyclerView recyclerView;
    Toolbar toolbar;
    FloatingActionButton addButton;
    DatabaseReference databaseReference;
    ArrayList<Recognition> recognitionsList;

    public RecognitionFragment(){

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recognition, container, false);
        recognitionsList = new ArrayList<>();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String userId = sharedPreferences.getString(Preferences.USERID,null);
        if (userId != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("personsList");
        }
        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.recognitionLabel));
        toolbar.setVisibility(View.VISIBLE);
        addButton = (FloatingActionButton) view.findViewById(R.id.addButton);
        recyclerView = (RecyclerView) view.findViewById(R.id.people_recycler);
        recyclerView.setNestedScrollingEnabled(false);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
//        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getContext()));
        final RecyclerView.Adapter mAdapter = new RecognitionAdapter(getContext(),recognitionsList);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Recognition recognition = snapshot.getValue(Recognition.class);
                    String key=snapshot.getKey();
                    recognition.setKey(key);
                    Log.d("Recognition", "Invoked");
                    recognitionsList.add(recognition);
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
                Intent intent = new Intent(getContext(), RecognitionActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }


}