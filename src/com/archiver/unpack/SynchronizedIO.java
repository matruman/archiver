package com.archiver.unpack;

import com.archiver.Main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

public class SynchronizedIO {

    private FileInputStream inputStream;
    private FileOutputStream outputStream;
    private volatile int lastReadUser;
    private volatile int lastWriteUser;
    private int blocksCount;

    public SynchronizedIO(FileInputStream inputStream, FileOutputStream outputStream, int blocksCount)
    {
        this.inputStream = inputStream;
        lastReadUser = Main.MAX_THREAD - 1;
        lastWriteUser = Main.MAX_THREAD - 1;
        this.blocksCount = blocksCount;
        this.outputStream = outputStream;
    }

    public synchronized int read(int id, int previous, byte[] buff) {

        while (lastReadUser != previous) {
            try {
                wait();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }

        if (blocksCount == 0) {
            lastReadUser = id;
            notifyAll();
            return 0;
        }

        int ret;
        try {
            ret = inputStream.read(buff, 0, Unpack.readInt(inputStream));
            blocksCount--;
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
            return  -1;
        }

        lastReadUser = id;
        notifyAll();
        return ret;
    }

    public synchronized void write(int id, int previous, ByteBuffer buffer) {

        while (lastWriteUser != previous) {
            try {
                wait();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }

        try {
            outputStream.write(buffer.array(), 0, buffer.position());
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

        lastWriteUser = id;
        notifyAll();
    }
}
 
