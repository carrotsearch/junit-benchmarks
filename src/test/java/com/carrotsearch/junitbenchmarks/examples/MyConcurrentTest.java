package com.carrotsearch.junitbenchmarks.examples;

import junit.framework.Assert;

import org.junit.Before;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;

public class MyConcurrentTest
{
	int roundNo;
	
    @Rule
    public MethodRule benchmarkRun = new BenchmarkRule();

    @Test
    @BenchmarkOptions(benchmarkRounds = 100, warmupRounds = 10)
    public void twentyMillisSequentially() throws Exception
    {
        Thread.sleep(20);
    }

    @Test
    @BenchmarkOptions(benchmarkRounds = 100, warmupRounds = 10, concurrency=1)
    public void twentyMillisSingleThread() throws Exception
    {
        Thread.sleep(20);
    }
    
    @Test
    @BenchmarkOptions(benchmarkRounds = 100, warmupRounds = 10, concurrency=0)
    public void twentyMillisDefaultConcurrency() throws Exception
    {
        Thread.sleep(20);
    }
    
    @Test
    @BenchmarkOptions(benchmarkRounds = 100, warmupRounds = 10, concurrency=4)
    public void twentyMillisConcurrently() throws Exception
    {
        Thread.sleep(20);
    }    

    @Test(timeout=10)
    @BenchmarkOptions(benchmarkRounds = 100, warmupRounds = 10)
    public void twentyMillisSequentiallyWithTimeout() throws Exception
    {
        Thread.sleep(20);
    }

    @Test(timeout=30)
    @BenchmarkOptions(benchmarkRounds = 100, warmupRounds = 10, concurrency=4)
    public void statefullTwentyMillisConcurrentlyWithTimeout() throws Exception
    {
    	synchronized (this) {
    		System.out.println(Thread.currentThread().getName() + " " + roundNo++);
    		if (roundNo == 30) {
    			System.out.println("Mo' sleeping "+ Thread.currentThread().getName() + " " + roundNo++);
    			Thread.sleep(20);
    		}
    	}
    	Thread.sleep(20);

    }
    
    @Test
    @BenchmarkOptions(benchmarkRounds = 100, warmupRounds = 10, concurrency=4)
    public void statefullTwentyMillisConcurrently() throws Exception
    {
    	synchronized (this) {
    		System.out.println(Thread.currentThread().getName() + " " + roundNo++);
    	}
    	Thread.sleep(20);
    }    

    @Test
    @BenchmarkOptions(benchmarkRounds = 100, warmupRounds = 10, callgc=true)
    public void twentyMillisSequentiallyWithGC() throws Exception
    {
        Thread.sleep(20);
    }

    @Test
    @BenchmarkOptions(benchmarkRounds = 100, warmupRounds = 10, concurrency=1, callgc=true)
    public void twentyMillisSingleThreadWithGC() throws Exception
    {
        Thread.sleep(20);
    }
    

    @Test
    @BenchmarkOptions(benchmarkRounds = 100, warmupRounds = 10, concurrency=0, callgc=true)
    public void twentyMillisDefaultConcurrencyWithGC() throws Exception
    {
        Thread.sleep(20);
    }

    @Test
    @BenchmarkOptions(benchmarkRounds = 100, warmupRounds = 10, concurrency=4, callgc=true)
    public void twentyMillisConcurrentlyWithGC() throws Exception
    {
        Thread.sleep(20);
    }    

    @Test
    @BenchmarkOptions(benchmarkRounds = 100, warmupRounds = 10, concurrency=4, callgc=true)
    public void statefullTwentyMillisConcurrentlyWithGC() throws Exception
    {
    	synchronized (this) {
    		System.out.println(Thread.currentThread().getName() + " " + roundNo++);
    	}
    	Thread.sleep(20);
    }    
    
    @Test
    @BenchmarkOptions(benchmarkRounds = 100, warmupRounds = 10, concurrency=4)
    public void twentyMillisConcurrentlyInError() throws Exception
    {
    	synchronized (this) {
    		System.out.println(Thread.currentThread().getName() + " " + roundNo++);
    		if (roundNo == 30) {
    			throw new Exception("Exception at 30th iteration");
    		}
    	}
    	Thread.sleep(20);
    }    

    @Test
    @BenchmarkOptions(benchmarkRounds = 100, warmupRounds = 10, concurrency=4)
    public void twentyMillisConcurrentlyInFailure() throws Exception
    {
    	synchronized (this) {
    		System.out.println(Thread.currentThread().getName() + " " + roundNo++);
    		if (roundNo == 30) {
    			Assert.fail("Assertion failure at 30th iteration");
    		}
    	}
    	Thread.sleep(20);
    }    

    @Test(expected = Exception.class)
    @BenchmarkOptions(benchmarkRounds = 100, warmupRounds = 10, concurrency=4)
    public void twentyMillisConcurrentlyWithExpectedException() throws Exception
    {
    	synchronized (this) {
    		System.out.println(Thread.currentThread().getName() + " " + roundNo++);
    	}
    	Thread.sleep(20);
    	throw new Exception("Expected exception");
    }    

    @Test(expected = Exception.class)
    @BenchmarkOptions(benchmarkRounds = 100, warmupRounds = 10, concurrency=4)
    public void twentyMillisConcurrentlyWithoutExpectedException() throws Exception
    {
    	synchronized (this) {
    		System.out.println(Thread.currentThread().getName() + " " + roundNo++);
    		Thread.sleep(20);
    		if (roundNo != 30) 
    			throw new Exception("Expected exception");
    	}
    }    
    
    @Test
    @BenchmarkOptions(benchmarkRounds = 100, warmupRounds = 10)
    public void twentyMillisSequentiallyInError() throws Exception
    {
    	System.out.println(Thread.currentThread().getName() + " " + roundNo++);
    	Thread.sleep(20);
   		if (roundNo == 30) {
   			throw new Exception("Exception at 30th iteration");
   		}
    }
    
    @Test
    @BenchmarkOptions(benchmarkRounds = 100, warmupRounds = 10)
    public void twentyMillisSequentiallyInFailure() throws Exception
    {
    	System.out.println(Thread.currentThread().getName() + " " + roundNo++);
    	Thread.sleep(20);
   		if (roundNo == 30) {
   			Assert.fail("Assertion failure at 30th iteration");
   		}
    }
    
    @Test(expected = Exception.class)
    @BenchmarkOptions(benchmarkRounds = 100, warmupRounds = 10)
    public void twentyMillisSequentiallyWithExpectedException() throws Exception
    {
   		System.out.println(Thread.currentThread().getName() + " " + roundNo++);
    	Thread.sleep(20);
    	throw new Exception("Expected exception");
    }    

    @Test(expected = Exception.class)
    @BenchmarkOptions(benchmarkRounds = 100, warmupRounds = 10)
    public void twentyMillisSequentiallyWithoutExpectedException() throws Exception
    {
		System.out.println(Thread.currentThread().getName() + " " + roundNo++);
		Thread.sleep(20);
		if (roundNo != 30) 
			throw new Exception("Expected exception");
    }    
    
    @Before
    public void reset() {
    	roundNo = 0;
    }
}