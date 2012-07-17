package com.carrotsearch.junitbenchmarks;

import java.lang.management.ManagementFactory;

/**
 * Used to specify what time to measure in {@link BenchmarkOptions}.
 */
public enum Clock
{

    /**
     * Invokes {@link System#nanoTime()}
     */
    REAL_TIME
            {
                @Override
                long time()
                {
                    return System.nanoTime() / FACTOR;
                }
            },
    /**
     * Invokes {@link java.lang.management.ThreadMXBean#getCurrentThreadCpuTime()}
     */
    CPU_TIME
            {
                @Override
                long time()
                {
                    return ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime() / FACTOR;
                }
            },
    /**
     * Invokes {@link java.lang.management.ThreadMXBean#getCurrentThreadUserTime()}
     */
    USER_TIME
            {
                @Override
                long time()
                {
                    return ManagementFactory.getThreadMXBean().getCurrentThreadUserTime() / FACTOR;
                }
            };

    private static final int FACTOR = 1000000;

    abstract long time();
}
