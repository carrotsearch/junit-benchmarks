package com.carrotsearch.junitbenchmarks;


/**
 * An interface for consumers of benchmark results.
 */
public interface IResultsConsumer
{
    /**
     * Accepts the results of a single benchmark.
     */
    void accept(Result result);
}
