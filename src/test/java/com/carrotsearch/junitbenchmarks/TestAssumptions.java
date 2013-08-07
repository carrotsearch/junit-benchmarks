package com.carrotsearch.junitbenchmarks;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.JUnitCore;

/**
 * Test assumptions don't cause a failure.
 */
public class TestAssumptions
{
    private static final ArrayList<Result> results = new ArrayList<Result>();
    private static final IResultsConsumer resultsConsumer = new IResultsConsumer()
    {
        public void accept(Result result)
        {
            results.add(result);
        }
    };
    
    static volatile boolean runsAsNested;

    public static class AssumeOnBeforeClass
    {
        @Rule
        public TestRule benchmarkRun = new BenchmarkRule(resultsConsumer);

        @BeforeClass
        public static void beforeClass()
        {
            if (runsAsNested) Assume.assumeTrue(false);
        }

        @Test
        public void testDummy() {}
    }

    public static class AssumeOnBeforeInstance
    {
        @Rule
        public TestRule benchmarkRun = new BenchmarkRule(resultsConsumer);

        @Before
        public void beforeInstance()
        {
            if (runsAsNested) Assume.assumeTrue(false);
        }

        @Test
        public void testDummy() {}
    }
    
    public static class AssumeOnTest
    {
        @Rule
        public TestRule benchmarkRun = new BenchmarkRule(resultsConsumer);

        @Test
        public void testDummy() 
        {
            if (runsAsNested) Assume.assumeTrue(false);
        }
    }
    
    @Before
    public void enableNested()
    {
        runsAsNested = true;
        results.clear();
    }
    
    @After
    public void disableNested()
    {
        runsAsNested = false;
    }

    @Test
    public void testBeforeClass()
    {
        org.junit.runner.Result result = JUnitCore.runClasses(AssumeOnBeforeClass.class);
        assertThat(result.getFailures()).isEmpty();
        assertThat(result.getIgnoreCount()).isEqualTo(1);
        assertThat(result.getRunCount()).isEqualTo(0);
        assertThat(result.getFailureCount()).isEqualTo(0);
    }
    
    @Test
    public void testBeforeInstance()
    {
        org.junit.runner.Result result = JUnitCore.runClasses(AssumeOnBeforeInstance.class);
        assertThat(result.getFailures()).isEmpty();
        assertThat(result.getIgnoreCount()).isEqualTo(0);
        assertThat(result.getRunCount()).isEqualTo(1);
        assertThat(result.getFailureCount()).isEqualTo(0);
    }

    @Test
    public void testOnTest()
    {
        org.junit.runner.Result result = JUnitCore.runClasses(AssumeOnTest.class);
        assertThat(result.getFailures()).isEmpty();
        assertThat(result.getIgnoreCount()).isEqualTo(0);
        assertThat(result.getRunCount()).isEqualTo(1);
        assertThat(result.getFailureCount()).isEqualTo(0);
    }

}
