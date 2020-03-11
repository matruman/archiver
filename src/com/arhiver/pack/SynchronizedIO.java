package com.arhiver.pack;

import com.arhiver.Main;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

public class SynchronizedIO {

    private FileInputStream inputStream;
    private FileOutputStream outputStream;
    private volatile int lastReader;
    private volatile int lastWriter;

    public SynchronizedIO(FileInputStream inputStream, FileOutputStream outputStream) {

        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.lastReader = Main.MAX_THREAD - 1;
        this.lastWriter = Main.MAX_THREAD - 1;
    }

    public synchronized int read(byte[] buff, int id, int prev) {

        while (prev != lastReader) {

            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
        int count = 0;
        try {
            count = inputStream.read(buff);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        lastReader = id;
        notifyAll();
        return count;
    }

    public synchronized void write(ByteBuffer buffer, int id, int prev) {

        while (prev != lastWriter) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
        try {
            outputStream.write(buffer.array(), 0, buffer.position());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        lastWriter = id;
        notifyAll();
    }
}
