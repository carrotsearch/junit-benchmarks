package com.carrotsearch.junitbenchmarks;

import java.io.*;

import org.junit.*;
import org.junit.rules.MethodRule;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

/**
 * 
 */
public class TestConcurrencyOverride
{
    static StringWriter sw = new StringWriter();
    static IResultsConsumer stringConsumer = new WriterConsumer(sw);

    public static class Nested
    {
        @Rule
        public MethodRule benchmarkRun = new BenchmarkRule(stringConsumer);

        @Test
        @BenchmarkOptions(benchmarkRounds = 20, warmupRounds = 10)
        public void testMethodA() throws Exception
        {
            Thread.sleep(20);
        }
    }

    @Test
    public void testConcurrencyOverride() throws IOException
    {
        System.setProperty(BenchmarkOptionsSystemProperties.CONCURRENCY_PROPERTY, "2");

        sw.getBuffer().setLength(0);
        Result runClasses = JUnitCore.runClasses(Nested.class);
        Assert.assertEquals(1, runClasses.getRunCount());
        Assert.assertTrue(sw.getBuffer().toString().contains("threads: 2"));
    }

    @AfterClass
    public static void cleanup()
    {
        System.clearProperty(BenchmarkOptionsSystemProperties.CONCURRENCY_PROPERTY);
    }
}
