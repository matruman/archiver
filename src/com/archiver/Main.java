package com.archiver;

import com.archiver.file.FileHandler;

public class Main {

    public static final int BUFF_SIZE = 4096;
    public static final int UNPACK_BUFF_SIZE = 2 * BUFF_SIZE;
    public static final int MIN_OVERLAP = 5;
    public static final int MAX_BYTE = 127;
    public static final int LENGTH_ADDRESS = 2;
    public static final int MAX_THREAD = 4;
    public static final String INVALID_UNPACK = "Invalid archive file";
    public static final String INVALID_DIR = "Cannot create output directory";

    public static void main(String[] args) {

        FileHandler.fileHandler(args);
    }
}
