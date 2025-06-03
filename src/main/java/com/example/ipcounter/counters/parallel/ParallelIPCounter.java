package com.example.ipcounter.counters.parallel;

import com.example.ipcounter.readers.parallel.DataReader;

import java.nio.file.Path;
import java.util.BitSet;
import java.util.concurrent.*;

public class ParallelIPCounter {

    private static final int QUEUE_CAPACITY = 10000;
    private static final int THREADS = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
    private static final int BITSET_SIZE = Integer.MAX_VALUE;

    public static long countUniqueIPs(Path filePath) throws InterruptedException {
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
        BitSet bitSetPositive = new BitSet(BITSET_SIZE);
        BitSet bitSetNegative = new BitSet(BITSET_SIZE);
        Object lock = new Object();

        ExecutorService readerExecutor = Executors.newSingleThreadExecutor();
        ExecutorService workerExecutor = Executors.newFixedThreadPool(THREADS);

        try {
            readerExecutor.submit(new DataReader(queue, filePath.toString(), THREADS));

            CountDownLatch latch = new CountDownLatch(THREADS);
            for (int i = 0; i < THREADS; i++) {
                final int threadId = i;
                workerExecutor.submit(() -> {
                    new IPConverter(queue, bitSetPositive, bitSetNegative, lock, threadId, THREADS).run();
                    latch.countDown();
                });
            }

            latch.await();

        } finally {
            readerExecutor.shutdown();
            workerExecutor.shutdown();
        }

        return (long) bitSetPositive.cardinality() + bitSetNegative.cardinality();
    }
}
