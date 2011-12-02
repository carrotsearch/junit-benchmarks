package com.carrotsearch.junitbenchmarks;

import static com.carrotsearch.junitbenchmarks.BenchmarkOptionsSystemProperties.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * Benchmark evaluator statement.
 */
final class BenchmarkStatement extends Statement
{
    /**
     * Factored out as a nested class as it needs to keep some data during test
     * evaluation.
     */
    private abstract class BaseEvaluator
    {
        final protected ArrayList<SingleResult> results;

        final protected int warmupRounds;
        final protected int benchmarkRounds;
        final protected int totalRounds;

        protected long warmupTime;
        protected long benchmarkTime;

        protected BaseEvaluator(int warmupRounds, int benchmarkRounds, int totalRounds)
        {
            super();
            this.warmupRounds = warmupRounds;
            this.benchmarkRounds = benchmarkRounds;
            this.totalRounds = totalRounds;
            this.results = new ArrayList<SingleResult>(totalRounds);
        }

        protected GCSnapshot gcSnapshot = null;

        protected abstract Result evaluate() throws Throwable;

        protected final SingleResult evaluateInternally(int round) throws InvocationTargetException
        {
            // We assume no reordering will take place here.
            final long startTime = System.currentTimeMillis();
            cleanupMemory();
            final long afterGC = System.currentTimeMillis();

            if (round == warmupRounds)
            {
                gcSnapshot = new GCSnapshot();
                benchmarkTime = System.currentTimeMillis();
                warmupTime = benchmarkTime - warmupTime;
            }

            try
            {
                base.evaluate();
                final long endTime = System.currentTimeMillis();
                return new SingleResult(startTime, afterGC, endTime);
            }
            catch (Throwable t)
            {
                throw new InvocationTargetException(t);
            }
        }

        protected Result computeResult()
        {
            final Statistics stats = Statistics.from(
                results.subList(warmupRounds, totalRounds));

            return new Result(target, method, benchmarkRounds, warmupRounds, warmupTime,
                benchmarkTime, stats.evaluation, stats.gc, gcSnapshot, 1);
        }
    }

    /**
     * Performs test method evaluation sequentially.
     */
    private final class SequentialEvaluator extends BaseEvaluator
    {
        SequentialEvaluator(int warmupRounds, int benchmarkRounds, int totalRounds)
        {
            super(warmupRounds, benchmarkRounds, totalRounds);
        }

        @Override
        public Result evaluate() throws Throwable
        {
            warmupTime = System.currentTimeMillis();
            benchmarkTime = 0;
            for (int i = 0; i < totalRounds; i++)
            {
                results.add(evaluateInternally(i));
            }
            benchmarkTime = System.currentTimeMillis() - benchmarkTime;

            return computeResult();
        }
    }

    /**
     * Performs test method evaluation concurrently. The basic idea is to obtain a
     * {@link ThreadPoolExecutor} instance (either new one on each evaluation as it is
     * implemented now or a shared one to avoid excessive thread allocation), wrap it into
     * a <tt>CompletionService&lt;SingleResult&gt;</tt>, pause its execution until the
     * associated task queue is filled with <tt>totalRounds</tt> number of
     * <tt>EvaluatorCallable&lt;SingleResult&gt;</tt>.
     */
    private final class ConcurrentEvaluator extends BaseEvaluator
    {
        private final class EvaluatorCallable implements Callable<SingleResult>
        {
            // Sequence number in order to keep track of warmup / benchmark phase
            private final int i;

            public EvaluatorCallable(int i)
            {
                this.i = i;
            }

            @Override
            public SingleResult call() throws Exception
            {
                latch.await();
                return evaluateInternally(i);
            }
        }

        private final int concurrency;
        private final CountDownLatch latch;

        ConcurrentEvaluator(int warmupRounds, int benchmarkRounds, int totalRounds,
            int concurrency)
        {
            super(warmupRounds, benchmarkRounds, totalRounds);

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
            CompletionService<SingleResult> completed = new ExecutorCompletionService<SingleResult>(
                executor);

            for (int i = 0; i < totalRounds; i++)
            {
                completed.submit(new EvaluatorCallable(i));
            }

            // Allow all the evaluators to proceed to the warmup phase.
            latch.countDown();

            warmupTime = System.currentTimeMillis();
            benchmarkTime = 0;
            try
            {
                for (int i = 0; i < totalRounds; i++)
                {
                    results.add(completed.take().get());
                }

                benchmarkTime = System.currentTimeMillis() - benchmarkTime;
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

    /**
     * How many warmup runs should we execute for each test method?
     */
    final static int DEFAULT_WARMUP_ROUNDS = 5;

    /**
     * How many actual benchmark runs should we execute for each test method?
     */
    final static int DEFAULT_BENCHMARK_ROUNDS = 10;

    /**
     * If <code>true</code>, the local overrides using {@link BenchmarkOptions} are
     * ignored and defaults (or globals passed via system properties) are used.
     */
    private boolean ignoreAnnotationOptions = Boolean
        .getBoolean(IGNORE_ANNOTATION_OPTIONS_PROPERTY);

    /**
     * Disable all forced garbage collector calls.
     */
    private boolean ignoreCallGC = Boolean.getBoolean(IGNORE_CALLGC_PROPERTY);

    private final Object target;
    private final FrameworkMethod method;
    private final BenchmarkOptions options;
    private final IResultsConsumer [] consumers;

    private final Statement base;

    /* */
    public BenchmarkStatement(Statement base, FrameworkMethod method, Object target,
        IResultsConsumer... consumers)
    {
        this.base = base;
        this.method = method;
        this.target = target;
        this.consumers = consumers;

        this.options = resolveOptions(method);
    }

    /* Provide the default options from the annotation. */
    @BenchmarkOptions
    @SuppressWarnings("unused")
    private void defaultOptions()
    {
    }

    /* */
    private BenchmarkOptions resolveOptions(FrameworkMethod method)
    {
        // Method-level override.
        BenchmarkOptions options = method.getAnnotation(BenchmarkOptions.class);
        if (options != null) return options;

        // Class-level override. Look for annotations in this and superclasses.
        Class<?> clz = target.getClass();
        while (clz != null)
        {
            options = clz.getAnnotation(BenchmarkOptions.class);
            if (options != null) return options;

            clz = clz.getSuperclass();
        }

        // Defaults.
        try
        {
            return getClass().getDeclaredMethod("defaultOptions").getAnnotation(
                BenchmarkOptions.class);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /* */
    @Override
    public void evaluate() throws Throwable
    {
        final int warmupRounds = getIntOption(options.warmupRounds(),
            WARMUP_ROUNDS_PROPERTY, DEFAULT_WARMUP_ROUNDS);

        final int benchmarkRounds = getIntOption(options.benchmarkRounds(),
            BENCHMARK_ROUNDS_PROPERTY, DEFAULT_BENCHMARK_ROUNDS);

        final int concurrency = getIntOption(options.concurrency(), CONCURRENCY_PROPERTY,
            BenchmarkOptions.CONCURRENCY_SEQUENTIAL);

        final int totalRounds = warmupRounds + benchmarkRounds;

        final BaseEvaluator evaluator; 
        if (concurrency == BenchmarkOptions.CONCURRENCY_SEQUENTIAL)
        {
            evaluator = new SequentialEvaluator(warmupRounds, benchmarkRounds, totalRounds); 
        }
        else
        {
            /*
             * Just don't allow call GC during concurrent execution.
             */
            if (options.callgc())
                throw new IllegalArgumentException("Concurrent benchmark execution must be"
                    + " combined ignoregc=\"true\".");

            int threads = (concurrency == BenchmarkOptions.CONCURRENCY_AVAILABLE_CORES 
                    ? Runtime.getRuntime().availableProcessors() 
                    : concurrency);
            
            evaluator = new ConcurrentEvaluator(
                warmupRounds, benchmarkRounds, totalRounds, threads);
        }

        final Result result = evaluator.evaluate();

        for (IResultsConsumer consumer : consumers)
            consumer.accept(result);
    }

    /**
     * Best effort attempt to clean up the memory if {@link BenchmarkOptions#callgc()} is
     * enabled.
     */
    private void cleanupMemory()
    {
        if (ignoreCallGC) return;
        if (!options.callgc()) return;

        /*
         * Best-effort GC invocation. I really don't know of any other way to ensure a GC
         * pass.
         */
        System.gc();
        System.gc();
        Thread.yield();
    }

    /**
     * Get an integer override from system properties.
     */
    private int getIntOption(int localValue, String property, int defaultValue)
    {
        final String v = System.getProperty(property);
        if (v != null && v.trim().length() > 0)
        {
            defaultValue = Integer.parseInt(v);
        }

        if (ignoreAnnotationOptions || localValue < 0)
        {
            return defaultValue;
        }

        return localValue;
    }
}