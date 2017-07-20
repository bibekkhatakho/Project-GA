package com.project.group.projectga.fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.project.group.projectga.R;
import com.project.group.projectga.activities.PhotosActivity;
import com.project.group.projectga.adapters.Adapter_PhotosFolder;
import com.project.group.projectga.adapters.MyGridAdapter;
import com.project.group.projectga.helpers.BitmapHelper;
import com.project.group.projectga.models.GridViewItem;
import com.project.group.projectga.models.Model_images;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class GalleryFragment extends Fragment {

    Toolbar toolbar;
	MenuItem search;
    public static ArrayList<Model_images> al_images = new ArrayList<>();
    boolean boolean_folder;
    Adapter_PhotosFolder obj_adapter;
    GridView gv_folder;
    FloatingActionButton cameraButton;
    private static final int REQUEST_PERMISSIONS = 100;
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    String projectName = "ProjectGA";
    File directory;
    String mCurrentPhotoPath;

    List<GridViewItem> gridItems;
    GridView gridView;

    public GalleryFragment(){

    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        directory = new File(Environment.getExternalStorageDirectory() + projectName);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        ImageView icon = (ImageView) getActivity().findViewById(R.id.toolbarIcon);
        icon.setImageResource(R.drawable.ic_perm_media_black_24dp);
        icon.setColorFilter(getResources().getColor(R.color.Gallery));
        TextView title = (TextView) getActivity().findViewById(R.id.toolbarTitle);
        title.setText(getString(R.string.galleryLabel));
        title.setTextColor(getResources().getColor(R.color.textInputEditTextColor));
        toolbar.setBackground(getResources().getDrawable(R.drawable.tile_green));
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_menu_green_24dp));
        cameraButton = (FloatingActionButton) view.findViewById(R.id.cameraBtn);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        //gridView = (GridView) view.findViewById(R.id.gridView);

        gv_folder = (GridView)view.findViewById(R.id.gv_folder);
        gv_folder.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getContext(), PhotosActivity.class);
                intent.putExtra("value",i);
                startActivity(intent);
            }
        });

        if ((ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            if ((ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) && (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE))) {

            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS);
            }
        }else {
            Log.e("Else","Else");
            fn_imagespath();
        }
//
		
		setHasOptionsMenu(true);
		
        return view;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {photoFile = createImageFile();

            } catch (IOException ex) {

                Context context = getContext();
                CharSequence text = "Photo cannot be stored.";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Context context = getContext();
                Uri photoURI = FileProvider.getUriForFile(context,
                        "com.example.projectga.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }else{
                Context context = getContext();
                CharSequence text = "Attention! Required to take picture!!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        }
    }

    public void createFolder(){
        if (!directory.exists()){
            directory.mkdirs();
        }

    }

    private File createImageFile() throws IOException {
        createFolder();
        // Create an image file name
        Context context = getContext();
        String timeStamp = new SimpleDateFormat("dd-MMM-yyyy").format(new Date());
        String imageFileName = projectName + "_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        addImageToGallery(image, context);

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getPath();
        return image;
    }

    public static void addImageToGallery(File image, final Context context) {
        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, image.toString());

        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

    }

	 @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        search = menu.add("search").setIcon(R.drawable.ic_search_green_24dp).setShowAsActionFlags(1);

        super.onCreateOptionsMenu(menu, inflater);
    }

    public ArrayList<Model_images> fn_imagespath() {
        al_images.clear();

        int int_position = 0;
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;

        String absolutePathOfImage = null;
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

        final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
        cursor = getContext().getContentResolver().query(uri, projection, null, null, orderBy + " DESC");

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);
            Log.e("Column", absolutePathOfImage);
            Log.e("Folder", cursor.getString(column_index_folder_name));

            for (int i = 0; i < al_images.size(); i++) {
                if (al_images.get(i).getStr_folder().equals(cursor.getString(column_index_folder_name))) {
                    boolean_folder = true;
                    int_position = i;
                    break;
                } else {
                    boolean_folder = false;
                }
            }

            if (boolean_folder) {

                ArrayList<String> al_path = new ArrayList<>();
                al_path.addAll(al_images.get(int_position).getAl_imagepath());
                al_path.add(absolutePathOfImage);
                al_images.get(int_position).setAl_imagepath(al_path);

            } else {
                ArrayList<String> al_path = new ArrayList<>();
                al_path.add(absolutePathOfImage);
                Model_images obj_model = new Model_images();
                obj_model.setStr_folder(cursor.getString(column_index_folder_name));
                obj_model.setAl_imagepath(al_path);
                al_images.add(obj_model);
            }
        }

        for (int i = 0; i < al_images.size(); i++) {
            Log.e("FOLDER", al_images.get(i).getStr_folder());
            for (int j = 0; j < al_images.get(i).getAl_imagepath().size(); j++) {
                Log.e("FILE", al_images.get(i).getAl_imagepath().get(j));
            }
        }
        obj_adapter = new Adapter_PhotosFolder(getContext(),al_images);
        gv_folder.setAdapter(obj_adapter);
        return al_images;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults.length > 0 && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        fn_imagespath();
                    } else {
                        Toast.makeText(getContext(), "The app was not allowed to read or write to your storage. Hence, it cannot function properly. Please consider granting it this permission", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

//private void setGridAdapter(String path) {
//    // Create a new grid adapter
//
//    gridItems = createGridItems(path);
//
//    MyGridAdapter adapter = new MyGridAdapter(getContext(), gridItems);
//
//    // Set the grid adapter
//    gridView.setAdapter(adapter);
//
//    // Set the onClickListener
//    gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//        @Override
//        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            if (gridItems.get(position).isDirectory()) {
//                setGridAdapter(gridItems.get(position).getPath());
//            }
//            else {
//                gridItems.get(position).getImage();
//            }
//        }
//    });
//
//    gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//        @Override
//        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//            if (gridItems.get(position).isDirectory()) {
//                Dialog dialog = new Dialog(getContext());
//                dialog.setTitle("Test");
//                dialog.setContentView(R.layout.activity_important_people);
//                dialog.show();
//                return false;
//            } else {
//                Dialog dialog = new Dialog(getContext());
//                dialog.setTitle("Test");
//                dialog.setContentView(R.layout.activity_reset_password);
//                dialog.show();
//                return false;
//            }
//        }
//    });
//}

//    private List<GridViewItem> createGridItems(String directoryPath) {
//        List<GridViewItem> items = new ArrayList<>();
//
//        // List all the items within the folder.
//        File[]  files = new File(directoryPath).listFiles(new ImageFileFilter());
//        if(files != null && files.length !=0) {
//            for (File file : files) {
//
//                // Add the directories containing images or sub-directories
//                if (file.isDirectory()
//                        && file.listFiles(new ImageFileFilter()).length > 0) {
//
//                    items.add(new GridViewItem(file.getAbsolutePath(), true, null));
//                }
//                // Add the images
//                else {
//                    Bitmap image = BitmapHelper.decodeBitmapFromFile(file.getAbsolutePath(),
//                            50,
//                            50);
//                    items.add(new GridViewItem(file.getAbsolutePath(), false, image));
//                }
//            }
//        }else{
//            Toast.makeText(getContext(), "There are no items in the specified path to be displayed.", Toast.LENGTH_LONG).show();
//        }
//        return items;
//    }
//
//
//
//    /**
//     * Checks the file to see if it has a compatible extension.
//     */
//    private boolean isImageFile(String filePath) {
//        if (filePath.endsWith(".jpg") || filePath.endsWith(".png"))
//        // Add other formats as desired
//        {
//            return true;
//        }
//        return false;
//    }
//
//    /**
//     * This can be used to filter files.
//     */
//    private class ImageFileFilter implements FileFilter {
//
//        @Override
//        public boolean accept(File file) {
//            if (file.isDirectory()) {
//                return true;
//            }
//            else if (isImageFile(file.getAbsolutePath())) {
//                return true;
//            }
//            return false;
//        }
//    }

}