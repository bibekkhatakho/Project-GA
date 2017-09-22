package com.project.group.projectga.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.project.group.projectga.R;
import com.project.group.projectga.activities.ImportantPeoplesActivity;
import com.project.group.projectga.models.ImportantPeople;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class RecognitionResultAdapter extends RecyclerView.Adapter<RecognitionResultAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<ImportantPeople> mImportantPeoplesList;
    Voice mPlay;

    public RecognitionResultAdapter(Context context, ArrayList<ImportantPeople> importantPeoplesList, Voice play) {
        mContext = context;
        mImportantPeoplesList = importantPeoplesList;
        mPlay = play;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_recognition_result, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final ImportantPeople importantPeople = mImportantPeoplesList.get(position);

        holder.personName.setText(importantPeople.getName());
        holder.personRelation.setText(importantPeople.getRelation());
        holder.shortDescription.setText(importantPeople.getShortDescription());
        holder.longDescription.setText(importantPeople.getLongDescription());
        holder.play.setOnClickListener(new View.OnClickListener() {
            String shortPersonName  = importantPeople.getName().replaceAll("\\.","");
            String shortRelation  = importantPeople.getRelation().replaceAll("\\.","");
            String shortDescription  = importantPeople.getShortDescription().replaceAll("\\.","");
            String longString  = importantPeople.getLongDescription().replaceAll("\\.","               ");

            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[] {"Quick Memory?", "Detailed Memory?"};

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            mPlay.say(shortPersonName);
                            mPlay.playSilence();
                            mPlay.say(shortRelation);
                            mPlay.playSilence();
                            mPlay.say(shortDescription);
                        }
                        if (which == 1) {
                            mPlay.say(shortPersonName);
                            mPlay.playSilence();
                            mPlay.say(shortRelation);
                            mPlay.playSilence();
                            mPlay.say(shortDescription);
                            mPlay.playSilence();
                            mPlay.say(longString);
                        }
                    }
                });
                builder.show();
            }
        });
        Picasso.with(mContext).load(importantPeople.getProfile()).error(R.drawable.ic_error_outline_black_24dp).placeholder(R.drawable.ic_account_circle_white_24dp).into(holder.profilePicture);

    }


    @Override
    public int getItemCount() {
        if (mImportantPeoplesList == null) {
            return 0;
        } else {
            return mImportantPeoplesList.size();
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView personName, personRelation, shortDescription, longDescription;
        CircularImageView profilePicture;
        ImageView play;


        ViewHolder(View v) {
            super(v);
            profilePicture = (CircularImageView) v.findViewById(R.id.profilePicture);
            personName = (TextView) v.findViewById(R.id.personName);
            personRelation = (TextView) v.findViewById(R.id.personRelation);
            shortDescription = (TextView) v.findViewById(R.id.shortDescription);
            longDescription = (TextView) v.findViewById(R.id.longDescription);
            play = (ImageView)v.findViewById(R.id.playInfoView);
        }
    }
}