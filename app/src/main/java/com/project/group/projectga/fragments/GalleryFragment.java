package com.project.group.projectga.fragments;

import android.Manifest;
import android.app.Dialog;
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
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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

import com.project.group.projectga.R;
import com.project.group.projectga.activities.PhotosActivity;
import com.project.group.projectga.adapters.Adapter_PhotosFolder;
import com.project.group.projectga.adapters.MyGridAdapter;
import com.project.group.projectga.helpers.BitmapHelper;
import com.project.group.projectga.models.GridViewItem;
import com.project.group.projectga.models.Model_images;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class GalleryFragment extends Fragment {

    Toolbar toolbar;
	MenuItem search;
    public static ArrayList<Model_images> al_images = new ArrayList<>();
    boolean boolean_folder;
    Adapter_PhotosFolder obj_adapter;
    GridView gv_folder;
    private static final int REQUEST_PERMISSIONS = 100;

    List<GridViewItem> gridItems;
    GridView gridView;

    public GalleryFragment(){

    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


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
        toolbar.setBackground(getResources().getDrawable(R.drawable.tile_green));
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_menu_green_24dp));

        gridView = (GridView) view.findViewById(R.id.gridView);

        setGridAdapter(Environment.getExternalStorageDirectory().getPath());


//        gv_folder = (GridView)view.findViewById(R.id.gv_folder);
//        gv_folder.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Intent intent = new Intent(getContext(), PhotosActivity.class);
//                intent.putExtra("value",i);
//                startActivity(intent);
//            }
//        });
//
//        if ((ContextCompat.checkSelfPermission(getContext(),
//                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(getContext(),
//                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
//            if ((ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) && (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
//                    Manifest.permission.READ_EXTERNAL_STORAGE))) {
//
//            } else {
//                ActivityCompat.requestPermissions(getActivity(),
//                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
//                        REQUEST_PERMISSIONS);
//            }
//        }else {
//            Log.e("Else","Else");
//            fn_imagespath();
//        }
//
		
		setHasOptionsMenu(true);
		
        return view;
    }
	
	 @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        search = menu.add("search").setIcon(R.drawable.ic_search_green_24dp).setShowAsActionFlags(1);

        super.onCreateOptionsMenu(menu, inflater);
    }

//    public ArrayList<Model_images> fn_imagespath() {
//        al_images.clear();
//
//        int int_position = 0;
//        Uri uri;
//        Cursor cursor;
//        int column_index_data, column_index_folder_name;
//
//        String absolutePathOfImage = null;
//        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//
//        String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
//
//        final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
//        cursor = getContext().getContentResolver().query(uri, projection, null, null, orderBy + " DESC");
//
//        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
//        column_index_folder_name = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
//        while (cursor.moveToNext()) {
//            absolutePathOfImage = cursor.getString(column_index_data);
//            Log.e("Column", absolutePathOfImage);
//            Log.e("Folder", cursor.getString(column_index_folder_name));
//
//            for (int i = 0; i < al_images.size(); i++) {
//                if (al_images.get(i).getStr_folder().equals(cursor.getString(column_index_folder_name))) {
//                    boolean_folder = true;
//                    int_position = i;
//                    break;
//                } else {
//                    boolean_folder = false;
//                }
//            }
//
//            if (boolean_folder) {
//
//                ArrayList<String> al_path = new ArrayList<>();
//                al_path.addAll(al_images.get(int_position).getAl_imagepath());
//                al_path.add(absolutePathOfImage);
//                al_images.get(int_position).setAl_imagepath(al_path);
//
//            } else {
//                ArrayList<String> al_path = new ArrayList<>();
//                al_path.add(absolutePathOfImage);
//                Model_images obj_model = new Model_images();
//                obj_model.setStr_folder(cursor.getString(column_index_folder_name));
//                obj_model.setAl_imagepath(al_path);
//                al_images.add(obj_model);
//            }
//        }
//
//        for (int i = 0; i < al_images.size(); i++) {
//            Log.e("FOLDER", al_images.get(i).getStr_folder());
//            for (int j = 0; j < al_images.get(i).getAl_imagepath().size(); j++) {
//                Log.e("FILE", al_images.get(i).getAl_imagepath().get(j));
//            }
//        }
//        obj_adapter = new Adapter_PhotosFolder(getContext(),al_images);
//        gv_folder.setAdapter(obj_adapter);
//        return al_images;
//    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        switch (requestCode) {
//            case REQUEST_PERMISSIONS: {
//                for (int i = 0; i < grantResults.length; i++) {
//                    if (grantResults.length > 0 && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
//                        fn_imagespath();
//                    } else {
//                        Toast.makeText(getContext(), "The app was not allowed to read or write to your storage. Hence, it cannot function properly. Please consider granting it this permission", Toast.LENGTH_LONG).show();
//                    }
//                }
//            }
//        }
//    }
private void setGridAdapter(String path) {
    // Create a new grid adapter

    gridItems = createGridItems(path);

    MyGridAdapter adapter = new MyGridAdapter(getContext(), gridItems);

    // Set the grid adapter
    gridView.setAdapter(adapter);

    // Set the onClickListener
    gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (gridItems.get(position).isDirectory()) {
                setGridAdapter(gridItems.get(position).getPath());
            }
            else {
                gridItems.get(position).getImage();
            }
        }
    });

    gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if (gridItems.get(position).isDirectory()) {
                Dialog dialog = new Dialog(getContext());
                dialog.setTitle("Test");
                dialog.setContentView(R.layout.activity_recognition);
                dialog.show();
                return false;
            } else {
                Dialog dialog = new Dialog(getContext());
                dialog.setTitle("Test");
                dialog.setContentView(R.layout.activity_reset_password);
                dialog.show();
                return false;
            }
        }
    });
}

    private List<GridViewItem> createGridItems(String directoryPath) {
        List<GridViewItem> items = new ArrayList<>();

        // List all the items within the folder.
        File[]  files = new File(directoryPath).listFiles(new ImageFileFilter());
        if(files != null && files.length !=0) {
            for (File file : files) {

                // Add the directories containing images or sub-directories
                if (file.isDirectory()
                        && file.listFiles(new ImageFileFilter()).length > 0) {

                    items.add(new GridViewItem(file.getAbsolutePath(), true, null));
                }
                // Add the images
                else {
                    Bitmap image = BitmapHelper.decodeBitmapFromFile(file.getAbsolutePath(),
                            50,
                            50);
                    items.add(new GridViewItem(file.getAbsolutePath(), false, image));
                }
            }
        }else{
            Toast.makeText(getContext(), "There are no items in the specified path to be displayed.", Toast.LENGTH_LONG).show();
        }
        return items;
    }



    /**
     * Checks the file to see if it has a compatible extension.
     */
    private boolean isImageFile(String filePath) {
        if (filePath.endsWith(".jpg") || filePath.endsWith(".png"))
        // Add other formats as desired
        {
            return true;
        }
        return false;
    }

    /**
     * This can be used to filter files.
     */
    private class ImageFileFilter implements FileFilter {

        @Override
        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            }
            else if (isImageFile(file.getAbsolutePath())) {
                return true;
            }
            return false;
        }
    }

}