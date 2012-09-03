package com.carrotsearch.junitbenchmarks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.dom4j.Document;
import org.junit.*;
import org.junit.rules.TestRule;

/**
 * Creates a benchmark with two measured methods. One of the test methods has an
 * overridden number of warmup and benchmark rounds.
 */
public class TestXmlConsumer
{
    private static final File resultsFile = new File("results.xml");
    private static XMLConsumer xmlConsumer;

    @BeforeClass
    public static void checkFile() throws IOException
    {
        if (resultsFile.exists())
            assertTrue(resultsFile.delete());

        xmlConsumer = new XMLConsumer(resultsFile);
    }

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule(xmlConsumer);

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
        xmlConsumer.close();

        assertTrue(resultsFile.exists());
        final Document d = new org.dom4j.io.SAXReader().read(resultsFile);
        assertEquals(2, d.selectNodes("//testname").size());
        assertTrue(resultsFile.delete());
    }
}
