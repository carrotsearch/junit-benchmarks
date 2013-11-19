package com.carrotsearch.junitbenchmarks.examples;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.BenchmarkRunner;

@RunWith(BenchmarkRunner.class)
public class MyTest
{
    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();

    @Test
    @BenchmarkOptions(benchmarkRounds = 3, warmupRounds = 1)
    public void eightyMillis() throws Exception
    {
        Thread.sleep(80);
    }
    
    @Test
    @BenchmarkOptions(benchmarkRounds = 3, warmupRounds = 1)
    public void fortyMillis() throws Exception
    {
        Thread.sleep(40);
    }
    
    @Before
    public void setUp() throws Exception{
       Thread.sleep(50);
    }
    
    @After
    public void tearDown() throws Exception{
       Thread.sleep(60);
    }
}