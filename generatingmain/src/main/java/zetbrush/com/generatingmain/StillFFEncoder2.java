package zetbrush.com.generatingmain;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;

import java.io.File;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by Arman on 5/26/15.
 */
public class StillFFEncoder2 extends AsyncTask<Integer,Integer,Integer> implements ICommandProvider {

    private static final String TAG = "StillFFENCODER";
    boolean checker = false;
    File workingforpath = null;
    File img = null;
    String outputFold="";
    Context _ctx;
    private final Set<IThreadCompleteListener> listeners = new CopyOnWriteArraySet<IThreadCompleteListener>();



    public StillFFEncoder2(Context ctx){
        this._ctx = ctx;
    }

    public StillFFEncoder2(File file, Context ctx){
        workingforpath=file;
        this._ctx=ctx;
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

    @Override
    protected Integer doInBackground(final Integer... params) {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);
        int vidcnt = 1;
        if (params[0] < 1) {
            params[0] = 1;
        }


        try {
            //  execFFmpegBinary(new FFmpeg(MainActivity.this),commandStill);
            final int[]i = new int[1];
            img = new File(workingforpath.getParentFile().getPath() + "/image_" + String.format("%03d", i[0]) + ".jpg");

            String commandSt = getCommand(img.getAbsolutePath(),String.valueOf(params[0]),String.valueOf(i[0]));

            String commandStill = "-y -loop 1 -i " +
                    img.getAbsolutePath() +
                    " -t " + params[0] + " " +
                    Environment.getExternalStorageDirectory().getPath() + "/still_" + i[0] + ".mp4";

            FFmpeg ffmg = new FFmpeg(this._ctx);
            try {
                ffmg.execute(commandSt, new ExecuteBinaryResponseHandler() {
                    @Override
                    public void onFailure(String s) {

                        Log.i("FFMPEGG", "FAILED with output : " + s);
                    }

                    @Override
                    public void onSuccess(String s) {

                        Log.i("FFMPEGG", "SUCCESS with output : " + s);


                    }

                    @Override
                    public void onProgress(String s) {
                        Log.d(TAG, "onProgress : ffmpeg " + s);


                        publishProgress(i[0]);
                        // progressDialog.setMessage("Processing\n" + s);
                    }

                    @Override
                    public void onStart() {


                        Log.d(TAG, "Started command : ffmpeg ");
                        //  progressDialog.setMessage("Processing...");
                        // progressDialog.show();
                    }

                    @Override
                    public void onFinish() {
                        Log.d(TAG, "Finished command : ffmpeg ");
                        i[0]++;
                        File img = new File(workingforpath.getParentFile().getPath() + "/image_" + String.format("%03d", i[0]) + ".jpg");

                        if (img.exists()) {
                            checker = true;
                            String commandStill = "-y -r 4 -loop 1 -i " +
                                    img.getAbsolutePath() +
                                    " -t " + params[0] + " " +
                                    Environment.getExternalStorageDirectory().getPath() + "/still_" + i[0] + ".mp4";
                            if (checker) {

                                try {
                                    new FFmpeg(StillFFEncoder2.this._ctx).execute(getCommand(img.getAbsolutePath(),String.valueOf(params[0]), String.valueOf(i[0])), this);
                                } catch (FFmpegCommandAlreadyRunningException e) {
                                    e.printStackTrace();
                                }


                            }

                        }else{
                            checker = false;
                            notifyListeners(2); //notyfing

                            // progressDialog.dismiss();
                        }
                    }
                });

            } catch (FFmpegCommandAlreadyRunningException e) {
                e.printStackTrace();

            }




        } catch (Exception e) {
            Log.e(TAG, "IO", e);
        }
        return 2; // finished code
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        outputFold = Environment.getExternalStorageDirectory().getAbsolutePath()+"/req_images/transitions";
        File out = new File(outputFold);
        if(!out.exists()){
            out.mkdirs();
        }

    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (!values[0].equals(null))
            Log.d("conf. ", "" + values[0]);
    }

    @Override
    protected void onPostExecute(Integer result) {
        Log.d("stillFFencoder"," is ready");



    }

    @Override
    public String getCommand(String...param) {
        return "-y -loop 1 -i " +
                param[0] +
                " -t " + param[1] + " "+" -preset ultrafast -bsf:v h264_mp4toannexb " +
                outputFold + "/still_" + param[2] + ".ts";
    }


}