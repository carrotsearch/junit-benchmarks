package com.carrotsearch.junitbenchmarks;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.Test;

/**
 * @see "JUNITBENCH-53"
 */
@BenchmarkOptions(warmupRounds = 333, benchmarkRounds = 333)
public class TestClassLevelAnnotation extends AbstractBenchmark
{
    private static int totalRounds;

    @Test
    public void benchMethod()
    {
        totalRounds++;
    }

    @AfterClass
    public static void verifyCounts()
    {
        assertEquals("Wrong count.", 666, totalRounds);
    }
}
