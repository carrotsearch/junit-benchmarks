package com.carrotsearch.junitbenchmarks.h2;

import java.lang.annotation.*;

/**
 * Generate a graphical summary of the historical and current run of a given
 * set of methods. 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface BenchmarkHistoryChart
{
    /**
     * Maximum number of historical runs to take into account.
     */
    int maxRuns() default Integer.MAX_VALUE;
}