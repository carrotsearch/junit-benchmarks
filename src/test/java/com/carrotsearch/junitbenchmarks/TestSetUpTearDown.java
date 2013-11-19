package com.carrotsearch.junitbenchmarks;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

@RunWith(BenchmarkRunner.class)
public class TestSetUpTearDown
{
   
   public  static final double DELTA = 0.002;
   private static Map<String, Result> results = new HashMap<String,Result>();

   private static IResultsConsumer resultsConsumer = new IResultsConsumer()
   {
       public void accept(Result result)
       {
           results.put(result.description.getMethodName(), result);
       }
   };

   @Rule
   public TestRule benchmarkRun = new BenchmarkRule(resultsConsumer);

    @Test
    @BenchmarkOptions(benchmarkRounds = 5, warmupRounds = 2)
    public void testEightyMillis() throws Exception
    {
        Thread.sleep(80);
    }
    
    @Test
    @BenchmarkOptions(benchmarkRounds = 9, warmupRounds = 3, concurrency = 3)
    public void testFortyMillis() throws Exception
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
    
    @AfterClass
    public static void verify()
    {
        
        assertEquals(2, results.size());

        final double avg1 = results.get("testEightyMillis").roundAverage.avg;
        final double avg2 = results.get("testFortyMillis").roundAverage.avg;

        assertEquals(0.08, avg1, DELTA);
        assertEquals(0.04, avg2, DELTA);
    }
}