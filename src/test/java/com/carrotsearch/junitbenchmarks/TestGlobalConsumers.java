package com.carrotsearch.junitbenchmarks;

import static org.junit.Assert.assertEquals;

import java.io.*;

import org.junit.*;
import org.junit.rules.TestRule;

/**
 * Test global consumers and properties.
 */
public class TestGlobalConsumers
{
    private static final File resultsFile = new File("results.xml");

    @BeforeClass
    public static void checkFile() throws IOException
    {
        System.setProperty(BenchmarkOptionsSystemProperties.CONSUMERS_PROPERTY, 
              ConsumerName.CONSOLE + ", "
            + ConsumerName.XML);
        
        System.setProperty(BenchmarkOptionsSystemProperties.XML_FILE_PROPERTY, resultsFile.getAbsolutePath());

        // Close any previous globals.
        closeGlobals();
    }

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();

    @Test
    public void testMethodA()
    {
        // empty.
    }

    @Test
    public void testMethodB()
    {
        // empty.
    }

    @AfterClass
    public static void verify() throws Exception
    {
        assertEquals(2, closeGlobals());
        Common.existsAndDelete(resultsFile.getAbsolutePath());

        System.clearProperty(BenchmarkOptionsSystemProperties.CONSUMERS_PROPERTY);
        System.clearProperty(BenchmarkOptionsSystemProperties.XML_FILE_PROPERTY);
    }

    /*
     * 
     */
    private static int closeGlobals() throws IOException
    {
        if (BenchmarkOptionsSystemProperties.consumers == null)
            return 0;

        for (IResultsConsumer c : BenchmarkOptionsSystemProperties.consumers)
        {
            if (c instanceof Closeable)
                ((Closeable) c).close();
        }
        final int count = BenchmarkOptionsSystemProperties.consumers.length;
        BenchmarkOptionsSystemProperties.consumers = null;
        return count;
    }
}
