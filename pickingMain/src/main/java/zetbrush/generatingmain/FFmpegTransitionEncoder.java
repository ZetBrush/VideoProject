package zetbrush.generatingmain;

import android.os.Environment;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;

import java.io.File;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by Arman on 4/22/15.
 */
public class FFmpegTransitionEncoder extends ModernAsyncTask<Integer, Integer, Integer> implements ICommandProvider {
    static int vidcount;
    static int imageCount;
    private final android.content.Context ctx;
    ProgressHandler progressHandler;
    IProgress prg;

    private static final String TAG = "ffencoder";


    public FFmpegTransitionEncoder(android.content.Context ctx, ProgressHandler prhandler) {
        this.ctx = ctx;
        this.progressHandler = prhandler;
    }


    private final Set<IThreadCompleteListener> listeners
            = new CopyOnWriteArraySet<IThreadCompleteListener>();
    public final void addListener(final IThreadCompleteListener listener) {
        listeners.add(listener);
    }
    public void setProgressListener( IProgress prg){
        this.prg = prg;
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
    protected Integer doInBackground(Integer... params) {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);


        final int[] counter = {0};
        final String dirNm = Environment.getExternalStorageDirectory().getAbsolutePath()+"/req_images/ts";
        final String outputName = Environment.getExternalStorageDirectory().getAbsolutePath()+"/req_images/transitions/trans";



        File dir = new File(dirNm+ ++counter[0]);
        if(dir.exists()){
            File outputTransF = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/req_images/transitions");
            if(!outputTransF.exists()){
                outputTransF.mkdirs();
            }
            String vidNm = outputName+counter[0];
            final FFmpeg encoder= new FFmpeg(ctx);

            try {
                encoder.execute(getCommand(dir.getAbsolutePath(),vidNm ), new FFmpegExecuteResponseHandler() {
                    @Override
                    public void onSuccess(String message) {
                        File dir = new File(dirNm + ++counter[0]);
                        String vidNm = outputName+counter[0];
                        publishProgress(counter[0]);
                        if (dir.exists()) {
                            try {
                                encoder.execute(getCommand(dir.getAbsolutePath(),vidNm),this);
                            } catch (FFmpegCommandAlreadyRunningException e) {
                                e.printStackTrace();
                            }

                        }else {
                             notifyListeners(3); /// notyfing when all chain of enoding transactions are finished
                            Log.d(TAG, message);
                        };
                    }

                    @Override
                    public void onProgress(String message) {
                        Log.d(TAG,message);
                    }

                    @Override
                    public void onFailure(String message) {
                        Log.d(TAG,message);
                    }

                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onFinish() {
                        Log.d(TAG, "Finished!!");
                    }
                });
            } catch (FFmpegCommandAlreadyRunningException e) {
                e.printStackTrace();
            }


        }


        return 1; //Transition Ready CODE

    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (!values[0].equals(null)) {
            String tmp = (int) (((float) values[0] / (imageCount * 24)) * 100) + "%";
            prg.progress(progressHandler.updateProgress(imageCount),"Adding Transitions");
            //progress.setText(tmp);
        }
    }



    @Override
    protected void onPostExecute(Integer result) {
        //progress.setText("wait..");

    }


    @Override
    public String getCommand(String... param) {
        return "-y -i " +param[0] + "/image_%05d.jpg -r 25 -preset ultrafast -bsf:v h264_mp4toannexb "+ param[1]+".ts";
    }
}
