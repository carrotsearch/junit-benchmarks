package com.carrotsearch.junitbenchmarks.examples;

import java.util.Random;
import org.junit.Test;
import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;

public class WhatsMyAverageRunningTime extends AbstractBenchmark
{
    private final static Random rnd = new Random();

    @BenchmarkOptions(benchmarkRounds = 50, warmupRounds = 0)
    @Test
    public void question() throws Exception
    {
        Thread.sleep(rnd.nextInt(100));
    }
}