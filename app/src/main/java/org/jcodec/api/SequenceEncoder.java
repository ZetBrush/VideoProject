package org.jcodec.api;

import com.coremedia.iso.boxes.Container;
import com.coremedia.iso.boxes.sampleentry.SampleEntry;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.tracks.AACTrackImpl;
import com.googlecode.mp4parser.authoring.tracks.H264TrackImpl;



import org.jcodec.codecs.h264.H264Encoder;
import org.jcodec.codecs.h264.H264Utils;
import org.jcodec.common.NIOUtils;
import org.jcodec.common.SeekableByteChannel;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.containers.mkv.MKVDemuxer;
import org.jcodec.containers.mkv.MKVMuxer;
import org.jcodec.containers.mp4.Brand;
import org.jcodec.containers.mp4.MP4Packet;
import org.jcodec.containers.mp4.TrackType;
import org.jcodec.containers.mp4.muxer.FramesMP4MuxerTrack;
import org.jcodec.containers.mp4.muxer.MP4Muxer;
import org.jcodec.scale.ColorUtil;
import org.jcodec.scale.Transform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
 * This class is part of JCodec ( www.jcodec.org ) This software is distributed
 * under FreeBSD License
 * 
 * @author The JCodec project
 * 
 */
public class SequenceEncoder {
    private SeekableByteChannel ch;
    private SeekableByteChannel chm;
    private Picture toEncode;
    private Transform transform;
    private H264Encoder encoder;
    private ArrayList<ByteBuffer> spsList;
    private ArrayList<ByteBuffer> ppsList;
    private FramesMP4MuxerTrack outTrack;
    private ByteBuffer _out;
    private int frameNo;
    private MP4Muxer muxer;
    private static int framesec =1;

    public static void setFrameDuration(int sec){
       framesec = sec;
    }

    public SequenceEncoder(File out ) throws IOException {
        this.ch = NIOUtils.writableFileChannel(out);

       // this.chm = NIOUtils.writableFileChannel(mus);
        // Muxer that will store the encoded frames
        muxer = new MP4Muxer(ch, Brand.MOV);
      //  muxer.addTrackForCompressed(TrackType.SOUND, 5);

      //  muxer.addCompressedAudioTrack("acc",10, 2, 256,44100,null);
        // Add video track to muxer
        outTrack = muxer.addTrackForCompressed(TrackType.VIDEO, 1);
        // outTrack = muxer.addTrackForCompressed(TrackType.SOUND, 1);


        // Allocate a buffer big enough to hold output frames
        _out = ByteBuffer.allocate(1920 * 1080 * 6);

        // Create an instance of encoder
        encoder = new H264Encoder();

        // Transform to convert between RGB and YUV
        transform = ColorUtil.getTransform(ColorSpace.RGB, encoder.getSupportedColorSpaces()[0]);

        // Encoder extra data ( SPS, PPS ) to be stored in a special place of
        // MP4
        spsList = new ArrayList<ByteBuffer>();
        ppsList = new ArrayList<ByteBuffer>();

    }

    public void encodeNativeFrame(Picture pic) throws IOException {
        if (toEncode == null) {
            toEncode = Picture.create(pic.getWidth(), pic.getHeight(), encoder.getSupportedColorSpaces()[0]);
        }

        // Perform conversion
        transform.transform(pic, toEncode);

        // Encode image into H.264 frame, the result is stored in '_out' buffer
        _out.clear();
        ByteBuffer result = encoder.encodeFrame(toEncode, _out);

        // Based on the frame above form correct MP4 packet
        spsList.clear();
        ppsList.clear();
        H264Utils.wipePS(result, spsList, ppsList);
        H264Utils.encodeMOVPacket(result);


        // Add packet to video track
        outTrack.addFrame(new MP4Packet(
                result,
                frameNo*framesec,
                1,
                framesec,
                frameNo,
                true,
                null,
                frameNo*framesec,
                0));

        frameNo++;
    }

    public void finish() throws IOException {
        // Push saved SPS/PPS to a special storage in MP4

      //  org.jcodec.containers.mp4.boxes.SampleEntry smpentry = H264Utils.createMOVSampleEntry(spsList, ppsList);
        outTrack.addSampleEntry(H264Utils.createMOVSampleEntry(spsList, ppsList));

        // Write MP4 header and finalize recording

        muxer.writeHeader();

        NIOUtils.closeQuietly(ch);
    }
    }


    //public void addAudioTrack(ByteBuffer result){




        /*try {



            AACTrackImpl aacTrack = new AACTrackImpl(new FileDataSourceImpl("/storage/removable/sdcard1/DCIM/100ANDRO/newfold/strangeclouds.aac"));
            H264TrackImpl h264Track = new H264TrackImpl(new FileDataSourceImpl( new FileInputStream(result).getChannel()));
            Movie movie = new Movie();
            movie.addTrack(h264Track);
            movie.addTrack(aacTrack);
            Container mp4file = new DefaultMp4Builder().build(movie);
            FileChannel fc = new FileOutputStream(new File("myoutput.mp4")).getChannel();
            mp4file.writeContainer(fc);
            fc.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

