package com.carrotsearch.junitbenchmarks;

import static org.junit.Assert.assertTrue;

import java.io.File;

/**
 * Common test utilities.
 */
public final class Common
{
    private Common()
    {
        // no instances.
    }

    public static void assertFileExists(String fileName)
    {
        final File f = new File(fileName);  
        assertTrue(f.exists());
        assertTrue(f.delete());
    }
}
