package com.example.ipcounter.counters.memory;

import com.example.ipcounter.Main;
import com.example.ipcounter.counters.IPAddressCounter;
import com.example.ipcounter.readers.buff.BufferedFileReader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;

public class ByteBasedMemoryCounter implements IPAddressCounter {
    private static final long BUFFER_SIZE = (1L << 32) >> 3; // 2^32 bits for all IPv4 addresses -> convert to bytes

    private ByteBuffer memoryBuffer;
    private long uniqueCount;

    @Override
    public long countUnique(Path filePath) throws IOException {
        // Allocate a direct buffer in memory
        memoryBuffer = ByteBuffer.allocateDirect((int) BUFFER_SIZE);

        uniqueCount = 0;
        try (BufferedFileReader reader = new BufferedFileReader(filePath, Main.bufferSizeBytes)) {
            byte[] buffer;
            int bytesRead;

            int ip = 0;
            int octet = 0;
            int octetCount = 0;

            while ((bytesRead = reader.readNextBlock()) != -1) {
                buffer = reader.getBuffer();
                int length = reader.getBytesRead();

                for (int i = 0; i < length; i++) {
                    byte b = buffer[i];
                    if (b >= '0' && b <= '9') {
                        octet = octet * 10 + (b - '0');
                    } else if (b == '.') {
                        ip = (ip << 8) | octet;
                        octet = 0;
                        octetCount++;
                    } else if (b == '\n' || b == '\r') {
                        if (octetCount == 3) {
                            ip = (ip << 8) | octet;
                            int index = ip & 0xFFFFFFFF;
                            int byteIndex = index >>> 3;
                            int bitIndex = index & 7;
                            byte value = memoryBuffer.get(byteIndex);
                            if ((value & (1 << bitIndex)) == 0) {
                                memoryBuffer.put(byteIndex, (byte) (value | (1 << bitIndex)));
                                uniqueCount++;
                            }
                        }
                        ip = 0;
                        octet = 0;
                        octetCount = 0;
                    }
                }
            }

            // Handle the last IP if the file does not end with a newline
            if (octetCount == 3) {
                ip = (ip << 8) | octet;
                int index = ip & 0xFFFFFFFF;
                int byteIndex = index >>> 3;
                int bitIndex = index & 7;
                byte value = memoryBuffer.get(byteIndex);
                if ((value & (1 << bitIndex)) == 0) {
                    memoryBuffer.put(byteIndex, (byte) (value | (1 << bitIndex)));
                    uniqueCount++;
                }
            }
        }

        return uniqueCount;
    }
}
