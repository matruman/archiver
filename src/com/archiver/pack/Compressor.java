package com.archiver.pack;

import com.archiver.Main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Compressor {

    private FileOutputStream outputStream;
    private ArrayList<File> files;


    public Compressor(FileOutputStream outputStream, ArrayList<File> files) {

        this.files = files;
        this.outputStream = outputStream;
    }

    public void run() {

        long t1 = System.currentTimeMillis();
       
        Thread[] threads = new Thread[Main.MAX_THREAD];
        SynchronizedIO synchronizedIO = new SynchronizedIO(files, outputStream);
        for (int i = 0; i < Main.MAX_THREAD; i++) {

            int prev = i == 0 ? Main.MAX_THREAD - 1 : i - 1;
            threads[i] = new Thread(new ParallelCompressor(synchronizedIO, i, prev));
            threads[i].start();
        }
        for (int i = 0; i < Main.MAX_THREAD; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
        System.out.println(System.currentTimeMillis() - t1);
    }
}
