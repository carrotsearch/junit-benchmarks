package com.carrotsearch.junitbenchmarks;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

/**
 * Test the average times.
 */
public class TestTimes
{
    private static ArrayList<Result> results = new ArrayList<Result>();

    private static IResultsConsumer resultsConsumer = new IResultsConsumer() {
        public void accept(Result result)
        {
            results.add(result);
        }
    };
    
    @Rule
    public TestRule benchmarkRun = new BenchmarkRule(resultsConsumer);

    @Test
    public void test100msDelay() throws Exception
    {
        Thread.sleep(100);
    }

    @AfterClass
    public static void verify()
    {
        assertEquals(1, results.size());

        final double avg = results.get(0).roundAverage.location;
        final double delta = 0.02;
        assertTrue(avg > 0.1 - delta && avg < 0.1 + delta);
    }
}
