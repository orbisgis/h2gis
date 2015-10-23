package org.h2gis.h2spatialapi;

import java.io.IOException;
import java.io.InputStream;

/**
 * Wrap an input stream for triggering progression. Need to know the expected length of stream
 * @author Nicolas Fortin
 */
public class InputStreamProgressMonitor extends InputStream {
    private ProgressVisitor progress;
    private InputStream inputStream;
    private static final int PRECISION = 100;
    private final long expectedSize;
    private long streamedLength = 0;

    public InputStreamProgressMonitor(ProgressVisitor progress, InputStream inputStream, long expectedSize) {
        this.progress = progress.subProcess(PRECISION);
        this.inputStream = inputStream;
        this.expectedSize = expectedSize;
    }

    private int progress(int bytesRead) {
        return (int)progress((long)bytesRead);
    }

    private long progress(long bytesRead) {
        streamedLength+=bytesRead;
        progress.setStep((int)(((double)streamedLength / expectedSize) * PRECISION));
        return bytesRead;
    }

    @Override
    public int read() throws IOException {
        progress(1);
        return inputStream.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return progress(inputStream.read(b));
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
            return progress(inputStream.read(b, off, len));
    }

    @Override
    public long skip(long n) throws IOException {
        return progress(inputStream.skip(n));
    }

    @Override
    public int available() throws IOException {
        return inputStream.available();
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
        progress.endOfProgress();
    }

    @Override
    public void mark(int readlimit) {
        inputStream.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        inputStream.reset();
        progress.setStep(0);
        streamedLength = 0;
    }

    @Override
    public boolean markSupported() {
        return inputStream.markSupported();
    }
}
