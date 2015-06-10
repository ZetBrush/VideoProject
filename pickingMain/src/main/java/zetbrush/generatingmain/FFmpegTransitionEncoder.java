package zetbrush.generatingmain;

import android.os.Environment;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;

import java.io.File;

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


    public FFmpegTransitionEncoder(android.content.Context ctx, ProgressHandler prhandler,IThreadCompleteListener listener) {
        this.ctx = ctx;
        this.progressHandler = prhandler;
        this.listener=listener;
    }


    private final IThreadCompleteListener listener;


    public void setProgressListener( IProgress prg){
        this.prg = prg;
    }



    private final void notifyListener(int code) {

            this.listener.notifyOfThreadComplete(code);

    }

    @Override
    protected Integer doInBackground(Integer... params) {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);

        imageCount=params[0];
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

                        if (dir.exists()) {

                            try {
                                encoder.execute(getCommand(dir.getAbsolutePath(),vidNm),this);
                                publishProgress(counter[0]);
                            } catch (FFmpegCommandAlreadyRunningException e) {
                                e.printStackTrace();
                            }

                        }else {
                             notifyListener(3); /// notyfing when all chain of enoding transactions are finished
                            Log.d(TAG, message);
                        }
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
            Log.d("Adding transitions","prg.progress( "+ progressHandler.updateProgress(imageCount) + "  imageCount "+imageCount+"values..."+ values[0]);
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
