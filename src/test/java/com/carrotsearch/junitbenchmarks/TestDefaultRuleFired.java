package com.carrotsearch.junitbenchmarks;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.Test;

/**
 * Creates a benchmark with two measured methods. One of the test methods has an
 * overridden number of warmup and benchmark rounds.
 */
public class TestDefaultRuleFired extends AbstractBenchmark
{
    private static int regularMethodInvocationCount;
    private static int customAnnotationMethodInvocationCount;

    @Test
    public void testNormalMethod()
    {
        regularMethodInvocationCount++;
    }

    @Test
    @BenchmarkOptions(warmupRounds = 3, benchmarkRounds = 5)
    public void testCustomAnnotation()
    {
        customAnnotationMethodInvocationCount++;
    }

    @AfterClass
    public static void verifyCounts()
    {
        assertEquals("Custom method invocation count.", 8,
            customAnnotationMethodInvocationCount);

        assertEquals("Regular method invocation count.",
            BenchmarkStatement.DEFAULT_BENCHMARK_ROUNDS
                + BenchmarkStatement.DEFAULT_WARMUP_ROUNDS, regularMethodInvocationCount);
    }
}
