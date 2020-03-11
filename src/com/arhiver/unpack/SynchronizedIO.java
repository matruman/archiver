package com.arhiver.unpack;

import com.arhiver.Main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

public class SynchronizedIO {

    private FileInputStream inputStream;
    private FileOutputStream outputStream;
    private File file;
    private volatile int lastReadUser;
    private volatile int lastWriteUser;
    private int blocksCount;

    public SynchronizedIO(FileInputStream inputStream, String name, int blocksCount)
    {
        this.inputStream = inputStream;
        mkdirIfExists();
        this.file = new File(Main.DIR_NAME + "/" + name);
        lastReadUser = Main.MAX_THREAD - 1;
        lastWriteUser = Main.MAX_THREAD - 1;
        this.blocksCount = blocksCount;
        try {
            this.file.createNewFile();
            this.outputStream = new FileOutputStream(this.file);
        }
        catch (Exception e) {
            System.out.println(Main.INVALID_UNPACK);
            System.exit(1);
        }
    }

    private synchronized void mkdirIfExists() {
        File dir = new File(Main.DIR_NAME);

        if (!dir.isDirectory()) {
            if (!dir.mkdir()) {
                System.out.println(Main.INVALID_DIR);
                System.exit(1);
            }
        }
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
 
