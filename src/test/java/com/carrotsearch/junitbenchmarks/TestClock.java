package com.carrotsearch.junitbenchmarks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

public class TestClock
{
    private static Map<String, Result> results = new HashMap<String,Result>();

    private static IResultsConsumer resultsConsumer = new IResultsConsumer()
    {
        public void accept(Result result)
        {
            results.put(result.description.getMethodName(), result);
        }
    };

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule(resultsConsumer);


    @Test
    @BenchmarkOptions(clock = Clock.USER_TIME)
    public void testUserTime() throws Exception
    {
        Thread.sleep(100);
    }

    @Test
    @BenchmarkOptions(clock = Clock.CPU_TIME)
    public void testCpuTime() throws Exception
    {
        Thread.sleep(100);
    }


    @Test
    @BenchmarkOptions(clock = Clock.REAL_TIME)
    public void testRealTime() throws Exception
    {
        Thread.sleep(100);
    }


    @AfterClass
    public static void verify()
    {
        final double delta = 0.02;
        assertEquals(3, results.size());

        final double avg1 = results.get("testUserTime").roundAverage.location;
        final double avg2 = results.get("testCpuTime").roundAverage.location;
        final double avg3 = results.get("testRealTime").roundAverage.location;

        assertTrue(avg1 > -delta && avg1 < delta);
        assertTrue(avg2 > -delta && avg2 < delta);
        assertTrue(avg3 > 0.1 - delta && avg3 < 0.1 + delta);
    }
}
