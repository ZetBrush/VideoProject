package com.luminous.pick;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
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
import android.view.Menu;
import android.view.MenuInflater;
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
    private ProgressDialog pd;

    private LinkedList<String> pathlist;

    private int arrayLength = 0;
    private SharedPreferences sharedPreferences;

    private static final String root = Environment.getExternalStorageDirectory().toString();
    private File myDir = new File(root + "/req_images");


    Button playBut;

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


        playBut = (Button) findViewById(R.id.bt);
        sharedPreferences = getPreferences(MODE_PRIVATE);

        pathlist = new LinkedList<>();
        /*TextView txt = (TextView) findViewById(R.id.selected_count);
        txt.setVisibility(View.GONE);*/

        recyclerView = (RecyclerView) findViewById(R.id.rec_test);
        currentImage = (ImageView) findViewById(R.id.image_id);
        btnGalleryPickMul = (Button) findViewById(R.id.btnGalleryPickMul);
        myRecyclerViewAdapter = new MyRecyclerViewAdapter(arrayList, pathlist, currentImage, btnGalleryPickMul);
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

        /*next = (Button) findViewById(R.id.go_button);
        next.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (arrayList.size() > 0) {

                    myDir.mkdirs();


                    //Toast.makeText(getApplicationContext(), pathlist.size()+"", Toast.LENGTH_SHORT).show();
                    SaveToMemary saveToMemary = new SaveToMemary();
                    saveToMemary.execute(pathlist);

                    intent = new Intent("android.intent.action.videogen");
                    intent.putExtra("myimagespath", myDir.toString());
                    //startActivity(intent);
                    //finish();
                } else {
                    Toast.makeText(getApplicationContext(), "you have no image", Toast.LENGTH_SHORT).show();
                }
                //foo(getApplicationContext());
            }
        });*/

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

        playBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*for (int i = 0; i < arrayList.size(); i++) {
                    currentImage.setImageBitmap(arrayList.get(i));
                }*/
                startService(new Intent(getApplicationContext(), MyService.class));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
            all_path = data.getStringArrayExtra("all_path");

            if (all_path.length > 0) {

                arrayLength = sharedPreferences.getInt("length", 0);
                //System.gc();
                Bitmap bm = Bitmap.createBitmap(10, 10, Bitmap.Config.RGB_565);
                bm.eraseColor(Color.LTGRAY);

                for (int i = 0; i < all_path.length; i++) {

                    pathlist.add(all_path[i]);
                    arrayList.add(bm);
                }

                //System.gc();
                DownloadFilesTask dtt = new DownloadFilesTask();
                dtt.execute(pathlist);
                Toast.makeText(getApplicationContext(), "" + pathlist.size(), Toast.LENGTH_SHORT).show();
            }
            //btnGalleryPickMul.setVisibility(View.VISIBLE);
            //next.setVisibility(View.VISIBLE);
        }
    }

    private class DownloadFilesTask extends AsyncTask<LinkedList<String>, Integer, ArrayList<Bitmap>> {
        protected ArrayList<Bitmap> doInBackground(LinkedList<String>... path) {

            if (path[0].size() > 0) {
                for (int i = 0; i < path[0].size(); i++) {

                    Bitmap bitmap = null;
                    try {
                        bitmap = Utils.currectlyOrientation(path[0].get(i), 300, 300);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    bitmap = Utils.scaleCenterCrop(bitmap, 300, 300);
                    arr1.add(bitmap);
                    publishProgress(i);
                    Log.d("path[0] length" + i, "" + path[0].size());
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
            arr1.removeAll(arr1);
            //setBadge(getApplicationContext(),0);
            Toast.makeText(getApplicationContext(), "done", Toast.LENGTH_SHORT).show();
        }
    }

    private class SaveToMemary extends AsyncTask<LinkedList<String>, Integer, Void> {

        protected Void doInBackground(LinkedList<String>... path) {

            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE);
            Log.d("arralist 1 ", " " + arrayList.size());
            for (int i = 0; i < path[0].size(); i++) {

                String fname = "image_" + String.format("%03d", i) + ".png";

                try {
                    File file = new File(myDir, fname);
                    Bitmap bitmap = null;
                    if (file.exists())
                        file.delete();
                    bitmap = Utils.currectlyOrientation(path[0].get(i), 700, 700);
                    bitmap = Utils.scaleCenterCrop(bitmap, 700, 700);
                    FileOutputStream out = new FileOutputStream(file);

                    bitmap.compress(Bitmap.CompressFormat.PNG, 50, out);
                    setBadge(getApplicationContext(), i);
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
                setBadge(getApplicationContext(), 0);
                startActivity(intent);
                overridePendingTransition(R.transition.fade_in, R.transition.fade_out);
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

    public void foo(Context context) {
        Intent intent = new Intent();

        intent.setAction("com.sonyericsson.home.action.UPDATE_BADGE");
        intent.putExtra("com.sonyericsson.home.intent.extra.badge.ACTIVITY_NAME", "com.luminous.pick.MainActivity");
        intent.putExtra("com.sonyericsson.home.intent.extra.badge.SHOW_MESSAGE", true);
        intent.putExtra("com.sonyericsson.home.intent.extra.badge.MESSAGE", "99");
        intent.putExtra("com.sonyericsson.home.intent.extra.badge.PACKAGE_NAME", "com.luminous.pick");

        sendBroadcast(intent);
        Toast.makeText(getApplicationContext(), "badge", Toast.LENGTH_SHORT).show();
    }

    private class Down extends AsyncTask<Bitmap, Integer, Void> {
        protected Void doInBackground(Bitmap... path) {
            currentImage.setImageBitmap(path[0]);
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
