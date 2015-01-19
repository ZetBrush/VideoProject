package com.luminous.pick;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.*;
import android.os.Process;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class MainActivity extends Activity {

    private ImageView currentImage;
    private RecyclerView recyclerView;
    private Button btnGalleryPickMul;
    private Button next;

    private MyRecyclerViewAdapter myRecyclerViewAdapter;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;
    private RecyclerView.ItemAnimator itemAnimator;

    private ArrayList<Bitmap> arrayList = new ArrayList<>();
    private ArrayList<Bitmap> arr1 = new ArrayList<Bitmap>();
    private ImageLoader imageLoader;

    private int[] firstItemPos;
    private int[] lastItemPos;
    private String[] all_path;
    Intent intent=null;
    GalAdapter adat;
     ProgressDialog pd;

    String root = Environment.getExternalStorageDirectory().toString();
    File myDir = new File(root + "/req_images");

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

        TextView txt=(TextView)findViewById(R.id.selected_count);
        txt.setVisibility(View.GONE);
        adat = new GalAdapter(getApplicationContext(), imageLoader, arrayList,txt);

        recyclerView = (RecyclerView) findViewById(R.id.rec_test);
        currentImage = (ImageView) findViewById(R.id.image_id);

        myRecyclerViewAdapter = new MyRecyclerViewAdapter(arrayList, currentImage);
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

        next = (Button) findViewById(R.id.go_button);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (arrayList.size() > 0) {

                    myDir.mkdirs();


                    SaveToMemary saveToMemary = new SaveToMemary();
                    saveToMemary.execute(all_path);

                    intent = new Intent("android.intent.action.videogen");
                    intent.putExtra("myimagespath", myDir.toString());

                    //startActivity(intent);
                    //finish();
                } else {
                    Toast.makeText(getApplicationContext(), "you have no image", Toast.LENGTH_SHORT).show();
                }

            }
        });

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (myRecyclerViewAdapter.getItemCount() > 0) {
                    firstItemPos = staggeredGridLayoutManager.findFirstCompletelyVisibleItemPositions(firstItemPos);
                    lastItemPos = staggeredGridLayoutManager.findLastVisibleItemPositions(lastItemPos);

                    //currentImage.setImageBitmap(myRecyclerViewAdapter.getCurrentItem());
                    if (arrayList.size() > 0) {
                        currentImage.setImageBitmap(arrayList.get(firstItemPos[0]));
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /*File file= new File(android.os.Environment.getExternalStorageDirectory()+ "/req_images");
        if(file.isDirectory())
        {
            //file.listFiles().length
        }*/

        if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
            all_path = data.getStringArrayExtra("all_path");

            if (all_path.length > 0) {


                System.gc();
                Bitmap bm = Bitmap.createBitmap(10, 10, Bitmap.Config.RGB_565);
                bm.eraseColor(Color.LTGRAY);

                for (int i = 0; i < all_path.length; i++) {

                    arrayList.add(bm);
                }

                System.gc();
                DownloadFilesTask dtt = new DownloadFilesTask();
                dtt.execute(all_path);
                Toast.makeText(getApplicationContext(), "" + all_path.length, Toast.LENGTH_SHORT).show();
                //myRecyclerViewAdapter.notifyDataSetChanged();
            }
            //btnGalleryPickMul.setVisibility(View.GONE);
            next.setVisibility(View.VISIBLE);
        }
    }

    private class DownloadFilesTask extends AsyncTask<String[], Integer, ArrayList<Bitmap>> {
        protected ArrayList<Bitmap> doInBackground(String[]... path) {

            if (path[0].length > 0) {
                for (int i = 0; i < path[0].length; i++) {

                    //Bitmap bitmap = BitmapFactory.decodeFile(path[0][i]);
                    //Bitmap bitmap=decodeSampledBitmapFromPath(path[0][i],640,640);
                    Bitmap bitmap = null;
                    try {
                        bitmap = Utils.currectlyOrientation(path[0][i],300,300);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    bitmap = Utils.scaleCenterCrop(bitmap, 300, 300);
                    //bitmap = Utils.scaleCenterCrop(bitmap, 640, 640);
                    //Toast.makeText(getApplicationContext(),""+arr1.size(),Toast.LENGTH_SHORT).show();
                    arr1.add(bitmap);
                    publishProgress(i);
                    Log.d("path[0] length" + i, "" + path[0].length);

                }
            }
            return arr1;
        }

        protected void onProgressUpdate(Integer... progress) {

            if (progress[0] == 0) {
                currentImage.setImageBitmap(arr1.get(0));
            }

            arrayList.set(progress[0], arr1.get(progress[0]));
            myRecyclerViewAdapter.notifyDataSetChanged();
        }

        protected void onPostExecute(ArrayList<Bitmap> result) {
            Toast.makeText(getApplicationContext(), "done", Toast.LENGTH_SHORT).show();
        }
    }

    private class SaveToMemary extends AsyncTask<String [], Integer, Void> {

        protected Void doInBackground(String [] ... path) {

            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE);
            Log.d("arralist 1 ", " " + arrayList.size());


            for (int i = 0; i<path[0].length; i++) {

                String fname = "image_" + String.format("%03d", i) + ".png";

                try {

                    File file = new File(myDir, fname);
                    Bitmap bitmap = null;
                    if (file.exists())
                        file.delete();
                    bitmap = Utils.currectlyOrientation(path[0][i],600,600);
                    bitmap=Utils.scaleCenterCrop(bitmap,700,700);
                    FileOutputStream out = new FileOutputStream(file);

                    bitmap.compress(Bitmap.CompressFormat.PNG, 50, out);
                    out.flush();
                    out.close();
                    bitmap.recycle();

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Error while SaveToMemory", Toast.LENGTH_SHORT).show();
                }
                publishProgress(i);
            }

            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            if (progress[0] % 3 == 0) {
                if(pd!=null)
                pd.setMessage("Doing.. "+(((float) progress[0] / arrayList.size()) * 100) + "%");
               // Toast.makeText(getApplicationContext(), (((float) progress[0] / arrayList.size()) * 100) + "%", Toast.LENGTH_SHORT).show();
            }
            Log.d("importing images", "image" + progress[0]);
        }

        protected void onPostExecute(Void result) {
            Toast.makeText(getApplicationContext(), "done", Toast.LENGTH_SHORT).show();
            if (pd!=null) {
                pd.dismiss();
                startActivity(intent);

            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            File dir = new File(Environment.getExternalStorageDirectory() + "/req_images");
            if (dir.isDirectory()) {
                String[] children = dir.list();
                for (int i = 0; i < children.length; i++) {
                    new File(dir, children[i]).delete();
                }
            }
            pd= new ProgressDialog(MainActivity.this);
            pd.setTitle("Processing...");
            pd.setMessage("Please wait.");
            pd.setCancelable(false);
            pd.setIndeterminate(true);
            pd.show();

        }
    }

    public static Bitmap decodeSampledBitmapFromPath(String path, int reqWidth,
                                                     int reqHeight) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap bmp = BitmapFactory.decodeFile(path, options);
        bmp = Utils.scaleCenterCrop(bmp, 640, 640);
        return bmp;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }
}
