package com.archiver.pack;

import com.archiver.Main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

public class SynchronizedIO {
    
    private Iterator<File> fileIterator;
    private FileOutputStream outputStream;
    private volatile FileInputStream inputStream;
    private volatile File file;
    private volatile boolean isNewFile;
    private volatile boolean isOver;
    private volatile int lastReader;
    private volatile int lastWriter;

    public SynchronizedIO(List<File> files, FileOutputStream outputStream) {
        
        this.fileIterator = files.listIterator();
        this.isNewFile = true;
        this.isOver = false;
        this.inputStream = null;
        this.outputStream = outputStream;
        this.lastReader = Main.MAX_THREAD - 1;
        this.lastWriter = Main.MAX_THREAD - 1;
        openNewFile();
    }

    private void openNewFile() {
        try {
            if (inputStream != null)
                inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        if (!fileIterator.hasNext()) {
            isOver = true;
            return ;
        }
        try {
            file = fileIterator.next();
            inputStream = new FileInputStream(file);
            isNewFile = true;
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private void writeFileInfo(ByteBuffer buffer) {
        
        int blocksCount = (int) (file.length() / Main.BUFF_SIZE);
            if (file.length() % Main.BUFF_SIZE > 0)
                blocksCount++;
        buffer.putShort((short) file.getName().getBytes(Charset.forName("UTF-8")).length);
        buffer.put(file.getName().getBytes(Charset.forName("UTF-8")));
        buffer.putInt(blocksCount);
        isNewFile = false;
    }

    public synchronized int read(byte[] buff, ByteBuffer buffer, int id, int prev) {

        while (prev != lastReader) {

            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
        if (isOver) {
            lastReader = id;
            notifyAll();
            return -1;
        }
        if (isNewFile)
            writeFileInfo(buffer);
        int count = 0;
        try {
            count = inputStream.read(buff);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        lastReader = id;
        if (count < Main.BUFF_SIZE)
            openNewFile();
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
