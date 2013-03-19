package com.carrotsearch.junitbenchmarks.h2;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.*;
import org.junit.rules.TestRule;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import com.carrotsearch.junitbenchmarks.*;
import com.carrotsearch.junitbenchmarks.annotation.*;

/**
 * Test method-level chart generation, with subclass hierarchy.
 * 
 * @see "http://issues.carrot2.org/browse/JUNITBENCH-40"
 */
public class TestH2MethodChartClassHierarchy
{
    @AxisRange(max = 1)
    @BenchmarkMethodChart(filePrefix = "a")
    @BenchmarkHistoryChart(filePrefix = "ha")
    @BenchmarkOptions(callgc = false)
    public abstract static class A
    {
        private static H2Consumer h2consumer;
        private static File dbFile;

        @BeforeClass
        public static void h2open() throws Exception
        {
            dbFile = new File("temp.db");
            h2consumer = new H2Consumer(dbFile);
        }

        @AfterClass
        public static void h2close() throws Exception
        {
            h2consumer.close();
            assertTrue(new File(dbFile.getAbsolutePath() + ".h2.db").delete());
        }

        @Rule
        public TestRule benchmarkRun = new BenchmarkRule(h2consumer);

        @Test
        public void onlyInSuper() throws Exception
        {
            Thread.sleep(20);
        }

        @Test
        public abstract void inherited() throws Exception;
    }

    @BenchmarkMethodChart(filePrefix = "b")
    @BenchmarkHistoryChart(filePrefix = "hb")
    public static class B extends A
    {
        @Override
        public void inherited() throws Exception
        {
            Thread.sleep(40);
        }
    }

    @BenchmarkHistoryChart(filePrefix = "hc")
    @BenchmarkMethodChart(filePrefix = "c")
    public static class C extends B
    {
        @Override
        public void inherited() throws Exception
        {
            Thread.sleep(80);
        }
    }

    public static class D extends A
    {
        @Override
        public void inherited() throws Exception
        {
            Thread.sleep(40);
        }
    }

    @Test
    public void testClassHierarchy()
    {
        Result result = JUnitCore.runClasses(B.class, C.class, D.class);
        Assert.assertEquals(0, result.getFailureCount());
        Common.existsAndDelete("a.html", "a.jsonp");
        Common.existsAndDelete("b.html", "b.jsonp");
        Common.existsAndDelete("c.html", "c.jsonp");
        Common.existsAndDelete("ha.html", "ha.jsonp");
        Common.existsAndDelete("hb.html", "hb.jsonp");
        Common.existsAndDelete("hc.html", "hc.jsonp");
    }
}
