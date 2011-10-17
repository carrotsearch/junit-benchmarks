package com.carrotsearch.junitbenchmarks.h2;

import java.io.File;
import java.sql.SQLException;
import java.util.Random;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;

import com.carrotsearch.junitbenchmarks.*;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.annotation.LabelType;

/**
 * Declare a single method only. 
 */
@BenchmarkHistoryChart(labelWith = LabelType.CUSTOM_KEY)
@BenchmarkOptions(callgc = false)
public class SingleMethodSlave
{
    static boolean cleanup = true; 
    static final File dbFile = new File(SingleMethodSlave.class.getName());
    static final File dbFileFull = new File(dbFile.getName() + ".h2.db");

    private static H2Consumer h2consumer;

    @Rule
    public MethodRule benchmarkRun = new BenchmarkRule(h2consumer);

    @BeforeClass
    public static void checkFile() throws SQLException
    {
        h2consumer = new H2Consumer(dbFile);
    }

    @Test
    public void testMethodA() throws Exception
    {
        Thread.sleep(new Random().nextInt(20));
    }

    @AfterClass
    public static void verify() throws Exception
    {
        h2consumer.close();

        if (cleanup)
        {
            Common.existsAndDelete(SingleMethodSlave.class.getName() + ".html");
            Common.existsAndDelete(SingleMethodSlave.class.getName() + ".json");
            dbFileFull.delete();
        }
    }
}
