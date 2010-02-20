package com.carrotsearch.junitbenchmarks;


/**
 * An interface for consumers of benchmark results.
 */
public interface IResultsConsumer
{
    /**
     * Accept results of a single benchmark.
     */
    void accept(Result result);
}
