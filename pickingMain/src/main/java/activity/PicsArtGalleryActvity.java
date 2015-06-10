package activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.picsart.api.LoginManager;
import com.picsart.api.Photo;
import com.picsart.api.PicsArtConst;
import com.picsart.api.RequestListener;
import com.picsart.api.UserController;
import com.picsartvideo.R;

import java.util.ArrayList;

import adapter.PicsArtGalleryAdapter;
import item.PicsArtGalleryItem;
import utils.FileUtils;
import utils.SpacesItemDecoration;

public class PicsArtGalleryActvity extends ActionBarActivity {

    private ProgressBar progressBar;
    private RecyclerView recyclerView;

    private StaggeredGridLayoutManager staggeredGridLayoutManager;
    private RecyclerView.ItemAnimator itemAnimator;
    private PicsArtGalleryAdapter picsArtGalleryAdapter;

    private ArrayList<Photo> photos = new ArrayList<>();
    private ArrayList<PicsArtGalleryItem> picsArtGalleryItems = new ArrayList<>();
    SharedPreferences sharedPreferences;
    UserController userController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pics_art_gallery_actvity);

        init();
        Log.d("onCreat thread", Thread.currentThread().getName() + " " + Thread.currentThread().getId());

         sharedPreferences = this.getSharedPreferences("pics_art_video", MODE_PRIVATE);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!LoginManager.getInstance().hasValidSession(this)) {
            LoginManager.getInstance().openSession(PicsArtGalleryActvity.this, new RequestListener(0) {
                @Override
                public void onRequestReady(int reqnumber, String message) {
                    if (reqnumber == 7777) {

                        Log.d("first onReq", Thread.currentThread().getName() + " " + Thread.currentThread().getId());

                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                userController = new UserController(PicsArtGalleryActvity.this);
                                userController.setListener(dloadTriger);
                                userController.requestUserPhotos("me", 0, Integer.MAX_VALUE);

                            }
                        }).start();

                    }
                }
            });


        } else {
            if (sharedPreferences.getBoolean("isopen", false) == false) {

                userController = new UserController(PicsArtGalleryActvity.this);
                userController.requestUserPhotos("me", 0, UserController.MAX_LIMIT);
                userController.setListener(dloadTriger);


            }else{
                FileUtils.readListFromJson(PicsArtGalleryActvity.this, picsArtGalleryItems, "myfile.json");
                picsArtGalleryAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }


        }
    }
    final int DLTRIGGER = 852258;

    RequestListener dloadTriger = new RequestListener(DLTRIGGER) {
        @Override
        public void onRequestReady(int i, String s) {
            new AsyncTask<Integer, Void, Void>() {
                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    progressBar.post(new Runnable() {
                        @Override
                        public void run() {
                            picsArtGalleryAdapter.notifyDataSetChanged();
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }

                @Override
                protected Void doInBackground(Integer... integers) {
                    photos = userController.getPhotos();
                    for (int j = 0; j < photos.size(); j++) {
                        PicsArtGalleryItem picsArtGalleryItem = new PicsArtGalleryItem(photos.get(j).getUrl(),
                                photos.get(j).getWidth(), photos.get(j).getHeight(), false, false);
                        picsArtGalleryItems.add(picsArtGalleryItem);

                    }
                    FileUtils.writeListToJson(PicsArtGalleryActvity.this, picsArtGalleryItems, "myfile.json");
                    Log.d("onRun thread", Thread.currentThread().getName() + " " + Thread.currentThread().getId());


                    return null;
                }
            }.execute();
        }
    };



    private void init() {

        PicsArtConst.CLIENT_ID = "PicsArt_Videos87qaKs7n1l8IeKKJ";
        PicsArtConst.CLIENT_SECRET = "1q8UtXc1rLGO3SQI67LUg9eLOtgn259w";
        PicsArtConst.REDIRECT_URI = "localhost";
        PicsArtConst.GRANT_TYPE = "authorization_code";

        getSupportActionBar().setTitle("PicsArt Videos");

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = displaymetrics.widthPixels;

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        recyclerView = (RecyclerView) findViewById(R.id.pics_art_rec_view);

        picsArtGalleryAdapter = new PicsArtGalleryAdapter(picsArtGalleryItems, this, width, getSupportActionBar());
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        itemAnimator = new DefaultItemAnimator();

        recyclerView.setHasFixedSize(true);
        recyclerView.setClipToPadding(true);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerView.setItemAnimator(itemAnimator);
        recyclerView.setAdapter(picsArtGalleryAdapter);
        recyclerView.addItemDecoration(new SpacesItemDecoration(2));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_pics_art_gallery_actvity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            if (picsArtGalleryAdapter.getSelected().size() < 1) {
                Toast.makeText(getApplicationContext(), "no images selected", Toast.LENGTH_LONG).show();
            } else {

                SharedPreferences sharedPreferences = this.getSharedPreferences("pics_art_video", MODE_PRIVATE);
                if (sharedPreferences.getBoolean("custom_gallery_isopen", false) == true || sharedPreferences.getBoolean("pics_art_gallery_isopen", false) == true) {
                    Intent data = new Intent().putExtra("image_paths", picsArtGalleryAdapter.getSelected());
                    setResult(RESULT_OK, data);
                } else {
                    Intent intent = new Intent(PicsArtGalleryActvity.this, SlideShowActivity.class);
                    intent.putCharSequenceArrayListExtra("image_paths", picsArtGalleryAdapter.getSelected());
                    startActivity(intent);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("pics_art_gallery_isopen", true);
                    editor.commit();
                }
                finish();
            }

            return true;
        }
        if (id == R.id.logout) {
            LoginManager.getInstance().closeSession(PicsArtGalleryActvity.this);
            Toast.makeText(getApplicationContext(), "logged out", Toast.LENGTH_LONG).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("pics_art_video", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isopen", true);
        editor.commit();
        super.onDestroy();
    }

}
