package com.example.ipcounter;

import com.example.ipcounter.util.BufferedFileReader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.BitSet;

public class ByteBasedBitSetCounter implements IPAddressCounter {
    private static final int BITSET_SIZE = 1 << 31;

    private BitSet bitSetPositive;
    private BitSet bitSetNegative;
    private long uniqueCount;

    @Override
    public long countUnique(Path filePath) throws IOException {
        bitSetPositive = new BitSet(BITSET_SIZE);
        bitSetNegative = new BitSet(BITSET_SIZE);
        uniqueCount = 0;

        try (BufferedFileReader reader = new BufferedFileReader(filePath)) {
            byte[] buffer;
            int bytesRead;
            StringBuilder lineBuilder = new StringBuilder();
            String residue = "";

            while ((bytesRead = reader.readNextBlock()) != -1) {
                buffer = reader.getBuffer();
                int length = reader.getBytesRead();

                if (!residue.isEmpty()) {
                    lineBuilder.append(residue);
                    residue = "";
                }

                for (int i = 0; i < length; i++) {
                    byte b = buffer[i];
                    if (b == '\n' || b == '\r') {
                        if (lineBuilder.length() > 0) {
                            String line = lineBuilder.toString();
                            int ip = parseIp(line);
                            if (ip >= 0) {
                                if (!bitSetPositive.get(ip)) {
                                    bitSetPositive.set(ip);
                                    uniqueCount++;
                                }
                            } else {
                                int idx = ip & 0x7FFFFFFF; // Clear sign bit for negative index
                                if (!bitSetNegative.get(idx)) {
                                    bitSetNegative.set(idx);
                                    uniqueCount++;
                                }
                            }
                            lineBuilder.setLength(0);
                        }
                    } else {
                        lineBuilder.append((char) b);
                    }
                }

                if (lineBuilder.length() > 0) {
                    residue = lineBuilder.toString();
                    lineBuilder.setLength(0);
                }
            }

            if (!residue.isEmpty()) {
                int ip = parseIp(residue);
                if (ip >= 0) {
                    if (!bitSetPositive.get(ip)) {
                        bitSetPositive.set(ip);
                        uniqueCount++;
                    }
                } else {
                    int idx = ip & 0x7FFFFFFF;
                    if (!bitSetNegative.get(idx)) {
                        bitSetNegative.set(idx);
                        uniqueCount++;
                    }
                }
            }
        }

        return uniqueCount;
    }

    private int parseIp(String line) {
        String[] parts = line.split("\\.");
        int result = 0;
        for (String part : parts) {
            result = (result << 8) | Integer.parseInt(part);
        }
        return result;
    }
}
