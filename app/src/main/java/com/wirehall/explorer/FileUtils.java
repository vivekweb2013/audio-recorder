package com.wirehall.explorer;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class FileUtils {

    public static String generateFileName() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss a");
        String filename = df.format(Calendar.getInstance().getTime()).concat(".rec");
        return filename;
    }

    public static List<File> getAllFilesFromDirectory(String path, FilenameFilter filenameFilter) {
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File[] files = directory.listFiles(filenameFilter);

        if (files == null) {
            //Means pathname does not denote a directory, or if an I/O error occurs.
            throw new IllegalArgumentException("Problem accessing path: " + path);
        }

        return Arrays.asList(files);
    }
}
