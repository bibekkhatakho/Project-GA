package com.project.group.projectga.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.project.group.projectga.preferences.Preferences;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Created by MitchelSmith on 8/17/2017.
 */

public class BackupService extends IntentService {

    private String userId;

    private FirebaseAuth firebaseAuth;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;

    byte[] data;

    public BackupService() {
        super("BackupService");
    }

    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String userIdPref = sharedPreferences.getString(Preferences.USERID, "");

        if (userIdPref !=null) {
            userId = userIdPref;
            getInfo();
            backupPhotos(this);
        }else{
            Log.d("Empty User Id", "Backup Service");
        }
    }

    public void getInfo(){
            firebaseAuth = FirebaseAuth.getInstance();
            storageReference = FirebaseStorage.getInstance().getReference().child(userId).child("Gallery");
            databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("Photos");
    }

    public void backupPhotos(Context context) {
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;

        String absolutePathOfImage = null;
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

        final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
        cursor = context.getContentResolver().query(uri, projection, null, null, orderBy + " DESC");

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);
            Log.e("Column", absolutePathOfImage);
            Log.e("Folder", cursor.getString(column_index_folder_name));

            String folder = cursor.getString(column_index_folder_name);
            Uri photoUri = Uri.fromFile(new File(absolutePathOfImage));
            String originalFileUri = photoUri.getPath();
            String fileUri = photoUri.toString();

            storageReference = FirebaseStorage.getInstance().getReference().child(userId).child("Gallery");
            storageReference = storageReference.child(folder);

            fileUri = fileUri.substring(fileUri.lastIndexOf("/") + 1);

            String fileUriPeriod = fileUri.replace(".", ",");
            databaseReference.child(fileUriPeriod).child("folder").setValue(folder);

            Bitmap bitmap = BitmapFactory.decodeFile(originalFileUri);
            Log.e("FILE", originalFileUri);
            if (bitmap != null && !bitmap.toString().isEmpty()) {
                bitmap = rotateImage(bitmap, (originalFileUri));


                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                data = baos.toByteArray();
            }

            storageReference = storageReference.child(fileUri);
            UploadTask uploadTask = storageReference.putBytes(data);
            databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("Photos");
            storageReference = FirebaseStorage.getInstance().getReference().child(userId).child("Gallery");
        }
    }

    private Bitmap rotateImage(Bitmap bitmap, String photoPath) {
        Bitmap rotatedBitmap = bitmap;
        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(photoPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(270);
                break;
            default:
        }
        rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return rotatedBitmap;
    }
}
