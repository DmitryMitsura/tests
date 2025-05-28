package com.example.ipcounter.util;

import java.io.*;
import java.nio.file.*;

public class BufferedFileReader implements Closeable {
    private static final int BUFFER_SIZE = 8 * 1024 * 1024;
    private InputStream inputStream;
    private byte[] buffer;
    private int bytesRead;

    public BufferedFileReader(Path filePath) throws IOException {
        this(filePath, BUFFER_SIZE);
    }

    public BufferedFileReader(Path filePath, int bufferSize) throws IOException {
        this.inputStream = new BufferedInputStream(Files.newInputStream(filePath), bufferSize);
        this.buffer = new byte[bufferSize];
        this.bytesRead = 0;
    }

    public int readNextBlock() throws IOException {
        bytesRead = inputStream.read(buffer);
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
