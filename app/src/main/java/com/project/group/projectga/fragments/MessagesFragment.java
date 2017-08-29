package com.project.group.projectga.fragments;

import android.Manifest;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.group.projectga.R;
import com.project.group.projectga.models.LocationModel;
import com.project.group.projectga.models.Profile;
import com.project.group.projectga.preferences.Preferences;

import java.util.ArrayList;

public class MessagesFragment extends Fragment
{
    private static MessagesFragment inst;
    static boolean active = false;
    ArrayList<String> smsMessagesList = new ArrayList<>();
    ListView messages;
    ArrayAdapter arrayAdapter;
    EditText input;
    Button buttonSend;
    SmsManager smsManager = SmsManager.getDefault();
    String str,smsBody,smsAddress;
    String[] columns = new String[] { "address", "person", "date", "body","type" };
    private Context context;

    String userId;
    String number, numberPlus;
    String guardianEmail;
    String guardianName, standardName;

    DatabaseReference databaseReference;
    DatabaseReference databaseReferenceGuardian;

    Toolbar toolbar;

    private static final int READ_SMS_PERMISSIONS_REQUEST = 142;

    public static MessagesFragment instance()
    {
        return inst;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        active = true;
        inst = this;
    }

    public void updateInbox(final String smsMessage)
    {
        arrayAdapter.insert(smsMessage, 0);
        arrayAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);
        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        ImageView icon = (ImageView) getActivity().findViewById(R.id.toolbarIcon);
        icon.setImageResource(R.drawable.ic_perm_media_black_24dp);
        icon.setColorFilter(getResources().getColor(R.color.Recognition));
        TextView title = (TextView) getActivity().findViewById(R.id.toolbarTitle);
        title.setText(getString(R.string.messages_label));
        title.setTextColor(getResources().getColor(R.color.textInputEditTextColor));
        toolbar.setBackground(getResources().getDrawable(R.drawable.tile_red));

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        final String userType = preferences.getString(Preferences.USER_TYPE, "");
        String userIdPref = preferences.getString(Preferences.USERID, "");

        context = this.getContext();
        messages = (ListView) view.findViewById(R.id.messages);
        input = (EditText) view.findViewById(R.id.input);
        buttonSend = (Button) view.findViewById(R.id.buttonSend);

        getActivity().findViewById(R.id.messageActionButton).setVisibility(View.GONE);

        arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, smsMessagesList);
        messages.setAdapter(arrayAdapter);

        if (userIdPref != null) {
            userId = userIdPref;
            databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
            databaseReferenceGuardian = FirebaseDatabase.getInstance().getReference().child("guardians").child("guardianEmails");
        }

        if (userType.equalsIgnoreCase("Standard User")) {
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Profile profile = dataSnapshot.getValue(Profile.class);
                    if(dataSnapshot.exists()) {
                        if(profile !=null) {
                            guardianEmail = profile.getGuardianEmail().replace(".", ",");
                        }
                        databaseReferenceGuardian = databaseReferenceGuardian.child(guardianEmail);

                        databaseReferenceGuardian.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                LocationModel locationModel = dataSnapshot.getValue(LocationModel.class);
                                if (dataSnapshot.exists()) {
                                    if (locationModel != null && !locationModel.toString().isEmpty() && number != null && !number.isEmpty()) {
                                        number = locationModel.getGuardianNumber();
                                        guardianName = locationModel.getGuardianName();
                                        number = number.replaceAll("[^0-9]", "");
                                        numberPlus = "+1" + number;

                                        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                                            getPermissionToReadSMS();
                                        } else {
                                            refreshStandardSmsInbox();
                                        }
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        if (userType.equalsIgnoreCase("Guardian User")) {
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Profile profile = dataSnapshot.getValue(Profile.class);
                    if(dataSnapshot.exists()) {
                        if(profile != null) {
                            guardianEmail = profile.getEmail().replace(".", ",");
                        }
                        databaseReferenceGuardian = databaseReferenceGuardian.child(guardianEmail);

                        databaseReferenceGuardian.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                LocationModel locationModel = dataSnapshot.getValue(LocationModel.class);
                                if (dataSnapshot.exists()) {
                                    if (locationModel != null && !locationModel.toString().isEmpty() && number != null && !number.isEmpty()) {
                                        number = locationModel.getPatientNumber();
                                        standardName = locationModel.getPatientName();
                                        number = number.replaceAll("[^0-9]", "");
                                        numberPlus = "+1" + number;

                                        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                                            getPermissionToReadSMS();
                                        } else {
                                            refreshGuardianSmsInbox();
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        buttonSend.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
                {
                    getPermissionToReadSMS();
                }
                else
                {
                    smsBody = input.getText().toString();
                    smsAddress = number;
                    smsManager.sendTextMessage(smsAddress, null, smsBody, null, null);

                    //Toast.makeText(context, "Message sent!", Toast.LENGTH_SHORT).show();
                    String message = "SMS To: " + number +
                            "\n" + smsBody + "\n";
                    updateInbox(message);

                    //Clear message box for future message
                    input.setText("");

                    MessagesFragment inst = MessagesFragment.instance();
                }
            }
        });

        if (ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED)
        {
            getPermissionToReadSMS();
        }
        else
        {
            if(userType.equalsIgnoreCase("Standard User")) {
                refreshStandardSmsInbox();
            }
            if(userType.equalsIgnoreCase("Guardian User")) {
                refreshGuardianSmsInbox();
            }
        }

        return view;
    }

    public void getPermissionToReadSMS()
    {
        if (ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this.getActivity(),Manifest.permission.READ_SMS))
            {
                Toast.makeText(this.getContext(), "Please allow permission!", Toast.LENGTH_SHORT).show();
            }
            ActivityCompat.requestPermissions(this.getActivity(),new String[]{Manifest.permission.READ_SMS},
                    READ_SMS_PERMISSIONS_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],int[] grantResults)
    {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        final String userType = preferences.getString(Preferences.USER_TYPE, "");
        String userIdPref = preferences.getString(Preferences.USERID, "");
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == READ_SMS_PERMISSIONS_REQUEST)
        {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this.getContext(), "Read SMS permission granted", Toast.LENGTH_SHORT).show();
                if(userType.equalsIgnoreCase("Standard User")) {
                    refreshStandardSmsInbox();
                }
                if(userType.equalsIgnoreCase("Guardian User")) {
                    refreshGuardianSmsInbox();
                }
            }
            else
            {
                Toast.makeText(this.getContext(), "Read SMS permission denied", Toast.LENGTH_SHORT).show();
            }

        }
        else
        {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void refreshGuardianSmsInbox() {
        ContentResolver contentResolver = getActivity().getContentResolver();
        getPermissionToReadSMS();
        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms"), null, null, null, null);
        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexAddress = smsInboxCursor.getColumnIndex("address");
        if (indexBody < 0 || !smsInboxCursor.moveToFirst()) return;
        arrayAdapter.clear();

        do {
            String type = smsInboxCursor.getString(smsInboxCursor.getColumnIndex(columns[4]));
            if (type.equals("2")) {
                str = "\nYou:" +
                        "\n\n" + smsInboxCursor.getString(indexBody);
            } else {
                str = "\n" + standardName + ":" +
                        "\n\n" + smsInboxCursor.getString(indexBody);
            }
            if (smsInboxCursor.getString(indexAddress).equals(numberPlus) || smsInboxCursor.getString(indexAddress).equals(number)) {
                arrayAdapter.add(str);
            }
        } while (smsInboxCursor.moveToNext());
    }

    public void refreshStandardSmsInbox()
    {
        ContentResolver contentResolver = getActivity().getContentResolver();
        getPermissionToReadSMS();
        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms"), null, null, null, null);
        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexAddress = smsInboxCursor.getColumnIndex("address");
        if (indexBody < 0 || !smsInboxCursor.moveToFirst()) return;
        arrayAdapter.clear();

        do
        {
            String type = smsInboxCursor.getString(smsInboxCursor.getColumnIndex(columns[4]));
            if (type.equals("2")) {
                str = "\nYou:" +
                        "\n\n" + smsInboxCursor.getString(indexBody);
            }
            else {
                str = "\n" + guardianName + ":" +
                        "\n\n" + smsInboxCursor.getString(indexBody);
            }
            if (smsInboxCursor.getString(indexAddress).equals(numberPlus) || smsInboxCursor.getString(indexAddress).equals(number)) {
                arrayAdapter.add(str);
            }
        } while (smsInboxCursor.moveToNext());
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }

    @Override
    public void onDestroyView() {
        getActivity().findViewById(R.id.messageActionButton).setVisibility(View.VISIBLE);
        super.onDestroyView();
    }

}
