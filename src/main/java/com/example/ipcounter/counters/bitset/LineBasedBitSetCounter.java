package com.example.ipcounter.counters.bitset;

import com.example.ipcounter.Main;
import com.example.ipcounter.counters.IPAddressCounter;
import com.example.ipcounter.readers.line.FileLineReader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.BitSet;

public class LineBasedBitSetCounter implements IPAddressCounter {
    private static final int BITSET_SIZE = Integer.MAX_VALUE;

    private BitSet bitSetPositive;
    private BitSet bitSetNegative;

    @Override
    public long countUnique(Path filePath) throws IOException {
        bitSetPositive = new BitSet(BITSET_SIZE);
        bitSetNegative = new BitSet(BITSET_SIZE);
        FileLineReader.readFileLines(filePath, Main.bufferSizeBytes, this::processLine);
        return bitSetPositive.cardinality() + bitSetNegative.cardinality();
    }

    private void processLine(String line) {
        try {
            int ipAsInt = parseIp(line.trim());
            if (ipAsInt < 0) {
                bitSetNegative.set(ipAsInt ^ 0xFFFFFFFF);
            } else {
                bitSetPositive.set(ipAsInt);
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
