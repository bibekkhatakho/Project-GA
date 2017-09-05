package com.project.group.projectga.models;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.media.FaceDetector;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.util.Log;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.*;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.project.group.projectga.R;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.project.group.projectga.fragments.RecognitionFragment;
import com.project.group.projectga.fragments.RecognitionResultFragment;
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


    public static final int SIZE = 100;
    public static Bitmap testImage;
    public static String result;
    public static double distance;
    public static final long ONE_MEGABYTE = 1024 * 1024;
    public static boolean faceFound;


    private FragmentActivity activity;
    private RecognitionFragment fragment;

    FirebaseStorage storage;

    //    opencv_face.FaceRecognizer eigenFR;
//    opencv_face.FaceRecognizer fisherFR;
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

    //    public void waitForLock() {
//        Log.v(TAG, "waitForLock: start");
//        synchronized (LOCK) {
//            while (wait) {
//                Log.v(TAG, "waitForLock: wait is true, moving to wait()");
//                try {
//                    LOCK.wait();
//                } catch (InterruptedException e) {
//                    Log.v(TAG, "waitForLock: interrupted exception");
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
    public static Bitmap cropToFace(Bitmap source) {
        Bitmap result = source.copy(Bitmap.Config.RGB_565, true);
        Log.v(TAG, "cropToFace: image of size: " + result.getWidth() + " * " + result.getHeight());

        final int max_size = 800;
        int size = Math.max(result.getWidth(), result.getHeight());

        if (size > max_size) {
            double factor = ((double)max_size) / ((double)size);
            int w = (int) (result.getWidth() * factor);
            int h = (int) (result.getHeight() * factor);

            result = Bitmap.createScaledBitmap(result, w, h, true);
        }

        if (result.getWidth() % 2 == 1) {
            result = Bitmap.createScaledBitmap(result,
                    result.getWidth()+1, result.getHeight(), true);
        }
        if (result.getHeight() % 2 == 1) {
            result = Bitmap.createScaledBitmap(result,
                    result.getWidth(), result.getHeight()+1, true);
        }


        FaceDetector facer = new FaceDetector(result.getWidth(), result.getHeight(), 1);
        FaceDetector.Face[] faces = new FaceDetector.Face[1];

        int numFaces = facer.findFaces(result, faces);

        if ( numFaces > 0){
            faceFound = true;
            PointF midpoint = new PointF();
            faces[0].getMidPoint(midpoint);

            float distance = faces[0].eyesDistance();

            int startX = (int) Math.round(midpoint.x - (distance * 2));
            int startY = (int) Math.round(midpoint.y - (distance* 1.5));

            int widthX = Math.round(distance * 4);
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
            faceFound = false;
            Log.v(TAG, "cropToFace: NO FACES FOUND");
            return source;
        }
    }

    public static Bitmap cropMore(Bitmap source) {
        int segment = source.getHeight() / 4;
        int startX = segment;
        int startY = segment;
        int width = segment *2;
        return Bitmap.createBitmap(source, startX, startY, width, width);
    }

    public void giveContext(FragmentActivity suppliedActivity, RecognitionFragment suppliedFragment) {
        activity = suppliedActivity;
        fragment = suppliedFragment;
    }

    public void addData(String name, String url) {
        // add the name to the labels
        int id = getIdFromName(name);
        // add the image aLnd id to the training set

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
                Log.v(TAG, "buildTrainingSet: size mismatch: urls: " + urls.size() + ", ids: " + ids.size());
            }
            else {
                Log.v(TAG, "buildTrainingSet: Training set construction beginning");
            }

            storage = FirebaseStorage.getInstance();
            loadNext();
        }
        else {
            Log.v(TAG, "buildTrainingSet: No images exist to build training set.");
            ((TextView)activity.findViewById(R.id.loadingText)).setText("Please add important people photos in order to access Recognition.");
            activity.findViewById(R.id.progressBar).setVisibility(View.GONE);
            activity.findViewById(R.id.snapButton).setVisibility(View.GONE);
        }
    }

    private void loadNext() {

//        Log.v(TAG, "loadNext: start");

        if (training.size >= ids.size()) {
            ((TextView)activity.findViewById(R.id.loadingText)).setText("Loading Recognition Model...");
            initialTraining();
            return;
        }

        StorageReference httpsReference = storage.getReferenceFromUrl(urls.get(training.size));

        httpsReference.getBytes(99 * ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                bmp = cropMore(bmp);
                training.images.put(training.size, bitmapToMat(Bitmap.createScaledBitmap(bmp, SIZE, SIZE, true)));
                training.labelsBuf.put(training.size, ids.get(training.size));
                Log.v(TAG, "storage onSuccess: added data for id: " + ids.get(training.size));
                training.size++;
                loadNext();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Log.v(TAG, "onFailure: Failed to load image #: " + training.size);
                loadNext();
            }
        });
//        Log.v(TAG, "loadNext: bitmap loading initialized, waiting on picasso...");
    }

    // update model (left out for now)


    // train model
    public void initialTraining() {

        if (training.size > 0) {
            LBPHFR.train(training.images, training.labels);
            Log.v(TAG, "initialTraining: training successful, with # of images = " + training.size);
        }
        else {
            Log.v(TAG, "initialTraining: less than 1 image in training set! set size: " + training.size);
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

    public void predict(Bitmap bmp) {
        testImage = bmp;

        IntPointer label = new IntPointer(1);
        DoublePointer confidence = new DoublePointer(1);
        LBPHFR.predict(bitmapToMat(Bitmap.createScaledBitmap(cropMore(bmp), SIZE, SIZE, true)), label, confidence);

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

//        String path = getPathFromURI(Uri.fromFile(f));

//        Mat result = new Mat();

        Mat result = imread(f.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);

        if (result == null) {
            Log.v(TAG, "bitmapToMat: result is null");
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
////            Log.v(TAG, "getBitmapFromURL: Bitmap from URL threw an exception...");
////            return null;
////        }
//    }
}
