package com.project.group.projectga.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.project.group.projectga.R;
import com.project.group.projectga.models.Profile;
import com.project.group.projectga.preferences.Preferences;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.regex.Pattern;

import mehdi.sakout.fancybuttons.FancyButton;

import static android.app.Activity.RESULT_OK;

/**
 * Created by ramjiseetharaman on 6/30/17.
 */

public class ProfileFragment extends Fragment {

    TextView emailText, birthdayText, guardianEmail;
    TextInputEditText nameText, phoneText;
    CircularImageView circularProfilePhoto;
    FancyButton cameraButton, galleryButton, removeButton;

    ConstraintLayout guardianDividerLayout, guardianLayout;

    public static final int RC_SIGN_IN = 123;
    private static final int RC_PHOTO_PICKER = 3;
    public static final int RC_CAMERA_CODE = 123;

    String userId;
    Toolbar toolbar;
    MenuItem edit, save;

    DatabaseReference databaseReference;
    private StorageReference storageReference;

    public ProfileFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        final String userType = sharedPreferences.getString(Preferences.USER_TYPE, "");
        userId = sharedPreferences.getString(Preferences.USERID, null);
        if (userId != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
            storageReference = FirebaseStorage.getInstance().getReference();
        }

        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        ImageView icon = (ImageView) getActivity().findViewById(R.id.toolbarIcon);
        icon.setImageResource(R.drawable.ic_person_black_24dp);
        icon.setColorFilter(null);
        TextView title = (TextView) getActivity().findViewById(R.id.toolbarTitle);
        title.setText(getString(R.string.profile));
        title.setTextColor(getResources().getColor(R.color.colorPrimary));
        toolbar.setBackground(null);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_menu_red_24dp));

        toolbar.setVisibility(View.VISIBLE);

        guardianDividerLayout = (ConstraintLayout) view.findViewById(R.id.guardianDividerLayout);
        guardianLayout = (ConstraintLayout) view.findViewById(R.id.guardianLayout);

         if(userType.equalsIgnoreCase("Standard")) {
             guardianLayout.setVisibility(View.VISIBLE);
             guardianDividerLayout.setVisibility(View.VISIBLE);
             guardianEmail = (TextView) view.findViewById(R.id.guardianEmail);
         }else if(userType.equalsIgnoreCase("Guardian")){
             guardianLayout.setVisibility(View.GONE);
             guardianDividerLayout.setVisibility(View.GONE);
         }else{
             guardianLayout.setVisibility(View.VISIBLE);
             guardianDividerLayout.setVisibility(View.VISIBLE);
         }

        circularProfilePhoto = (CircularImageView) view.findViewById(R.id.circularPhoto);
        emailText = (TextView) view.findViewById(R.id.emailText);
        birthdayText = (TextView) view.findViewById(R.id.birthdayText);
        phoneText = (TextInputEditText) view.findViewById(R.id.phoneText);
        nameText = (TextInputEditText) view.findViewById(R.id.nameText);
        galleryButton = (FancyButton) view.findViewById(R.id.galleryButton);
        cameraButton = (FancyButton) view.findViewById(R.id.cameraButton);
        removeButton = (FancyButton) view.findViewById(R.id.removeButton);




        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Profile profile = dataSnapshot.getValue(Profile.class);
                nameText.setText(profile.getFullName());
                emailText.setText(profile.getEmail());
                if(userType.equalsIgnoreCase("Standard")) {
                    guardianEmail.setText(profile.getGuardianEmail());
                }
                birthdayText.setText(profile.getDateOfBirth());
                phoneText.setText(profile.getPhoneNumber());
                Picasso.with(getContext()).load(profile.getProfile()).error(R.drawable.ic_error_outline_black_24dp).into(circularProfilePhoto);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        setHasOptionsMenu(true);

        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
            }
        });

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("click", "remove");
                databaseReference.child("profile").setValue("https://firebasestorage.googleapis.com/v0/b/projectga-4c8e4.appspot.com/o/ic_account_circle_black_48dp.png?alt=media&token=20dba348-4406-4117-86ee-d2b0a06280d5");
                Toast.makeText(getContext(), "Profile Picture Removed", Toast.LENGTH_SHORT).show();
            }
        });

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, RC_CAMERA_CODE);
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {

            Uri selectedImageUri = data.getData();
            final StorageReference photoref = storageReference.child(userId).child(selectedImageUri.getLastPathSegment());
            photoref.putFile(selectedImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Picasso.with(getContext()).load(taskSnapshot.getDownloadUrl()).into(circularProfilePhoto);
                    databaseReference.child("profile").setValue(taskSnapshot.getDownloadUrl().toString());
                    Toast.makeText(getContext(), "Profile Picture Set", Toast.LENGTH_SHORT).show();

                }
            });

        } else if (requestCode == RC_CAMERA_CODE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            circularProfilePhoto.setImageBitmap(imageBitmap);

            circularProfilePhoto.setDrawingCacheEnabled(true);
            circularProfilePhoto.buildDrawingCache();
            Bitmap bitmap = circularProfilePhoto.getDrawingCache();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] d = baos.toByteArray();

            UploadTask uploadTask = storageReference.child(userId).putBytes(d);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    databaseReference.child("profile").setValue(taskSnapshot.getDownloadUrl().toString());
                    Toast.makeText(getContext(), "Profile Picture Set", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("fail", "fail");
                }
            });

        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        edit = menu.add("Edit").setIcon(R.drawable.ic_edit_black_24dp).setShowAsActionFlags(1);
        save = menu.add("Save").setIcon(R.drawable.ic_save_black_24dp).setVisible(false).setShowAsActionFlags(1);
        edit.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                edit.setVisible(false);
                save.setVisible(true);
                nameText.setEnabled(true);
                phoneText.setEnabled(true);
                phoneText.addTextChangedListener(new PhoneNumberFormattingTextWatcher() {
                    //we need to know if the user is erasing or inputing some new character
                    private boolean backspacingFlag = false;
                    //we need to block the :afterTextChanges method to be called again after we just replaced the EditText text
                    private boolean editedFlag = false;
                    //we need to mark the cursor position and restore it after the edition
                    private int cursorComplement;

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        //we store the cursor local relative to the end of the string in the EditText before the edition
                        cursorComplement = s.length()-phoneText.getSelectionStart();
                        //we check if the user ir inputing or erasing a character
                        if (count > after) {
                            backspacingFlag = true;
                        } else {
                            backspacingFlag = false;
                        }
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        // nothing to do here =D
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        String string = s.toString();
                        //what matters are the phone digits beneath the mask, so we always work with a raw string with only digits
                        String phone = string.replaceAll("[^\\d]", "");

                        //if the text was just edited, :afterTextChanged is called another time... so we need to verify the flag of edition
                        //if the flag is false, this is a original user-typed entry. so we go on and do some magic
                        if (!editedFlag) {

                            //we start verifying the worst case, many characters mask need to be added
                            //example: 999999999 <- 6+ digits already typed
                            // masked: (999) 999-999
                            if (phone.length() >= 6 && !backspacingFlag) {
                                //we will edit. next call on this textWatcher will be ignored
                                editedFlag = true;
                                //here is the core. we substring the raw digits and add the mask as convenient
                                String ans = "(" + phone.substring(0, 3) + ") " + phone.substring(3,6) + "-" + phone.substring(6);
                                phoneText.setText(ans);
                                //we deliver the cursor to its original position relative to the end of the string
                                phoneText.setSelection(phoneText.getText().length()-cursorComplement);

                                //we end at the most simple case, when just one character mask is needed
                                //example: 99999 <- 3+ digits already typed
                                // masked: (999) 99
                            } else if (phone.length() >= 3 && !backspacingFlag) {
                                editedFlag = true;
                                String ans = "(" +phone.substring(0, 3) + ") " + phone.substring(3);
                                phoneText.setText(ans);
                                phoneText.setSelection(phoneText.getText().length()-cursorComplement);
                            }
                            // We just edited the field, ignoring this cicle of the watcher and getting ready for the next
                        } else {
                            editedFlag = false;
                        }
                    }
                });
                return false;
            }
        });

        save.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                saveFunction();
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void saveFunction() {
        String fullName = nameText.getText().toString().trim();
        String phoneNumber = phoneText.getText().toString().trim();

        if (!validateForm(fullName, phoneNumber)) {
            return;
        }

        databaseReference.child("fullName").setValue(fullName);
        databaseReference.child("phoneNumber").setValue(phoneNumber);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Profile profile = dataSnapshot.getValue(Profile.class);
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = preferences.edit();
                if (profile != null) {
                    editor.putString(Preferences.NAME, profile.getFullName());
                    editor.putString(Preferences.EMAIL, profile.getEmail());
                }
                editor.apply();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        nameText.setEnabled(false);
        phoneText.setEnabled(false);
        edit.setVisible(true);
        save.setVisible(false);


    }

    private boolean validateForm(String name, String phoneNumber) {
        boolean valid = true;

        String textOnlyRegex = "^[\\p{L} .'-]+$";
        if (TextUtils.isEmpty(name) || !Pattern.matches(textOnlyRegex, name)) {
            nameText.setError("Please enter a valid name");
            valid = false;
        } else {
            nameText.setError(null);
        }
        if (TextUtils.isEmpty(phoneNumber)) {
            phoneText.setError("Phone number is required");
            valid = false;
        } else {
            phoneText.setError(null);
        }
        if(!isValidPhoneNumber(phoneNumber)){
            phoneText.setError("Please enter a valid Phone Number");
            valid = false;
        }else{
            phoneText.setError(null);
        }

        if(phoneNumber.length() <14){
            phoneText.setError("Phone number should be atleast 10 digits");
            valid = false;
        }else{
            phoneText.setError(null);
        }

        return valid;
    }

    private static boolean isValidPhoneNumber(String phoneNumber) {
        return !TextUtils.isEmpty(phoneNumber) && android.util.Patterns.PHONE.matcher(phoneNumber).matches();
    }
}