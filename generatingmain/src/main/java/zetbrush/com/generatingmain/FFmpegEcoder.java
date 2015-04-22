package zetbrush.com.generatingmain;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.*;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Arman on 4/22/15.
 */
public class FFmpegEcoder extends AsyncTask<File, Integer, Integer> {
    static int vidcount;
    static int imageCount;

    private static final String TAG = "ffencoder";

    protected Integer doInBackground(File... params) {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);

        try {


            int imgallcounter = 0;
            int transcounter = 0;
            int imagecounter = 0;
            String dirNm = params[0].getParentFile().getPath();


            while (true) {

                File img = new File(dirNm + "/" + "image_" + String.format("%03d", imagecounter) + ".jpg");
                String inptFile = "image_" + String.format("%03d", imagecounter) + ".jpg";

                Bitmap btm = BitmapFactory.decodeFile(img.getAbsolutePath());
                int width = btm.getWidth();
                int height = btm.getHeight();
                Bitmap transBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                FileOutputStream out = null;


                for (int i = 0; i < 25; i++) {

                    String imgoutputName = "image_" + String.format("%03d", imgallcounter++) + ".jpg";
                    Util.copyFile(img.getParentFile().getPath(), inptFile, imgoutputName, Environment.getExternalStorageDirectory().getPath() + "/tempimgs");
                }

                for (int i = 0; i < 25; i++) {
                    Canvas canvas = new Canvas(transBitmap);
                    canvas.drawRGB(0, 0, 0);
                    final Paint paint = new Paint();
                    Matrix mx = new Matrix();
                    ///choosing effect
                    if (MainActivity.currentEffect == 1) {
                        paint.setAlpha((i + 1) * 4);
                        canvas.drawBitmap(btm, 0, 0, paint);
                    } else if (MainActivity.currentEffect == 2) {
                        canvas.drawBitmap(btm, 700 - i * (700 / 24), 0, paint);
                    } else if (MainActivity.currentEffect >= 3) {
                        mx.postRotate(90 - i * 10);
                        mx.postTranslate(150 + 700 - i * (700 / 24 + 50), 0);
                        canvas.concat(mx);
                        canvas.drawBitmap(btm, 0, 0, paint);

                    }
                    /// starting encode frame
                    // se.encodeNativeFrameForPartialEffect(BitmapUtil.fromBitmap(transBitmap));
                    out = null;
                    try {
                        File filename = new File(Environment.getExternalStorageDirectory().getPath() + "/tempimgs" + "/image_" + String.format("%03d", imgallcounter++) + ".jpg");
                        out = new FileOutputStream(filename);
                        transBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
                        // PNG is a lossless format, the compression factor (100) is ignored
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (out != null) {
                                out.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    publishProgress(transcounter);
                    transcounter++;
                }
                vidcount++;
                //se.finish();
                System.gc();
                File imgg = new File(dirNm + "/image_" + String.format("%03d", imagecounter + 1) + ".jpg");
                if (!imgg.exists()) {
                    vidcount = 1;
                    break;
                }
                imagecounter++;

            }

        } catch (Exception e) {
            Log.e(TAG, "IO", e);
        }

        return 0;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (!values[0].equals(null)) {
            String tmp = (int) (((float) values[0] / (imageCount * 24)) * 100) + "%";
            //progress.setText(tmp);
        }
    }
    @Override
    protected void onPostExecute(Integer result) {
        //progress.setText("wait..");


    }


}
