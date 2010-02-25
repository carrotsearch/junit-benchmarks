package com.carrotsearch.junitbenchmarks.h2;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.sql.SQLException;

import org.junit.*;
import org.junit.rules.MethodRule;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.Common;
import com.carrotsearch.junitbenchmarks.h2.BenchmarkMethodChart;
import com.carrotsearch.junitbenchmarks.h2.H2Consumer;

/**
 * Test H2 consumer's chart generation functionality. 
 */
@BenchmarkMethodChart
@BenchmarkOptions(callgc = false)
public class TestH2MethodChart
{
    private static final File dbFile = new File(TestH2MethodChart.class.getName());
    private static final File dbFileFull = new File(dbFile.getName() + ".h2.db");

    private static H2Consumer h2consumer;

    @BeforeClass
    public static void checkFile() throws SQLException
    {
        if (dbFileFull.exists())
            assertTrue(dbFileFull.delete());

        h2consumer = new H2Consumer(dbFile);
    }

    @Rule
    public MethodRule benchmarkRun = new BenchmarkRule(h2consumer);

    @Test
    public void testMethodA() throws Exception
    {
        Thread.sleep(20);
    }

    @Test
    public void testMethodB() throws Exception
    {
        Thread.sleep(40);
    }

    @Test
    public void testMethodC() throws Exception
    {
        Thread.sleep(60);
    }

    @AfterClass
    public static void verify() throws Exception
    {
        h2consumer.close();
        assertTrue(dbFileFull.exists());
        assertTrue(dbFileFull.delete());

        Common.existsAndDelete(TestH2MethodChart.class.getName() + ".html");
        Common.existsAndDelete(TestH2MethodChart.class.getName() + ".json");
    }
}
