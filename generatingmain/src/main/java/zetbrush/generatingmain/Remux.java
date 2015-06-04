/*
package zetbrush.com.generatingmain;

import org.jcodec.common.SeekableByteChannel;
import org.jcodec.containers.mp4.Brand;
import org.jcodec.containers.mp4.MP4Packet;
import org.jcodec.containers.mp4.boxes.AudioSampleEntry;
import org.jcodec.containers.mp4.demuxer.AbstractMP4DemuxerTrack;
import org.jcodec.containers.mp4.demuxer.MP4Demuxer;
import org.jcodec.containers.mp4.muxer.FramesMP4MuxerTrack;
import org.jcodec.containers.mp4.muxer.MP4Muxer;
import org.jcodec.containers.mp4.muxer.PCMMP4MuxerTrack;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.jcodec.common.NIOUtils.readableFileChannel;
import static org.jcodec.common.NIOUtils.writableFileChannel;
import static org.jcodec.containers.mp4.TrackType.VIDEO;

*/
/**
 * This class is part of JCodec ( www.jcodec.org ) This software is distributed
 * under FreeBSD License
 * 
 * @author The JCodec project
 * 
 *//*

public class Remux {


       public  Remux(){

       }


    public void remuxcustom(File tgt, File audiosrcmp4, File videosourcemp4) throws IOException {
        SeekableByteChannel inputForAudio = null;
        SeekableByteChannel inputForVideo = null;
        SeekableByteChannel output = null;
        try {
            inputForAudio = readableFileChannel(audiosrcmp4);
            inputForVideo = readableFileChannel(videosourcemp4);

            output = writableFileChannel(tgt);

            MP4Demuxer demuxerForAudio = new MP4Demuxer(inputForAudio);
            MP4Demuxer demuxerForVideo = new MP4Demuxer(inputForVideo);

            MP4Muxer muxer = new MP4Muxer(output, Brand.MOV);

            List<AbstractMP4DemuxerTrack> at = demuxerForAudio.getAudioTracks();
            List<PCMMP4MuxerTrack> audioTracks = new ArrayList<PCMMP4MuxerTrack>();
            for (AbstractMP4DemuxerTrack demuxerTrack : at) {
                PCMMP4MuxerTrack att = muxer.addUncompressedAudioTrack(((AudioSampleEntry) demuxerTrack
                        .getSampleEntries()[0]).getFormat());
                audioTracks.add(att);
                att.setEdits(demuxerTrack.getEdits());
                att.setName(demuxerTrack.getName());

            }

            AbstractMP4DemuxerTrack vt = demuxerForVideo.getVideoTrack();
            FramesMP4MuxerTrack video = muxer.addTrackForCompressed(VIDEO, (int) vt.getTimescale());
            video.setTimecode(muxer.addTimecodeTrack((int) vt.getTimescale()));
            video.setEdits(vt.getEdits());
            video.addSampleEntries(vt.getSampleEntries());
            MP4Packet pkt = null;

            while ((pkt = (MP4Packet) vt.nextFrame()) != null) {
                pkt = processFrame(pkt);
                video.addFrame(pkt);

                for (int i = 0; i < at.size(); i++) {
                    AudioSampleEntry ase = (AudioSampleEntry) at.get(i).getSampleEntries()[0];
                    int frames = (int) (ase.getSampleRate() * pkt.getDuration() / vt.getTimescale());
                    MP4Packet apkt = (MP4Packet) at.get(i).nextFrame();
                    audioTracks.get(i).addSamples(apkt.getData());
                }
            }

            muxer.writeHeader();



        } finally {
            if (inputForAudio != null)
                inputForAudio.close();
            if (inputForVideo != null)
                inputForVideo.close();
            if (output != null)
                output.close();
        }


    }


    public void remux(File tgt, File src) throws IOException {
        SeekableByteChannel input = null;
        SeekableByteChannel output = null;
        try {
            input = readableFileChannel(src);
            output = writableFileChannel(tgt);
            MP4Demuxer demuxer = new MP4Demuxer(input);
            MP4Muxer muxer = new MP4Muxer(output, Brand.MOV);


            List<AbstractMP4DemuxerTrack> at = demuxer.getAudioTracks();
            List<PCMMP4MuxerTrack> audioTracks = new ArrayList<PCMMP4MuxerTrack>();
            for (AbstractMP4DemuxerTrack demuxerTrack : at) {
                PCMMP4MuxerTrack att = muxer.addUncompressedAudioTrack(((AudioSampleEntry) demuxerTrack
                        .getSampleEntries()[0]).getFormat());
                audioTracks.add(att);
                att.setEdits(demuxerTrack.getEdits());
                att.setName(demuxerTrack.getName());
            }

            AbstractMP4DemuxerTrack vt = demuxer.getVideoTrack();
            FramesMP4MuxerTrack video = muxer.addTrackForCompressed(VIDEO, (int) vt.getTimescale());
            // vt.open(input);
            video.setTimecode(muxer.addTimecodeTrack((int) vt.getTimescale()));
            video.setEdits(vt.getEdits());
            video.addSampleEntries(vt.getSampleEntries());
            MP4Packet pkt = null;
            while ((pkt = (MP4Packet)vt.nextFrame()) != null) {
                pkt = processFrame(pkt);
                video.addFrame(pkt);

                for (int i = 0; i < at.size(); i++) {
                    AudioSampleEntry ase = (AudioSampleEntry) at.get(i).getSampleEntries()[0];
                    int frames = (int) (ase.getSampleRate() * pkt.getDuration() / vt.getTimescale());
                    MP4Packet apkt = (MP4Packet)at.get(i).nextFrame();
                    audioTracks.get(i).addSamples(apkt.getData());
                }
            }

            muxer.writeHeader();
        } finally {
            if (input != null)
                input.close();
            if (output != null)
                output.close();
        }
    }

    protected MP4Packet processFrame(MP4Packet pkt) {
        return pkt;
    }

    public static File hidFile(File tgt) {
        File src = new File(tgt.getParentFile(), "." + tgt.getName());
        if (src.exists()) {
            int i = 1;
            do {
                src = new File(tgt.getParentFile(), "." + tgt.getName() + "." + (i++));
            } while (src.exists());
        }
        return src;
    }
}
*/
