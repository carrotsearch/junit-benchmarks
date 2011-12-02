package com.carrotsearch.junitbenchmarks;

import java.lang.reflect.Method;

import org.junit.runners.model.FrameworkMethod;

/**
 * A result of a single benchmark test.
 */
public final class Result
{
    final Object target;
    final FrameworkMethod method;

    public final int benchmarkRounds, warmupRounds;
    public final long warmupTime, benchmarkTime;
    public final Average roundAverage;
    public final Average gcAverage;
    public final GCSnapshot gcInfo;

    /**
     * Concurrency level (number of used threads).
     */
    int concurrency;

    /**
     * @param target Target object (test).
     * @param method Target tested method.
     * @param benchmarkRounds Number of executed benchmark rounds.
     * @param warmupRounds Number of warmup rounds.
     * @param warmupTime Total warmup time, includes benchmarking and GC overhead.
     * @param benchmarkTime Total benchmark time, includes benchmarking and GC overhead.
     * @param roundAverage Average and standard deviation from benchmark rounds.
     * @param gcAverage Average and standard deviation from GC cleanups.
     * @param gcInfo Extra information about GC activity.
     * @param concurrency {@link BenchmarkOptions#concurrency()} setting (or global override).
     */
    public Result(
        Object target, 
        FrameworkMethod method,
        int benchmarkRounds,
        int warmupRounds, 
        long warmupTime, 
        long benchmarkTime,
        Average roundAverage, 
        Average gcAverage, 
        GCSnapshot gcInfo,
        int concurrency)
    {
        this.target = target;
        this.method = method;
        this.benchmarkRounds = benchmarkRounds;
        this.warmupRounds = warmupRounds;
        this.warmupTime = warmupTime;
        this.benchmarkTime = benchmarkTime;
        this.roundAverage = roundAverage;
        this.gcAverage = gcAverage;
        this.gcInfo = gcInfo;
        this.concurrency = concurrency;
    }
    
    /**
     * Returns the short version of the test's class.
     */
    public String getShortTestClassName()
    {
        return target.getClass().getSimpleName();
    }

    /**
     * Returns the long version of the test's class.
     */
    public String getTestClassName()
    {
        return target.getClass().getName();
    }

    /**
     * @return Return the test method's name.
     */
    public String getTestMethodName()
    {
        return method.getName();
    }

    /**
     * Returns the class under test.
     */
    public Class<?> getTestClass()
    {
        return target.getClass();
    }

    /**
     * Returns the method under test. 
     */
    public Method getTestMethod()
    {
        return method.getMethod();
    }

    public int getThreadCount()
    {
        return concurrency;
    }
}
