package com.carrotsearch.junitbenchmarks;

import java.lang.management.ThreadInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.carrotsearch.junitbenchmarks.BenchmarkStatement.LongHolder;

/**
  * Factored out as a nested class as it needs to keep some data during test
  * evaluation.
  */
 abstract class LegacyBaseEvaluator implements BenchmarkEvaluator,TimeRecorder
 {
     /**
    * 
    */
     private final BenchmarkStatement benchmarkStatement;
     final private List<SingleResult> results;
     final protected int warmupRounds;
     final protected int benchmarkRounds;
     final protected int totalRounds;

     final private Clock clock;

     private long warmupTime;
     private long startTime;
     private long stopTime;
     
     private final ThreadLocal<LongHolder> previousThreadBlockedTime = new ThreadLocal<LongHolder>() {
         protected LongHolder initialValue() {
             return new BenchmarkStatement.LongHolder();
         }
     };

     protected LegacyBaseEvaluator(BenchmarkStatement benchmarkStatement, int warmupRounds, int benchmarkRounds, int totalRounds, Clock clock)
     {
         this.benchmarkStatement = benchmarkStatement;
         this.warmupRounds = warmupRounds;
         this.benchmarkRounds = benchmarkRounds;
         this.totalRounds = totalRounds;
         this.clock = clock;
         this.results = Collections.synchronizedList(new ArrayList<SingleResult>(totalRounds));
     }

     protected GCSnapshot gcSnapshot = null;

     protected final void evaluateInternally(int round) throws Throwable
     {
         // We assume no reordering will take place here.
         final long startTime = clock.time();
         benchmarkStatement.cleanupMemory();
         final long afterGC = clock.time();

         if (round == warmupRounds)
         {
             gcSnapshot = new GCSnapshot();
             warmupTime = clock.time();
         }

         benchmarkStatement.executeTest();
         final long endTime = clock.time();

         final long roundBlockedTime;
         if (BenchmarkStatement.supportsThreadContention) {
             final long threadId = Thread.currentThread().getId();
             final ThreadInfo threadInfo = BenchmarkStatement.threadMXBean.getThreadInfo(threadId);
             final long threadBlockedTime = threadInfo.getBlockedTime();
             final long previousValue = previousThreadBlockedTime.get().getAndSet(threadBlockedTime);
             roundBlockedTime = threadBlockedTime - previousValue;
         } else {
             roundBlockedTime = 0;
         }

         this.results.add(new DefaultSingleResult(startTime, afterGC, endTime, roundBlockedTime));
     }

     @Override
     public void startTest() {
        startTime = clock.time();
     }

     @Override
     public void stopTest() {
        stopTime = clock.time();
     }

     @Override
     public int startTestIteration() {
      return 0;
     }

     @Override
     public void stopTestIteration(int id) {
     }
     
     protected Result computeResult()
     {
         final Statistics stats = Statistics.from(
             results.subList(warmupRounds, totalRounds));

         return new Result(benchmarkStatement.description, benchmarkRounds, warmupRounds, warmupTime - startTime,
             stopTime - warmupTime, stats.evaluation, stats.blocked, stats.gc, gcSnapshot, 1);
     }
 }