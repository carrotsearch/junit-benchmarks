package com.carrotsearch.junitbenchmarks;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
  * Performs test method evaluation concurrently. The basic idea is to obtain a
  * {@link ThreadPoolExecutor} instance (either new one on each evaluation as it is
  * implemented now or a shared one to avoid excessive thread allocation), wrap it into
  * a <tt>CompletionService&lt;SingleResult&gt;</tt>, pause its execution until the
  * associated task queue is filled with <tt>totalRounds</tt> number of
  * <tt>EvaluatorCallable&lt;SingleResult&gt;</tt>.
  */
 final class ConcurrentEvaluator extends BaseEvaluator
 {

   private final class EvaluatorCallable implements Callable<Void>
     {
         // Sequence number in order to keep track of warmup / benchmark phase
         private final int i;

         public EvaluatorCallable(int i)
         {
             this.i = i;
         }

         @Override
         public Void call() throws Exception
         {
             latch.await();
             try {
                evaluateInternally(i);
                 return null;
             } catch (Exception e) {
                 throw e;
             } catch (Throwable t) {
                 throw new InvocationTargetException(t);
             }
         }
     }

     private final int concurrency;
     private final CountDownLatch latch;


     ConcurrentEvaluator(BenchmarkStatement benchmarkStatement, int warmupRounds, int benchmarkRounds, int totalRounds,
                         int concurrency, Clock clock)
     {
         super(benchmarkStatement, warmupRounds, benchmarkRounds, totalRounds, clock);
         this.concurrency = concurrency;
         this.latch = new CountDownLatch(1);
     }

     /**
      * Perform ThreadPoolExecution initialization. Returns new preconfigured
      * threadPoolExecutor for particular concurrency level and totalRounds to be
      * executed Candidate for further development to mitigate the problem of excessive
      * thread pool creation/destruction.
      *
      * @param concurrency
      * @param totalRounds
      */
     private final ExecutorService getExecutor(int concurrency, int totalRounds)
     {
         return new ThreadPoolExecutor(concurrency, concurrency, 10000,
                 TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(totalRounds));
     }

     /**
      * Perform proper ThreadPool cleanup.
      */
     private final void cleanupExecutor(ExecutorService executor)
     {
         @SuppressWarnings("unused")
         List<Runnable> pending = executor.shutdownNow();
         // Can pending.size() be > 0?
     }

     @Override
     public Result evaluate() throws Throwable
     {
         // Obtain ThreadPoolExecutor (new instance on each test method for now)
         ExecutorService executor = getExecutor(concurrency, totalRounds);
         CompletionService<Void> completed = new ExecutorCompletionService<Void>(
             executor);

         for (int i = 0; i < totalRounds; i++)
         {
             completed.submit(new EvaluatorCallable(i));
         }

         // Allow all the evaluators to proceed to the warmup phase.
         latch.countDown();
         startTest();
         
         try
         {
             for (int i = 0; i < totalRounds; i++)
             {          
                 completed.take().get();
             }
             
             stopTest();
             return computeResult();
         }
         catch (ExecutionException e)
         {
             // Unwrap the Throwable thrown by the tested method.
             e.printStackTrace();
             throw e.getCause().getCause();
         }
         finally
         {
             // Assure proper executor cleanup either on test failure or an successful completion
             cleanupExecutor(executor);
         }
     }

     @Override
     protected Result computeResult()
     {
         Result r = super.computeResult();
         r.concurrency = this.concurrency;
         return r;
     }
 }