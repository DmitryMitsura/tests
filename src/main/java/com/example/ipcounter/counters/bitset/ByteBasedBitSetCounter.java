package com.example.ipcounter.counters.bitset;

import com.example.ipcounter.Main;
import com.example.ipcounter.counters.IPAddressCounter;
import com.example.ipcounter.readers.buff.BufferedFileReader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.BitSet;

public class ByteBasedBitSetCounter implements IPAddressCounter {
    private static final int BITSET_SIZE = Integer.MAX_VALUE;

    private BitSet bitSetPositive;
    private BitSet bitSetNegative;

    @Override
    public long countUnique(Path filePath) throws IOException {
        bitSetPositive = new BitSet(BITSET_SIZE);
        bitSetNegative = new BitSet(BITSET_SIZE);

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
                            if (ip >= 0) {
                                bitSetPositive.set(ip);
                            } else {
                                int idx = ip ^ 0xFFFFFFFF;
                                bitSetNegative.set(idx);
                            }
                        }
                        ip = 0;
                        octet = 0;
                        octetCount = 0;
                    }
                }
                // Residual data is preserved for the next block
            }

            // Process the last IP if the file doesn't end with a newline
            if (octetCount == 3) {
                ip = (ip << 8) | octet;
                if (ip >= 0) {
                    bitSetPositive.set(ip);
                } else {
                    bitSetNegative.set(ip ^ 0xFFFFFFFF);
                }
            }
        }

        return bitSetPositive.cardinality() + bitSetNegative.cardinality();
    }
}
