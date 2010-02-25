package com.carrotsearch.junitbenchmarks;

import static org.junit.Assert.assertEquals;

import java.io.*;

import org.junit.*;
import org.junit.rules.MethodRule;

/**
 * Test global consumers and properties.
 */
public class TestGlobalConsumers
{
    private static final File resultsFile = new File("results.xml");

    @BeforeClass
    public static void checkFile() throws IOException
    {
        System.setProperty(Globals.CONSUMERS_PROPERTY, 
              ConsumerName.CONSOLE + ", "
            + ConsumerName.XML);
        
        System.setProperty(Globals.XML_FILE_PROPERTY, resultsFile.getAbsolutePath());
    }

    @Rule
    public MethodRule benchmarkRun = new BenchmarkRule();

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
        for (IResultsConsumer c : Globals.consumers)
        {
            if (c instanceof Closeable)
                ((Closeable) c).close();
        }
        assertEquals(2, Globals.consumers.length);
        Globals.consumers = null;
        Common.assertFileExists(resultsFile.getAbsolutePath());
    }
}
