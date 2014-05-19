package com.carrotsearch.junitbenchmarks;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

public class TestConcurrentEvaluator2
{
    private static ArrayList<Result> results = new ArrayList<Result>();

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule(new IResultsConsumer() {
        public void accept(Result result)
        {
            results.add(result);
        }
    });

    static final Object foo = new Object();

    @Test
    @BenchmarkOptions(
        benchmarkRounds = 1000, 
        warmupRounds = 5, 
        clock = Clock.NANO_TIME, 
        concurrency = 100)
    public void runHighlyConcurrentWithBlocking() throws Exception
    {
        synchronized (foo) {
            // Do nothing.
            Thread.sleep(1);
        }
    }
    
    @BeforeClass
    public static void beforeClass() {
        results.clear();
        
        Assume.assumeTrue("Should support thread contention.", 
            ManagementFactory.getThreadMXBean().isThreadContentionMonitoringSupported());
    }

    @AfterClass
    public static void afterClass() {
        Assert.assertEquals(1, results.size());
        Assert.assertTrue("Blocked time should be > 0: " + results.get(0).blockedAverage.location,
            results.get(0).blockedAverage.location > 0);
    }
}