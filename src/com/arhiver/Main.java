package com.arhiver;

import com.arhiver.pack.Compressor;

import java.io.File;
import java.io.FileOutputStream;

public class Main {

    public static final int BUFF_SIZE = 4096;
    public static final int UNPACK_BUFF_SIZE = 2 * BUFF_SIZE;
    public static final int MIN_OVERLAP = 5;
    public static final int MAX_BYTE = 127;
    public static final int LENGTH_ADDRESS = 2;
    public static final int MAX_THREAD = 4;
    public static final String DIR_NAME = "unpack";
    public static final String INVALID_UNPACK = "Invalid archive file";
    public static final String INVALID_DIR = "Cannot create output directory";

    public static int counter = 0;


    public static void main(String[] args) {


        FileOutputStream out;

        File[] files = new File[1];
        files[0] = new File("/Users/matruman/Desktop/untitledfolder/img.png");
        try {
            File output = new File("x.compress");
            output.createNewFile();
            out = new FileOutputStream(output);
            new Compressor(out, files).run();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(counter);
    }
}
