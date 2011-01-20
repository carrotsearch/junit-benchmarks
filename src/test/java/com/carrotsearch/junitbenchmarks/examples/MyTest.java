package com.carrotsearch.junitbenchmarks.examples;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;

public class MyTest
{
    @Rule
    public MethodRule benchmarkRun = new BenchmarkRule();

    @Test
    @BenchmarkOptions(benchmarkRounds = 1, warmupRounds = 0)
    public void twentyMillis() throws Exception
    {
        Thread.sleep(20);
    }
}