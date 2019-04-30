package com.gctest;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

class Address {
    String street1;
    String street2;
    String city;
    String state;
    String country;
}
class Profile {
    byte[] buffer;
    String name;
    String phoneNumber;
    Address address;

    public Profile(byte[] buffer, String name, String phoneNumber, Address address) {
        this.buffer = buffer;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }
}

public class Producer implements Runnable {
    private Deque<Profile> deque;
    private int objectSize;
    private int queueSize;
    private int noOfObjects;

    public Producer(int noOfObjects, int objectSizeInKb, int ttlSeconds) {
        this.deque = new ConcurrentLinkedDeque<>();
        this.objectSize = objectSizeInKb;
        this.queueSize = ttlSeconds * 1000;
        this.noOfObjects = noOfObjects;
    }

    public Deque<Profile> getDeque() {
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


    @Override
    public void run() {
        allocateObjects(noOfObjects, objectSize);
        unreferenceSomeObjects(1000);
    }

    private void unreferenceSomeObjects(int noOfObjects) {
        for(int i = 0; i < noOfObjects; i++) {
            deque.poll(); //unreference and remove from storagefri
        }
    }

    private void allocateObjects(int noOfObjects, int objectSize) {
        for (int i = 0; i < noOfObjects; i++) {
            byte[] array = new byte[objectSize];
            Address address = new Address();
            Profile p = new Profile(array, "", "", address);
            deque.add(p);
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
