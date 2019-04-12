package com.gctest;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Producer implements Runnable {
    private Deque<byte[]> deque;
    private int objectSize;
    private int queueSize;
    private int noOfObjects;
    private long someField = 1000L;
    private int anotherField2= 1200;

    public Producer(int noOfObjects, int objectSizeInKb, int ttlSeconds) {
        this.deque = new ConcurrentLinkedDeque<>();
        this.objectSize = objectSizeInKb;
        this.queueSize = ttlSeconds * 1000;
        this.noOfObjects = noOfObjects;
        this.someField = queueSize * 200;
        this.anotherField2 = queueSize * 20;

    }

    public Deque<byte[]> getDeque() {
        return deque;
    }

    public int getObjectSize() {
        return objectSize;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public int getNoOfObjects() {
        return noOfObjects;
    }

    public long getSomeField() {
        return someField;
    }

    public int getAnotherField2() {
        return anotherField2;
    }

    @Override
    public void run() {
        allocateObjects(noOfObjects, objectSize);
    }

    private void unreferenceSomeObjects() {
        for(int i = 0; i < 100; i++) {
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
        Producer command = new Producer(1000, killoBytes(100), 5);
        while(true) {
            command.run();
            Thread.sleep(1000);
        }

//        executorService.awaitTermination(10, TimeUnit.MINUTES);
    }

    private static int killoBytes(int n) {
        return n * 1024 * 8;
    }
}
