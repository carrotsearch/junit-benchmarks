package com.carrotsearch.junitbenchmarks;

import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Uladzimir Mihura
 *         Date: 8/8/12
 *         Time: 12:34 PM
 */
public class TestBlock
{
    private static ArrayList<Result> results = new ArrayList<Result>();

    private static IResultsConsumer resultsConsumer = new IResultsConsumer()
    {
        public void accept(Result result)
        {
            results.add(result);
        }
    };

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule(resultsConsumer, new WriterConsumer());


    @Test
    @BenchmarkOptions(clock = Clock.REAL_TIME, benchmarkRounds = 100, concurrency = 1000)
    public void testBlocking1() throws Exception
    {
        synchronized (this)
        {
            Thread.sleep(100);
        }
    }

    @Test
    @BenchmarkOptions(clock = Clock.REAL_TIME, benchmarkRounds = 10)
    public void testBlocking2() throws Exception
    {
        synchronized (this)
        {
            Thread.sleep(100);
        }
    }

    @Test
    @BenchmarkOptions(clock = Clock.REAL_TIME, concurrency = 3, benchmarkRounds = 20)
    public void testBlocking3() throws Exception
    {
        synchronized (this)
        {
            Thread.sleep(200);
        }
    }

    @AfterClass
    public static void verify()
    {
        final double delta = 0.1;
        assertEquals(3, results.size());

        final double avg0 = results.get(0).blockedAverage.avg;
        final double avg1 = results.get(1).blockedAverage.avg;
        final double avg2 = results.get(2).blockedAverage.avg;

        assertTrue(avg0 > 5.5 -delta && avg0 < 5.5 + delta);
        assertTrue(avg1 > -delta && avg1 < delta);
        assertTrue(avg2 > 0.5 - delta && avg2 < 0.5 + delta);
    }
}
