package com.carrotsearch.junitbenchmarks.examples;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

public class MedianNotMean {
    
    private int meanSleep = 100;
    private int medianSleep = 100;
    
    @Test
    @BenchmarkOptions(callgc = false, median = false,
            warmupRounds = 0, benchmarkRounds = 4)
	public void mean() throws Exception {
		Thread.sleep(meanSleep *= 2);
	}

    @Test
    @BenchmarkOptions(callgc = false, median = true,
            warmupRounds = 0, benchmarkRounds = 4)
	public void median() throws Exception {
		Thread.sleep(medianSleep *= 2);
	}
    
    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();
}