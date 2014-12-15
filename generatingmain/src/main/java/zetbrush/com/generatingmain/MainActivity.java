package zetbrush.com.generatingmain;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import org.jcodec.codecs.h264.H264Encoder;
import org.jcodec.codecs.h264.H264Utils;
import org.jcodec.common.FileChannelWrapper;
import org.jcodec.common.NIOUtils;
import org.jcodec.common.model.ColorSpace;


import org.jcodec.common.model.Picture;
import org.jcodec.containers.mp4.TrackType;
import org.jcodec.containers.mp4.Brand;
import org.jcodec.containers.mp4.MP4Packet;
import org.jcodec.containers.mp4.muxer.FramesMP4MuxerTrack;
import org.jcodec.containers.mp4.muxer.MP4Muxer;
import org.jcodec.scale.RgbToYuv420p;

import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {

    Button makeVideoButton;
    Button playButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       makeVideoButton = (Button)findViewById(R.id.makeVideoBut);




    }



    @Override
    protected void onResume(){
        super.onResume();





    }

    public void makevideoClick(View v){

        try{

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

            String path= "assets/me";
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
                    InputStream istr = assetMgr.open("image_"+"/"+String.format("%03d", i + 1)+".png");
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

                H264Utils.encodeMOVPacket(result, spsList, ppsList);
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

        }

    }


    }







    static void makeDirectory(String dir)
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
}
