/*
package zetbrush.com.generatingmain;

import android.util.Log;

import com.coremedia.iso.boxes.SampleDescriptionBox;
import com.coremedia.iso.boxes.sampleentry.AudioSampleEntry;
import com.googlecode.mp4parser.DataSource;
import com.googlecode.mp4parser.authoring.AbstractTrack;
import com.googlecode.mp4parser.authoring.Sample;
import com.googlecode.mp4parser.authoring.SampleImpl;
import com.googlecode.mp4parser.authoring.TrackMetaData;
import com.googlecode.mp4parser.boxes.mp4.ESDescriptorBox;
import com.googlecode.mp4parser.boxes.mp4.objectdescriptors.BitReaderBuffer;
import com.googlecode.mp4parser.boxes.mp4.objectdescriptors.DecoderConfigDescriptor;
import com.googlecode.mp4parser.boxes.mp4.objectdescriptors.ESDescriptor;
import com.googlecode.mp4parser.boxes.mp4.objectdescriptors.SLConfigDescriptor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

*/
/**
 * MPEG V1 Layer 3 Audio. Does not support IDv3 or any other tags. Only raw stream of MP3 frames.
 * See <a href="http://mpgedit.org/mpgedit/mpeg_format/mpeghdr.htm">http://mpgedit.org/mpgedit/mpeg_format/mpeghdr.htm</a>
 * for stream format description.
 *
 * @author Roman Elizarov
 *//*

public class MP3TrackImplNew extends AbstractTrack {
    private static final int MPEG_V1 = 0x3; // only support V1
    private static final int MPEG_L3 = 1; // only support L3
    private static final int[] SAMPLE_RATE = {44100, 48000, 32000, 0};
    private static final int[] BIT_RATE = {0, 32000, 40000, 48000, 56000, 64000, 80000, 96000, 112000, 128000, 160000, 192000, 224000, 256000, 320000, 0};
    private static final int SAMPLES_PER_FRAME = 1152; // Samples per L3 frame

    private static final int ES_OBJECT_TYPE_INDICATION = 0x6b;
    private static final int ES_STREAM_TYPE = 5;
    private final DataSource dataSource;

    TrackMetaData trackMetaData = new TrackMetaData();
    SampleDescriptionBox sampleDescriptionBox;
    MP3Header firstHeader;

    long maxBitRate;
    long avgBitRate;

    private List<Sample> samples;
    private long[] durations;


    public MP3TrackImplNew(DataSource channel) throws IOException {
        this(channel, "eng");
    }

    public void close() throws IOException {
        dataSource.close();
    }

    public MP3TrackImplNew(DataSource dataSource, String lang) throws IOException {
        super(dataSource.toString());
        this.dataSource = dataSource;
        samples = new LinkedList<Sample>();
        firstHeader = readSamples(dataSource);

        double packetsPerSecond = (double) firstHeader.sampleRate / SAMPLES_PER_FRAME;
        double duration = samples.size() / packetsPerSecond;

        long dataSize = 0;
        LinkedList<Integer> queue = new LinkedList<Integer>();
        for (Sample sample : samples) {
            int size = (int) sample.getSize();
            dataSize += size;
            queue.add(size);
            while (queue.size() > packetsPerSecond) {
                queue.pop();
            }
            if (queue.size() == (int) packetsPerSecond) {
                int currSize = 0;
                for (Integer aQueue : queue) {
                    currSize += aQueue;
                }
                double currBitRate = 8.0 * currSize / queue.size() * packetsPerSecond;
                if (currBitRate > maxBitRate) {
                    maxBitRate = (int) currBitRate;
                }
            }
        }

        avgBitRate = (int) (8 * dataSize / duration);

        sampleDescriptionBox = new SampleDescriptionBox();
        AudioSampleEntry audioSampleEntry = new AudioSampleEntry("mp4a");
        audioSampleEntry.setChannelCount(firstHeader.channelCount);
        audioSampleEntry.setSampleRate(firstHeader.sampleRate);
        audioSampleEntry.setDataReferenceIndex(1);
        audioSampleEntry.setSampleSize(16);


        ESDescriptorBox esds = new ESDescriptorBox();
        ESDescriptor descriptor = new ESDescriptor();
        descriptor.setEsId(0);

        SLConfigDescriptor slConfigDescriptor = new SLConfigDescriptor();
        slConfigDescriptor.setPredefined(2);
        descriptor.setSlConfigDescriptor(slConfigDescriptor);

        DecoderConfigDescriptor decoderConfigDescriptor = new DecoderConfigDescriptor();
        decoderConfigDescriptor.setObjectTypeIndication(ES_OBJECT_TYPE_INDICATION);
        decoderConfigDescriptor.setStreamType(ES_STREAM_TYPE);
        decoderConfigDescriptor.setMaxBitRate(maxBitRate);
        decoderConfigDescriptor.setAvgBitRate(avgBitRate);
        descriptor.setDecoderConfigDescriptor(decoderConfigDescriptor);

        ByteBuffer data = descriptor.serialize();
        esds.setData(data);
        audioSampleEntry.addBox(esds);
        sampleDescriptionBox.addBox(audioSampleEntry);

        trackMetaData.setCreationTime(new Date());
        trackMetaData.setModificationTime(new Date());
        trackMetaData.setLanguage(lang);
        trackMetaData.setVolume(1);
        trackMetaData.setTimescale(firstHeader.sampleRate); // Audio tracks always use sampleRate as timescale
        durations = new long[samples.size()];
        Arrays.fill(durations, SAMPLES_PER_FRAME);
    }

    public SampleDescriptionBox getSampleDescriptionBox() {
        return sampleDescriptionBox;
    }

    public long[] getSampleDurations() {
        return durations;
    }

    public TrackMetaData getTrackMetaData() {
        return trackMetaData;
    }

    public String getHandler() {
        return "soun";
    }

    public List<Sample> getSamples() {
        return samples;
    }

    class MP3Header {


        int getFrameLength() {
            return 144 * bitRate / sampleRate + padding;
        } private final int HEADER_SIZE = 4;
        private final int[][] bitrateTable = {
                { -1, -1, -1, -1, -1 },
                { 32, 32, 32, 32, 8 },
                { 64, 48, 40, 48, 16 },
                { 96, 56, 48, 56, 24 },
                { 128, 64, 56, 64, 32 },
                { 160, 80, 64, 80, 40 },
                { 192, 96, 80, 96, 48 },
                { 224, 112, 96, 112, 56 },
                { 256, 128, 112, 128, 64 },
                { 288, 160, 128, 144, 80 },
                { 320, 192, 160, 160, 96 },
                { 352, 224, 192, 176, 112 },
                { 384, 256, 224, 192, 128 },
                { 416, 320, 256, 224, 144 },
                { 448, 384, 320, 256, 160 },
                { -1, -1, -1, -1, -1 } };
        private final int[][] sampleTable = {
                { 44100, 22050, 11025 },
                { 48000, 24000, 12000 },
                { 32000, 16000, 8000 },
                { -1, -1, -1 } };
        private final String[] versionLabels = { "MPEG Version 2.5", null,
                "MPEG Version 2.0",
                "MPEG Version 1.0" };
        private final String[] layerLabels = { null, "Layer III", "Layer II",
                "Layer I" };
        private final String[] channelLabels = { "Stereo", "Joint Stereo (STEREO)",
                "Dual Channel (STEREO)",
                "Single Channel (MONO)" };
        private final String[] emphasisLabels = { "none", "50/15 ms", null,
                "CCIT J.17" };
        private final int MPEG_V_25 = 0;
        private final int MPEG_V_2 = 2;
        private final int MPEG_V_1 = 3;
        private final int MPEG_L_3 = 1;
        private final int MPEG_L_2 = 2;
        private final int MPEG_L_1 = 3;

         File mp3 = null;


         boolean copyrighted;
         boolean crced;
         boolean original;
         int emphasis;
         int mpegVersion;
         int layer;
         int protectionAbsent;

         int bitRateIndex;
         int bitRate;

         int sampleFrequencyIndex;
         int sampleRate;

         int padding;
         int channelMode;
         int channelCount;
        */
/**
         * Create an MPEGAudioFrameHeader from the file specified.  Upon creation
         * information will be read in from the first frame header the object
         * encounters in the file.
         *
         * @param mp3 the file to read from
         * @exception NoMPEGFramesException if the file is not a valid mpeg
         * @exception java.io.FileNotFoundException if an error occurs
         * @exception IOException if an error occurs
         *//*

        public MP3Header( File mp3 )
                throws NoMPEGFramesException, FileNotFoundException, IOException {

            this( mp3, 0 );
        }

        */
/**
         * Create an MPEGAudioFrameHeader from the file specified.  Upon creation
         * information will be read in from the first frame header the object
         * encounters in the file.  The offset tells the object where to start
         * searching for an MPEG frame.  If you know the size of an id3v2 tag
         * attached to the file and pass it to this ctor, it will take less time
         * to find the frame.
         *
         * @param mp3 the file to read from
         * @param offset the offset to start searching from
         * @exception NoMPEGFramesException if the file is not a valid mpeg
         * @exception FileNotFoundException if an error occurs
         * @exception IOException if an error occurs
         *//*

        public MP3Header( File mp3, int offset )
                throws NoMPEGFramesException, FileNotFoundException, IOException {

            this.mp3 = mp3;

            mpegVersion = -1;
            layer = -1;
            bitRate = -1;
            sampleRate = -1;
            channelMode = -1;
            copyrighted = false;
            crced = false;
            original = false;
            emphasis = -1;

            long location = findFrame( offset );

            if( location != -1 ) {
                readHeader( location );
            }
            else {
                throw new NoMPEGFramesException();
            }
        }

        */
/**
         * Searches through the file and finds the first occurrence of an mpeg
         * frame.  Returns the location of the header of the frame.
         *
         * @param offset the offset to start searching from
         * @return the location of the header of the frame
         * @exception FileNotFoundException if an error occurs
         * @exception IOException if an error occurs
         *//*

        private long findFrame( int offset )
                throws FileNotFoundException, IOException {

            RandomAccessFile raf = new RandomAccessFile( mp3, "r" );
            byte test;
            long loc = -1;
            raf.seek( offset );

            while( loc == -1 ) {
                test = raf.readByte();

                if( BinaryParser.matchPattern( test, "11111111" ) ) {
                    test = raf.readByte();

                    if( BinaryParser.matchPattern( test, "111xxxxx" ) ) {
                        loc = raf.getFilePointer() - 2;
                    }
                }
            }

            raf.close();

            return loc;
        }

        */
/**
         * Read in all the information found in the mpeg header.
         *
         * @param location the location of the header (found by findFrame)
         * @exception FileNotFoundException if an error occurs
         * @exception IOException if an error occurs
         *//*

        private void readHeader( long location )
                throws FileNotFoundException, IOException {

            RandomAccessFile raf = new RandomAccessFile( mp3, "r" );
            byte[] head = new byte[HEADER_SIZE];
            raf.seek( location );

            if( raf.read( head ) != HEADER_SIZE ) {
                throw new IOException("Error reading MPEG frame header.");
            }

            mpegVersion = BinaryParser.convertToDecimal( head[1], 3, 4 );
            layer = BinaryParser.convertToDecimal( head[1], 1, 2 );
            findBitRate( BinaryParser.convertToDecimal( head[2], 4, 7 ) );
            findSampleRate( BinaryParser.convertToDecimal( head[2], 2, 3 ) );
            channelMode = BinaryParser.convertToDecimal( head[3], 6, 7 );
            copyrighted = BinaryParser.bitSet( head[3], 3 );
            crced = !BinaryParser.bitSet( head[1], 0 );
            original = BinaryParser.bitSet( head[3], 2 );
            emphasis = BinaryParser.convertToDecimal( head[3], 0, 1 );
        }

        */
/**
         * Based on the bitrate index found in the header, try to find and set the
         * bitrate from the table.
         *
         * @param bitrateIndex the bitrate index read from the header
         *//*

        private void findBitRate( int bitrateIndex ) {
            int ind = -1;

            if( mpegVersion == MPEG_V_1 ) {
                if( layer == MPEG_L_1 ) {
                    ind = 0;
                }
                else if( layer == MPEG_L_2 ) {
                    ind = 1;
                }
                else if( layer == MPEG_L_3 ) {
                    ind = 2;
                }
            }
            else if( (mpegVersion == MPEG_V_2) || (mpegVersion == MPEG_V_25) ) {
                if( layer == MPEG_L_1 ) {
                    ind = 3;
                }
                else if( (layer == MPEG_L_2) || (layer == MPEG_L_3) ) {
                    ind = 4;
                }
            }

            if( (ind != -1) && (bitrateIndex >= 0) && (bitrateIndex <= 15) ) {
                bitRate = bitrateTable[bitrateIndex][ind];
            }
        }

        */
/**
         * Based on the sample rate index found in the header, attempt to lookup
         * and set the sample rate from the table.
         *
         * @param sampleIndex the sample rate index read from the header
         *//*

        private void findSampleRate( int sampleIndex ) {
            int ind = -1;

            switch( mpegVersion ) {
                case MPEG_V_1:
                    ind = 0;
                    break;
                case MPEG_V_2:
                    ind = 1;
                    break;
                case MPEG_V_25:
                    ind = 2;
            }

            if( (ind != -1) && (sampleIndex >= 0) && (sampleIndex <= 3) ) {
                sampleRate = sampleTable[sampleIndex][ind];
            }
        }

        */
/**
         * Return a string representation of this object.  Includes all information
         * read in.
         *
         * @return a string representation of this object
         *//*

        public String toString() {
            return getVersion() + " " + getLayer() + "\nBitRate:\t\t\t" +
                    getBitRate() + "kbps\nSampleRate:\t\t\t" + getSampleRate() +
                    "Hz\nChannelMode:\t\t\t" + getChannelMode() +
                    "\nCopyrighted:\t\t\t" + isCopyrighted() + "\nOriginal:\t\t\t" +
                    isOriginal() + "\nCRC:\t\t\t\t" + isProtected() +
                    "\nEmphasis:\t\t\t" + getEmphasis();
        }

        */
/**
         * Return the version of the mpeg in string form.  Ex: MPEG Version 1.0
         *
         * @return the version of the mpeg
         *//*

        public String getVersion() {
            String str = null;

            if( (mpegVersion >= 0) && (mpegVersion < versionLabels.length) ) {
                str = versionLabels[mpegVersion];
            }

            return str;
        }

        */
/**
         * Return the layer description of the mpeg in string form.
         * Ex: Layer III
         *
         * @return the layer description of the mpeg
         *//*

        public String getLayer() {
            String str = null;

            if( (layer >= 0) && (layer < layerLabels.length) ) {
                str = layerLabels[layer];
            }

            return str;
        }

        */
/**
         * Return the channel mode of the mpeg in string form.
         * Ex: Joint Stereo (STEREO)
         *
         * @return the channel mode of the mpeg
         *//*

        public String getChannelMode() {
            String str = null;

            if( (channelMode >= 0) && (channelMode < channelLabels.length) ) {
                str = channelLabels[channelMode];
            }

            return str;
        }

        */
/**
         * Returns the bitrate of the mpeg in kbps
         *
         * @return the bitrate of the mpeg in kbps
         *//*

        public int getBitRate() {
            return bitRate;
        }

        */
/**
         * Returns the sample rate of the mpeg in Hz
         *
         * @return the sample rate of the mpeg in Hz
         *//*

        public int getSampleRate() {
            return sampleRate;
        }

        */
/**
         * Returns true if the audio is copyrighted
         *
         * @return true if the audio is copyrighted
         *//*

        public boolean isCopyrighted() {
            return copyrighted;
        }

        */
/**
         * Returns true if this mpeg is protected by CRC
         *
         * @return true if this mpeg is protected by CRC
         *//*

        public boolean isProtected() {
            return crced;
        }

        */
/**
         * Returns true if this is the original media
         *
         * @return true if this is the original media
         *//*

        public boolean isOriginal() {
            return original;
        }

        */
/**
         * Returns the emphasis.  I don't know what this means, it just does it...
         *
         * @return the emphasis
         *//*

        public String getEmphasis() {
            String str = null;

            if( (emphasis >= 0) && (emphasis < emphasisLabels.length) ) {
                str = emphasisLabels[emphasis];
            }

            return str;
        }

        */
/**
         * Returns true if the file passed to the constructor is an mp3 (MPEG
         * layer III).
         *
         * @return true if the file is an mp3
         *//*

        public boolean isMP3() {
            return (layer == MPEG_L_3);
        }

    } // MPEGAudioFrameHeader

    private MP3Header readSamples(DataSource channel) throws IOException {
        MP3Header first = null;
        while (true) {
            long pos = channel.position();
            MP3Header hdr;
            if ((hdr = readMP3Header(channel)) == null)
                break;
            if (first == null)
                first = hdr;
            channel.position(pos);
            ByteBuffer data = ByteBuffer.allocate(hdr.getFrameLength());
            channel.read(data);
            data.rewind();
            samples.add(new SampleImpl(data));
        }
        return first;
    }

    private MP3Header readMP3Header(DataSource channel) throws IOException {

        MP3Header hdr=null;// = new MP3Header();
        ByteBuffer bb = ByteBuffer.allocate(4);
        while (bb.position() < 4) {
            if (channel.read(bb) == -1) {
                return null;
            }
        }

        BitReaderBuffer brb = new BitReaderBuffer((ByteBuffer) bb.rewind());
        int sync = brb.readBits(11); // A
        if (sync != 0x7ff){
            Log.d("Sync bits", String.valueOf(sync));
            throw new IOException("Expected Start Word 0x7ff");
        }
        hdr.mpegVersion = brb.readBits(1); // B

        if (hdr.mpegVersion != MPEG_V1)
           throw new IOException("Expected MPEG Version 1 (ISO/IEC 11172-3)");
        hdr.layer = brb.readBits(2); // C

        if (hdr.layer != MPEG_L3)
           throw new IOException("Expected Layer III");

        hdr.protectionAbsent = brb.readBits(1); // D

        hdr.bitRateIndex = brb.readBits(4); // E
        hdr.bitRate = BIT_RATE[hdr.bitRateIndex];
        if (hdr.bitRate == 0)
           throw new IOException("Unexpected (free/bad) bit rate");

        hdr.sampleFrequencyIndex = brb.readBits(2);
        hdr.sampleRate = SAMPLE_RATE[hdr.sampleFrequencyIndex]; // F
        if (hdr.sampleRate == 0)
            throw new IOException("Unexpected (reserved) sample rate frequency");

        hdr.padding = brb.readBits(1); // G padding
        brb.readBits(1); // H private

        hdr.channelMode = brb.readBits(2); // I
        hdr.channelCount = hdr.channelMode == 3 ? 1 : 2;
        return hdr;
    }

    @Override
    public String toString() {
        return "MP3TrackImpl";
    }
}
*/
