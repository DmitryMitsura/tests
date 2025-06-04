package com.example.ipcounter.counters.parallel;

import com.example.ipcounter.util.IpUtils;

import java.util.BitSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class IPConverter implements Runnable {

    private final BlockingQueue<String> queue;
    private final BitSet bitSetPositive;
    private final BitSet bitSetNegative;
    private final Object lockPositive;
    private final Object lockNegative;
    private final int threadId;

    public IPConverter(BlockingQueue<String> queue, BitSet bitSetPositive, BitSet bitSetNegative, Object lockPositive, Object lockNegative, int threadId) {
        this.queue = queue;
        this.bitSetPositive = bitSetPositive;
        this.bitSetNegative = bitSetNegative;
        this.lockPositive = lockPositive;
        this.lockNegative = lockNegative;
        this.threadId = threadId;
    }

    @Override
    public void run() {
        long processed = 0;
        try {
            while (true) {
                String line = queue.poll(2, TimeUnit.SECONDS);
                if (line == null) break;
                try {
                    int ip = IpUtils.parseIpToInt(line);
                    if (ip >= 0) {
                        synchronized (lockPositive) {
                            bitSetPositive.set(ip);
                        }
                    } else {
                        synchronized (lockNegative) {
                            bitSetNegative.set(ip ^ 0xFFFFFFFF);
                        }
                    }

                    processed++;
                    if (threadId == 0 && processed == 2_500_000) {
                        System.out.println("Approx total unic addresses: " + (bitSetPositive.cardinality()
                                + bitSetNegative.cardinality()) + " IPs");
                        processed = 0;
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
