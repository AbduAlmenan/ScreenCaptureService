package com.rosie.accessibilityservice;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static android.view.Surface.ROTATION_0;

/**
 * Created by ryuji on 2017-04-24.
 */

public class Reflection {
    private MyService service;
    private final static String TAG = "Reflection";
    private Class<?> mClass;

    private final static String className = "android.view.SurfaceControl";
    private final static String methodName = "screenshot";

    private static String STORE_DIRECTORY;
    private static int IMAGES_PRODUCED = 0;
    private List<AccessibilityNodeInfo> icons = new ArrayList<>();
    private List<Bitmap> iconBitmaps = new ArrayList<>();


    Reflection(MyService service){
        iconBitmaps.clear();
        icons.clear();
        this.service = service;
        this.icons = service.icons;
        try {
            mClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    void callMethod()  {
        try{
            createDirectory();
            Method cropshot = mClass.getMethod(methodName, Rect.class, int.class, int.class, int.class, int.class, boolean.class, int.class);
            if(icons.size() == 0) {
                Log.d(TAG, "There is no icon Rect");
                return;
            }
            for(int i = 0; i < icons.size() ; i ++ ) {
                Rect rect = new Rect();
                icons.get(i).getBoundsInScreen(rect);
                Bitmap result = (Bitmap) cropshot.invoke(null, rect, rect.width(), rect.height(), 0, 1000000, false, ROTATION_0);
                if(result != null){
                    iconBitmaps.add(result);
                    saveFile(result);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    ////////////////////////// save bitmap image ////////////////////////////////

    private void createDirectory (){

        File externalFilesDir = service.getExternalFilesDir(null);
        if (externalFilesDir != null) {
            STORE_DIRECTORY = externalFilesDir.getAbsolutePath() + "/screenshots/";
            File storeDirectory = new File(STORE_DIRECTORY);
            if (!storeDirectory.exists()) {
                boolean success = storeDirectory.mkdirs();
                if (!success) {
                    Log.e(TAG, "failed to create file storage directory.");
                    return;
                }
            }
        } else {
            Log.e(TAG, "failed to create file storage directory, getExternalFilesDir is null.");
            return;
        }
    }

    void saveFile(Bitmap result) throws FileNotFoundException {

        FileOutputStream fos = null;
        fos = new FileOutputStream(STORE_DIRECTORY + "/myscreen_" + IMAGES_PRODUCED + ".png");
        result.compress(Bitmap.CompressFormat.PNG, 100, fos);

        IMAGES_PRODUCED++;
        Log.d(TAG, "captured image: " + IMAGES_PRODUCED);

    }

}
