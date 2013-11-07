package com.carrotsearch.junitbenchmarks;

/**
  * Performs test method evaluation sequentially.
  */
 final class SequentialEvaluator extends BaseEvaluator
 {

   SequentialEvaluator(BenchmarkStatement benchmarkStatement, int warmupRounds, int benchmarkRounds, int totalRounds, Clock clock)
     {
         super(benchmarkStatement, warmupRounds, benchmarkRounds, totalRounds, clock);
     }

     @Override
     public Result evaluate() throws Throwable
     {
         startTest();
         for (int i = 0; i < totalRounds; i++)
         {
             evaluateInternally(i);
         }
         stopTest();
         return computeResult();
     }
 }