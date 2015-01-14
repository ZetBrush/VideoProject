package zetbrush.com.generatingmain;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;

import net.pocketmagic.android.openmxplayer.OpenMXPlayer;
import net.pocketmagic.android.openmxplayer.PlayerEvents;
import net.pocketmagic.android.openmxplayer.PlayerStates;

import org.jcodec.api.android.SequenceEncoder;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends ActionBarActivity {

    private Button makeVideoButton;
    private Button playButton;
    private TextView progress;
    private volatile boolean flag;
    private SeekBar seekbar;
    MediaExtractor extractor;
    private PlayerStates state = new PlayerStates();
    private String sourcePath = null;
    private int sourceRawResId = -1;
    private Context mContext;
    private boolean stop = false;
    private MediaCodec codec;
    private static int filecount = 0;
    private static final String TAG = "MediaMuxerTest";
    private static final boolean VERBOSE = false;
    private static final int MAX_SAMPLE_SIZE = 256 * 1024;
    private static final float LATITUDE = 0.0000f;
    private static final float LONGITUDE = -180.0f;
    private static final float BAD_LATITUDE = 91.0f;
    private static final float BAD_LONGITUDE = -181.0f;
    private static final float TOLERANCE = 0.0002f;
    private Resources mResources;
    private TextView interval ;
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
    private long musictotalTime =0;
    private String newMusicPath = null;
    private Long startMiliSc = null;
    private Long endMiliSc = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        makeVideoButton = (Button) findViewById(R.id.makeVideoBut);
        progress = (TextView) findViewById(R.id.progress);
        playButton = (Button) findViewById(R.id.playButtn);
        seekbar = (SeekBar) findViewById(R.id.seekbar);
        testmp3 = (Button)findViewById(R.id.TestMp3);
        pickMusicbtn = (Button)findViewById(R.id.pickMusicbtn);
        musicNameText = (TextView)findViewById(R.id.musicNameText);
        musicTimeText = (TextView)findViewById(R.id.musicTimeText);
        try {
            Intent intent = getIntent();
            if (intent != null) {
                path = intent.getExtras().getString("myimagespath");

            }

        }catch (Exception e){
            path = "/storage/removable/sdcard1/DCIM/100ANDRO/newfold";
        }


    }


    /////PickMusic///////
    public void onPickMusicClick(View v){
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent.createChooser(intent,"Complate action using"),5);



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==5 && data!=null)
        {

            Uri message=data.getData();
            newMusicPath=musicPath;
            musicPath = message.getPath();
            if(musicPath!=null)
            musicNameText.setText(musicPath);
            if(!musicPath.equals(newMusicPath)){
                try {
                    player.stop();
                    player.setDataSource("");
                }
                catch(NullPointerException e){}
            }

            PlayerEvents events1 = new PlayerEvents() {
                @Override
                public void onStart(String mime, int sampleRate, int channels, long duration) {
                    musictotalTime  = duration/1000;
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
            try{
                mplyr.play();
                mplyr.seek(2);
                try {
                    Thread.currentThread().wait(20);
                } catch (InterruptedException e) {

                }
                mplyr.stop();

            }
            catch (Exception e){

            }
            mplyr.stop();
            mplyr = null;

        }
    }

    //////DeleteMusicPath////

    public void onDeleteMusicPathClick(View v){
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
                if (fromUser) player.seek(progress);
            }
        });


    }

    private class ConcateAudioVideo extends AsyncTask<String,String,String>{

        protected String doInBackground(String...paths){
            try {
                concatWithAudio(paths[0],paths[1]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "doing..";
        }

        protected void onProgressUpdate(String... progresss) {
            progress.setText("processed " + String.valueOf(progresss[0])) ;
        }

        protected void onPostExecute(Long result) {
            progress.setText("Done!");
            File fil = new File(videoPath);
           boolean dele = fil.delete();
            videoPath = null;
            outputName=null;
            name[0]=null;

        }



}


    private static int count = 0;

    private class Encoder extends AsyncTask<File, Integer, Integer> {
        private static final String TAG = "ENCODER";

        protected Integer doInBackground(File... params) {

            SequenceEncoder se = null;
            try {
                videoPath = params[0].getParentFile().getPath()+"/"+name[0]+".mp4";
                se = new SequenceEncoder(new File(params[0].getParentFile(),
                        name[0]+".mp4"));

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
                progress.setText("processed " + String.valueOf(values[0]));
        }
        @Override
        protected void onPostExecute(Integer result){
            progress.setText("Ready!");
            if(musicPath!=null && videoPath!=null)
            {
                ConcateAudioVideo mux = new ConcateAudioVideo();
                mux.execute(musicPath,videoPath,"muxing");

            }

        }

    }



    PlayerEvents events = new PlayerEvents() {
        @Override
        public void onStop() {
            seekbar.setProgress(0);
            playButton.setText("Play");
            isplaying = false;
        }



        @Override
        public void onStart(String mime, int sampleRate, int channels, long duration) {
            Log.d("on startplay", "onStart called: " + mime + " sampleRate:" + sampleRate + " channels:" + channels );
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
            if(musicPath==null){
                player.stop();
                player.setDataSource("");

            }

        }

        @Override
        public void onPlay() {
            playButton.setText("Pause");
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
            playButton.setText("Play");
            isplaying = false;
        } else {
            if(musicPath!=null) {
                player.setDataSource(musicPath);   //"/storage/removable/sdcard1/DCIM/100ANDRO/newfold/strangeclouds.aac");
                player.play();
                playButton.setText("Pause");
                isplaying = true;
            }
            else{
                Toast.makeText(this,"No music is selected",Toast.LENGTH_SHORT).show();

            }
        }


    }

    final int[] scale= new int [2];
    final String[] name = new String[2];

    public void onSettingsButtonClick(View v){
        View popupView = getLayoutInflater().inflate(R.layout.popup_settings, null);

        PopupWindow popupWindow = new PopupWindow(popupView,
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

          interval = (TextView) popupView.findViewById(R.id.textInterval);
          timeinterval = (SeekBar)popupView.findViewById(R.id.tIntervalSeekBar);
          outputEditText = (EditText)popupView.findViewById(R.id.videoName);
          RangeSeekBar<Long> rengeSeekbar = (RangeSeekBar)popupView.findViewById(R.id.rangeSeekbar);

          timeinterval.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress==0)
                    progress=1;
                scale[0] = progress;
                interval.setText(String.valueOf(scale[0]));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d("value of frametime", "val "+ scale[0]);
                org.jcodec.api.SequenceEncoder.setFrameDuration(scale[0]);

            }

        });

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

        popupWindow.showAtLocation(v, Gravity.NO_GRAVITY,
                location[0]-v.getHeight()*3,   location[1]-v.getWidth()*3);



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

    public void onTestMp3Click(View v) {
        if (musicPath != null) {
            File mp3file = new File(musicPath);//"/storage/removable/sdcard1/Wyat.mp3");
            try {
                MPEGAudioFrameHeader mpaframe = new MPEGAudioFrameHeader(mp3file);
                Log.d("MP3Info", mpaframe.toString());


            } catch (NoMPEGFramesException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }



    private void concatWithAudio(String audiopath, String videopath) throws IOException{

            if (musicPath != null) {

                String video = videopath;//"/storage/removable/sdcard1/ggg.mp4";


                MovieCreator mc = new MovieCreator();
                // Movie videoin = MovieCreator.build("/storage/removable/sdcard1/outputaaac.mp4");
                Movie countVideo = mc.build(video);
                AACTrackImple aacTrack = new AACTrackImple(new FileDataSourceImpl(audiopath));//"/storage/removable/sdcard1/strangeclouds.aac"));
                //MP3TrackImpl mp3 = new MP3TrackImpl(new FileDataSourceImpl("/storage/removable/sdcard1/Wyat.mp3"));
                //  Log.d("mp3Info", mp3.toString());
                Movie audiomovie = new Movie();
                audiomovie.addTrack(aacTrack);


                Movie[] clips = new Movie[2];
                clips[0] = countVideo;
                clips[1] = audiomovie;

                List<Track> videoTracks = new LinkedList<Track>();
                List<Track> audioTracks = new LinkedList<Track>();
                long videotrackcount = 0;

                for (Movie movie : clips) {
                    for (Track track : movie.getTracks()) {
                        if (track.getHandler().equals("soun"))
                            audioTracks.add(track);

                        if (track.getHandler().equals("vide"))
                            videoTracks.add(track);
                    }
                }


                CroppedTrack ct = null;
                long endmilis =0;
                long startmilis =0;
                if(endMiliSc == null) {
                   endmilis = videotrackcount;
                }
                else
                    endmilis=endMiliSc;

                if(startMiliSc == null) {
                    startmilis = 1;
                }
                else startmilis = startMiliSc;
                    //TODO stuff
                Log.d("value of sliders", "  Min "+startMiliSc + ", Max "+endmilis);
                Log.d("value of audiotrack total sample size", "Value "+audioTracks.get(0).getSamples().size());
                videotrackcount = (videoTracks.get(0).getSamples().size() -1 )*(scale[0]);
                Log.d("scale[0]", "Value "+scale[0]);
                CroppedTrack dd = new CroppedTrack(audioTracks.get(0), 1, (int)(videotrackcount*1000/23.3)-1);
                Log.d("value of videotrack /88", "Value "+ videoTracks.get(0).getSamples().size());
                Log.d("value of sample max to cut", "Value "+ ((int)(videotrackcount*1000/23.3)-1));
                Log.d("value of sample*88", "Value "+ (videoTracks.get(0).getSamples().size() * 88));
              /*  if((long)((float)endmilis/24) >=videotrackcount ) {

                     ct = new CroppedTrack(audioTracks.get(0), (long)((float)startmilis/24), videotrackcount);
                }
                else  {
                   ct = new CroppedTrack(audioTracks.get(0), (long)((float)startmilis/24), (long)((float)endmilis/24));
                }*/
                Log.d("value of audiotrack total sample sizeafter crop", "Value "+audioTracks.get(0).getSamples().size());
                if (videoTracks.size() > 0)
                    // result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));

                    if (audioTracks.size() > 0 && dd !=null)
                        //  result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));

                        countVideo.addTrack(dd);

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String outputLocation = "/storage/removable/sdcard1/" + "VidGen_"+timeStamp + ".mp4";
                Container out = new DefaultMp4Builder().build(countVideo);
                FileChannel fc = new RandomAccessFile(String.format(outputLocation), "rw").getChannel();
                out.writeContainer(fc);
                fc.close();
            }
            else {Toast.makeText(this,"No music is selected",Toast.LENGTH_SHORT).show();}
        }


        ////////////

    public void makevideoClick(View v) {
        if (name[0]!=null) {

            if(path!=null) {

                File file = new File( path + "/image_000.png");

                String digits = file.getName().replaceAll("\\D+(\\d+)\\D+",
                        "$1");
                String mask = file.getName().replaceAll("(\\D+)\\d+(\\D+)",
                        "$1%0" + digits.length() + "d$2");

                new Encoder().execute(new File( path + "/", mask));
            }
            else
                Toast.makeText(getApplicationContext(),"path is null",Toast.LENGTH_SHORT).show();

        }

        else
            Toast.makeText(getApplicationContext(),"Please configure output settings",Toast.LENGTH_SHORT).show();

    }



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


}
