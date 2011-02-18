package com.carrotsearch.junitbenchmarks.h2;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import com.carrotsearch.junitbenchmarks.Common;
import com.carrotsearch.junitbenchmarks.BenchmarkOptionsSystemProperties;

/**
 *  
 */
public class TestH2HistoryChartCustomKey
{
    @Test
    public void testCustomKeyStored() throws Exception
    {
        RepeatedTestSlave.dbFileFull.delete();

        RepeatedTestSlave.cleanup = false;
        try
        {
            System.setProperty(BenchmarkOptionsSystemProperties.CUSTOMKEY_PROPERTY, "custom-key1");
            assertEquals(0, JUnitCore.runClasses(RepeatedTestSlave.class).getFailureCount());
            System.setProperty(BenchmarkOptionsSystemProperties.CUSTOMKEY_PROPERTY, "custom-key2");
            assertEquals(0, JUnitCore.runClasses(RepeatedTestSlave.class).getFailureCount());
            System.setProperty(BenchmarkOptionsSystemProperties.CUSTOMKEY_PROPERTY, "custom-key3");
            assertEquals(0, JUnitCore.runClasses(RepeatedTestSlave.class).getFailureCount());
        }
        finally
        {
            RepeatedTestSlave.cleanup = true;            
        }

        Common.existsAndDelete(RepeatedTestSlave.class.getName() + ".html");
        String content = Common.getAndDelete(
            new File(RepeatedTestSlave.class.getName() + ".json"));
        RepeatedTestSlave.dbFileFull.delete();

        assertEquals(1, count(content, "custom-key1"));
        assertEquals(1, count(content, "custom-key2"));
        assertEquals(1, count(content, "custom-key3"));
    }

    private int count(String content, String pattern)
    {
        Pattern p = Pattern.compile(pattern, Pattern.LITERAL);
        int cnt = 0;
        for (Matcher m = p.matcher(content); m.find(); cnt++)
        {
            // repeat.
        }
        return cnt;
    }

    @AfterClass
    public static void cleanup()
    {
        System.clearProperty(BenchmarkOptionsSystemProperties.CUSTOMKEY_PROPERTY);
    }
}
