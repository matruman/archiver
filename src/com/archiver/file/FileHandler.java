package com.archiver.file;

import com.archiver.Main;
import com.archiver.pack.Compressor;
import com.archiver.unpack.Unpack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class FileHandler {

    public static final String USAGE = "Usage:\n" +
            "\tTo pack files to archive:\n\t\tjava -jar archiver.jar archive_name.compress <files>\n" +
            "\tTo unpack files from archive:\n\t\tjava -jar archiver.jar <file.compress> -d <directory_name>";

    public static void fileHandler(String[] args) {

        if (args.length == 3 && isArchive(args[0]) && args[1].equals("-d"))
            unpack(args);
        else if (args.length > 1 && isArchive(args[0]))
            pack(args);
        else
            System.out.println(USAGE);
    }

    public static void pack(String[] args) {

        ArrayList<File> files = new ArrayList<>();
        FileOutputStream outputStream = null;

        for (int i = 1; i < args.length; i++) {
            File file = new File(args[i]);
            if (file.isFile())
                files.add(file);
            else
                System.out.format("\"%s\" is not a file\n", args[i]);
        }

        if (files.size() == 0)
            return;

        try {
            File out = new File(args[0]);
            if (out.exists())
            {
                System.out.printf("File \"%s\" already exists\n", args[0]);
                System.exit(0);
            }
            outputStream = new FileOutputStream(out);
        } catch (Exception e) {
            System.out.println("Cannot create output stream");
            System.exit(0);
        }

        new Compressor(outputStream, files).run();
    }

    public static void unpack(String[] args) {

        mkdirIfExists(args[2]);
        File file = new File(args[0]);
        try {
            FileInputStream inputStream = new FileInputStream(file);
            new Unpack(inputStream, args[2]).run();
        } catch (Exception e) {
            System.out.printf("Cannot open the file %s\n", args[0]);
        }
    }

    private static void mkdirIfExists(String dirName) {
        File dir = new File(dirName);

        if (!dir.isDirectory()) {
            if (!dir.mkdir()) {
                System.out.println(Main.INVALID_DIR);
                System.exit(0);
            }
        }
    }

    private static boolean isArchive(String arg) {

        String[] parts = arg.split("\\.");

        if (parts.length > 0 && parts[parts.length - 1].equals("compress"))
            return true;
        return false;
    }

}