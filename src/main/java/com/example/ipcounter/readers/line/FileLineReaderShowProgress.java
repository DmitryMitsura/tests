package com.example.ipcounter.readers.line;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class FileLineReaderShowProgress {
    private static final int ESTIMATE_SAMPLE_SIZE = 1000; // Sample size for estimating avg line size
    private static final long PROGRESS_STEP_LINES = 100_000_000; // How often to display progress

    public static void readFileLines(Path filePath, int bufferSize, Consumer<String> lineProcessor) throws IOException {
        long totalFileSize = Files.size(filePath);
        long bytesReadEstimate = 0;
        long lineCount = 0;
        long totalLineSize = 0;
        double avgLineSize = -1;

        try (BufferedReader reader = new BufferedReader(Files.newBufferedReader(filePath, StandardCharsets.UTF_8), bufferSize)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineProcessor.accept(line);
                lineCount++;

                if (avgLineSize < 0) { // Still estimating
                    totalLineSize += line.getBytes(StandardCharsets.UTF_8).length + System.lineSeparator().getBytes().length;
                    if (lineCount == ESTIMATE_SAMPLE_SIZE) {
                        avgLineSize = (double) totalLineSize / ESTIMATE_SAMPLE_SIZE;
                    }
                }

                if (lineCount % PROGRESS_STEP_LINES == 0) {
                    if (avgLineSize > 0) {
                        bytesReadEstimate = (long) (lineCount * avgLineSize);
                        double percent = (double) bytesReadEstimate / totalFileSize * 100;
                        System.out.printf("Estimated progress: %.2f%% (%d/%d bytes)%n", percent, bytesReadEstimate, totalFileSize);
                    } else {
                        System.out.printf("Processed %d lines so far... estimating progress...%n", lineCount);
                    }
                }
            }
        }
    }
}
