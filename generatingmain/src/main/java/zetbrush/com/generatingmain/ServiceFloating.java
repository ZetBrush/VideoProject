package zetbrush.com.generatingmain;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.lzyzsd.circleprogress.CircleProgress;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServiceFloating extends Service implements IThreadCompleteListener {

	public static  int ID_NOTIFICATION = 6778;
	private RelativeLayout parentlayout;
	private WindowManager windowManager;
	private ImageView chatHead;
	CircleProgress arcpr;
	private PopupWindow pwindo;
	boolean checker=false;
	boolean mHasDoubleClicked = false;
	long lastPressTime;
	private Boolean _enable = true;
	String outputFold = Environment.getExternalStorageDirectory().getAbsolutePath()+"/req_images/transitions";
	ArrayList<String> myArray;
	View v;
	CircleProgress progressView;
	int contract=0,imageCount,interval,effect;
	Long startMiliSc,endMiliSc;
	String vidName,musicPath;

	CopyOnWriteArrayList<Integer> progressOverall = new CopyOnWriteArrayList();



	void writeState(int state,Bundle extra) {
		SharedPreferences.Editor editor = getSharedPreferences("serviceStart", MODE_MULTI_PROCESS)
				.edit();
		editor.clear();
		editor.putInt("normalStart", state);
		editor.putInt("interval", extra.getInt("interval"));
		editor.putLong("startms", extra.getLong("startms"));
		editor.putLong("endms",extra.getLong("endms"));
		editor.putInt("effect", extra.getInt("effect"));
		editor.putString("name", extra.getString("name"));
		editor.putString("musicpath",extra.getString("musicpath"));

		editor.commit();
	}

	int getState() {
		return getApplicationContext().getSharedPreferences("serviceStart",
				MODE_MULTI_PROCESS).getInt("normalStart", 1);
	}




	@Override
	public int onStartCommand (Intent intent, int flags, int startId) {
		progressOverall.add(0);
		if(intent!=null && intent.getExtras()!=null) {
			Bundle extra = intent.getExtras();

			if (extra != null) {
				interval = extra.getInt("interval");
				startMiliSc = extra.getLong("startms");
				endMiliSc = extra.getLong("endms");
				effect = extra.getInt("effect");
				vidName = extra.getString("name");
				musicPath = extra.getString("musicpath");
				writeState(2,extra);

			}

			 progressView = (CircleProgress)v.findViewById(R.id.circle_progress);


			if (musicPath != null || musicPath != "") {

				String cmd =
						" -ss " + ((int) (startMiliSc / 1000)) + " -i " + musicPath + " -acodec copy " + Environment.getExternalStorageDirectory().getPath() + "/req_images/musictoadd.mp3";

				try {
					FFmpeg mmpg = new FFmpeg(ServiceFloating.this);

					mmpg.execute(cmd, new FFmpegExecuteResponseHandler() {
						@Override
						public void onSuccess(String message) {
							Log.d("FFMusic Success", message);
						}

						@Override
						public void onProgress(String message) {
							Log.d("FFMusic Progress", message);
						}

						@Override
						public void onFailure(String message) {
							Log.d("FFMusic Failure", message);
						}

						@Override
						public void onStart() {

						}

						@Override
						public void onFinish() {
							Log.d("FFMusic", "music cut finished");
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}





			String filename = Environment.getExternalStorageDirectory().getPath() + "/req_images/image_";
			String flnm = filename + "000.jpg";
			File ff = new File(flnm);
			ProgressHandler stillHandl = new ProgressHandler(20);
			if (ff.exists()) {

				StillFFEncoder2 thrd = new StillFFEncoder2(ff, this,stillHandl,imageCount);
				thrd.addThreadComplListener(ServiceFloating.this);
				thrd.setProgressListener(new IProgress() {
					@Override
					public void progress(int prg) {
						int prev= progressOverall.get(0);
						progressOverall.remove(0);
						progressOverall.add(prg+prev);
						final int pr = prg;
						v.post(new Runnable() {
							@Override
							public void run() {

								((CircleProgress)v.findViewById(R.id.circle_progress)).setProgress(pr);
							}
						});
					}
				});
				thrd.execute(interval);
				ProgressHandler transHandl = new ProgressHandler(30);
				TransitionFrameGen trgen = new TransitionFrameGen(this, this, effect,transHandl,imageCount);
				trgen.setProgressListener(new IProgress() {
					@Override
					public void progress(int prg) {
						int prev = progressOverall.get(0);
						progressOverall.remove(0);
						progressOverall.add(prg + prev);
						final int pr = prg;
						v.post(new Runnable() {
							@Override
							public void run() {

								((CircleProgress) v.findViewById(R.id.circle_progress)).setProgress(pr);
							}
						});
					}});

				trgen.execute(ff);

			}


		}
		return START_NOT_STICKY;
	}


	@Override
	public IBinder onBind(Intent intent) {



		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
     if(1!=getState()){
		SharedPreferences pref = getApplicationContext().getSharedPreferences("serviceStart",
				 MODE_MULTI_PROCESS);

		 interval = pref.getInt("interval",1);
		 startMiliSc = pref.getLong("startms",1);
		 endMiliSc = pref.getLong("endms",1);
		 effect = pref.getInt("effect",1);
		 vidName = pref.getString("name","");
		 musicPath = pref.getString("musicpath","");


	 }
		setImageCount();

		windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

		LayoutInflater inflater = LayoutInflater.from(this);
		 v =inflater.inflate(R.layout.circleprogressplace,null,true);
  			arcpr = (CircleProgress)v.findViewById(R.id.circle_progress);
		arcpr.setMax(100);

		//chatHead = new ImageView(this);

		//chatHead.setImageResource(R.drawable.floating2);



		final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_PHONE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.TRANSLUCENT);

		params.gravity = Gravity.TOP | Gravity.LEFT;
		params.x = 0;
		params.y = 100;

		windowManager.addView(v,params);

		try {
			v.setOnTouchListener(new View.OnTouchListener() {
				private WindowManager.LayoutParams paramsF = params;
				private int initialX;
				private int initialY;
				private float initialTouchX;
				private float initialTouchY;

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN:

							// Get current time in nano seconds.
							long pressTime = System.currentTimeMillis();


							// If double click...
							if (pressTime - lastPressTime <= 300) {
								createNotification();
								ServiceFloating.this.stopSelf();
								mHasDoubleClicked = true;
							} else { // If not double click....
								mHasDoubleClicked = false;
							}
							lastPressTime = pressTime;
							initialX = paramsF.x;
							initialY = paramsF.y;
							initialTouchX = event.getRawX();
							initialTouchY = event.getRawY();
							break;
						case MotionEvent.ACTION_UP:
							break;
						case MotionEvent.ACTION_MOVE:
							paramsF.x = initialX + (int) (event.getRawX() - initialTouchX);
							paramsF.y = initialY + (int) (event.getRawY() - initialTouchY);
							windowManager.updateViewLayout(v, paramsF);
							break;

					}
					return false;
				}
			});
		} catch (Exception e) {
			// TODO: handle exception
		}

		v.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {

				_enable = false;

			}
		});



		}


	public void createNotification(){
		Intent notificationIntent = new Intent(getApplicationContext(), ServiceFloating.class);
		PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, notificationIntent, 0);

		Notification notification = new Notification(R.drawable.floating2, "Click to start launcher", System.currentTimeMillis());
		notification.setLatestEventInfo(getApplicationContext(), "Start launcher" ,  "Click to start launcher", pendingIntent);
		notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONGOING_EVENT;

		NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

		notificationManager.notify(ID_NOTIFICATION, notification);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (arcpr != null) windowManager.removeView(v);
	}

	@Override
	public void notifyOfThreadComplete(int code) {
		contract+=code;
		CircleProgress cp = (CircleProgress)v.findViewById(R.id.circle_progress);
		cp.setProgress(contract*20);
		if(contract ==3) {
			contract=0;
			Log.d("ImageCount_Listen", String.valueOf(imageCount));
			setImageCount();
			Log.d("ImageCount_Listen", String.valueOf(imageCount));

			MergeVidsWorker merger = new MergeVidsWorker(ServiceFloating.this,
					Environment.getExternalStorageDirectory().getAbsoluteFile() + "/Vid_" + vidName,
					Environment.getExternalStorageDirectory().getAbsoluteFile()+"/req_images/musictoadd.mp3"){


			};

			merger.setListener(ServiceFloating.this,cp);
			merger.execute(imageCount, (int) (startMiliSc / 1000), (int) (endMiliSc / 1000));
		}

		if(code==666){
			ServiceFloating.this.stopSelf();
		}

	}

	public void setImageCount() {

		this.imageCount = getCount(Environment.getExternalStorageDirectory().getPath() + "/req_images");
	}

	private int getCount(String dirPath) {
		int fls = 0;
		File f = new File(dirPath);
		File[] files = f.listFiles();

		if (files != null)
			for (int i = 0; i < files.length; i++) {
				if(files[i].getName().contains(".jpg"))
					fls++;

			}
		return fls;
	}
	String getCommand(String...param) {
		return "-y -loop 1 -i " +
				param[0] +
				" -t " + param[1] + " "+" -preset ultrafast -bsf:v h264_mp4toannexb " +
				outputFold + "/still_" + param[2] + ".ts";
	}


}