package zetbrush.com.generatingmain;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.net.Uri;
import android.os.*;
import android.os.Process;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.Touch;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;

import net.pocketmagic.android.openmxplayer.OpenMXPlayer;
import net.pocketmagic.android.openmxplayer.PlayerEvents;
import net.pocketmagic.android.openmxplayer.PlayerStates;

import org.jcodec.api.android.SequenceEncoder;
import org.jcodec.scale.BitmapUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import zetbrush.com.view.ScrollPickerView;
import zetbrush.com.view.ScrollPickerViewListener;
import zetbrush.com.view.TouchScroll;


public class MainActivity extends ActionBarActivity {

    private Button makeVideoButton;
    private Button playButton;
    public TextView progress;
    private volatile boolean flag;
    private SeekBar seekbar;
    MediaExtractor extractor;
    private PlayerStates state = new PlayerStates();
    private String sourcePath = null;

    private boolean stop = false;
    private MediaCodec codec;
    private static int filecount = 0;
    private static final String TAG = "MediaMuxerTest";
    private static final boolean VERBOSE = false;
    private static final int MAX_SAMPLE_SIZE = 256 * 1024;
    private Resources mResources;
    private TextView interval;
    private SeekBar timeinterval;
    private Handler handler = new Handler();
    private String outputName = null;
    private EditText outputEditText;
    private String mime = null;
    private String path = null;
    private String musicPath = null;
    private Button pickMusicbtn;
    private TextView musicNameText;
    private String videoPath = null;
    private TextView musicTimeText;
    private long musictotalTime = 0;
    private String newMusicPath = null;
    private Long startMiliSc = null;
    private Long endMiliSc = null;
    private int imageCount = 0;
    private boolean fadeEffect = false;
    ProgressDialog progressDialog;
    Handler h;
    DisplayMetrics dm = null;
    public static int currentEffect =0;
    public static int intervalSec =0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        makeVideoButton = (Button) findViewById(R.id.makeVideoBut);
        makeVideoButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.selector_render_button));
        progress = (TextView) findViewById(R.id.progress);
        playButton = (Button) findViewById(R.id.playButtn);
        playButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.selectorplaybutton));
        seekbar = (SeekBar) findViewById(R.id.seekbar);
        testmp3 = (Button) findViewById(R.id.TestMp3);
        pickMusicbtn = (Button) findViewById(R.id.pickMusicbtn);
        musicNameText = (TextView) findViewById(R.id.musicNameText);
        musicTimeText = (TextView) findViewById(R.id.musicTimeText);
        try {
            Intent intent = getIntent();
            if (intent != null) {
                path = intent.getExtras().getString("myimagespath");

            }

        } catch (Exception e) {
            path = "/storage/removable/sdcard1/DCIM/100ANDRO/newfold";
        }
        setImageCount();

    }

    /////PickMusic///////
    public void onPickMusicClick(View v) {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent.createChooser(intent, "Complate action using"), 5);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 5 && data != null) {

            musicPath = AbsolutePathActivity.getPath(MainActivity.this, data.getData());
            newMusicPath = musicPath;
            if (musicPath != null)
                musicNameText.setText(musicPath);
            if (!musicPath.equals(newMusicPath)) {
                try {
                    player.stop();
                    player.setDataSource("");
                } catch (NullPointerException e) {
                }
            }

            PlayerEvents events1 = new PlayerEvents() {
                @Override
                public void onStart(String mime, int sampleRate, int channels, long duration) {
                    musictotalTime = duration / 1000;
                }

                @Override
                public void onPlay() {

                }

                @Override
                public void onPlayUpdate(int percent, long currentms, long totalms) {

                }

                @Override
                public void onStop() {

                }

                @Override
                public void onError() {

                }
            };
            OpenMXPlayer mplyr = new OpenMXPlayer(events1);
            mplyr.setDataSource(musicPath);
            try {
                mplyr.play();
                mplyr.seek(2);
                try {
                    Thread.currentThread().wait(20);
                } catch (InterruptedException e) {

                }
                mplyr.stop();

            } catch (Exception e) {

            }
            mplyr.stop();
            mplyr = null;

        }
    }

    //////DeleteMusicPath////

    public void setImageCount() {

        this.imageCount = getCount(Environment.getExternalStorageDirectory().getPath() + "/req_images");
    }

    private int getCount(String dirPath) {
        int fls = 0;
        File f = new File(dirPath);
        File[] files = f.listFiles();

        if (files != null)
            for (int i = 0; i < files.length; i++) {
                fls++;
                File file = files[i];
            }
        return fls;
    }

    public void onDeleteMusicPathClick(View v) {
        musicPath = null;
        player.stop();
        musictotalTime = 0;
        musicNameText.setText("No music Selected");

    }

    @Override
    protected void onResume() {
        super.onResume();


        seekbar = (SeekBar) findViewById(R.id.seekbar);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && player!=null) try{ player.seek(progress);} catch (NullPointerException e){};
            }
        });

    }

    private class ConcateAudioVideo extends AsyncTask<String, String, String> {

        protected String doInBackground(String... paths) {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE);
            try {
                concatWithAudio(paths[0], paths[1]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "doing..";
        }

        protected void onProgressUpdate(String... progresss) {
            progress.setText("processed " + String.valueOf(progresss[0]));
        }

        protected void onPostExecute(Long result) {
           /* progress.setText("Done!");
            File fil = new File(videoPath);
            boolean dele = fil.delete();*/
            videoPath = null;
            outputName = null;
            name[0] = null;

        }

    }


    private class Encoder extends AsyncTask<File, Integer, Integer> {
        private static final String TAG = "ENCODER";

        protected Integer doInBackground(File... params) {

            SequenceEncoder se = null;
            try {
                videoPath = Environment.getExternalStorageDirectory().getPath() + "/" + name[0] + ".mp4";
                se = new SequenceEncoder(new File(Environment.getExternalStorageDirectory(),
                        name[0] + ".mp4"));

                for (int i = 0; !flag; i++) {
                    File img = new File(params[0].getParentFile(),
                            String.format(params[0].getName(), i));
                    if (!img.exists())
                        break;
                    Bitmap frame = BitmapFactory.decodeFile(img
                            .getAbsolutePath());
                    se.encodeImage(frame);
                    publishProgress(i);

                }
                se.finish();
                // se.addAudioTrack();
            } catch (IOException e) {
                Log.e(TAG, "IO", e);
            }

            return 0;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (!values[0].equals(null))
                progress.setText("processed " + (int) (((float) values[0]) / imageCount * 100) + "%");
        }

        @Override
        protected void onPostExecute(Integer result) {
            progress.setText("Ready!");
            if (musicPath != null && videoPath != null) {
                ConcateAudioVideo mux = new ConcateAudioVideo();
                mux.execute(musicPath, videoPath, "muxing");

            } else finish();
        }
    }


    ////////////////////// FadeIn transaction Generator /////////////////////////

    static int vidcount = 1;

    private class PartialVidEncoder extends AsyncTask<File, Integer, Integer> {
        private static final String TAG = "PartialENCODER";

        protected Integer doInBackground(File... params) {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE);
            SequenceEncoderPartial se = null;
            try {

                se = new SequenceEncoderPartial(new File(params[0].getParentFile(),
                        "part" + vidcount + ".mp4"));

                int transcounter = 0;
                int imagecounter = 0;
                String dirNm = params[0].getParentFile().getPath();


                while (true) {

                    File img = new File(dirNm + "/image_" + String.format("%03d", imagecounter) + ".png");
                    Bitmap btm = BitmapFactory.decodeFile(img.getAbsolutePath());
                    int width = btm.getWidth();
                    int height = btm.getHeight();
                    Bitmap transBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

                    for (int i = 0; i < 10; i++) {
                        Canvas canvas = new Canvas(transBitmap);
                        canvas.drawRGB(0, 0, 0);
                        final Paint paint = new Paint();
                        Matrix mx = new Matrix();
                        ///choosing effect
                        if(currentEffect==1){
                            paint.setAlpha(i * 10);
                            canvas.drawBitmap(btm,0,0,paint);
                        }

                        else if (currentEffect==2) {
                            canvas.drawBitmap(btm, dm.widthPixels - i * (dm.widthPixels / 9), 0, paint);
                        }

                        else if(currentEffect>= 3){
                            mx.postRotate(90 - i*10);
                            mx.postTranslate(150+ dm.widthPixels - i * (dm.widthPixels / 10 + 50),0);
                            canvas.concat(mx);
                            canvas.drawBitmap(btm,0,0,paint);

                        }
                        /// starting encode frame
                        se.encodeNativeFrameForPartialEffect(BitmapUtil.fromBitmap(transBitmap));

                        publishProgress(transcounter);
                        transcounter++;
                    }
                    vidcount++;
                    se.finish();
                    System.gc();
                    File imgg = new File(dirNm + "/image_" + String.format("%03d", imagecounter + 1) + ".png");
                    if (!imgg.exists()) {
                        vidcount = 1;
                        break;
                    }
                    imagecounter++;
                    se = new SequenceEncoderPartial(new File(params[0].getParentFile(),
                            "part" + vidcount + ".mp4"));


                }


            } catch (IOException e) {
                Log.e(TAG, "IO", e);
            }

            return 0;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (!values[0].equals(null)) {
                String tmp = (int) (((float) values[0] / (imageCount * 10)) * 100) + "%";
                progress.setText(tmp);
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            progress.setText("Ready!");


        }
    }

    //////Still frame Encoder///////////
    int imagecounter = 0;

    private class StillVidEncoder extends AsyncTask<File, Integer, Integer> {

        private static final String TAG = "StillENCODER";

        protected Integer doInBackground(File... params) {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE);
            int vidcnt = 1;
            if (intervalSec < 1) {
                intervalSec = 1;
            }

            try {

                for (int i = 0; !flag; i++) {
                    File img = new File(params[0].getParentFile().getPath() + "/image_" + String.format("%03d", imagecounter) + ".png");

                    if (img.exists()) {

                        Bitmap frame = BitmapFactory.decodeFile(img.getAbsolutePath());
                        SequenceEncoderPartial sep = new SequenceEncoderPartial(new File(params[0].getParentFile(),
                                "still" + vidcnt + ".mp4"));
                        sep.encodeNativeFrameForPartialEffect(BitmapUtil.fromBitmap(frame));
                        sep.finish();
                        imagecounter++;
                        frame.recycle();

                        MovieCreator mc = new MovieCreator();

                        Movie mm = mc.build(params[0].getParentFile().getPath() + "/still" + vidcnt + ".mp4");
                        Track vidTr = mm.getTracks().get(0);
                        AppendTrack stillApTr;
                        Track[] vidTracks = new Track[intervalSec * 10];

                        for (int j = 0; j < intervalSec * 10; j++) {
                            vidTracks[j] = vidTr;
                        }

                        stillApTr = new AppendTrack(vidTracks);
                        Movie newmovie = new Movie();
                        newmovie.addTrack(stillApTr);
                        String outputLocation = Environment.getExternalStorageDirectory().getPath() + "/req_images/stilll" + vidcnt + ".mp4";
                        Container out = new DefaultMp4Builder().build(newmovie);
                        FileChannel fc = new RandomAccessFile(String.format(outputLocation), "rw").getChannel();
                        out.writeContainer(fc);
                        fc.close();
                        vidcnt++;

                    } else {
                        break;
                    }

                    System.gc();
                    publishProgress(imagecounter);


                }


            } catch (IOException e) {
                Log.e(TAG, "IO", e);
            }

            return 0;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (!values[0].equals(null))
                progress.setText("conf " + values[0]);
        }

        @Override
        protected void onPostExecute(Integer result) {
            progress.setText("wait..");

            String vid1 = Environment.getExternalStorageDirectory().getPath() + "/req_images/part";
            String vid2 = Environment.getExternalStorageDirectory().getPath() + "/req_images/stilll";

            AppendTrack apTrc = null;

            try {
                Track[] videoTracks = new Track[imagecounter * 2];
                for (int i = 1, j = 0; i <= imagecounter; i++, j++) {

                    MovieCreator mc = new MovieCreator();
                    Movie mt1 = mc.build(vid1 + i + ".mp4");
                    mc = new MovieCreator();
                    videoTracks[j] = mt1.getTracks().get(0);
                    Movie ms1 = mc.build(vid2 + i + ".mp4");
                    videoTracks[j + 1] = ms1.getTracks().get(0);
                    j++;

                }
                apTrc = new AppendTrack(videoTracks);

                Movie newmovie = new Movie();
                newmovie.addTrack(apTrc);
                //Log.d("result video size", "Size:  "+ result.getTracks().size() + " vid samples " + (result.getTracks().get(0).getSamples().size()*scale[0] *1000/23.2) +" vid samples " + (result.getTracks().get(1).getSamples().size() /23.2) );
                // String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String outputLocation = Environment.getExternalStorageDirectory().getPath() + "/req_images/fadedvid.mp4";
                Container out = new DefaultMp4Builder().build(newmovie);
                FileChannel fc = new RandomAccessFile(String.format(outputLocation), "rw").getChannel();
                out.writeContainer(fc);
                fc.close();
                progress.setText("adding music");
                ConcateAudioVideo mux = new ConcateAudioVideo();
                mux.execute(musicPath, outputLocation, "muxing");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

                                                /////   Player  //////
    ImageView imagepreview;
    PlayerEvents events = new PlayerEvents() {
        @Override
        public void onStop() {
            seekbar.setProgress(0);
            playButton.setText("Play");
            isplaying = false;
        }

        @Override
        public void onStart(String mime, int sampleRate, int channels, long duration) {
            Log.d("on startplay", "onStart called: " + mime + " sampleRate:" + sampleRate + " channels:" + channels);
            if (duration == 0) {

            } else {

            }
            // .setText("Playing content:" + mime + " " + sampleRate + "Hz " + (duration/1000000) + "sec");
        }

        @Override
        public void onPlayUpdate(int percent, long currentms, long totalms) {
            seekbar.setProgress(percent);
            musicTimeText.setText(String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(currentms),
                    TimeUnit.MILLISECONDS.toSeconds(currentms) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentms))
            ));
            musictotalTime = totalms;
            if (musicPath == null) {
                player.stop();
                player.setDataSource("");

            }

        }

        @Override
        public void onPlay() {
            imagepreview = (ImageView) findViewById(R.id.imagepreview);
            playButton.setText("||");

        }

        @Override
        public void onError() {
            seekbar.setProgress(0);
            Toast.makeText(MainActivity.this, "Not supported content..", Toast.LENGTH_SHORT).show();
            player = new OpenMXPlayer(events);
        }
    };

    boolean isplaying = false;
    OpenMXPlayer player = null;

    public void onPlayClick(View v) {

        if (player == null) {
            player = new OpenMXPlayer(events);

        }

        if (isplaying) {
            player.pause();
            playButton.setText("|>");
            isplaying = false;
        } else {
            if (musicPath != null) {
                player.setDataSource(musicPath);   //"/storage/removable/sdcard1/DCIM/100ANDRO/newfold/strangeclouds.aac");
                player.play();
                playButton.setText("||");
                isplaying = true;
            } else {
                Toast.makeText(this, "No music is selected", Toast.LENGTH_SHORT).show();

            }
        }


    }



    final String[] name = new String[2];

    ScrollPickerView effectPicker;


        ///////  Settings

    public void onSettingsButtonClick(View v) {
        TouchScroll.idgen =0;
        Animation bottomUp = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.top_up);

        final View popupView = getLayoutInflater().inflate(R.layout.popup_settings, null);
        popupView.startAnimation(bottomUp);
        popupView.setVisibility(View.VISIBLE);

        PopupWindow popupWindow = new PopupWindow(popupView,
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);




        outputEditText = (EditText) popupView.findViewById(R.id.videoName);

        effectPicker = (ScrollPickerView)popupView.findViewById(R.id.scrollpicker);
        RangeSeekBar<Long> rengeSeekbar = (RangeSeekBar) popupView.findViewById(R.id.rangeSeekbar);
        Resources res = getResources();

        final String[] labels = res.getStringArray(R.array.effects_list);
        final String[] secs = res.getStringArray(R.array.seconds_list);
        effectPicker.addSlot(secs,2, ScrollPickerView.ScrollType.Loop);
        effectPicker.addSlot(labels,3, ScrollPickerView.ScrollType.Loop);
               effectPicker.setSlotIndex(1,currentEffect);
            effectPicker.setSlotIndex(0,intervalSec);
           //  effectPicker.setSlotIndex(1,intervalSec);


        outputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                name[0]=s.toString();
            }
        });

        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable());
        int location[] = new int[2];

        v.getLocationOnScreen(location);

        popupWindow.showAtLocation(v, Gravity.TOP,  0, 165 );
               // location[0]-v.getHeight()*3,   location[1]-v.getWidth()*3);

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                Animation bottomDn = AnimationUtils.loadAnimation(getApplicationContext(),
                        R.anim.top_down);
                popupView.startAnimation(bottomDn);

            }
        });



        long start =0;
        long minv = 20;

        rengeSeekbar.setRangeValues(start, musictotalTime);
        rengeSeekbar.setSelectedMinValue(minv);
        rengeSeekbar.setSelectedMaxValue(musictotalTime);
      if(musicPath!=null) {

          rengeSeekbar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Long>() {
              @Override
              public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Long minValue, Long maxValue) {
                    startMiliSc = minValue;
                    endMiliSc = maxValue;
                  //Log.i(TAG, "selected new range values: MIN=" + minValue + ", MAX=" + maxValue);


              }
          });
      }



    }

    ////////////////////////////TEST MP3 ///////////////////////////////

    Button testmp3;

    public void onTestMp3Click(View v) throws IOException {

        String filename = "/storage/removable/sdcard1/image_000.png";
        File img = new File(filename);
        Bitmap frame = BitmapFactory.decodeFile(img.getAbsolutePath());
        SequenceEncoderPartial sep = new SequenceEncoderPartial(new File("/storage/removable/sdcard1",
                "still_count.mp4"));

        sep.encodeNativeFrameForPartialEffect(BitmapUtil.fromBitmap(frame));

        sep.finish();
        frame.recycle();
        MovieCreator mc = new MovieCreator();
        Movie mm = mc.build("/storage/removable/sdcard1/still_count.mp4");

        Track vidTr = mm.getTracks().get(0);
        AppendTrack stillApTr = null;

        Track[] vidTracks = new Track[intervalSec*10];


        try {
            for (int i = 0; i<intervalSec*10; i++) {
                vidTracks[i]=vidTr;
                }
            stillApTr  = new AppendTrack(vidTracks);
            Movie newmovie = new Movie();
            newmovie.addTrack(stillApTr);
            //Log.d("result video size", "Size:  "+ result.getTracks().size() + " vid samples " + (result.getTracks().get(0).getSamples().size()*scale[0] *1000/23.2) +" vid samples " + (result.getTracks().get(1).getSamples().size() /23.2) );
            // String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String outputLocation = Environment.getExternalStorageDirectory().getPath() + "/stilledNew.mp4";
            Container out = new DefaultMp4Builder().build(newmovie);
            FileChannel fc = new RandomAccessFile(String.format(outputLocation), "rw").getChannel();
            out.writeContainer(fc);
            fc.close();
            }
        catch (Exception e){e.printStackTrace();}



    }


    private void concatWithAudio(String audiopath, String videopath) throws IOException{


        if (musicPath != null) {

            String video = videopath;// Environment.getExternalStorageDirectory().getPath()+"/req_images/xcgh.mp4";

            MovieCreator mc = new MovieCreator();
            Movie countVideo = mc.build(video);

            AACTrackImple aacTrack = new AACTrackImple(new FileDataSourceImpl(audiopath));//"/storage/removable/sdcard1/strangeclouds.aac"));
            //MP3TrackImpl mp3 = new MP3TrackImpl(new FileDataSourceImpl("/storage/removable/sdcard1/Wyat.mp3"));
            IsoFile isoFile = new IsoFile(video);
            double videoLengthInSeconds = (double)
                    isoFile.getMovieBox().getMovieHeaderBox().getDuration() /
                    isoFile.getMovieBox().getMovieHeaderBox().getTimescale();

            CroppedTrack ct = null;
            double endmilis =0;
            double startmilis =0;
                endmilis=endMiliSc/1000;

            if(startMiliSc == null) {
                startmilis = 1;
            }
            else startmilis = startMiliSc/1000;

            if(endmilis-startmilis>videoLengthInSeconds){
                endmilis = startmilis+videoLengthInSeconds;
            }

            /////cutting
           // Log.d("value of videotrackcunt ", "Value " + videotrackcount + "ending time cont "+ ((int)((float)endmilis/23.2)) );

             ct = new CroppedTrack(aacTrack, (long)(startmilis*43.066), (long)(endmilis*43.066));



            Movie newmovie = new Movie();
            newmovie.addTrack(countVideo.getTracks().get(0));
            newmovie.addTrack(ct);

            //Log.d("result video size", "Size:  "+ result.getTracks().size() + " vid samples " + (result.getTracks().get(0).getSamples().size()*scale[0] *1000/23.2) +" vid samples " + (result.getTracks().get(1).getSamples().size() /23.2) );

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String outputLocation = Environment.getExternalStorageDirectory().getPath()+ "/VidGen_"+name[0] + ".mp4";
            Container out = new DefaultMp4Builder().build(newmovie);
            FileChannel fc = new RandomAccessFile(String.format(outputLocation), "rw").getChannel();
            out.writeContainer(fc);
            fc.close();

        }
        else {
            String video = videopath;
            Movie newmovie = new Movie();
            MovieCreator mc = new MovieCreator();
            Movie countVideo = mc.build(video);
            newmovie.addTrack(countVideo.getTracks().get(0));
            //Log.d("result video size", "Size:  "+ result.getTracks().size() + " vid samples " + (result.getTracks().get(0).getSamples().size()*scale[0] *1000/23.2) +" vid samples " + (result.getTracks().get(1).getSamples().size() /23.2) );
            String outputLocation = Environment.getExternalStorageDirectory().getPath()+ "/VidGen_"+name[0] + ".mp4";
            Container out = new DefaultMp4Builder().build(newmovie);
            FileChannel fc = new RandomAccessFile(String.format(outputLocation), "rw").getChannel();
            out.writeContainer(fc);
            fc.close();
            Looper.prepare();
            Toast.makeText(getApplicationContext(),"No music is selected",Toast.LENGTH_SHORT).show();

           }
        finish();
    }


                                        ////////////  Making Video /////////


    public void makevideoClick(View v) {
        if (name[0] != null) {

            if (currentEffect==0) {

                if (path != null) {

                    path = Environment.getExternalStorageDirectory().getPath() + "/req_images";
                    File file = new File(path + "/image_000.png");

                    String digits = file.getName().replaceAll("\\D+(\\d+)\\D+",
                            "$1");
                    String mask = file.getName().replaceAll("(\\D+)\\d+(\\D+)",
                            "$1%0" + digits.length() + "d$2");

                    new Encoder().execute(new File(path + "/", mask));

                } else
                    Toast.makeText(getApplicationContext(), "path is null", Toast.LENGTH_SHORT).show();

            } else {
                String filename = Environment.getExternalStorageDirectory().getPath() + "/req_images/image_";
                String filename2 = "/storage/removable/sdcard1/image_faded";

                String filenm = Environment.getExternalStorageDirectory().getPath() + "/vidgen_faded/image_faded";
                File f = new File(Environment.getExternalStorageDirectory().getPath() + "/vidgen_faded");
                if (!(f.exists() && f.isDirectory())) {
                    f.mkdir();
                } else {

                }


                String flnm = filename + "000.png";
                File ff = new File(flnm);
                if (ff.exists()) {
                    new PartialVidEncoder().execute(ff);

                }


                File fadfile = new File(filename + "000.png");
                if (fadfile.exists()) {
                    new StillVidEncoder().execute(fadfile);

                }

            }

        }
        else
            Toast.makeText(getApplicationContext(), "Please configure output settings", Toast.LENGTH_SHORT).show();
    }


    ////End of Making Video ///////

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*private class Decoder extends AsyncTask<File, Integer, Integer> {
        private static final String TAG = "DECODER";

        protected Integer doInBackground(File... params) {
            FileChannelWrapper ch = null;
            try {
                ch = NIOUtils.readableFileChannel(params[0]);
                FrameGrab frameGrab = new FrameGrab(ch);
                FrameGrab.MediaInfo mi = frameGrab.getMediaInfo();
                Bitmap frame = Bitmap.createBitmap(mi.getDim().getWidth(), mi
                        .getDim().getHeight(), Bitmap.Config.ARGB_8888);

                for (int i = 0; !flag; i++) {
                    frameGrab.getFrame(frame);
                    if (frame == null)
                        break;
                    OutputStream os = null;
                    try {
                        os = new BufferedOutputStream(new FileOutputStream(
                                new File(params[0].getParentFile(),
                                        String.format("img%08d.jpg", i))));
                        frame.compress(Bitmap.CompressFormat.JPEG, 90, os);
                    } finally {
                        if (os != null)
                            os.close();
                    }
                    publishProgress(i);

                }
            } catch (IOException e) {
                Log.e(TAG, "IO", e);
            } catch (JCodecException e) {
                Log.e(TAG, "JCodec", e);

            } finally {
                NIOUtils.closeQuietly(ch);
            }
            return 0;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            //progress.setText(String.valueOf(values[0]));
        }
    }
*/

    private void cloneMediaUsingMuxer(String srcvid, String srcAud, String dstMediaPath,
                                      int expectedTrackCount, int degrees) throws IOException {

        // Set up MediaExtractor to read from the source.

        MediaExtractor extractor = new MediaExtractor();
        extractor.setDataSource(srcvid);


        MediaExtractor extractorAud = new MediaExtractor();
        extractorAud.setDataSource(srcAud);

        int trackCount = extractor.getTrackCount();
        int trackCountAud = extractorAud.getTrackCount();


        //assertEquals("wrong number of tracks", expectedTrackCount, trackCount);

        // Set up MediaMuxer for the destination.
        MediaMuxer muxer;
        muxer = new MediaMuxer(dstMediaPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

      //  muxer.addTrack(MediaFormat.createVideoFormat("video/avc", 480, 320));
      //  muxer.addTrack(MediaFormat.createAudioFormat("audio/mp4a-latm", 48000, 2));

        // Set up the tracks.
        HashMap<Integer, Integer> indexMap = new HashMap<Integer, Integer>(trackCount);
        HashMap<Integer, Integer> indexMapAud = new HashMap<Integer, Integer>(trackCountAud);
        for (int i = 0; i < trackCount; i++) {
            extractor.selectTrack(i);
            MediaFormat format = extractor.getTrackFormat(i);
            int dstIndex = muxer.addTrack(format);
            indexMap.put(i, dstIndex);
        }

        for (int i = 0; i < trackCount; i++) {
            extractorAud.selectTrack(i);
            MediaFormat format = extractorAud.getTrackFormat(i);
            int dstIndex = muxer.addTrack(format);
            indexMapAud.put(i, dstIndex);

        }
        Log.d("IndexMaps", "sizes IndexMap "+ indexMap.size() + "IndexMapAud "+ indexMapAud.size(), null);


    // Copy the samples from MediaExtractor to MediaMuxer.
    boolean sawEOS = false;
    int bufferSize = MAX_SAMPLE_SIZE;
    int frameCount = 0;
    int offset = 100;

    ByteBuffer dstBuf = ByteBuffer.allocate(bufferSize);
    MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();


        muxer.start();
        boolean secondstage = false;

        while (!sawEOS) {
            if(secondstage){
                break;
            }
            bufferInfo.offset = offset;
            bufferInfo.size = extractor.readSampleData(dstBuf, offset);


            if (bufferInfo.size < 0 ) {

               Log.d(TAG, "saw input EOS.");

               // sawEOS = true;
                (bufferInfo.size) = 0;
                frameCount = 0;
                while(!sawEOS){

                    if (bufferInfo.size < 0) {
                        sawEOS = true;
                        secondstage = true;
                        bufferInfo.size =0;
                        muxer.stop();
                        break;
                    }
                   else{
                        bufferInfo.presentationTimeUs = extractorAud.getSampleTime();
                        bufferInfo.flags = extractorAud.getSampleFlags();
                        int trackIndex = extractorAud.getSampleTrackIndex();

                        muxer.writeSampleData(indexMapAud.get(trackIndex), dstBuf,
                                bufferInfo);
                        extractorAud.advance();

                        frameCount++;

                        Log.d(TAG, "Frame (" + frameCount + ") " +
                                "PresentationTimeUs:" + bufferInfo.presentationTimeUs +
                                " Flags:" + bufferInfo.flags +
                                " TrackIndex:" + trackIndex +
                                " Size(KB) " + bufferInfo.size / 1024);

                        }

                    }


                 }


             else {
                bufferInfo.presentationTimeUs = extractor.getSampleTime();
                bufferInfo.flags = extractor.getSampleFlags();
                int trackIndex = extractor.getSampleTrackIndex();

                muxer.writeSampleData(indexMap.get(trackIndex), dstBuf,
                        bufferInfo);
                extractor.advance();

                frameCount++;

                    Log.d(TAG, "Frame (" + frameCount + ") " +
                            "PresentationTimeUs:" + bufferInfo.presentationTimeUs +
                            " Flags:" + bufferInfo.flags +
                            " TrackIndex:" + trackIndex +
                            " Size(KB) " + bufferInfo.size / 1024);

            }

        }

       // muxer.release();

        return;
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        try{

            File dir = new File(Environment.getExternalStorageDirectory()+"/req_images");
            if (dir.isDirectory()) {
                String[] children = dir.list();
                for (int i = 0; i < children.length; i++) {
                    new File(dir, children[i]).delete();
                }

            }

        } catch(Exception e){ }
    }

    @Override
    public void onBackPressed() {
        try {

            File dir = new File(Environment.getExternalStorageDirectory() + "/req_images");
            if (dir.isDirectory()) {
                String[] children = dir.list();
                for (int i = 0; i < children.length; i++) {
                    new File(dir, children[i]).delete();
                }
            }

        } catch (Exception e){}
        finish();
    }

    public static String getRealPathFromUri(Activity activity, Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = activity.managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    static int counter =1;



}
