package com.project.group.projectga.models;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.util.Log;


import com.google.firebase.storage.StorageReference;
import com.project.group.projectga.R;
import com.project.group.projectga.activities.MainMenuActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.project.group.projectga.fragments.RecognitionFragment;
import com.project.group.projectga.fragments.RecognitionResultFragment;
import com.project.group.projectga.helpers.FaceCropper;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_face;

import static android.content.ContentValues.TAG;
import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import org.bytedeco.javacpp.opencv_core.Mat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

//import static org.bytedeco.javacpp.opencv_face.createEigenFaceRecognizer;
//import static org.bytedeco.javacpp.opencv_face.createFisherFaceRecognizer;
import static org.bytedeco.javacpp.opencv_face.createLBPHFaceRecognizer;

public class Recognition {


    public static Bitmap testImage;
    public static String result;


    private FragmentActivity activity;
    private RecognitionFragment fragment;


//    opencv_face.FaceRecognizer eigenFR;
//    opencv_face.FaceRecognizer fisherFR;
    private opencv_face.FaceRecognizer LBPHFR = createLBPHFaceRecognizer();
    private FaceCropper fc = new FaceCropper();

    private List<Label> labels = new ArrayList<>();
    private List<Integer> ids = new ArrayList<>();
    private List<String> urls = new ArrayList<>();

    private class Label {
        String name;
        int id;
    }

    private class TrainingSet {
        opencv_core.MatVector images;
        opencv_core.Mat labels;
        IntBuffer labelsBuf;
        int size = 0;
    }

    private TrainingSet training = new TrainingSet();

//    public void waitForLock() {
//        Log.d(TAG, "waitForLock: start");
//        synchronized (LOCK) {
//            while (wait) {
//                Log.d(TAG, "waitForLock: wait is true, moving to wait()");
//                try {
//                    LOCK.wait();
//                } catch (InterruptedException e) {
//                    Log.d(TAG, "waitForLock: interrupted exception");
//                }
//            }
//        }
//    }
//    private void lock() {
//        synchronized (LOCK) {
//            wait = true;
//        }
//    }
//
//    private void unlock() {
//        synchronized (LOCK) {
//            wait = false;
//            LOCK.notifyAll();
//        }
//    }

    public void giveContext(FragmentActivity suppliedActivity, RecognitionFragment suppliedFragment) {
        activity = suppliedActivity;
        fragment = suppliedFragment;
    }

    public void addData(String name, String url) {
        // add the name to the labels
        int id = getIdFromName(name);
        // add the image and id to the training set

        ids.add(id);
        urls.add(url);
    }

    // add to training data
    public void buildTrainingSet() {

        fc.setMaxFaces(1);
        training.size = 0;

        training.images = new opencv_core.MatVector(ids.size());
        training.labels = new Mat(ids.size(), 1, CV_32SC1);
        training.labelsBuf = training.labels.createBuffer();

        if (urls.size() != ids.size()) {
            Log.d(TAG, "buildTrainingSet: size mismatch: imgs: " + urls.size() + ", ids: " + ids.size());
        }
        else {
            Log.d(TAG, "buildTrainingSet: Training set construction beginning");
        }

        //StorageReference httpsReference = getStorage().getReferenceFromUrl();

        loadNext();
    }

    private void loadNext() {

        Log.d(TAG, "loadNext: start");
        
        if (training.size >= ids.size()) {
            ((TextView)activity.findViewById(R.id.loadingText)).setText("Loading Recognition Model...");
            initialTraining();
            return;
        }
        Picasso.with(activity).load(urls.get(training.size))
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded (final Bitmap bitmap, Picasso.LoadedFrom from){
                        Log.d(TAG, "onBitmapLoaded: start");

                        training.images.put(training.size, bitmapToMat(fc.getCroppedImage(bitmap.copy(bitmap.getConfig(), true))));
                        training.labelsBuf.put(training.size,ids.get(training.size));
                        Log.d(TAG, "onBitmapLoaded: added data for id: " + ids.get(training.size));
                        training.size++;
                        loadNext();
                    }



                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                    }
                });
    }
    
    // update model (left out for now)


    // train model
    public void initialTraining() {

        if (training.size > 0) {
            LBPHFR.train(training.images, training.labels);
            Log.d(TAG, "initialTraining: training successful, with # of images = " + training.size);
        }
        else {
            Log.d(TAG, "initialTraining: less than 1 image in training set! set size: " + training.size);
        }

        CardView loadingCard = (CardView) activity.findViewById(R.id.loadingCard);

        loadingCard.setVisibility(View.GONE);
        fragment.setCamButton();

//        eigenFR = createEigenFaceRecognizer();
//        fisherFR = createFisherFaceRecognizer();
//        if (training.size > 1) {
//            eigenFR.train(training.images,training.labels);
//            fisherFR.train(training.images,training.labels);
//        }
    }

    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                                   boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
    }

    public void predict(Bitmap bmp) {
        testImage = scaleDown(fc.getCroppedImage(bmp), 250, false);



        IntPointer label = new IntPointer(1);
        DoublePointer confidence = new DoublePointer(1);
        LBPHFR.predict(bitmapToMat(testImage), label, confidence);
        int predictedLabel = label.get(0);

        result = getNameFromID(predictedLabel);



        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        Fragment rrf = new RecognitionResultFragment();
        ft.replace(R.id.container_gaFragments, rrf);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.commit();
    }

    private Mat bitmapToMat(Bitmap bitmap) {
        File f = new File(activity.getExternalFilesDir(null), "temp.jpg");
        try {
            OutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        }
        catch (Exception e) {
            // nothing for now
        }

//        String path = getPathFromURI(Uri.fromFile(f));

//        Mat result = new Mat();

        Mat result = imread(f.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);

        if (result == null) {
            Log.d(TAG, "bitmapToMat: result is null");
        }

        return result;
    }

    private int getIdFromName(String name) {
        for (Label label : labels) {
            if (name.equals(label.name)) return label.id;
        }
        Label l = new Label();
        l.name = name;
        l.id = labels.size();
        labels.add(l);
        return l.id;
    }

    private String getNameFromID(int id) {
        for (Label label : labels) {
            if (id == label.id) return label.name;
        }
        return null;
    }

//    String getPathFromURI(Uri uri) {
//        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
//        cursor.moveToFirst();
//        String document_id = cursor.getString(0);
//        document_id = document_id.substring(document_id.lastIndexOf(":")+1);
//        cursor.close();
//
//        cursor = context.getContentResolver().query(
//                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
//        cursor.moveToFirst();
//        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
//        cursor.close();
//
//        return path;
//    }

//    private Bitmap getBitmapFromURL(String src) {
//        Picasso.with(context).load(src)
//                .into(new Target() {
//                    @Override
//                    public void onBitmapLoaded (final Bitmap bitmap, Picasso.LoadedFrom from){
//                        if (fc.getMaxFaces() != 1) fc.setMaxFaces(1);
//
//                        training.images.put(bitmapToMat(fc.getCroppedImage(bitmap)));
//                        count++;
//                    }
//
//                    @Override
//                    public void onBitmapFailed(Drawable errorDrawable) {
//                    }
//
//                    @Override
//                    public void onPrepareLoad(Drawable placeHolderDrawable) {
//                    }
//                });
////        try {
////            URL url = new URL(src);
////            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
////            connection.setDoInput(true);
////            connection.connect();
////            InputStream input = connection.getInputStream();
////            return BitmapFactory.decodeStream(input);
////        } catch (IOException e) {
////            Log.d(TAG, "getBitmapFromURL: Bitmap from URL threw an exception...");
////            return null;
////        }
//    }
}
