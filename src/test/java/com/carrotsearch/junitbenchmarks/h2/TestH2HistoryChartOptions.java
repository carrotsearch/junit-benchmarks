package com.carrotsearch.junitbenchmarks.h2;

import static org.junit.Assert.*;

import java.io.File;
import java.sql.SQLException;
import java.util.Random;

import org.junit.*;
import org.junit.rules.MethodRule;

import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.Common;

@BenchmarkHistoryChart(filePrefix = "class-global")
public class TestH2HistoryChartOptions
{
    static final File dbFile = new File(RepeatedTestSlave.class.getName());
    static final File dbFileFull = new File(dbFile.getName() + ".h2.db");
    static final File chartsDir = new File("tmp-subdir");

    private static H2Consumer h2consumer;

    @Rule
    public MethodRule benchmarkRun = new BenchmarkRule(h2consumer);

    @BeforeClass
    public static void checkFile() throws SQLException
    {
        Common.deleteDir(chartsDir);
        chartsDir.mkdir();

        h2consumer = new H2Consumer(dbFile, chartsDir, null);
    }

    @Test
    public void testMethodA() throws Exception
    {
        Thread.sleep(new Random().nextInt(20));
    }

    @BenchmarkHistoryChart(filePrefix = "method-level")
    @Test
    public void testMethodB() throws Exception
    {
        Thread.sleep(new Random().nextInt(30));
    }

    @BenchmarkHistoryChart(filePrefix = "method-level")
    @Test
    public void testMethodC() throws Exception
    {
        Thread.sleep(new Random().nextInt(30));
    }

    @AfterClass
    public static void verify() throws Exception
    {
        h2consumer.close();
        assertTrue(dbFileFull.delete());

        String c1 = Common.getAndDelete(new File(chartsDir, "class-global" + ".json"));
        assertTrue(c1.contains("testMethodA"));
        assertTrue(c1.contains("testMethodB"));
        assertTrue(c1.contains("testMethodC"));

        String c2 = Common.getAndDelete(new File(chartsDir, "method-level" + ".json"));
        assertFalse(c2.contains("testMethodA"));
        assertTrue(c2.contains("testMethodB"));
        assertTrue(c2.contains("testMethodC"));


        assertTrue(Common.deleteDir(chartsDir));
    }
}
