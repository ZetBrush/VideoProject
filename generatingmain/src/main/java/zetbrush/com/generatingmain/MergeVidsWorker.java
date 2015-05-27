package zetbrush.com.generatingmain;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;

/**
 * Created by Arman on 5/6/15.
 */
public class MergeVidsWorker extends AsyncTask<Integer, Integer, Integer> implements ICommandProvider {
    Context ctx;
    String stillPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/req_images/transitions/still_";
    String transVidpath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/req_images/transitions/trans";
    String outputVidName="";
    String audioPath="";

    public MergeVidsWorker(Context ctx,String outputnm,String audiopath){
        this.ctx = ctx;
        this.outputVidName=outputnm;
        this.audioPath=audiopath;
    }


    @Override
    protected Integer doInBackground(final Integer... params) {

        final FFmpeg mmpg = new FFmpeg(ctx);
        try {
            final boolean[] check = {true};
            mmpg.execute(getCommand(params[0].toString(), outputVidName), new FFmpegExecuteResponseHandler() {
                @Override
                public void onSuccess(String message) {

                    Log.d("Merging.....",message);
                }

                @Override
                public void onProgress(String message) {

                    Log.d("Merging.....",message);
                }

                @Override
                public void onFailure(String message)
                {
                    Log.d("Merging....Failure",message);
                }

                @Override
                public void onStart() {

                }

                @Override
                public void onFinish() {
                    Log.d("Merging.....","Finished!");
                   if(check[0] && (audioPath!=null || audioPath!="")) {
                        check[0] =false;
                       //String cmd = "-y -i video.mp4 -i inputfile.mp3 -ss 30 -t 70 -acodec copy -vcodec copy outputfile.mp4"     -ss "+params[1]+" -t "+params[2] +";
                        String cmd = "-i " + outputVidName + ".mp4 -i "+audioPath+" -map 0:0 -af afade=t=out:st="+(params[2]-params[1]-2)+ ":d=2 -map 1:0 -shortest " + outputVidName + "_m.mp4";
                        try {

                            mmpg.execute(cmd, this);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                    Toast.makeText(ctx,"Video is ready!",Toast.LENGTH_SHORT).show();
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }


        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);

    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

    }

    @Override
    public String getCommand(String... param) {
        Log.d("Merge Command","-i " + "concat:" +videosPathBuilder(Integer.valueOf(param[0])) + " -preset ultrafast "+ "-c copy "+param[1]+".mp4");
        return "-i " + "concat:" +videosPathBuilder(Integer.valueOf(param[0])) + " -preset ultrafast "+ "-c copy "+param[1]+".mp4";
    }


    private String videosPathBuilder(int count){

        StringBuilder sb = new StringBuilder("");
        int counter =1;
        while(count>0 ) {
            count--;

            sb.append(stillPath+(counter-1)+".ts");
            sb.append("|");
            sb.append(transVidpath+counter+".ts");
            counter++;
            if(count>0) sb.append("|");

        }
    Log.d("MERGE_INFO COMMAND", sb.toString());
        return sb.toString();
    }
}
