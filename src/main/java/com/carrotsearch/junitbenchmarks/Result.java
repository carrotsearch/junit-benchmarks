package com.carrotsearch.junitbenchmarks;

import org.junit.runners.model.FrameworkMethod;

/**
 * A result of a single benchmark test.
 */
public final class Result
{
    public final Object target;
    public final FrameworkMethod method;
    public final int benchmarkRounds, warmupRounds;
    public final long warmupTime, benchmarkTime;
    public final Average roundAverage;
    public final Average gcAverage;
    public final GCSnapshot gcInfo;

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
        GCSnapshot gcInfo)
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
    }
}
