package com.luminous.pick;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;
import com.nostra13.universalimageloader.utils.StorageUtils;

public class CustomGalleryActivity extends Activity {

    private Handler handler;
    private GalleryAdapter adapter;

    private Button btnGalleryOk;

    private String action;
    private ImageLoader imageLoader;

    GalAdapter myAdat;

    private ArrayList<Bitmap> arr = new ArrayList<>();

    private int width;
    private int height;

    Bitmap bmUpRightPartial;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.gallery);

        action = getIntent().getAction();
        if (action == null) {
            finish();
        }

        File file = new File("/storage/external_SD/pictures");
        File imageFiles[] = file.listFiles();

        for (int i = 0; i < imageFiles.length; i++) {
            getDropboxIMGSize(imageFiles[i].toString());

            float a = width / 370.0f;

            int halfWidth = 370;
            float halfHeight = height / a;
            Bitmap bt = Bitmap.createBitmap(halfWidth, (int) halfHeight, Bitmap.Config.RGB_565);
            bt.eraseColor(Color.GRAY);
            arr.add(bt);

        }


        initImageLoader();
        init();

        /*DownloadFilesTask dtt = new DownloadFilesTask();
        dtt.execute(imageFiles);*/
    }

    private void initImageLoader() {
        try {
            String CACHE_DIR = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/.temp_tmp";
            new File(CACHE_DIR).mkdirs();

            File cacheDir = StorageUtils.getOwnCacheDirectory(getBaseContext(),
                    CACHE_DIR);

            DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                    .cacheOnDisc(true).imageScaleType(ImageScaleType.EXACTLY)
                    .bitmapConfig(Bitmap.Config.RGB_565).build();
            ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(
                    getBaseContext())
                    .defaultDisplayImageOptions(defaultOptions)
                    .discCache(new UnlimitedDiscCache(cacheDir))
                    .memoryCache(new WeakMemoryCache());

            ImageLoaderConfiguration config = builder.build();

            imageLoader = ImageLoader.getInstance();
            imageLoader.init(config);

        } catch (Exception e) {

        }
    }

    private void init() {

        handler = new Handler();

        RecyclerView recyc = (RecyclerView) findViewById(R.id.gal_rec);
        recyc.setHasFixedSize(true);
        myAdat = new GalAdapter(this,imageLoader, arr);
        StaggeredGridLayoutManager sglm = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        recyc.setClipToPadding(true);
        RecyclerView.ItemAnimator ra = new DefaultItemAnimator();
        recyc.setLayoutManager(sglm);
        recyc.setAdapter(myAdat);
        recyc.setItemAnimator(ra);


        Button go = (Button) findViewById(R.id.go_button);
        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.selectAll(true);
            }
        });

        btnGalleryOk = (Button) findViewById(R.id.btnGalleryOk);
        btnGalleryOk.setOnClickListener(mOkClickListener);

        new Thread() {

            @Override
            public void run() {
                Looper.prepare();
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        myAdat.addAll(getGalleryPhotos());
                    }
                });
                Looper.loop();
            }
        }.start();

    }


    View.OnClickListener mOkClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            ArrayList<CustomGallery> selected = myAdat.getSelected();

            String[] allPath = new String[selected.size()];
            for (int i = 0; i < allPath.length; i++) {
                allPath[i] = selected.get(i).sdcardPath;
            }

            Intent data = new Intent().putExtra("all_path", allPath);
            setResult(RESULT_OK, data);
            finish();

        }
    };

    private ArrayList<CustomGallery> getGalleryPhotos() {
        ArrayList<CustomGallery> galleryList = new ArrayList<CustomGallery>();

        try {
            final String[] columns = {MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media._ID};
            final String orderBy = MediaStore.Images.Media._ID;

            Cursor imagecursor = managedQuery(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
                    null, null, orderBy);

            if (imagecursor != null && imagecursor.getCount() > 0) {

                while (imagecursor.moveToNext()) {
                    CustomGallery item = new CustomGallery();

                    int dataColumnIndex = imagecursor
                            .getColumnIndex(MediaStore.Images.Media.DATA);

                    item.sdcardPath = imagecursor.getString(dataColumnIndex);

                    galleryList.add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // show newest photo at beginning of the list
        Collections.reverse(galleryList);
        return galleryList;
    }

    public Bitmap currectlyOrientation(String file) throws IOException {

        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        bounds.inPurgeable = true;
        bounds.inInputShareable = true;
        BitmapFactory.decodeFile(file, bounds);


        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bm = BitmapFactory.decodeFile(file, opts);
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;

        int rotationAngle = 0;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;

        Matrix matrix = new Matrix();
        matrix.setRotate(rotationAngle, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);

        return rotatedBitmap;
    }

    private class DownloadFilesTask extends AsyncTask<File[], Integer, ArrayList<Bitmap>> {
        protected ArrayList<Bitmap> doInBackground(File[]... path) {
            ArrayList<Bitmap> arr1 = new ArrayList<Bitmap>();


            if (path[0].length > 0) {
                for (int i = 0; i < path[0].length; i++) {

                    Bitmap bg = null;
                    try {
                        bg = currectlyOrientation(path[0][i].toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    int width = bg.getWidth();
                    int height = bg.getHeight();

                    float x = width / 370.0f;

                    int halfWidth = 370;
                    float halfHeight = height / x;

                    bmUpRightPartial = Bitmap.createScaledBitmap(bg, halfWidth, (int) halfHeight, true);
                    arr1.add(bmUpRightPartial);
                    publishProgress(i);
                }
            }
            return arr1;
        }

        protected void onProgressUpdate(Integer... progress) {
            arr.set(progress[0],bmUpRightPartial);
            myAdat.notifyDataSetChanged();
        }

        protected void onPostExecute(ArrayList<Bitmap> result) {

        }
    }

    private void getDropboxIMGSize(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        height = options.outHeight;
        width = options.outWidth;

    }

}
