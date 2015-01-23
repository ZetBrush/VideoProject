package com.luminous.pick;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.*;
import android.os.Process;
import android.support.v7.widget.DefaultItemAnimator;
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
    private Intent intent = null;
    private GalAdapter adat;
    private ProgressDialog pd;

    private int arrayLength = 0;
    private SharedPreferences sharedPreferences;

    private static final String root = Environment.getExternalStorageDirectory().toString();
    private File myDir = new File(root + "/req_images");

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


        sharedPreferences = getPreferences(MODE_PRIVATE);

        TextView txt = (TextView) findViewById(R.id.selected_count);
        txt.setVisibility(View.GONE);
        adat = new GalAdapter(getApplicationContext(), imageLoader, arrayList, txt);

        recyclerView = (RecyclerView) findViewById(R.id.rec_test);
        currentImage = (ImageView) findViewById(R.id.image_id);
        btnGalleryPickMul = (Button) findViewById(R.id.btnGalleryPickMul);
        myRecyclerViewAdapter = new MyRecyclerViewAdapter(arrayList, currentImage, btnGalleryPickMul);
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL);   // staggered grid
        itemAnimator = new DefaultItemAnimator();

        recyclerView.setAdapter(myRecyclerViewAdapter);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerView.setItemAnimator(itemAnimator);


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
                    //foo(getApplicationContext());

            }
        });

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (myRecyclerViewAdapter.getItemCount() > 0) {
                    firstItemPos = staggeredGridLayoutManager.findFirstCompletelyVisibleItemPositions(firstItemPos);
                    lastItemPos = staggeredGridLayoutManager.findLastVisibleItemPositions(lastItemPos);

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


        arrayLength = sharedPreferences.getInt("length", 0);
        if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
            all_path = data.getStringArrayExtra("all_path");

            if (all_path.length > 0) {

                //setBadge(getApplicationContext(),all_path.length);

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
            }
            //btnGalleryPickMul.setVisibility(View.GONE);
            next.setVisibility(View.VISIBLE);
        }
    }

    private class DownloadFilesTask extends AsyncTask<String[], Integer, ArrayList<Bitmap>> {
        protected ArrayList<Bitmap> doInBackground(String[]... path) {

            if (path[0].length > 0) {
                for (int i = 0; i < path[0].length; i++) {

                    Bitmap bitmap = null;
                    try {
                        bitmap = Utils.currectlyOrientation(path[0][i], 300, 300);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    bitmap = Utils.scaleCenterCrop(bitmap, 300, 300);
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
            arrayList.set(arrayLength + progress[0], arr1.get(progress[0]));
            myRecyclerViewAdapter.notifyDataSetChanged();
        }

        protected void onPostExecute(ArrayList<Bitmap> result) {
            arr1.removeAll(arr1);
            //setBadge(getApplicationContext(),0);
            Toast.makeText(getApplicationContext(), "done", Toast.LENGTH_SHORT).show();
        }
    }

    private class SaveToMemary extends AsyncTask<String[], Integer, Void> {

        protected Void doInBackground(String[]... path) {

            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE);
            Log.d("arralist 1 ", " " + arrayList.size());
            for (int i = 0; i < path[0].length; i++) {

                String fname = "image_" + String.format("%03d", i) + ".png";

                try {
                    File file = new File(myDir, fname);
                    Bitmap bitmap = null;
                    if (file.exists())
                        file.delete();
                    bitmap = Utils.currectlyOrientation(path[0][i], 600, 600);
                    bitmap = Utils.scaleCenterCrop(bitmap, 700, 700);
                    FileOutputStream out = new FileOutputStream(file);

                    bitmap.compress(Bitmap.CompressFormat.PNG, 50, out);
                    setBadge(getApplicationContext(),i);
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
                if (pd != null)
                    pd.setMessage("Doing.. " + ((int) (((float) progress[0] / arrayList.size()) * 100)) + "%");
                // Toast.makeText(getApplicationContext(), (((float) progress[0] / arrayList.size()) * 100) + "%", Toast.LENGTH_SHORT).show();
            }
            Log.d("importing images", "image" + progress[0]);
        }

        protected void onPostExecute(Void result) {
            Toast.makeText(getApplicationContext(), "done", Toast.LENGTH_SHORT).show();
            if (pd != null) {
                pd.dismiss();
                arr1.removeAll(arr1);
                arrayList.removeAll(arrayList);
                myRecyclerViewAdapter.notifyDataSetChanged();
                currentImage.setImageBitmap(null);
                btnGalleryPickMul.setVisibility(View.VISIBLE);
                setBadge(getApplicationContext(),0);
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

            pd = new ProgressDialog(MainActivity.this);
            pd.setTitle("Processing...");
            pd.setMessage("Please wait.");
            pd.setCancelable(false);
            pd.setIndeterminate(true);
            pd.show();
        }
    }

    @Override
    protected void onPause() {
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putInt("length", arrayList.size());
        ed.apply();
        super.onPause();
    }

    public static void setBadge(Context context, int count) {

        String launcherClassName = getLauncherClassName(context);
        if (launcherClassName == null) {
            return;
        }

        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count", count);
        intent.putExtra("badge_count_package_name", context.getPackageName());
        intent.putExtra("badge_count_class_name", launcherClassName);
        context.sendBroadcast(intent);
    }

    public static String getLauncherClassName(Context context) {

        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;
            if (pkgName.equalsIgnoreCase(context.getPackageName())) {
                String className = resolveInfo.activityInfo.name;
                return className;
            }
        }
        return null;
    }

    public  void foo(Context context){
        try {
            intent.setAction("com.sonyericsson.home.action.UPDATE_BADGE");
            intent.putExtra("com.sonyericsson.home.intent.extra.badge.ACTIVITY_NAME", getLauncherClassName(context));
            intent.putExtra("com.sonyericsson.home.intent.extra.badge.SHOW_MESSAGE", true);
            intent.putExtra("com.sonyericsson.home.intent.extra.badge.MESSAGE", 10);
            intent.putExtra("com.sonyericsson.home.intent.extra.badge.PACKAGE_NAME", context.getPackageName());
            sendBroadcast(intent);
        } catch (Exception localException) {
            Log.e("CHECK", "Sony : " + localException.getLocalizedMessage());
        }
    }
}
