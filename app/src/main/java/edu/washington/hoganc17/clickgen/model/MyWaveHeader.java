package edu.washington.hoganc17.clickgen.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


// Code from: https://stackoverflow.com/questions/40929243/how-to-mix-two-wav-files-without-noise

public class MyWaveHeader {

    private static final String TAG = "WaveHeader";

    private static final int HEADER_LENGTH = 44;

    /** Indicates PCM format. */
    public static final short FORMAT_PCM = 1;
    /** Indicates ALAW format. */
    public static final short FORMAT_ALAW = 6;
    /** Indicates ULAW format. */
    public static final short FORMAT_ULAW = 7;

    private short mFormat;
    private short mNumChannels;
    private int mSampleRate;
    private short mBitsPerSample;
    private int mNumBytes;

    // Default WaveHeader for our servers expected output
    public MyWaveHeader(int numBytes) {
        this((short) 1, (short) 2, 22050, (short) 16, numBytes);
    }

    public MyWaveHeader(short format, short numChannels, int sampleRate, short bitsPerSample, int numBytes) {
        mFormat = format;
        mSampleRate = sampleRate;
        mNumChannels = numChannels;
        mBitsPerSample = bitsPerSample;
        mNumBytes = numBytes;
    }

    private static void readId(InputStream in, String id) throws IOException {
        for (int i = 0; i < id.length(); i++) {
            if (id.charAt(i) != in.read()) throw new IOException( id + " tag not present");
        }
    }

    private static int readInt(InputStream in) throws IOException {
        return in.read() | (in.read() << 8) | (in.read() << 16) | (in.read() << 24);
    }

    private static short readShort(InputStream in) throws IOException {
        return (short)(in.read() | (in.read() << 8));
    }

    public int write(OutputStream out) throws IOException {
        /* RIFF header */
        writeId(out, "RIFF");
        writeInt(out, 36 + mNumBytes);
        writeId(out, "WAVE");

        /* fmt chunk */
        writeId(out, "fmt ");
        writeInt(out, 16);
        writeShort(out, mFormat);
        writeShort(out, mNumChannels);
        writeInt(out, mSampleRate);
        writeInt(out, mNumChannels * mSampleRate * mBitsPerSample / 8);
        writeShort(out, (short)(mNumChannels * mBitsPerSample / 8));
        writeShort(out, mBitsPerSample);

        /* data chunk */
        writeId(out, "data");
        writeInt(out, mNumBytes);

        return HEADER_LENGTH;
    }

    private static void writeId(OutputStream out, String id) throws IOException {
        for (int i = 0; i < id.length(); i++) out.write(id.charAt(i));
    }

    private static void writeInt(OutputStream out, int val) throws IOException {
        out.write(val >> 0);
        out.write(val >> 8);
        out.write(val >> 16);
        out.write(val >> 24);
    }

    private static void writeShort(OutputStream out, short val) throws IOException {
        out.write(val >> 0);
        out.write(val >> 8);
    }

    @Override
    public String toString() {
        return String.format(
                "WaveHeader format=%d numChannels=%d sampleRate=%d bitsPerSample=%d numBytes=%d",
                mFormat, mNumChannels, mSampleRate, mBitsPerSample, mNumBytes);
    }}
