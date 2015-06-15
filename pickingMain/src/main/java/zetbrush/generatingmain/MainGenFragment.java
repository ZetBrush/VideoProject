package zetbrush.generatingmain;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
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
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItem;
import com.picsartvideo.R;

import net.pocketmagic.android.openmxplayer.OpenMXPlayer;
import net.pocketmagic.android.openmxplayer.PlayerEvents;

import java.io.File;
import java.util.concurrent.TimeUnit;

import zetbrush.com.view.ScrollPickerView;
import zetbrush.com.view.TouchScroll;

/**
 * Created by Arman
 */

public class MainGenFragment extends Fragment {

    private static final String TAG = "MainGenFrag";
    public static int currentEffect = 0;
    public static int intervalSec = 0;
    static int vidcount = 1;

    public static final String[] name = new String[2];
    public TextView progress;

    ProgressDialog progressDialog;
    DisplayMetrics dm = null;
    FFmpeg ffmpeg;
    boolean isplaying = false;
    OpenMXPlayer player = null;
    ScrollPickerView effectPicker;

    private Button makeVideoButton;
    private Button playButton;

    private SeekBar seekbar;

    private EditText outputEditText;

    private String path = null;
    private static String musicPath = null;
    private Button pickMusicbtn;
    private TextView musicNameText;

    private TextView musicTimeText;
    private long musictotalTime = 0;


    RelativeLayout rlLayout;
    private Button settingsButton;
    private ImageView deleteMusic;
    RangeSeekBar<Long> rengeSeekbar;


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

        int position = FragmentPagerItem.getPosition(getArguments());


        if(position==0){

            rlLayout    = (RelativeLayout)    inflater.inflate(R.layout.fragment_music, container, false);
            playButton = (Button) rlLayout.findViewById(R.id.playButtn);
            playButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.selectorplaybutton));
            seekbar = (SeekBar) rlLayout.findViewById(R.id.seekbar);

            pickMusicbtn = (Button) rlLayout.findViewById(R.id.pickMusicbtn);
            musicNameText = (TextView) rlLayout.findViewById(R.id.musicNameText);
            musicTimeText = (TextView) rlLayout.findViewById(R.id.musicTimeText);
            deleteMusic = (ImageView)rlLayout.findViewById(R.id.deleteMusicImg);

            pickMusicbtn.setOnClickListener(onPickMusicClick);
            playButton.setOnClickListener(onPlayClick);
            deleteMusic.setOnClickListener(onDeleteMusicPathClick);
            rengeSeekbar = (RangeSeekBar) rlLayout.findViewById(R.id.rangeSeekbar);

            rengeSeekbar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Long>() {
                @Override
                public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Long minValue, Long maxValue) {
                    startMiliSc = minValue;
                    endMiliSc = maxValue;
                    //Log.i(TAG, "selected new range values: MIN=" + minValue + ", MAX=" + maxValue);

                    rengeSeekbar.setSelectedMinValue(minValue);
                    rengeSeekbar.setSelectedMaxValue(maxValue);
                }

            });



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

        else if (position==1){

            rlLayout    = (RelativeLayout)    inflater.inflate(R.layout.fragment_fxtiming, container, false);
            effectPicker = (ScrollPickerView) rlLayout.findViewById(R.id.scrollpicker);
            Resources res = getResources();

            final String[] labels = res.getStringArray(R.array.effects_list);
            final String[] secs = res.getStringArray(R.array.seconds_list);
            effectPicker.addSlot(secs, 2, ScrollPickerView.ScrollType.Loop);
            effectPicker.addSlot(labels, 3, ScrollPickerView.ScrollType.Loop);
            effectPicker.setSlotIndex(1, currentEffect);
            effectPicker.setSlotIndex(0, intervalSec);



        }

        else if(position==2){
            rlLayout    = (RelativeLayout)    inflater.inflate(R.layout.fragment_rendersave, container, false);
            dm = new DisplayMetrics();
            super.getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            makeVideoButton = (Button) rlLayout.findViewById(R.id.makeVideoBut);
            makeVideoButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.selector_render_button));

            outputEditText = (EditText) rlLayout.findViewById(R.id.videoName);

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



            makeVideoButton.setOnClickListener(makevideoClick);
           // settingsButton.setOnClickListener(onSettingsButtonClick);
           // playButton.setOnClickListener(onPlayClick);
           // deleteMusic.setOnClickListener(onDeleteMusicPathClick);
        }
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
    public void onActivityResult(final int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 5 && data != null) {

            musicPath = AbsolutePathActivity.getPath(super.getActivity(), data.getData());
            newMusicPath = musicPath;
            if (musicPath != null)
                musicNameText.setText(musicPath);
             if (!musicPath.equals(newMusicPath)) {
                newMusicPath = musicPath;
                try {
                    long start = 0;
                    long minv = 20;
                    player = new OpenMXPlayer();


                    if (musicPath != null) {


                    }
                    player.stop();
                    player.setDataSource("");
                } catch (NullPointerException e) {
                }
            }

            PlayerEvents events1 = new PlayerEvents() {
                @Override
                public void onStart(String mime, int sampleRate, int channels, long duration) {
                    musictotalTime = duration / 1000;

                    rengeSeekbar.setRangeValues(0l, musictotalTime);
                    rengeSeekbar.setSelectedMinValue(30l);
                    rengeSeekbar.setSelectedMaxValue(musictotalTime-35);
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
                mplyr.seek(4);
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

    View.OnClickListener onDeleteMusicPathClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            musicPath = null;
            if(player!=null)
            player.stop();
            musictotalTime = 0;
            musicNameText.setText("No music Selected");
            rengeSeekbar.resetSelectedValues();
        }
    };



    @Override
    public void onResume() {
        super.onResume();


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


//// settings

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




    ///////  render

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
