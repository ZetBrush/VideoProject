package zetbrush.generatingmain;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaExtractor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.picsartvideo.R;

import net.pocketmagic.android.openmxplayer.OpenMXPlayer;
import net.pocketmagic.android.openmxplayer.PlayerEvents;

import java.io.File;
import java.util.concurrent.TimeUnit;

import zetbrush.com.view.ScrollPickerView;
import zetbrush.com.view.TouchScroll;


public class MainGenFragment extends DialogFragment {

    private static final String TAG = "MainGenFrag";
    public static int currentEffect = 0;
    public static int intervalSec = 0;
    static int vidcount = 1;
    static int counter = 1;
    private static int filecount = 0;
    public static final String[] name = new String[2];
    public TextView progress;
    MediaExtractor extractor;
    ProgressDialog progressDialog;
    DisplayMetrics dm = null;
    FFmpeg ffmpeg;
    //////Still frame Encoder///////////
    int imagecounter = 0;
    /////   Player  //////
    ImageView imagepreview;
    boolean isplaying = false;
    OpenMXPlayer player = null;
    ScrollPickerView effectPicker;
    Button testmp3;
    private Button makeVideoButton;
    private Button playButton;
    private volatile boolean flag;
    private SeekBar seekbar;
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
    private static boolean stillflag = false;
    private static boolean transitflag = false;
    int contract =0; // validator for merging process
    RelativeLayout rlLayout;
    private Button settingsButton;


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
            imagepreview = (ImageView) rlLayout.findViewById(R.id.imagepreview);
            playButton.setText("||");

        }

        @Override
        public void onError() {
            seekbar.setProgress(0);
            Toast.makeText(MainGenFragment.super.getActivity(), "Not supported content..", Toast.LENGTH_SHORT).show();
            player = new OpenMXPlayer(events);
        }
    };

    private String newMusicPath = null;

    //////DeleteMusicPath////
    public static Long startMiliSc = null;
    public static Long endMiliSc = null;
    public static int imageCount = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentActivity faActivity  = (FragmentActivity) super.getActivity();


         rlLayout    = (RelativeLayout)    inflater.inflate(R.layout.fragment_main, container, false);


        dm = new DisplayMetrics();
        super.getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        makeVideoButton = (Button) rlLayout.findViewById(R.id.makeVideoBut);
        makeVideoButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.selector_render_button));
        progress = (TextView)  rlLayout.findViewById(R.id.progress);
        playButton = (Button) rlLayout.findViewById(R.id.playButtn);
        playButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.selectorplaybutton));
        seekbar = (SeekBar) rlLayout.findViewById(R.id.seekbar);
        testmp3 = (Button) rlLayout.findViewById(R.id.TestMp3);
        pickMusicbtn = (Button) rlLayout.findViewById(R.id.pickMusicbtn);
        musicNameText = (TextView) rlLayout.findViewById(R.id.musicNameText);
        musicTimeText = (TextView) rlLayout.findViewById(R.id.musicTimeText);
        settingsButton = (Button)rlLayout.findViewById(R.id.settingsButton);



        pickMusicbtn.setOnClickListener(onPickMusicClick);
        makeVideoButton.setOnClickListener(makevideoClick);
        settingsButton.setOnClickListener(onSettingsButtonClick);
        playButton.setOnClickListener(onPlayClick);

        return rlLayout; // We must return the loaded Layout
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Intent intent = super.getActivity().getIntent();
            if (intent != null) {
                path = intent.getExtras().getString("myimagespath");
            }

        } catch (Exception e) {
            path = Environment.getDownloadCacheDirectory().getPath()+"/req_images";
        }
        setImageCount();

        loadFFMpegBinary();
    }

    public MainGenFragment(){

    }



    private void loadFFMpegBinary() {
        try {
            ffmpeg = FFmpeg.getInstance(super.getActivity());
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    showUnsupportedExceptionDialog();
                }
            });
        } catch (FFmpegNotSupportedException e) {
            showUnsupportedExceptionDialog();
        }
    }



    private void showUnsupportedExceptionDialog() {
        new AlertDialog.Builder(super.getActivity())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("dev not supported")
                .setMessage("dev not supported")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainGenFragment.super.getActivity().finish();
                    }
                })
                .create()
                .show();

    }

    /////PickMusic///////
   View.OnClickListener onPickMusicClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent.createChooser(intent, "Complate action using"), 5);
    }};

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 5 && data != null) {

            musicPath = AbsolutePathActivity.getPath(super.getActivity(), data.getData());
            newMusicPath = musicPath;
            if (musicPath != null)
                musicNameText.setText("");
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

    public void onDeleteMusicPathClick(View v) {
        musicPath = null;
        player.stop();
        musictotalTime = 0;
        musicNameText.setText("No music Selected");

    }



    @Override
    public void onResume() {
        super.onResume();


        seekbar = (SeekBar) rlLayout.findViewById(R.id.seekbar);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && player != null) try {
                    player.seek(progress);
                } catch (NullPointerException e) {
                }

            }
        });

    }

View.OnClickListener onPlayClick = new View.OnClickListener() {
    @Override
    public void onClick(View view) {

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
                Toast.makeText(MainGenFragment.super.getActivity(), "No music is selected", Toast.LENGTH_SHORT).show();

            }
            }
        }
 };



 View.OnClickListener onSettingsButtonClick = new View.OnClickListener() {
     @Override
     public void onClick(View view) {


        TouchScroll.idgen = 0;
        Animation bottomUp = AnimationUtils.loadAnimation(MainGenFragment.super.getActivity().getApplicationContext(),
                R.anim.top_up);

        final View popupView = MainGenFragment.super.getActivity().getLayoutInflater().inflate(R.layout.popup_settings, null);
        popupView.startAnimation(bottomUp);
        popupView.setVisibility(View.VISIBLE);

        PopupWindow popupWindow = new PopupWindow(popupView,
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);


        outputEditText = (EditText) popupView.findViewById(R.id.videoName);

        effectPicker = (ScrollPickerView) popupView.findViewById(R.id.scrollpicker);
        RangeSeekBar<Long> rengeSeekbar = (RangeSeekBar) popupView.findViewById(R.id.rangeSeekbar);
        Resources res = getResources();

        final String[] labels = res.getStringArray(R.array.effects_list);
        final String[] secs = res.getStringArray(R.array.seconds_list);
        effectPicker.addSlot(secs, 2, ScrollPickerView.ScrollType.Loop);
        effectPicker.addSlot(labels, 3, ScrollPickerView.ScrollType.Loop);
        effectPicker.setSlotIndex(1, currentEffect);
        effectPicker.setSlotIndex(0, intervalSec);
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
                name[0] = s.toString();
            }
        });

        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable());
        int location[] = new int[2];

         view.getLocationOnScreen(location);

        popupWindow.showAtLocation(view, Gravity.TOP, 0, 165);
        // location[0]-v.getHeight()*3,   location[1]-v.getWidth()*3);

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                Animation bottomDn = AnimationUtils.loadAnimation(MainGenFragment.super.getActivity().getApplicationContext(),
                        R.anim.top_down);
                popupView.startAnimation(bottomDn);

            }
        });


        long start = 0;
        long minv = 20;

        rengeSeekbar.setRangeValues(start, musictotalTime);
        rengeSeekbar.setSelectedMinValue(minv);
        rengeSeekbar.setSelectedMaxValue(musictotalTime);
        if (musicPath != null) {

            rengeSeekbar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Long>() {
                @Override
                public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Long minValue, Long maxValue) {
                    startMiliSc = minValue;
                    endMiliSc = maxValue;
                    //Log.i(TAG, "selected new range values: MIN=" + minValue + ", MAX=" + maxValue);


                }
            });
        }}
    };


    int videoDuration() {
        int dur = (imageCount*intervalSec + intervalSec);

        return dur;
    }




    ///////  Settings

View.OnClickListener makevideoClick = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        if (name[0] != null) {
            progressDialog = new ProgressDialog(MainGenFragment.super.getActivity());
            progressDialog.setTitle(null);

            if (currentEffect == 0) {
                    currentEffect=1;


            } else {
                String filename = Environment.getExternalStorageDirectory().getPath() + "/req_images/image_";

                String flnm = filename + "000.jpg";
                File ff = new File(flnm);
                if (ff.exists()) {

                    Intent intnt = new Intent(MainGenFragment.super.getActivity(),ServiceFloating.class);
                    intnt.putExtra("interval",intervalSec);
                    intnt.putExtra("startms",startMiliSc);
                    intnt.putExtra("endms",endMiliSc);
                    intnt.putExtra("effect",currentEffect);
                    intnt.putExtra("name", name[0]);
                    intnt.putExtra("musicpath", musicPath);
                    MainGenFragment.super.getActivity().startService(intnt);
                    MainGenFragment.super.getActivity().finish();

                }

                /*if(musicPath!=null || musicPath!=""){

                    String cmd =
                            "-i "+musicPath+" -ss "+((int)(startMiliSc/1000))+" -af afade=t=out:st="+((int)((endMiliSc/1000)
                            - (startMiliSc/1000))-2)+ ":d=2 " + Environment.getExternalStorageDirectory().getPath() + "/req_images/musictoadd.mp3";

                    try {
                        FFmpeg mmpg = new FFmpeg(MainGenFragment.this);

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
                }*/


               /* File fadfile = new File(filename + "000.png");
                if (fadfile.exists()) {
                    new StillVidEncoder().execute(fadfile);

                }*/

            }

        } else
            Toast.makeText(MainGenFragment.super.getActivity().getApplicationContext(), "Please configure output settings", Toast.LENGTH_SHORT).show();

    }

};


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }




    @Override
    public void onDestroy() {
        super.onDestroy();
       /* try {

            File dir = new File(Environment.getExternalStorageDirectory() + "/req_images");
            if (dir.isDirectory()) {
                String[] children = dir.list();
                for (int i = 0; i < children.length; i++) {
                    new File(dir, children[i]).delete();
                }

            }

        } catch (Exception e) {
        }*/
    }


    ////End of Making Video ///////


    public void onBackPressed() {
       /* try {

            File dir = new File(Environment.getExternalStorageDirectory() + "/req_images");
            if (dir.isDirectory()) {
                String[] children = dir.list();
                for (int i = 0; i < children.length; i++) {
                    new File(dir, children[i]).delete();
                }
            }

        } catch (Exception e) {
        }
        finish();*/
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }





    private class TransitionFrameGenerator extends AsyncTask<File, Integer, Integer> {

        private static final String TAG = "ffencoder";


        protected Integer doInBackground(File... params) {
            Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE);

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

                   /* int width = btm.getWidth();
                    int height = btm.getHeight();
                    Bitmap transBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    FileOutputStream out = null;*/


                    Effects.builder(ef)
                            .setParams(dm)
                            .generateFrames(btm, vidcount);

                    publishProgress(transcounter);
                    transcounter++;
                    vidcount++;
                    System.gc();
                    File imgg = new File(dirNm + "/image_" + String.format("%03d", imagecounter + 1) + ".jpg");
                    if (!imgg.exists()) {
                        vidcount = 1;
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
                progress.setText(tmp);

            }
        }
        @Override
        protected void onPostExecute(Integer result) {
            progress.setText("wait..");

            //FFmpegTransitionEncoder ffmpegins = new FFmpegTransitionEncoder(MainGenFragment.this,);
            //ffmpegins.addListener(MainGenFragment.this);
           // ffmpegins.execute(imageCount);

        }


    }

    private static class UIHelper {
        public static void setupTitleBar(Activity c) {
            final boolean customTitleSupported = c.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

            c.setContentView(R.layout.fragment_main);

            if (customTitleSupported) {
                c.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.fragment_main);
            }
        }
    }


}
