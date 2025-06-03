package com.example.ipcounter.counters.parallel;

import com.example.ipcounter.util.IpUtils;

import java.util.BitSet;
import java.util.concurrent.BlockingQueue;

public class IPConverter implements Runnable {

    private final BlockingQueue<String> queue;
    private final BitSet bitSetPositive;
    private final BitSet bitSetNegative;
    private final Object lock;
    private final int threadId;
    private final int totalThreads;
    private static final String POISON_PILL = "POISON_PILL";
    private static final int BITSET_SIZE = Integer.MAX_VALUE;

    public IPConverter(BlockingQueue<String> queue, BitSet bitSetPositive, BitSet bitSetNegative, Object lock, int threadId, int totalThreads) {
        this.queue = queue;
        this.bitSetPositive = bitSetPositive;
        this.bitSetNegative = bitSetNegative;
        this.lock = lock;
        this.threadId = threadId;
        this.totalThreads = totalThreads;
    }

    @Override
    public void run() {
        long processed = 0;
        try {
            while (true) {
                String line = queue.take();
                if (POISON_PILL.equals(line)) {
                    queue.put(POISON_PILL);
                    break;
                }
                if (line == null || line.trim().isEmpty()) continue;
                try {
                    long ip = IpUtils.parseIpToLong(line);
                    synchronized (lock) {
                        int index = (int) (Math.abs(ip % BITSET_SIZE));
                        if (ip >= 0) {
                            bitSetPositive.set(index);
                        } else {
                            bitSetNegative.set(index);
                        }
                    }
                    processed++;
                    if (threadId == 0 && processed == 2_500_000) {
                        System.out.println("Approx total unic addresses: " + (bitSetPositive.cardinality()
                        + bitSetNegative.cardinality()) + " IPs");
                        processed = 0;
                    }
                } catch (IllegalArgumentException ignored) {}
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
