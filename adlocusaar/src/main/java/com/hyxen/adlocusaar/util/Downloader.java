package com.hyxen.adlocusaar.util;

import android.content.Context;
import android.text.format.DateUtils;

import com.hyxen.adlocusaar.net.HxRequest;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by kiddchen on 2014/1/16.
 */
public class Downloader
{

    private static final int BUFFER_SIZE = 4096;
    private static final TaskRunner mTaskRunner = new TaskRunner();


    public static void asyncDownload(final Context context, final String url, final String saveFileName, final DownloadListener listener)
    {
        mTaskRunner.put(new Runnable()
        {
            @Override
            public void run()
            {
                boolean isSuccess = downloadFile(context, url, saveFileName);
                if (listener != null)
                {
                    listener.onDownloadComplete(isSuccess);
                }
            }
        });
    }
    public static void asyncDownloadAndUnzip(final Context context, final String url, final String saveFileName, final DownloadListener listener)
    {
        mTaskRunner.put(new Runnable()
        {
            @Override
            public void run()
            {
                boolean isSuccess = downloadAndUnzip(context, url, saveFileName);
                if (listener != null)
                {
                    listener.onDownloadComplete(isSuccess);
                }
            }
        });
    }

    public static boolean syncDownload(final Context context, final String url, final String saveFileName)
    {
        return downloadFile(context, url, saveFileName);
    }
    public static boolean syncDownloadAndUnzip(final Context context, final String url, final String saveFileName)
    {
        return downloadAndUnzip(context, url, saveFileName);
    }


    protected static boolean downloadAndUnzip(Context context, String dlPatch, String saveFileName)
    {
        String tempFileName = "temp_download_" + System.currentTimeMillis();
        boolean isSuccess = downloadFile(context, dlPatch, tempFileName);
        File f = context.getFileStreamPath(saveFileName);
        f.delete();
        if(isSuccess)
        {
            isSuccess = unZipDatabase(context, tempFileName, saveFileName);
        }
        context.getFileStreamPath(tempFileName).delete();
        return isSuccess;
    }

    protected static boolean downloadFile(Context context, String dlPatch, String fileName)
    {
        try
        {
            File cacheDir = context.getCacheDir();
            cacheDir.mkdirs();
            deleteOldFiles(cacheDir, null);
            String dlName = dlPatch.substring(dlPatch.lastIndexOf('/') + 1, dlPatch.length());
            File cacheFile = new File(cacheDir, dlName);

            File dbFile = context.getFileStreamPath(fileName);
            if(cacheFile.exists())
            {
                copyFile(cacheFile, dbFile);
                return true;
            }
            HxRequest r = new HxRequest(context, dlPatch);
            r.setGetByte();
            r.run();
            byte[] byteResult = r.getResultByteArray();
            if(byteResult == null)
            {
                return false;
            }
            //開啟data/data的檔案以存放inputStream

            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(cacheFile));
            bos.write(byteResult);
            bos.flush();
            bos.close();
            byteResult = null;
            copyFile(cacheFile, dbFile);
            System.gc();
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public static void deleteOldFiles(File dir, String prefix)
    {
        if (!dir.isDirectory() || !dir.exists()) return;
        File[] oodFile = dir.listFiles(new PrefixTodayFilter(prefix));
        if(oodFile != null) {
            for (File file : oodFile) {
                file.delete();
            }
        }
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException
    {
        destFile.delete();
        if(!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        }
        finally {
            if(source != null) {
                source.close();
            }
            if(destination != null) {
                destination.close();
            }
        }
    }

    private static boolean unZipDatabase(Context context, String fileName, String databaseName)
    {
        try
        {
            InputStream tISStream = context.openFileInput(fileName);

            ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(tISStream));
            ZipEntry zipEntry;

            while ((zipEntry = zipInputStream.getNextEntry()) != null)
            {
                File file = context.getDatabasePath(databaseName);
                file.getParentFile().mkdirs();
                if (file.exists())
                {
                    file.delete();
                }

                if(zipEntry.isDirectory())
                {
                    file.mkdirs();
                }
                else
                {
                    byte buffer[] = new byte[BUFFER_SIZE];
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream, BUFFER_SIZE);

                    int count;
                    while ((count = zipInputStream.read(buffer, 0, BUFFER_SIZE)) != -1)
                    {
                        bufferedOutputStream.write(buffer, 0, count);
                    }
                    bufferedOutputStream.flush();
                    bufferedOutputStream.close();
                }
            }
            zipInputStream.close();
            System.gc();
            return true;
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return false;
    }



    public interface DownloadListener
    {
        void onDownloadComplete(boolean isSuccess);
    }

    private static class PrefixTodayFilter implements FilenameFilter {

        final String prefix;

        /**
         *
         * @param prefix Just filter prefix file, null well filter all.
         */
        PrefixTodayFilter(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public boolean accept(File dir, String filename) {
            if (prefix != null && !filename.startsWith(prefix)) return false; 
            boolean isTodayFile = DateUtils.isToday(new File(dir, filename).lastModified());
            return !isTodayFile;
        }
    }
}

