package zetbrush.com.generatingmain;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.XmlBox;
import com.googlecode.mp4parser.util.Path;

import org.jcodec.api.JCodecException;
import org.jcodec.api.android.FrameGrab;
import org.jcodec.api.android.SequenceEncoder;
import org.jcodec.common.FileChannelWrapper;
import org.jcodec.common.NIOUtils;
import org.jcodec.common.SeekableByteChannel;
import org.jcodec.containers.mp4.Brand;
import org.jcodec.containers.mp4.MP4Packet;
import org.jcodec.containers.mp4.boxes.AudioSampleEntry;
import org.jcodec.containers.mp4.demuxer.AbstractMP4DemuxerTrack;
import org.jcodec.containers.mp4.demuxer.MP4Demuxer;
import org.jcodec.containers.mp4.muxer.FramesMP4MuxerTrack;
import org.jcodec.containers.mp4.muxer.MP4Muxer;
import org.jcodec.containers.mp4.muxer.PCMMP4MuxerTrack;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.List;

import ar.com.daidalos.afiledialog.FileChooserDialog;

import static org.jcodec.common.NIOUtils.readableFileChannel;
import static org.jcodec.common.NIOUtils.writableFileChannel;
import static org.jcodec.containers.mp4.TrackType.VIDEO;

public class MainActivity extends ActionBarActivity {

    private Button makeVideoButton;
    private  Button playButton;
    private TextView progress;
    private volatile boolean flag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       makeVideoButton = (Button)findViewById(R.id.makeVideoBut);
        progress = (TextView)findViewById(R.id.progress);



    }



    @Override
    protected void onResume(){
        super.onResume();





    }
    private static int count =0;
    private class Encoder extends AsyncTask<File, Integer, Integer> {
        private static final String TAG = "ENCODER";

        protected Integer doInBackground(File... params) {

            SequenceEncoder se = null;
            try {
                se = new SequenceEncoder(new File(params[0].getParentFile(),
                        "vid_enc.mp4"));

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
                se.addAudioTrack();
            } catch (IOException e) {
                Log.e(TAG, "IO", e);
            }

           // progress.setText("done!");
            return 0;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progress.setText("processed "+String.valueOf(values[0]));
        }


    }


    public void makevideoClick(View v){

        FileChooserDialog dialog = new FileChooserDialog(v.getContext());
        dialog.addListener(new FileChooserDialog.OnFileSelectedListener() {
            @Override
            public void onFileSelected(Dialog source, File folder, String name) {
            }

            @Override
            public void onFileSelected(Dialog source, File file) {
                source.hide();
                String digits = file.getName().replaceAll("\\D+(\\d+)\\D+",
                        "$1");
                String mask = file.getName().replaceAll("(\\D+)\\d+(\\D+)",
                        "$1%0" + digits.length() + "d$2");

                new Encoder().execute(new File(file.getParentFile(), mask));
            }
        });
        dialog.show();

        File sdCard = Environment.getExternalStorageDirectory();
        for (int i = 0; i < 100; i++) {
            try {
                FileInputStream fileInputStream = new FileInputStream(new File(sdCard, "DCIM/"));
                IsoFile isoFile = new IsoFile(String.valueOf(fileInputStream.getChannel()));
               // IsoFile isoFile = new IsoFile(Channels.newChannel(new FileInputStream(this.filePath)));
                //Path path = new Path(isoFile);
                XmlBox xmlBox = (XmlBox) Path.getPath(isoFile, "/moov/meta/xml ");
                String xml = xmlBox.getXml();
                //System.err.println(xml;
            } catch (IOException e) {

            }
        }






    }


       /* try{

            int fps=24;
            RgbToYuv420p transform = new RgbToYuv420p(0, 0);

            FileChannelWrapper ch = NIOUtils.writableFileChannel(SDPathToFile("", "out.mp4"));
            final MP4Muxer muxer = new MP4Muxer(ch, Brand.MP4);

            // Add a video track
            FramesMP4MuxerTrack outTrack = muxer.addTrackForCompressed(TrackType.VIDEO, (int)fps);

            // Create H.264 encoder
            H264Encoder encoder = new H264Encoder(); // not we could use a rate control in the constructor

            // Allocate a buffer that would hold an encoded frame
            ByteBuffer _out = ByteBuffer.allocate(640 * 480 * 6); //Not sur about RGB

            // Allocate storage for SPS/PPS, they need to be stored separately in a special place of MP4 file
            ArrayList<ByteBuffer> spsList = new ArrayList<ByteBuffer>();
            ArrayList<ByteBuffer> ppsList = new ArrayList<ByteBuffer>();


            final int numberOfimage = 2;

            String path= "src/main/";
            int num = 0;
            Picture rgb = null;

            Picture yuv=null;
            //for (File file : directory.listFiles()) {

            int[] packed = null;
            for (int i=0; i<numberOfimage; i++) {
                System.out.println("Num"+num);
                Bitmap bitmap=null;
                try {

                    AssetManager assetMgr = this.getAssets();

                    InputStream istr = assetMgr.open("image_" + String.format("%03d", i + 1) + ".png");
                    bitmap = BitmapFactory.decodeStream(istr);
                    //  bitmap=bitmap.copy(Bitmap.Config.ARGB_8888,true);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    //System.out.println("Could not read image "+file);
                }
                if(rgb == null) {
                    rgb = Picture.create((int)bitmap.getWidth(), (int)bitmap.getHeight(), ColorSpace.RGB);//YUV420);//RGB);
                }

                int[] dstData = rgb.getPlaneData(0);
                if (packed==null)
                    packed = new int[bitmap.getWidth() * bitmap.getHeight()];

                bitmap.getPixels(packed, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(),
                        bitmap.getHeight());

                for (int iu = 0, srcOff = 0, dstOff = 0; iu < bitmap.getHeight(); iu++) {
                    for (int j = 0; j < bitmap.getWidth(); j++, srcOff++, dstOff += 3) {
                        int rgbo = packed[srcOff];
                        dstData[dstOff] = (rgbo >> 16) & 0xff;
                        dstData[dstOff + 1] = (rgbo >> 8) & 0xff;
                        dstData[dstOff + 2] = rgbo & 0xff;
                    }
                }
                bitmap.recycle();


                if (yuv==null)
                    yuv = Picture.create(rgb.getWidth(), rgb.getHeight(), ColorSpace.YUV420);
                transform.transform(rgb, yuv);

                // rgb = null;

                ByteBuffer result = encoder.encodeFrame(yuv,_out); //toEncode
                _out.clear();
                // yuv = null;



              // ByteBuffer result = encoder.encodeFrame(_out, yuv); //toEncode

                //  yuv = null;
                spsList.clear();
                ppsList.clear();

                H264Utils.encodeMOVPacket(result,spsList,ppsList);
                outTrack.addFrame(new MP4Packet(result,num,(int)fps, 1, num, true, null, num, 0));
                result = null;
                System.gc();

                num++;

            }
            yuv=null;
            packed=null;
            System.gc();
            outTrack.addSampleEntry(H264Utils.createMOVSampleEntry(spsList, ppsList));
            // Write MP4 header and finalize recording
            muxer.writeHeader();
            NIOUtils.closeQuietly(ch);
        } catch (Exception e) {
            e.printStackTrace();

        }*/




  /*  static void makeDirectory(String dir)
    {
        File extBaseDir = Environment.getExternalStorageDirectory();
        File file = new File(extBaseDir.getAbsoluteFile()+"/"+dir);
        if(!file.exists()){
            if(!file.mkdirs()){
                //  throw new Exception("Could not create directories, "+file.getAbsolutePath());
            }
        }
    }

    static File SDPathToFile(String filePatho, String fileName)
    {
        File extBaseDir = Environment.getExternalStorageDirectory();
        if (filePatho==null||filePatho.length()==0||filePatho.charAt(0)!='/')
            filePatho="/"+filePatho;
        makeDirectory(filePatho);
        File file = new File(extBaseDir.getAbsoluteFile()+filePatho);
        return new File(file.getAbsolutePath()+"/"+fileName);//file;
    }*/



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
}
