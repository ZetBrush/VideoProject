package zetbrush.generatingmain;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.*;
import android.util.Log;

import java.io.File;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by Arman on 5/26/15.
 */
public class TransitionFrameGen extends ModernAsyncTask<File, Integer, Integer> {

    private static final String TAG = "ffencoder";
    private Context _ctx;
    int currentEffect;
    int vidcount = 1;
    private final Set<IThreadCompleteListener> listeners = new CopyOnWriteArraySet<IThreadCompleteListener>();
    IThreadCompleteListener listener;
    int imageCount;
    ProgressHandler prhandler;
    IProgress prgr;

 public TransitionFrameGen(Context ctx, IThreadCompleteListener lis, int currEffect,ProgressHandler prhand, int imageCount){
    this._ctx=ctx;
    this.currentEffect=currEffect;
    this.listener=lis;
     this.imageCount=imageCount;
     this.prhandler= prhand;
 }
    public final void addListener(final IThreadCompleteListener listener) {
        listeners.add(listener);
    }
    public final void removeListener(final IThreadCompleteListener listener) {
        listeners.remove(listener);
    }
    private final void notifyListeners(int code) {
        for (IThreadCompleteListener listener : listeners) {
            listener.notifyOfThreadComplete(code);
        }
    }
    public void setProgressListener( IProgress prg){
        this.prgr = prg;
    }

    protected Integer doInBackground(File... params) {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);

        try {


            int imgallcounter = 0;
            int transcounter = 0;
            int imagecounter = 0;
            String dirNm = params[0].getParentFile().getPath();
            Effects.EFFECT ef = Effects.EFFECT.FADE;
            switch (currentEffect) {
                case 1:
                    ef= Effects.EFFECT.FADE;
                    break;
                case 2:
                    ef = Effects.EFFECT.SlIDE;
                    break;
                case 3:
                    ef= Effects.EFFECT.ROTATE;
                    break;
                default: ef = Effects.EFFECT.FADE;

            }

            while (true) {
                File filedir = new File(Environment.getExternalStorageDirectory().getPath() + "/req_images/ts"+vidcount);
                if(!filedir.exists())
                    filedir.mkdirs();

                File img = new File(dirNm + "/" + "image_" + String.format("%03d", imagecounter) + ".jpg");
                String inptFile = "image_" + String.format("%03d", imagecounter) + ".jpg";

                Bitmap btm = BitmapFactory.decodeFile(img.getAbsolutePath());


                Effects.builder(ef)
                        .generateFrames(btm, vidcount);

                publishProgress(transcounter);
                transcounter++;

                vidcount++;

                File imgg = new File(dirNm + "/image_" + String.format("%03d", imagecounter + 1) + ".jpg");
                if (!imgg.exists()) {
                    vidcount = 1;
                    notifyListeners(1); /// service thread complate key1
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
            Log.d("TransitionFrame"," "+values[0]);
            prgr.progress(prhandler.updateProgress((int) (((values[0]+1) / (imageCount * 1.0)) * 100)));
        }
    }
    @Override
    protected void onPostExecute(Integer result) {
        notifyListeners(1);
    }

}
