package com.example.ipcounter.util;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class BufferedFileReaderShowProgress implements Closeable {
    private static final int BUFFER_SIZE = 8 * 1024 * 1024;
    private static final long PROGRESS_STEP_LINES = 100_000_000; // How often to display progress

    private InputStream inputStream;
    private byte[] buffer;
    private int bytesRead;
    private int fileSizeInMb;
    private int showCounter;

    public BufferedFileReaderShowProgress(Path filePath) throws IOException {
        this(filePath, BUFFER_SIZE);
    }

    public BufferedFileReaderShowProgress(Path filePath, int bufferSize) throws IOException {
        this.inputStream = new BufferedInputStream(Files.newInputStream(filePath), bufferSize);
        this.buffer = new byte[bufferSize];
        this.bytesRead = 0;
        this.fileSizeInMb = (int) (Files.size(filePath) >> 20);
        this.showCounter = 0;
    }

    public int readNextBlock() throws IOException {
        bytesRead = inputStream.read(buffer);
        showCounter++;
        if (showCounter % 50 == 0) {
            int megaBytesRead = showCounter * 8;
            System.out.printf("Processed progress: %d Mb from %d Mb", megaBytesRead, fileSizeInMb);
        }

        return bytesRead;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public int getBytesRead() {
        return bytesRead;
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }
}
