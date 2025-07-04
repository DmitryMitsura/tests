package com.example.ipcounter;

import com.example.ipcounter.util.TimeUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.NoSuchFileException;
import java.nio.file.AccessDeniedException;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;

public class Main {
    public static int bufferSizeBytes = 8 * 1024 * 1024; // default

    public static void main(String[] args) {
        Properties config = new Properties();
        String algorithm;
        Path filePath;

        // Read configuration
        try {
            config.load(Files.newBufferedReader(Path.of("config.properties")));
            algorithm = config.getProperty("algorithm", "linebitset");
            filePath = Path.of(config.getProperty("filePath"));
            bufferSizeBytes = Integer.parseInt(config.getProperty("buffer.size.mb", "8")) * 1024 * 1024;

        } catch (IOException e) {
            System.err.println("Error reading configuration file: " + e.getMessage());
            return;
        }

        // Check file existence and readability
        try {
            if (!Files.exists(filePath)) {
                throw new NoSuchFileException(filePath.toString());
            }
            if (!Files.isReadable(filePath)) {
                throw new AccessDeniedException(filePath.toString());
            }
        } catch (IOException e) {
            System.err.println("File check error: " + e.getMessage());
            return;
        }

        // Count unique IPs,
        try {
            System.out.println("Algorithm: " + algorithm + ". Buffer size in Mb " + bufferSizeBytes);

            IPAddressCounter counter = switch (algorithm) {
                case "linebitset" -> new LineBasedBitSetCounter();
                case "bytebitset" -> new ByteBasedBitSetCounter();
                case "bytememory" -> new ByteBasedMemoryCounter();
                default -> throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
            };

            Instant start = Instant.now();
            System.out.println("Processing started at: " + start);

            long uniqueCount = counter.countUnique(filePath);

            Instant end = Instant.now();
            System.out.println("Processing ended at: " + end);

            Duration duration = Duration.between(start, end);
            System.out.println("Total time: " + TimeUtils.formatDuration(duration));

            System.out.println("Unique IP count: " + uniqueCount);
            System.out.flush();

            System.err.flush();
            System.exit(0);

        } catch (IllegalArgumentException e) {
            System.err.println("Invalid parameter: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("File processing error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
