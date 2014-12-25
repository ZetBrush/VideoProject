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
    TextView interval ;
    SeekBar timeinterval;
    Handler handler = new Handler();
    String outputName = null;
    EditText outputEditText;
    String mime = null;
    int sampleRate = 0, channels = 0, bitrate = 0;
    long presentationTimeUs = 0, duration = 0;
     String path = null;
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

        // Remux remuxer = new Remux();

        //remuxer.remuxcustom(out,sourceAudio,sourceVieo);

      /*  MovieCreator mc = new MovieCreator();
        Movie video = mc.build(Channels.newChannel(AppendExample.class.getResourceAsStream("/count-video.mp4")));
        Movie audio = mc.build(Channels.newChannel(AppendExample.class.getResourceAsStream("/count-english-audio.mp4")));


        List<Track> videoTracks = video.getTracks();
        video.setTracks(new LinkedList<Track>());

        List<Track> audioTracks = audio.getTracks();


        for (Track videoTrack : videoTracks) {
            video.addTrack(new AppendTrack(videoTrack, videoTrack));
        }
        for (Track audioTrack : audioTracks) {
            video.addTrack(new AppendTrack(audioTrack, audioTrack));
        }

        IsoFile out = new DefaultMp4Builder().build(video);
        FileOutputStream fos = new FileOutputStream(new File(String.format("output.mp4")));
        out.getBox(fos.getChannel());
        fos.close();*/

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
        String video = "/storage/removable/sdcard1/countvideo.mp4";


       /* Movie countVideo = MovieCreator.build(video);
        Movie countAudioDeutsch = MovieCreator.build(audioDeutsch);
        Movie countAudioEnglish = MovieCreator.build(audioEnglish);

        Track audioTrackDeutsch = countAudioDeutsch.getTracks().get(0);
        audioTrackDeutsch.getTrackMetaData().setLanguage("deu");
        Track audioTrackEnglish = countAudioEnglish.getTracks().get(0);
        audioTrackEnglish.getTrackMetaData().setLanguage("eng");

        countVideo.addTrack(audioTrackDeutsch);
        countVideo.addTrack(audioTrackEnglish);

        {
            Container out = new DefaultMp4Builder().build(countVideo);
            FileOutputStream fos = new FileOutputStream(new File("/storage/removable/sdcard1/ouuuuutput.mp4"));
            out.writeContainer(fos.getChannel());
            fos.close();
        }
        {
            FragmentedMp4Builder fragmentedMp4Builder = new FragmentedMp4Builder();
            fragmentedMp4Builder.setIntersectionFinder(new SyncSampleIntersectFinderImpl(countVideo, null, -1));
            Container out = fragmentedMp4Builder.build(countVideo);
            FileOutputStream fos = new FileOutputStream(new File("/storage/removable/sdcard1/output-frag.mp4"));
            out.writeContainer(fos.getChannel());
            fos.close();
        }
*/

        MovieCreator mc = new MovieCreator();
        // Movie videoin = MovieCreator.build("/storage/removable/sdcard1/outputaaac.mp4");
        Movie countVideo = mc.build(video);
        AACTrackImple aacTrack = new AACTrackImple(new FileDataSourceImpl("/storage/removable/sdcard1/strangeclouds.aac"));
        //  H264TrackImpl mp4track = new H264TrackImpl(new FileDataSourceImpl("/storage/removable/sdcard1/vid_enc.mp4"));
        //  Movie videoin = MovieCreator.build("/storage/removable/sdcard1/countvideo.mp4");
        // Container out2 = new DefaultMp4Builder().build(videoin);
        // videoin.addTrack(aacTrack);
        // Movie m = new Movie();
        // m.addTrack(aacTrack);
        //  m.addTrack(mp4track);


        List<Sample> audioSamples = aacTrack.getSamples();


        if (countVideo.getTimescale() < aacTrack.getDuration()) {
            long time = aacTrack.getDuration() - countVideo.getTimescale();
            int toRemove = (int) time * 1024 + 1;

            List<Sample> newSamples = null;
            int i = audioSamples.size();
            for (int j = toRemove; j > 0; j--) {
                audioSamples.remove(i - 1);
                i--;

            }
            aacTrack.setSamples(audioSamples);
        }






        countVideo.addTrack(aacTrack);


      /*  long starttime2 = System.currentTimeMillis();
        FileChannel fc2 = new RandomAccessFile("/storage/removable/sdcard1/countvideo.mp4", "rw").getChannel();
        out2.writeContainer(fc2);
        long size2 = fc2.size();
        fc2.truncate(fc2.position());
        fc2.close();
        System.err.println("Writing " + size2 / 1024 / 1024 + "MB took " + (System.currentTimeMillis() - starttime2));
        */
        DefaultMp4Builder mp4Builder = new DefaultMp4Builder();
        Container out = mp4Builder.build(countVideo);
        FileOutputStream fos = new FileOutputStream("/storage/removable/sdcard1/outputasactmp4.mp4");
        FileChannel fc = fos.getChannel();
        out.writeContainer(fc);

        fos.close();


    }

       /* try {
           // String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
            H264TrackImpl h264Track = new H264TrackImpl(new FileDataSourceImpl(source));
            AACTrackImpl aacTrack = new AACTrackImpl(new FileDataSourceImpl( "/storage/removable/sdcard1/strangeclouds.aac" ));

            Movie movie = new Movie();
            movie.addTrack(h264Track);
            movie.addTrack(aacTrack);
            Container mp4file = new DefaultMp4Builder().build(movie);

            FileChannel fc = new FileOutputStream(new File("/storage/removable/sdcard1" +"/vidosik_output.mp4")).getChannel();
            mp4file.writeContainer(fc);
            fc.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/






       // cloneMediaUsingMuxer("/storage/removable/sdcard1/DCIM/100ANDRO/newfold/vid_enc.mp4","/storage/removable/sdcard1/DCIM/100ANDRO/newfold/Honor.mp3","/storage/removable/sdcard1/DCIM/100ANDRO/newfold/vidasas_enc.mp4",4,4);


       /* String outputFile = "/sdcard/muxerExceptions.mp4";
        MediaMuxer muxer;

        // Throws exception b/c start() is not called.
        muxer = new MediaMuxer(outputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        muxer.start();
        muxer.addTrack(MediaFormat.createVideoFormat("video/avc", 480, 320));
        muxer.addTrack(MediaFormat.createAudioFormat("audio/mp4a-latm", 44100, 2));

        muxer.stop();


        try {

        } catch (IllegalStateException e) {
            // expected
        }

        //  MediaFormat audioFormat = new MediaFormat(...);
        //  MediaFormat videoFormat = new MediaFormat(...);
        MediaFormat audioFormat = new MediaFormat();
        MediaFormat videoFormat = new MediaFormat();
        int audioTrackIndex = muxer.addTrack(audioFormat);
        int videoTrackIndex = muxer.addTrack(videoFormat);


        int bufferSize = (640 * 480 * 6);
        boolean isAudioSample = false;
        ByteBuffer inputBuffer = ByteBuffer.allocate(bufferSize);
        boolean finished = false;
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();


        for (; filecount < 2; ) {
            finished = false;
            if (filecount == 1)
                sourcePath = "/storage/removable/sdcard1/DCIM/100ANDRO/newfold/vid_enc.mp4";
            else if (filecount == 0) {
                sourcePath = "/storage/removable/sdcard1/DCIM/100ANDRO/newfold/strangeclouds.aac";
            }
            filecount++;


            // sourcePath = "/storage/removable/sdcard1/DCIM/100ANDRO/newfold/strangeclouds.aac";
            // getInputBuffer() will fill the inputBuffer with one frame of encoded
            // sample from either MediaCodec or MediaExtractor, set isAudioSample to
            // true when the sample is audio data, set up all the fields of bufferInfo,
            // and return true if there are no more samples.

            extractor = new MediaExtractor();
            // try to set the source, this might fail
            try {
                if (sourcePath != null) extractor.setDataSource(this.sourcePath);

            } catch (Exception e) {
                Log.e("asasas", "exception:" + e.getMessage());

                return;
            }

            // Read track header
            MediaFormat format = null;
            try {

                format = extractor.getTrackFormat(0);
                mime = format.getString(MediaFormat.KEY_MIME);
                sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
                // if duration is 0, we are probably playing a live stream
                duration = format.getLong(MediaFormat.KEY_DURATION);
                bitrate = format.getInteger(MediaFormat.KEY_BIT_RATE);


                //bufferInfo.set();

            } catch (Exception e) {
                Log.e("except params", "Reading format parameters exception:" + e.getMessage());
                // don't exit, tolerate this error, we'll fail later if this is critical
            }
            Log.d("Info", "Track info: mime:" + mime + " sampleRate:" + sampleRate + " channels:" + channels + " bitrate:" + bitrate + " duration:" + duration);

            while (!finished) {
                // check we have audio content we know
                if (format != null || mime.startsWith("audio/")) {
                    isAudioSample = true;
                    if (events != null) handler.post(new Runnable() {
                        @Override
                        public void run() {
                            events.onError();
                        }
                    });
                    return;
                }
                if (format != null || mime.startsWith("video/")) {
                    isAudioSample = false;
                    //  if (events != null) handler.post(new Runnable() { @Override public void run() { events.onError();  } });
                    return;
                }
                muxer.start();
                // create the actual decoder, using the mime to select
                codec = MediaCodec.createDecoderByType(mime);


                // check we have a valid codec instance
                if (codec == null) {
                    if (events != null) handler.post(new Runnable() {
                        @Override
                        public void run() {
                            events.onError();
                        }
                    });
                    return;
                }


                codec.configure(format, null, null, 0);
                codec.start();
                ByteBuffer[] codecInputBuffers = codec.getInputBuffers();
                ByteBuffer[] codecOutputBuffers = codec.getOutputBuffers();
                if (codecInputBuffers == null) {
                    finished = true; // getInputBuffer(inputBuffer, isAudioSample, bufferInfo);
                }
                if (!finished) {


                    int currentTrackIndex = isAudioSample ? audioTrackIndex : videoTrackIndex;
                    for (ByteBuffer btbuf : codecInputBuffers) {
                        muxer.writeSampleData(currentTrackIndex, btbuf, bufferInfo);
                    }
                }
            }
            ;
        }
        muxer.stop();
        muxer.release();*/



  /*  public boolean getInputBuffer(ByteBuffer inputBuffer, boolean isAudioSample,MediaCodec.BufferInfo bufferInfo){



    }*/


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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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


        // Test setLocation out of bound cases
       /* try {
            muxer.setLocation(BAD_LATITUDE, LONGITUDE);
            fail("setLocation succeeded with bad argument: [" + BAD_LATITUDE + "," + LONGITUDE
                    + "]");
        } catch (IllegalArgumentException e) {
            // Expected
        }
        try {
            muxer.setLocation(LATITUDE, BAD_LONGITUDE);
            fail("setLocation succeeded with bad argument: [" + LATITUDE + "," + BAD_LONGITUDE
                    + "]");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        muxer.setLocation(LATITUDE, LONGITUDE);  */

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
