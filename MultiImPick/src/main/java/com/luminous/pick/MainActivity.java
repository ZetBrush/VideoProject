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
                startChatUntent.putExtra("myimagespath",Environment.getExternalStorageDirectory().toString()+"/req_images");
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
                if(newState==0) {
                    DownloadFilesTask aft = new DownloadFilesTask();
                    aft.execute(all_path[lastItemPos[0]]);

                }
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

           // Bitmap bm=decodeSampledBitmapFromResource(file1[0],0,200,200);




            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root + "/req_images");
            myDir.mkdirs();

            for (int i=0;i<all_path.length;i++) {

                String fname = "Image_" + i + ".png";
                File file = new File(myDir, fname);

                Bitmap bitmap = null;
                Log.i("gag", "" + file);
                if (file.exists())
                    file.delete();
                try {
                    bitmap = currectlyOrientation(all_path[i], 100, 100);
                    FileOutputStream out = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 50, out);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                arrayList.add(bitmap);

            }

            /*for (String string : all_path) {

                final int THUMBSIZE = 100;

                Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(string),
                        THUMBSIZE, THUMBSIZE);


                Bitmap bitmap = null;
                try {
                    bitmap = currectlyOrientation(string, 100, 100);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                arrayList.add(bitmap);
            }*/

            Bitmap tk = null;
            try {
                tk = currectlyOrientation(all_path[0], 400, 400);
            } catch (IOException e) {
                e.printStackTrace();
            }

            currentImage.setImageBitmap(tk);
            myRecyclerViewAdapter.notifyDataSetChanged();
            btnGalleryPickMul.setBackgroundResource(R.drawable.add1);
            //Toast.makeText(getApplicationContext(),""+all_path.length,Toast.LENGTH_LONG).show();
        }
    }

    public Bitmap scaleCenterCrop(Bitmap source, int newHeight, int newWidth) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();

        // Compute the scaling factors to fit the new height and width, respectively.
        // To cover the final image, the final scaling will be the bigger
        // of these two.
        float xScale = (float) newWidth / sourceWidth;
        float yScale = (float) newHeight / sourceHeight;
        float scale = Math.max(xScale, yScale);

        // Now get the size of the source bitmap when scaled
        float scaledWidth = scale * sourceWidth;
        float scaledHeight = scale * sourceHeight;

        // Let's find out the upper left coordinates if the scaled bitmap
        // should be centered in the new size give by the parameters
        float left = (newWidth - scaledWidth) / 2;
        float top = (newHeight - scaledHeight) / 2;

        // The target rectangle for the new, scaled version of the source bitmap will now
        // be
        RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);

        // Finally, we create a new bitmap of the specified size and draw our new,
        // scaled bitmap onto it.
        Bitmap dest = Bitmap.createBitmap(newWidth, newHeight, source.getConfig());
        Canvas canvas = new Canvas(dest);
        canvas.drawBitmap(source, null, targetRect, null);

        return dest;
    }

    public Bitmap currectlyOrientation(String file, int newHeight, int newWidth) throws IOException {

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
        rotatedBitmap = scaleCenterCrop(rotatedBitmap, newHeight, newWidth);

        return rotatedBitmap;
    }

    private class DownloadFilesTask extends AsyncTask<String, Integer, Bitmap> {
        protected Bitmap doInBackground(String... path) {
            Bitmap tk = null;
            try {
                tk = currectlyOrientation(path[0], 400, 400);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return tk;
        }

        @Override

        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected void onPostExecute(Bitmap result) {
            currentImage.setImageBitmap(result);
        }
    }

    public static String encodeTobase64(Bitmap image) {
        Bitmap immagex = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immagex.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        Log.e("LOOK", imageEncoded);
        return imageEncoded;
    }

    public static Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

}
