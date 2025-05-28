package com.example.ipcounter.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class FileLineReader {

    public static void readFileLines(Path filePath, int bufferSize, Consumer<String> lineProcessor) throws IOException {
        try (BufferedReader reader = new BufferedReader(Files.newBufferedReader(filePath, StandardCharsets.UTF_8), bufferSize)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineProcessor.accept(line);
            }
        }
    }
}
