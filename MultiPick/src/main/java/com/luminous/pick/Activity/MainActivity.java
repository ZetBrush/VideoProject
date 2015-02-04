package com.luminous.pick.Activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.*;
import android.os.Process;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.luminous.pick.Utils.Action;
import com.luminous.pick.Adapter.MyRecyclerViewAdapter;
import com.luminous.pick.R;
import com.luminous.pick.Utils.Utils;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class MainActivity extends Activity {


    private ImageView currentImage;
    private RecyclerView recyclerView;
    private Button btnGalleryPickMul;
    private Button playBut;
    private ProgressDialog pd;
    private SeekBar seekBar;
    ImageView gic;

    private MyRecyclerViewAdapter myRecyclerViewAdapter;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;
    private RecyclerView.ItemAnimator itemAnimator;
    private LinearLayoutManager linearLayoutManager;

    private ArrayList<Bitmap> arrayList = new ArrayList<>();
    private ArrayList<Bitmap> arr1 = new ArrayList<>();
    private LinkedList<String> pathlist = new LinkedList<>();

    private int[] firstItemPos;
    private int[] lastItemPos;
    private String[] all_path;

    private ImageLoader imageLoader;
    private Intent intent = null;
    private int arrayLength = 0;
    private SharedPreferences sharedPreferences;
    private SlideShow slideShow;
    private Boolean playButtonIsSelected = false;

    private static final String root = Environment.getExternalStorageDirectory().toString();
    private File myDir = new File(root + "/req_images");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.show();

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

        recyclerView = (RecyclerView) findViewById(R.id.rec_test);
        currentImage = (ImageView) findViewById(R.id.image_id);
        btnGalleryPickMul = (Button) findViewById(R.id.btnGalleryPickMul);
        playBut = (Button) findViewById(R.id.bt);
        //seekBar = (SeekBar) findViewById(R.id.seek_bar);


        Bitmap bm = Bitmap.createBitmap(10, 200, Bitmap.Config.RGB_565);
        bm.eraseColor(Color.RED);
        //seekBar.setThumb(new BitmapDrawable(bm));
        gic = (ImageView) findViewById(R.id.gic);
        gic.setImageBitmap(bm);
        gic.setVisibility(View.GONE);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = displaymetrics.widthPixels;
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, width);

        currentImage.setLayoutParams(layoutParams);

        myRecyclerViewAdapter = new MyRecyclerViewAdapter(arrayList, pathlist, currentImage, btnGalleryPickMul);
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL);   // staggered grid
        linearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        itemAnimator = new DefaultItemAnimator();

        //recyclerView.addItemDecoration(new SpacesItemDecoration(5));
        recyclerView.setAdapter(myRecyclerViewAdapter);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(itemAnimator);

        btnGalleryPickMul.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(Action.ACTION_MULTIPLE_PICK);
                startActivityForResult(i, 200);
            }
        });

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);


                /*if (myRecyclerViewAdapter.getItemCount() > 0) {
                    firstItemPos = staggeredGridLayoutManager.findFirstCompletelyVisibleItemPositions(firstItemPos);
                    lastItemPos = staggeredGridLayoutManager.findLastVisibleItemPositions(lastItemPos);


                    if (arrayList.size() > 0) {
                        currentImage.setImageBitmap(arrayList.get(firstItemPos[0]));
                    }
                }*/
            }
        });

        recyclerView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

            }
        });

        playBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(),linearLayoutManager.getFocusedChild().getWidth() +"",Toast.LENGTH_SHORT).show();
                if (playButtonIsSelected == false) {
                    playButtonIsSelected = true;
                    slideShow = new SlideShow();
                    slideShow.execute();

                } else {
                    playButtonIsSelected = false;
                    slideShow.cancel(true);
                    Toast.makeText(getApplicationContext(), "stoped", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
            all_path = data.getStringArrayExtra("all_path");

            if (all_path.length > 0) {

                gic.setVisibility(View.VISIBLE);
                arrayLength = sharedPreferences.getInt("length", 0);
                Bitmap bm = Bitmap.createBitmap(10, 10, Bitmap.Config.RGB_565);
                bm.eraseColor(Color.LTGRAY);

                for (int i = 0; i < all_path.length; i++) {

                    pathlist.add(all_path[i]);
                    arrayList.add(bm);
                }

                DownloadFilesTask dtt = new DownloadFilesTask();
                dtt.execute(all_path);
                //Toast.makeText(getApplicationContext(), "" + pathlist.size(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class DownloadFilesTask extends AsyncTask<String[], Integer, ArrayList<Bitmap>> {
        protected ArrayList<Bitmap> doInBackground(String[]... path) {

            if (path[0].length > 0) {
                for (int i = 0; i < path[0].length; i++) {

                    Bitmap bitmap = null;
                    try {
                        bitmap = Utils.currectlyOrientation(path[0][i], 400, 400);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    bitmap = Utils.scaleCenterCrop(bitmap, 400, 400);
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

    private class SlideShow extends AsyncTask<Void, Integer, Void> {
        protected Void doInBackground(Void... path) {

            for (int i = 0; i < arrayList.size(); i++) {
                for (int j = 110; j > 0; j--) {
                    if (isCancelled()) return null;

                    final int finalI = i;
                    final int finalJ = j;
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            linearLayoutManager.scrollToPositionWithOffset(finalI + 1, finalJ * 2);
                            //recyclerView.smoothScrollToPosition(finalI);
                            currentImage.setImageBitmap(arrayList.get(finalI));
                        }
                    });
                    try {
                        TimeUnit.MILLISECONDS.sleep(8);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            playButtonIsSelected = false;
            super.onPostExecute(aVoid);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_next:

                if (arrayList.size() > 0) {
                    myDir.mkdirs();
                    SaveToMemary saveToMemary = new SaveToMemary();
                    saveToMemary.execute(pathlist);
                    intent = new Intent("android.intent.action.videogen");
                    intent.putExtra("myimagespath", myDir.toString());
                } else {
                    Toast.makeText(getApplicationContext(), "you have no image", Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        arrayList.removeAll(arrayList);
        pathlist.removeAll(pathlist);
        super.onDestroy();
    }
}
