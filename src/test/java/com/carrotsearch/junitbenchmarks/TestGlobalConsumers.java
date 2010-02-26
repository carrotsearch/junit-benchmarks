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

        // Close any previous globals.
        closeGlobals();
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
        assertEquals(2, closeGlobals());
        Common.existsAndDelete(resultsFile.getAbsolutePath());

        System.clearProperty(Globals.CONSUMERS_PROPERTY);
        System.clearProperty(Globals.XML_FILE_PROPERTY);
    }

    /*
     * 
     */
    private static int closeGlobals() throws IOException
    {
        if (Globals.consumers == null)
            return 0;

        for (IResultsConsumer c : Globals.consumers)
        {
            if (c instanceof Closeable)
                ((Closeable) c).close();
        }
        final int count = Globals.consumers.length;
        Globals.consumers = null;
        return count;
    }
}
