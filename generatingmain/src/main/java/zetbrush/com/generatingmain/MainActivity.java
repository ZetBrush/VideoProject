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
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
import com.googlecode.mp4parser.authoring.Sample;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import zetbrush.com.generatingmain.AACTrackImple;
import net.pocketmagic.android.openmxplayer.OpenMXPlayer;
import net.pocketmagic.android.openmxplayer.PlayerEvents;
import net.pocketmagic.android.openmxplayer.PlayerStates;

import org.jcodec.api.JCodecException;
import org.jcodec.api.android.FrameGrab;
import org.jcodec.api.android.SequenceEncoder;
import org.jcodec.common.FileChannelWrapper;
import org.jcodec.common.NIOUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.googlecode.mp4parser.authoring.builder.FragmentedMp4Builder;
import com.googlecode.mp4parser.authoring.builder.SyncSampleIntersectFinderImpl;
import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.builder.FragmentedMp4Builder;
import com.googlecode.mp4parser.authoring.builder.SyncSampleIntersectFinderImpl;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AACTrackImpl;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;

public class MainActivity extends ActionBarActivity {

    private Button makeVideoButton;
    private Button playButton;
    private TextView progress;
    private volatile boolean flag;
    private SeekBar seekbar;
    private Button compositionButton;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        makeVideoButton = (Button) findViewById(R.id.makeVideoBut);
        progress = (TextView) findViewById(R.id.progress);
        playButton = (Button) findViewById(R.id.playButtn);
        seekbar = (SeekBar) findViewById(R.id.seekbar);
        compositionButton = (Button) findViewById(R.id.compositionButton);

        try {
            Intent intent = getIntent();
            if (intent != null) {
                path = intent.getExtras().getString("myimagespath");

            }
        }catch (Exception e){}


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


    private static int count = 0;

    private class Encoder extends AsyncTask<File, Integer, Integer> {
        private static final String TAG = "ENCODER";

        protected Integer doInBackground(File... params) {

            SequenceEncoder se = null;
            try {
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
           outputName=null;
            name[0]=null;
        }

    }


    PlayerEvents events = new PlayerEvents() {
        @Override
        public void onStop() {
            seekbar.setProgress(0);

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
        }

        @Override
        public void onPlay() {
        }

        @Override
        public void onError() {
            seekbar.setProgress(0);
            Toast.makeText(MainActivity.this, "Error!", Toast.LENGTH_SHORT).show();
            //.setText("An error has been encountered");
        }
    };

    boolean isplaying = false;
    OpenMXPlayer player = null;

    public void onPlayClick(View v) {
        if (player == null) {
            player = new OpenMXPlayer(events);

        }

        if (isplaying) {
            player.stop();
            isplaying = false;
        } else {
            player.setDataSource("/storage/removable/sdcard1/DCIM/100ANDRO/newfold/strangeclouds.aac");

            player.play();
            isplaying = true;
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


    }



    public void onCompositionClick(View v) throws IOException {
        // MediaMuxer muxer = new MediaMuxer("/storage/removable/sdcard1/DCIM/100ANDRO/newfold/outt.mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

        // File sourceAudio = new File("/storage/removable/sdcard1/outputaaac.mp4");
        // File sourceVieo = new File("/storage/removable/sdcard1//vid_enc.mp4");
        //  File outt = new File("/storage/removable/sdcard1/viiddos.mp4");
        String sourceAudiop = "/storage/removable/sdcard1/outputaaac.mp4";
        String sorcevideo = "/storage/removable/sdcard1//vid_enc.mp4";


        String f1 = "/storage/removable/sdcard1/DCIM/100ANDO/newfold/strangeclouds.aac";
        String f2 = "/storage/removable/sdcard1//newoutput.mp4";
        //String f3 = AppendExample.class.getProtectionDomain().getCodeSource().getLocation().getFile() + "/1365070453555.mp4";

/*
        Movie[] inMovies = new Movie[]{
                MovieCreator.build(f1),
                MovieCreator.build(f2),
               *//* MovieCreator.build(f3)*//*};

        List<Track> videoTracks = new LinkedList<Track>();
        List<Track> audioTracks = new LinkedList<Track>();

        for (Movie m : inMovies) {
            for (Track t : m.getTracks()) {
                if (t.getHandler().equals("soun")) {
                    audioTracks.add(t);
                }
                if (t.getHandler().equals("vide")) {
                    videoTracks.add(t);
                }
            }
        }

        Movie result = new Movie();


        if (audioTracks.size() > 0) {
            result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
        }
        if (videoTracks.size() > 0) {
            result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
        }

        Container out = new DefaultMp4Builder().build(result);

        FileChannel fc = new RandomAccessFile(String.format("audioVideo.mp4"), "rw").getChannel();
        out.writeContainer(fc);
        fc.close();*/


        String audioDeutsch = "/storage/removable/sdcard1/countdeutchaudio.mp4";
        String audioEnglish = "/storage/removable/sdcard1/countenglishaudio.mp4";
        String video = "/storage/removable/sdcard1/ggg.mp4";




        MovieCreator mc = new MovieCreator();
        // Movie videoin = MovieCreator.build("/storage/removable/sdcard1/outputaaac.mp4");
        Movie countVideo = mc.build(video);
          AACTrackImple aacTrack = new AACTrackImple(new FileDataSourceImpl("/storage/removable/sdcard1/strangeclouds.aac"));
       // MP3TrackImpl mp3 = new MP3TrackImpl(new FileDataSourceImpl("/storage/removable/sdcard1/KD_minu.mp3"));
      //  Log.d("mp3Info", mp3.toString());
         Movie audiomovie = new Movie();
            audiomovie.addTrack(aacTrack);


        //  MP3TrackImpl mp3Track = new MP3TrackImpl(new FileDataSourceImpl("/storage/removable/sdcard1/Music/StrangeClouds.mp3"));
        //  H264TrackImpl mp4track = new H264TrackImpl(new FileDataSourceImpl("/storage/removable/sdcard1/vid_enc.mp4"));
        //  Movie videoin = MovieCreator.build("/storage/removable/sdcard1/countvideo.mp4");
        // Container out2 = new DefaultMp4Builder().build(videoin);
        // videoin.addTrack(aacTrack);
        // Movie m = new Movie();
        // m.addTrack(aacTrack);
        //  m.addTrack(mp4track);

        Movie[] clips = new Movie[2];
        clips[0] = countVideo;
        clips[1] = audiomovie;

        List<Track> videoTracks = new LinkedList<Track>();
        List<Track> audioTracks = new LinkedList<Track>();
        int videotrackcount = 0;

        for (Movie movie : clips) {
            for (Track track : movie.getTracks()) {
                if (track.getHandler().equals("soun"))
                    audioTracks.add(track);

                if (track.getHandler().equals("vide"))
                    videoTracks.add(track);
            }
        }

        videotrackcount = videoTracks.get(0).getSamples().size() * 90;
        CroppedTrack dd = new CroppedTrack(audioTracks.get(0), 0, videotrackcount);

        if (videoTracks.size() > 0)
            // result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));

            if (audioTracks.size() > 0)
                //  result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
                countVideo.addTrack(dd);

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String outputLocation = "/storage/removable/sdcard1/" + timeStamp + ".mp4";
        Container out = new DefaultMp4Builder().build(countVideo);
        FileChannel fc = new RandomAccessFile(String.format(outputLocation), "rw").getChannel();
        out.writeContainer(fc);
        fc.close();
        }

        ////////////

    public void makevideoClick(View v) {
        if (name[0]!=null) {

            if(path!=null) {

                File file = new File( path + "/image_0.png");

                String digits = file.getName().replaceAll("\\D+(\\d+)\\D+",
                        "$1");
                String mask = file.getName().replaceAll("(\\D+)\\d+(\\D+)",
                        "$1%0" + digits.length() + "d$2");

                new Encoder().execute(new File( path + "/", mask));
            }

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

    private class Decoder extends AsyncTask<File, Integer, Integer> {
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
