package com.luminous.pick.Utils;

import android.graphics.Bitmap;
import android.util.LruCache;

import java.util.ArrayList;

/**
 * Created by intern on 1/15/15.
 */
public class Example extends LruCache<String, Bitmap> {

    private static final int DEFAULT_CACHE_SIZE = (int) (Runtime.getRuntime().maxMemory() / 1024) / 8;
    private static Example instance;
    private static int count=0;
    private static ArrayList<Integer> selectedImages=new ArrayList<>();

    private Example(final int maxSize) {
        super(maxSize);
    }

    public static Example get(){
        if(instance==null){
            final int cacheSize=(int)(Runtime.getRuntime().maxMemory())/1024;
            instance=new Example(cacheSize);
        }
        return instance;
    }

    public static void addBitmap(String key, Bitmap bitmap) {
        if (getBitmap(key) == null) {
            Example.get().put(key, bitmap);
            count++;
        }
    }

    public static Bitmap getBitmap(String key) {
        return Example.get().get(key);
    }

    @Override
    protected int sizeOf(String key, Bitmap value) {
        return value == null ? 0 : value.getRowBytes() * value.getHeight() / 1024;
    }

    public static int getCount(){
        return count;
    }

    public static void addImagePos(int i){
        selectedImages.add(i);
    }

    public static void removeImagePos(int i){
        selectedImages.remove(i);
    }
    public static ArrayList<Integer> getSelectedImages(){
        return selectedImages;
    }
}

