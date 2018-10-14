package com.wirehall.audiorecorder.explorer;

import android.support.annotation.NonNull;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

public class FileUtils {

    public static String generateFileName() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss a");
        String filename = df.format(Calendar.getInstance().getTime()).concat(".rec");
        return filename;
    }

    @NonNull
    public static List<File> getAllFilesFromDirectory(String path, FilenameFilter filenameFilter) {
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File[] files = directory.listFiles(filenameFilter);

        if (files == null) {
            //Means pathname does not denote a directory, or if an I/O error occurs.
            // Or could be due to missing storage permissions
            throw new IllegalArgumentException("Problem accessing path: " + path);
        }


        Arrays.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
            }
        });

        return new ArrayList<>(Arrays.asList(files));
    }
}
