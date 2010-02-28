package com.carrotsearch.junitbenchmarks.h2;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import com.carrotsearch.junitbenchmarks.Common;
import com.carrotsearch.junitbenchmarks.Globals;

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
            System.setProperty(Globals.CUSTOMKEY_PROPERTY, "custom-key1");
            assertEquals(0, JUnitCore.runClasses(RepeatedTestSlave.class).getFailureCount());
            System.setProperty(Globals.CUSTOMKEY_PROPERTY, "custom-key2");
            assertEquals(0, JUnitCore.runClasses(RepeatedTestSlave.class).getFailureCount());
            System.setProperty(Globals.CUSTOMKEY_PROPERTY, "custom-key3");
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
        
        assertTrue(content.contains("custom-key1"));
        assertTrue(content.contains("custom-key2"));
        assertTrue(content.contains("custom-key3"));
    }

    @AfterClass
    public static void cleanup()
    {
        System.clearProperty(Globals.CUSTOMKEY_PROPERTY);
    }
}
