package com.project.group.projectga.models;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.util.Log;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.*;
import com.project.group.projectga.R;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.project.group.projectga.fragments.RecognitionFragment;
import com.project.group.projectga.fragments.RecognitionResultFragment;

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

import static org.bytedeco.javacpp.opencv_face.createLBPHFaceRecognizer;

public class Recognition {


    public static final int SIZE = 150;
    public static Bitmap testImage;
    public static String result;
    public static double distance;
    public static final long ONE_MEGABYTE = 1024 * 1024;


    private FragmentActivity activity;
    private RecognitionFragment fragment;

    FirebaseStorage storage;

    private opencv_face.FaceRecognizer LBPHFR = createLBPHFaceRecognizer();

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

    public static Bitmap cropToFace(Bitmap source) {
        Bitmap result = source.copy(Bitmap.Config.RGB_565, true);
        Log.d(TAG, "cropToFace: image of size: " + result.getWidth() + " * " + result.getHeight());

        final int max_size = 1600;
        int size = Math.max(result.getWidth(), result.getHeight());
        double factor = ((double)max_size) / ((double)size);

        if (size > max_size) {
            int w = (int) (result.getWidth() * factor);
            int h = (int) (result.getHeight() * factor);

            result = Bitmap.createScaledBitmap(result, w, h, true);
        }

        FaceDetector facer = new FaceDetector(result.getWidth(), result.getHeight(), 1);
        FaceDetector.Face[] faces = new FaceDetector.Face[1];

        int numFaces = facer.findFaces(result, faces);

        if ( numFaces > 0){
            PointF midpoint = new PointF();
            faces[0].getMidPoint(midpoint);

            float distance = faces[0].eyesDistance();

            int startX = (int) Math.round(midpoint.x - (distance * 1.5));
            int startY = (int) Math.round(midpoint.y - distance);

            int widthX = Math.round(distance * 3);
            int widthY = widthX;

            if (startX < 0) {
                widthX += startX;
                startX = 0;
            }
            if (startY < 0) {
                widthY += startY;
                startY = 0;
            }
            if ((startX + widthX) > result.getWidth()) widthX = result.getWidth() - startX;
            if ((startY + widthY) > result.getWidth()) widthY = result.getHeight() - startY;

            return Bitmap.createBitmap(result, startX, startY, widthX, widthY);
        }
        else {
            Log.d(TAG, "cropToFace: NO FACES FOUND");
            return source;
        }
    }

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
        if (urls.size() > 0) {

            training.size = 0;

            training.images = new opencv_core.MatVector(ids.size());
            training.labels = new Mat(ids.size(), 1, CV_32SC1);
            training.labelsBuf = training.labels.createBuffer();

            if (urls.size() != ids.size()) {
                Log.d(TAG, "buildTrainingSet: size mismatch: urls: " + urls.size() + ", ids: " + ids.size());
            }
            else {
                Log.d(TAG, "buildTrainingSet: Training set construction beginning");
            }

            storage = FirebaseStorage.getInstance();
            loadNext();
        }
        else {
            Log.d(TAG, "buildTrainingSet: No images exist to build training set.");
            ((TextView)activity.findViewById(R.id.loadingText)).setText("Please add important people photos in order to access Recognition.");
            ((ProgressBar)activity.findViewById(R.id.progressBar)).setVisibility(View.GONE);
        }
    }

    private void loadNext() {

//        Log.d(TAG, "loadNext: start");

        if (training.size >= ids.size()) {
            ((TextView)activity.findViewById(R.id.loadingText)).setText("Loading Recognition Model...");
            initialTraining();
            return;
        }

        StorageReference httpsReference = storage.getReferenceFromUrl(urls.get(training.size));

        httpsReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                training.images.put(training.size, bitmapToMat(Bitmap.createScaledBitmap(bmp, SIZE, SIZE, true)));
                training.labelsBuf.put(training.size, ids.get(training.size));
                Log.d(TAG, "storage onSuccess: added data for id: " + ids.get(training.size));
                training.size++;
                loadNext();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Log.d(TAG, "onFailure: Failed to load image #: " + training.size);
                loadNext();
            }
        });
//        Log.d(TAG, "loadNext: bitmap loading initialized, waiting on picasso...");
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

    }

    public void predict(Bitmap bmp) {
        testImage = bmp;

        IntPointer label = new IntPointer(1);
        DoublePointer confidence = new DoublePointer(1);
        LBPHFR.predict(bitmapToMat(Bitmap.createScaledBitmap(bmp, SIZE, SIZE, true)), label, confidence);

        result = getNameFromID(label.get(0));
        distance = confidence.get(0);

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
}
