package com.luminous.pick;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.SyncImageLoadingListener;

public class MainActivity extends Activity {

    private ImageView currentImage;
    private RecyclerView recyclerView;
    private Button btnGalleryPickMul;
    private Button go;

    private MyRecyclerViewAdapter myRecyclerViewAdapter;
    private LinearLayoutManager linearLayoutManager;
    private GridLayoutManager gridLayoutManager;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;
    private RecyclerView.ItemAnimator itemAnimator;

    private ArrayList<Bitmap> arrayList = new ArrayList<>();
    private ImageLoader imageLoader;

    private int[] firstItemPos;
    private int[] lastItemPos;
    private String[] all_path;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);

        initImageLoader();
        init();
    }

    private void initImageLoader() {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisc().imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
                .bitmapConfig(Bitmap.Config.RGB_565).build();
        ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(
                this).defaultDisplayImageOptions(defaultOptions).memoryCache(
                new WeakMemoryCache());

        ImageLoaderConfiguration config = builder.build();
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);
    }

    private void init() {

        recyclerView = (RecyclerView) findViewById(R.id.rec_test);
        currentImage = (ImageView) findViewById(R.id.image_id);

        myRecyclerViewAdapter = new MyRecyclerViewAdapter(arrayList);
        linearLayoutManager = new LinearLayoutManager(this);  // for listview
        gridLayoutManager = new GridLayoutManager(this, 3);   // grid view with 3 column
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL);   // staggered grid
        itemAnimator = new DefaultItemAnimator();

        recyclerView.setAdapter(myRecyclerViewAdapter);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerView.setItemAnimator(itemAnimator);

        btnGalleryPickMul = (Button) findViewById(R.id.btnGalleryPickMul);
        btnGalleryPickMul.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(Action.ACTION_MULTIPLE_PICK);
                startActivityForResult(i, 200);
            }
        });

        go = (Button) findViewById(R.id.go_button);
        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent startChatUntent = new Intent("android.intent.action.videogen");
                startChatUntent.putExtra("myimagespath", Environment.getExternalStorageDirectory().toString() + "/req_images");
                //Toast.makeText(getApplicationContext(), "" + recyclerView.getWidth(), Toast.LENGTH_LONG).show();
                startActivity(startChatUntent);
                finish();

            }
        });

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                firstItemPos = staggeredGridLayoutManager.findFirstCompletelyVisibleItemPositions(firstItemPos);
                lastItemPos = staggeredGridLayoutManager.findLastVisibleItemPositions(lastItemPos);

                currentImage.setImageBitmap(arrayList.get(firstItemPos[0]));
                /*if (newState == 0) {
                    DownloadFilesTask aft = new DownloadFilesTask();
                    aft.execute(all_path[lastItemPos[0]]);

                }*/
            }
        });

        recyclerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
            all_path = data.getStringArrayExtra("all_path");

            if (all_path.length > 0) {

                Bitmap bm = Bitmap.createBitmap(400, 400, Bitmap.Config.RGB_565);
                bm.eraseColor(Color.GRAY);

                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/req_images");
                myDir.mkdirs();

                for (int i = 0; i < all_path.length; i++) {

                    /*String fname = "Image_" + i + ".png";
                    File file = new File(myDir, fname);

                    Bitmap bitmap = null;
                    Log.i("gag", "" + file);
                    if (file.exists())
                        file.delete();
                    try {
                        //bitmap = currectlyOrientation(all_path[i], 100, 100);
                        bitmap = imageLoader.loadImageSync("file://" + all_path[i]);
                        //bitmap = Utils.(bitmap, 500, 500);
                        FileOutputStream out = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 50, out);

                        out.flush();
                        out.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/


                    arrayList.add(bm);

                }

                DownloadFilesTask dtt = new DownloadFilesTask();
                dtt.execute(all_path);

                //myRecyclerViewAdapter.notifyDataSetChanged();
                btnGalleryPickMul.setBackgroundResource(R.drawable.add1);
                //Toast.makeText(getApplicationContext(),""+all_path.length,Toast.LENGTH_LONG).show();
            }
        }
    }

    public Bitmap scaleCenterCrop(Bitmap source, int newHeight, int newWidth) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();

        float xScale = (float) newWidth / sourceWidth;
        float yScale = (float) newHeight / sourceHeight;
        float scale = Math.max(xScale, yScale);

        float scaledWidth = scale * sourceWidth;
        float scaledHeight = scale * sourceHeight;

        float left = (newWidth - scaledWidth) / 2;
        float top = (newHeight - scaledHeight) / 2;

        RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);

        Bitmap dest = Bitmap.createBitmap(newWidth, newHeight, source.getConfig());
        Canvas canvas = new Canvas(dest);
        canvas.drawBitmap(source, null, targetRect, null);

        return dest;
    }

    private class DownloadFilesTask extends AsyncTask<String[], Integer, ArrayList<Bitmap>> {
        protected ArrayList<Bitmap> doInBackground(String[]... path) {
            ArrayList<Bitmap> arr1 = new ArrayList<Bitmap>();

            if (path[0].length > 0) {
                for (int i = 0; i < path[0].length; i++) {

                    BitmapFactory.Options opt = new BitmapFactory.Options();
                    opt.inSampleSize = 4;
                    Bitmap bg = null;
                    try {
                        bg = Utils.currectlyOrientation(path[0][i]);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    arrayList.add(Bitmap.createScaledBitmap(bg, 500, 500, true));
                    //Example.addBitmap(String.valueOf(i), bmUpRightPartial);
                    publishProgress(i);
                    //bg.recycle();
                }
            }
            return arrayList;
        }

        protected void onProgressUpdate(Integer... progress) {

            Bitmap bitmap = arrayList.get(progress[0]);
            arrayList.set(progress[0], bitmap);
            myRecyclerViewAdapter.notifyDataSetChanged();
        }

        protected void onPostExecute(ArrayList<Bitmap> result) {
        }
    }
}
