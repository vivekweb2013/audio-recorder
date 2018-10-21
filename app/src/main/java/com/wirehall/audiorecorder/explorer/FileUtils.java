package com.wirehall.audiorecorder.explorer;

import android.media.MediaMetadataRetriever;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

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


    /**
     * @param bytes file size in bytes
     * @param si    The SI prefixes are standardized for use in the International System of Units (SI)
     * @return String returns the human readable file size
     * <p>
     * BITS                 SI          BINARY
     * <p>
     * 0:                   0 B         0 B
     * 27:                  27 B        27 B
     * 999:                 999 B       999 B
     * 1000:                1.0 kB      1000 B
     * 1023:                1.0 kB      1023 B
     * 1024:                1.0 kB      1.0 KiB
     * 1728:                1.7 kB      1.7 KiB
     * 110592:              110.6 kB    108.0 KiB
     * 7077888:             7.1 MB      6.8 MiB
     * 452984832:           453.0 MB    432.0 MiB
     * 28991029248:         29.0 GB     27.0 GiB
     * 1855425871872:       1.9 TB      1.7 TiB
     * 9223372036854775807: 9.2 EB      8.0 EiB   (Long.MAX_VALUE)
     */
    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static String humanReadableDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy, hh:mm aa");
        String formattedDate = dateFormat.format(date);
        return formattedDate;
    }

    public static String humanReadableDuration(String mediaPath) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(mediaPath);
        long duration = Long.parseLong(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        mmr.release();

        if (TimeUnit.MILLISECONDS.toMinutes(duration) < 1) {
            return String.format("%d sec",
                    TimeUnit.MILLISECONDS.toSeconds(duration) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
            );
        }

        return String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
        );
    }

}
