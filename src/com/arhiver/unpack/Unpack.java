package com.arhiver.unpack;

import com.arhiver.Main;

import java.io.FileInputStream;
import java.nio.ByteBuffer;


public class Unpack {

    private FileInputStream inputStream;
    private ParallelUnpacker[] threads;
    private String dir;

    public Unpack(FileInputStream inputStream, String dir) {

        this.dir = dir;
        this.inputStream = inputStream;
        threads = new ParallelUnpacker[Main.MAX_THREAD];
    }

    public void run() {

        String name;

        while ((name = getName(inputStream)) != null) {

            int countBlocks = readInt(inputStream);
            SynchronizedIO synchronizedIO = new SynchronizedIO(inputStream, name, countBlocks, dir);
            for (int i = 0; i < Main.MAX_THREAD; i++) {
                threads[i] =  new ParallelUnpacker(synchronizedIO, i);
                threads[i].start();
            }
            for (int i = 0; i < Main.MAX_THREAD; i++) {
                try {
                    threads[i].join();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static int readInt(FileInputStream inputStream) {

        byte[] arr = new byte[4];

        try {
            inputStream.read(arr);
        }
        catch (Exception e)
        {
            System.out.println("Cannot read int");
            e.printStackTrace();
            System.exit(0);
        }

        ByteBuffer buff = ByteBuffer.wrap(arr);

        int res = buff.getInt();
        return res;
    }

    public static int readShort(FileInputStream inputStream) {

        byte[] arr = new byte[2];

        try {
            inputStream.read(arr);
        }
        catch (Exception e)
        {
            System.out.println("Cannot read short");
            e.printStackTrace();
            System.exit(0);
        }

        ByteBuffer buff = ByteBuffer.wrap(arr);

        int res = buff.getShort();
        return res;
    }

    private String getName(FileInputStream in) {

        int len  = readShort(in);
        if (len == 0)
            return null;
        if (len < 0)
        {
            System.out.println(Main.INVALID_UNPACK);
            System.exit(0);
        }
        int count;
        byte[] str = new byte[len];

        try {
            count = in.read(str);
            if (count < 0)
                return null;
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(0);
        }
        return new String(str);
    }
}
