package com.carrotsearch.junitbenchmarks;

import java.lang.management.ThreadInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.carrotsearch.junitbenchmarks.BenchmarkStatement.LongHolder;

/**
  * Factored out as a nested class as it needs to keep some data during test
  * evaluation.
  */
 abstract class BaseEvaluator implements BenchmarkEvaluator,TimeRecorder
 {
     /**
    * 
    */
     private final BenchmarkStatement benchmarkStatement;
     final private SimpleSingleResult[] results;
     final protected int warmupRounds;
     final protected int benchmarkRounds;
     final protected int totalRounds;

     final private Clock clock;

     private long warmupTime;
     private long startTime;
     private long stopTime;
     private final AtomicInteger idGenerator;
     
     private final ThreadLocal<LongHolder> previousThreadBlockedTime = new ThreadLocal<LongHolder>() {
         protected LongHolder initialValue() {
             return new BenchmarkStatement.LongHolder();
         }
     };

     protected BaseEvaluator(BenchmarkStatement benchmarkStatement, int warmupRounds, int benchmarkRounds, int totalRounds, Clock clock)
     {
         this.benchmarkStatement = benchmarkStatement;
         this.warmupRounds = warmupRounds;
         this.benchmarkRounds = benchmarkRounds;
         this.totalRounds = totalRounds;
         this.clock = clock;
         this.results = new SimpleSingleResult[totalRounds];
         this.idGenerator = new AtomicInteger(0);
     }

     protected GCSnapshot gcSnapshot = null;

     protected final void evaluateInternally(int round) throws Throwable
     {
         // We assume no reordering will take place here.
         final long startTime = clock.time();
         benchmarkStatement.cleanupMemory();
         final long afterGC = clock.time();
         results[round] = new SimpleSingleResult(afterGC - startTime);
         
         if (round == warmupRounds)
         {
             gcSnapshot = new GCSnapshot();
             warmupTime = clock.time();
         }

         benchmarkStatement.executeTest();
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
        final int id = idGenerator.getAndIncrement();
        results[id].startTestTime = clock.time();
      return id;
     }

     @Override
     public void stopTestIteration(int id) {
        results[id].stopTestTime = clock.time();
        if (BenchmarkStatement.supportsThreadContention) {
           final long threadId = Thread.currentThread().getId();
           final ThreadInfo threadInfo = BenchmarkStatement.threadMXBean.getThreadInfo(threadId);
           final long threadBlockedTime = threadInfo.getBlockedTime();
           final long previousValue = previousThreadBlockedTime.get().getAndSet(threadBlockedTime);
           results[id].blockTime = threadBlockedTime - previousValue;
       } else {
          results[id].blockTime = 0;
       }
     }
     
     protected Result computeResult()
     {
        List<SingleResult> results = new ArrayList<SingleResult>(totalRounds - warmupRounds);
        for(int i=warmupRounds; i<totalRounds; i++){
           results.add(this.results[i]);
        }
         final Statistics stats = Statistics.from(results);

         return new Result(benchmarkStatement.description, benchmarkRounds, warmupRounds, warmupTime - startTime,
             stopTime - warmupTime, stats.evaluation, stats.blocked, stats.gc, gcSnapshot, 1);
     }
     
     
     class SimpleSingleResult implements SingleResult {

        private final long gcTime;
        private long startTestTime, stopTestTime, blockTime;
        
        SimpleSingleResult(long gcTime) {
           this.gcTime = gcTime;
        }

        @Override
        public long gcTime() {
           return gcTime;
        }

        @Override
        public long evaluationTime() {
           return stopTestTime - startTestTime;
        }

        @Override
        public long blockTime() {
           return blockTime;
        }

     }

 }