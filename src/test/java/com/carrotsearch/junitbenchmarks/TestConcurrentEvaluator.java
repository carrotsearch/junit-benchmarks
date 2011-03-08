package com.carrotsearch.junitbenchmarks;

import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.junit.Before;

import org.junit.*;
import org.junit.rules.MethodRule;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;

public class TestConcurrentEvaluator
{
    @SuppressWarnings("serial")
    private static class NestedException extends Exception
    {
    }

    @Rule
    public MethodRule benchmarkRun = new BenchmarkRule();

    private AtomicInteger roundNo = new AtomicInteger();

    @Test
    @BenchmarkOptions(benchmarkRounds = 20, warmupRounds = 10)
    public void twentyMillisSequentially() throws Exception
    {
        Thread.sleep(20);
    }

    @Test
    @BenchmarkOptions(benchmarkRounds = 20, warmupRounds = 10, concurrency = 1)
    public void twentyMillisSingleThread() throws Exception
    {
        Thread.sleep(20);
    }

    @Test
    @BenchmarkOptions(benchmarkRounds = 20, warmupRounds = 10, concurrency = 0)
    public void twentyMillisDefaultConcurrency() throws Exception
    {
        Thread.sleep(20);
    }

    @Test
    @BenchmarkOptions(benchmarkRounds = 20, warmupRounds = 10, concurrency = 4)
    public void twentyMillisConcurrently() throws Exception
    {
        Thread.sleep(20);
    }

    @Test
    @BenchmarkOptions(benchmarkRounds = 20, warmupRounds = 10, concurrency = 4)
    public void statefullTwentyMillisConcurrently() throws Exception
    {
        Thread.sleep(20);
    }

    @Test
    @BenchmarkOptions(benchmarkRounds = 20, warmupRounds = 10, callgc = true)
    public void twentyMillisSequentiallyWithGC() throws Exception
    {
        Thread.sleep(20);
    }

    @Test
    @BenchmarkOptions(benchmarkRounds = 20, warmupRounds = 10, concurrency = 4)
    public void twentyMillisConcurrentlyInError() throws Exception
    {
        Thread.sleep(20);
    }

    /**
     * JUnit expects every run to thrown an exception if expected is set in
     * {@link Test#expected()}. We can't guarantee this with concurrent execution, so it's
     * impossible to make this test succeed.
     */
    @Ignore
    @Test
    @BenchmarkOptions(benchmarkRounds = 20, warmupRounds = 10, concurrency = 4)
    public void twentyMillisConcurrentlyInFailure() throws Exception
    {
        if (roundNo.incrementAndGet() == 30)
        {
            throw new NestedException();
        }
        Thread.sleep(20);
    }

    @Test(expected = NestedException.class)
    @BenchmarkOptions(benchmarkRounds = 20, warmupRounds = 10, concurrency = 4)
    public void twentyMillisConcurrentlyWithExpectedException() throws Exception
    {
        Thread.sleep(20);
        throw new NestedException();
    }

    /**
     * JUnit expects every run to thrown an exception if expected is set in
     * {@link Test#expected()}. We can't guarantee this with concurrent execution, so it's
     * impossible to make this test succeed.
     */
    @Ignore
    @Test
    @BenchmarkOptions(benchmarkRounds = 20, warmupRounds = 10, concurrency = 1)
    public void twentyMillisSequentiallyInFailure() throws Exception
    {
        Thread.sleep(20);
        if (roundNo.incrementAndGet() == 30)
        {
            Assert.fail("Assertion failure at 30th iteration");
        }
    }

    @Test(expected = Exception.class)
    @BenchmarkOptions(benchmarkRounds = 20, warmupRounds = 10)
    public void twentyMillisSequentiallyWithExpectedException() throws Exception
    {
        Thread.sleep(20);
        throw new Exception("Expected exception");
    }

    @Before
    public void reset()
    {
        roundNo.set(0);
    }
}