package com.example.ipcounter.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class FileLineReader {
    private static final int BUFFER_SIZE = 8 * 1024 * 1024; // 8 MB

    public static void readFileLines(Path filePath, Consumer<String> lineProcessor) throws IOException {
        try (BufferedReader reader = new BufferedReader(Files.newBufferedReader(filePath, StandardCharsets.UTF_8), BUFFER_SIZE)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineProcessor.accept(line);
            }
        }
    }
}
