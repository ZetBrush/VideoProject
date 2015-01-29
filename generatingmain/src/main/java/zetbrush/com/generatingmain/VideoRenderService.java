package zetbrush.com.generatingmain;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Arman on 1/29/15.
 */
public class VideoRenderService extends IntentService {
    private int progress =0;



    public VideoRenderService(String name) {
        super(name);

    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
