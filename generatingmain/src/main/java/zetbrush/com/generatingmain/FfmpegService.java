package zetbrush.com.generatingmain;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Arman on 4/22/15.
 */
public class FfmpegService extends Service {

    private FfmpegService ffmpeg;

    public FfmpegService(){}

    public FfmpegService getInstance(){
        synchronized (FfmpegService.class){
            if(ffmpeg==null){
                ffmpeg = new FfmpegService();
            }
        }
        return ffmpeg;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.


        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();

    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
