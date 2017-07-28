package com.project.group.projectga.helpers;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.project.group.projectga.R;
import com.project.group.projectga.models.CustomIcons;

import java.util.ArrayList;

/**
 * Created by ramjiseetharaman on 7/27/17.
 */

public class MapsCustomIconSpinnerAdapter extends ArrayAdapter<CustomIcons> {

    int groupid;
    Activity context;
    ArrayList<CustomIcons> list;
    LayoutInflater inflater;

    public MapsCustomIconSpinnerAdapter(Activity context, int groupid, ArrayList<CustomIcons> list){
        super(context,groupid,list);
        this.list=list;
        inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.groupid=groupid;
    }

    public View getView(int position, View convertView, ViewGroup parent ){
        View itemView=inflater.inflate(groupid,parent,false);
        ImageView imageView=(ImageView)itemView.findViewById(R.id.img);
        imageView.setImageResource(list.get(position).getImageId());
        return itemView;
    }


    public View getDropDownView(int position, View convertView, ViewGroup
            parent){
        return getView(position,convertView,parent);

    }
}


