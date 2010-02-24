package com.carrotsearch.junitbenchmarks;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.sql.SQLException;

import org.junit.*;
import org.junit.rules.MethodRule;

import com.carrotsearch.junitbenchmarks.h2.GenerateMethodChart;
import com.carrotsearch.junitbenchmarks.h2.H2Consumer;

/**
 * Test H2 consumer's chart generation functionality. 
 */
@GenerateMethodChart
@BenchmarkOptions(callgc = false)
public class TestH2Charts
{
    private static final File dbFile = new File("test-benchmarks");
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

        final File chart = new File(TestH2Charts.class.getName() + ".html");  
        assertTrue(chart.exists());
        //assertTrue(chart.delete());
    }
}
