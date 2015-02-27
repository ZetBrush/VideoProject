package com.luminous.pick.Sevice;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.luminous.pick.Activity.MainActivity;
import com.luminous.pick.R;
import com.luminous.pick.Utils.Utils;

import java.io.File;
import java.io.FileOutputStream;


/**
 * Created by intern on 1/27/15.
 */
public class MyService extends Service {

    private static final String root = Environment.getExternalStorageDirectory().toString();
    private File myDir = new File(root + "/req_images");
    private boolean isDone = false;
    private String[] paths;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private int id = 1;

    final String LOG_TAG = "myLogs";

    public boolean getBoolean() {
        return isDone;
    }

    public void onCreate() {
        super.onCreate();
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(getApplicationContext());

        mBuilder.setContentTitle("Download")
                .setContentText("Download in progress")
                .setSmallIcon(R.drawable.ic_action_download);

        mBuilder.setProgress(100, 0, false);
        mNotifyManager.notify(id, mBuilder.build());
        isDone = false;
        Log.d(LOG_TAG, "onCreate");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");
        paths = intent.getStringArrayExtra("paths");
        someTask(paths);
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        super.onDestroy();
        isDone = true;
        mBuilder.setContentText("Download complete");
        // Removes the progress bar
        mBuilder.setProgress(0, 0, false);
        mNotifyManager.notify(id, mBuilder.build());
        Log.d(LOG_TAG, "onDestroy");
    }

    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        return null;
    }

    void someTask(final String[] paths) {
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                for (int i = 1; i < paths.length; i++) {
                    String fname = "image_" + String.format("%03d", i) + ".png";

                    try {
                        File file = new File(myDir, fname);
                        Bitmap bitmap = null;
                        if (file.exists())
                            file.delete();

                        mBuilder.setProgress(100, i * (100 / paths.length), false);
                        mNotifyManager.notify(id, mBuilder.build());

                        bitmap = Utils.currectlyOrientation(paths[i], 700, 700);
                        bitmap = Utils.scaleCenterCrop(bitmap, 700, 700);
                        FileOutputStream out = new FileOutputStream(file);

                        bitmap.compress(Bitmap.CompressFormat.PNG, 50, out);
                        out.flush();
                        out.close();
                        bitmap.recycle();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Error while SaveToMemory", Toast.LENGTH_SHORT).show();
                    }
                }
                stopSelf();
            }
        }).start();
    }
}