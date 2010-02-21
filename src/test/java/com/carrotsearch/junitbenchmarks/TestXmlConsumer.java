package com.carrotsearch.junitbenchmarks;

import static org.junit.Assert.*;

import java.io.File;

import org.dom4j.Document;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;

/**
 * Creates a benchmark with two measured methods. One of the test methods has an
 * overridden number of warmup and benchmark rounds.
 */
public class TestXmlConsumer
{
    private static final File resultsFile = new File("results.xml");
    private static XMLConsumer xmlConsumer;

    @BeforeClass
    public static void checkFile()
    {
        if (resultsFile.exists())
            assertTrue(resultsFile.delete());

        xmlConsumer = new XMLConsumer(resultsFile);
    }

    @Rule
    public MethodRule benchmarkRun = new BenchmarkRule(xmlConsumer);

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
