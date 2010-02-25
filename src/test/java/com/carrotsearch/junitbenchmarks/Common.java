package com.carrotsearch.junitbenchmarks;

import static org.junit.Assert.assertTrue;

import java.io.*;

/**
 * Common test utilities.
 */
public final class Common
{
    private Common()
    {
        // no instances.
    }

    /*
     * 
     */
    public static void existsAndDelete(String fileName)
    {
        final File f = new File(fileName);
        assertTrue(f.exists());
        assertTrue(f.delete());
    }

    /*
     * 
     */
    public static String getAndDelete(File file) throws IOException 
    {
        assertTrue(file.exists());
        assertTrue(file.isFile() && file.canRead());

        InputStream is = new FileInputStream(file);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final byte [] buffer = new byte [1024];
        int cnt;
        while ((cnt = is.read(buffer)) > 0) {
            baos.write(buffer, 0, cnt);
        }
        is.close();
        baos.close();

        assertTrue(file.delete());
        
        return new String(baos.toByteArray(), "UTF-8");
    }

    /**
     * Delete recursively.
     */
    public static boolean deleteDir(File dir)
    {
        if (!dir.exists()) return true;

        File [] subfiles = dir.listFiles();
        if (subfiles.length > 0)
        {
            for (File f : subfiles)
            {
                if (f.isFile())
                    f.delete();
                else
                    deleteDir(f);
            }
        }
        return dir.delete();
    }
}
