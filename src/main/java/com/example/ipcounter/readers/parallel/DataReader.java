package com.example.ipcounter.readers.parallel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;

public class DataReader implements Runnable {

    private final BlockingQueue<String> queue;
    private final String filename;
    private final int consumerThreadsCount;
    private static final String POISON_PILL = "POISON_PILL";

    public DataReader(BlockingQueue<String> queue, String filename, int consumerThreadsCount) {
        this.queue = queue;
        this.filename = filename;
        this.consumerThreadsCount = consumerThreadsCount;
    }

    @Override
    public void run() {
        try (var linesStream = Files.lines(Paths.get(filename))) {
            linesStream.forEach(line -> {
                try {
                    queue.put(line);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // После завершения добавляем poison pills для корректного завершения потоков-консьюмеров
            for (int i = 0; i < consumerThreadsCount; i++) {
                try {
                    queue.put(POISON_PILL);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
