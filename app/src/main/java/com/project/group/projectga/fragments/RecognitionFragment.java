package com.project.group.projectga.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.flurgle.camerakit.CameraListener;
import com.flurgle.camerakit.CameraView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.group.projectga.R;
import com.project.group.projectga.models.Recognition;

import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import static android.content.ContentValues.TAG;


public class RecognitionFragment extends Fragment  {

    Toolbar toolbar;
    CameraView cam;
    FloatingActionButton snap;
    Recognition recognizer = new Recognition();

    private ProgressBar spinner;
    private CardView loadingCard;
    private TextView loadingText;


    public RecognitionFragment(){

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        spinner = (ProgressBar) getView().findViewById(R.id.progressBar);
        loadingCard = (CardView) getView().findViewById(R.id.loadingCard);
        loadingText = (TextView) getView().findViewById(R.id.loadingText);

        spinner.isIndeterminate();

        cam = (CameraView) getActivity().findViewById(R.id.camera);

        snap = (FloatingActionButton) getActivity().findViewById(R.id.snapButton);

        cam.setCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(byte[] picture) {
                super.onPictureTaken(picture);

                // Create a bitmap
                Bitmap result = BitmapFactory.decodeByteArray(picture, 0, picture.length);

                loadingText.setText("Recognizing face...");

                recognizer.predict(result);

            }
        });

        loadingText.setText("Accessing Database...");

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d(TAG, "readPhotos: user id: " + uid);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("Important Peoples");
        Log.d(TAG, "readPhotos: database reference set: " + databaseReference.toString());


        recognizer.giveContext(getActivity(), this);
        //get the important people from firebase
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: start");
                if (dataSnapshot.exists()) {
                    Log.d(TAG, "onDataChange: datasnapshot obtained");

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String name = snapshot.child("name").getValue(String.class);
                        String picURL = snapshot.child("profile").getValue(String.class);

                        Log.d(TAG, "onDataChange: person name: " + name);

                        recognizer.addData(name, picURL);
                    }
                    recognizer.buildTrainingSet();
                }
                else {
                    Log.d(TAG, "onDataChange: datasnapshot does not exist...");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // do nothing for now
                Log.d(TAG, "onCancelled: database access error at readPhotos");
            }
        });
    }

    public void setCamButton() {
        getActivity().findViewById(R.id.snapButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingCard.setVisibility(View.VISIBLE);
                loadingText.setText("Saving Image...");
                cam.captureImage();
            }
        });
    }

    @Override
    public void onPause() {
        cam.stop();
        super.onPause();
    }

    @Override
    public void onResume() {

        cam.start();
        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recognition, container, false);
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // add the search icon to the toolbar
        menu.add(null).setIcon(R.drawable.ic_android_trans_24dp).setShowAsActionFlags(1);

        // implement search functionality here

        super.onCreateOptionsMenu(menu, inflater);
    }

}
