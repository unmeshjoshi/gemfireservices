package com.gctest;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Producer implements Runnable {
    private static ScheduledExecutorService executorService =
            Executors.newScheduledThreadPool(2);
    private Deque<byte[]> deque;
    private int objectSize;
    private int queueSize;
    public Producer(int objectSizeInKb, int ttlSeconds) {
        this.deque = new ArrayDeque<byte[]>();
        this.objectSize = objectSizeInKb;
        this.queueSize = ttlSeconds * 1000;
    }
    @Override
    public void run() {
        allocateObjects(100, objectSize);
    }

    private void unreferenceSomeObjects() {
        if (deque.size() > queueSize) {
            byte[] localRef = deque.poll(); //unreference and remove from storage
        }
    }

    private void allocateObjects(int noOfObjects, int objectSize) {
        for (int i = 0; i < noOfObjects; i++) {
            deque.add(new byte[objectSize]);
            unreferenceSomeObjects();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        executorService.scheduleAtFixedRate(new Producer(killoBytes(200), 5), 0,
                20, TimeUnit.MILLISECONDS);


        TimeUnit.MINUTES.sleep(10);
        executorService.shutdownNow();
    }

    private static int killoBytes(int n) {
        return n * 1024 * 1024 / 1000;
    }
}
