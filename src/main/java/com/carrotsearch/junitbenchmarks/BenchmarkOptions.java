package com.carrotsearch.junitbenchmarks;

import java.lang.annotation.*;

/**
 * Benchmark options applicable to methods annotated as tests.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface BenchmarkOptions
{
    /**
     * @return Call {@link System#gc()} before each test. This may slow down the tests
     *         in a significant way, so disabling it is sensible in most cases.
     */
    boolean callgc() default false;

    /**
     * Sets the number of warmup rounds for the test. If negative, the default is taken
     * from global options.
     */
    int warmupRounds() default -1;

    /**
     * Sets the number of benchmark rounds for the test. If negative, the default is taken
     * from global options.
     */
    int benchmarkRounds() default -1;
    
    /**
     * Specifies concurrent/sequential execution model
     * <ul>
     * <li>-1 - executed sequentially</li>
     * <li>0 - executed concurrently with as much threads as reported by Runtime.getRuntime().availableProcessors()</li>
     * <li>1 - executed concurrently by the thread pool scaled down to 1 thread</li>
     * <li>n - executed concurrently with arbitrarily set number of threads</li>
     * </ul>
     */
    int concurrency() default -1;    
}