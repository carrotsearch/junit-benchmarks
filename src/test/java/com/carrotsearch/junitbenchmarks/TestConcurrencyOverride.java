package com.carrotsearch.junitbenchmarks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

/**
 * 
 */
public class TestConcurrencyOverride
{
    public static class Nested extends AbstractBenchmark
    {
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

        PrintStream ps = System.out;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out, true, "UTF-8"));
        try {
            Result runClasses = JUnitCore.runClasses(Nested.class);
            System.out.flush();
            Assert.assertEquals(1, runClasses.getRunCount());
            Assert.assertTrue(new String(out.toByteArray(), "UTF-8").contains("threads: 2"));
        } finally {
            System.setOut(ps);
        }
    }

    @AfterClass
    public static void cleanup()
    {
        System.clearProperty(BenchmarkOptionsSystemProperties.CONCURRENCY_PROPERTY);
    }
}
