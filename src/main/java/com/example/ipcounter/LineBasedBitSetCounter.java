package com.example.ipcounter;

import com.example.ipcounter.util.FileLineReader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.BitSet;

public class LineBasedBitSetCounter implements IPAddressCounter {
    private static final int BITSET_SIZE = Integer.MAX_VALUE;

    private BitSet bitSetPositive;
    private BitSet bitSetNegative;
    private long uniqueCount;

    @Override
    public long countUnique(Path filePath) throws IOException {
        bitSetPositive = new BitSet(BITSET_SIZE);
        bitSetNegative = new BitSet(BITSET_SIZE);
        uniqueCount = 0;
        FileLineReader.readFileLines(filePath, Main.bufferSizeBytes, this::processLine);
        return uniqueCount;
    }

    private void processLine(String line) {
        try {
            int ipAsInt = parseIp(line.trim());
            if (ipAsInt < 0) {
                int index = ipAsInt ^ 0xFFFFFFFF;
                if (!bitSetNegative.get(index)) {
                    bitSetNegative.set(index);
                    uniqueCount++;
                }
            } else {
                if (!bitSetPositive.get(ipAsInt)) {
                    bitSetPositive.set(ipAsInt);
                    uniqueCount++;
                }
            }
        } catch (NumberFormatException e) {
        }
    }

    private int parseIp(String ip) {
        String[] parts = ip.split("\\.");
        if (parts.length != 4) throw new NumberFormatException("Invalid IP format: " + ip);
        int result = 0;
        for (int i = 0; i < 4; i++) {
            int part = Integer.parseInt(parts[i]);
            if (part < 0 || part > 255) throw new NumberFormatException("Invalid IP part: " + ip);
            result |= (part & 0xFF) << (8 * (3 - i));
        }
        return result;
    }
}
