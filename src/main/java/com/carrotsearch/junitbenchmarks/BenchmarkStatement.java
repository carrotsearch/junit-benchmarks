package com.carrotsearch.junitbenchmarks;

import static com.carrotsearch.junitbenchmarks.BenchmarkOptionsSystemProperties.BENCHMARK_ROUNDS_PROPERTY;
import static com.carrotsearch.junitbenchmarks.BenchmarkOptionsSystemProperties.CONCURRENCY_PROPERTY;
import static com.carrotsearch.junitbenchmarks.BenchmarkOptionsSystemProperties.IGNORE_ANNOTATION_OPTIONS_PROPERTY;
import static com.carrotsearch.junitbenchmarks.BenchmarkOptionsSystemProperties.IGNORE_CALLGC_PROPERTY;
import static com.carrotsearch.junitbenchmarks.BenchmarkOptionsSystemProperties.WARMUP_ROUNDS_PROPERTY;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Benchmark evaluator statement.
 */
final class BenchmarkStatement extends Statement
{
    static class LongHolder {
        public long value;
        
        public long getAndSet(long newValue) {
            long tmp = value;
            value = newValue;
            return tmp;
        }
    }

    final static ThreadMXBean threadMXBean;
    final static boolean supportsThreadContention;
    static {
        threadMXBean = ManagementFactory.getThreadMXBean();
        supportsThreadContention = threadMXBean.isThreadContentionMonitoringSupported();
        if (supportsThreadContention) {
            threadMXBean.setThreadContentionMonitoringEnabled(true);
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

    final Description description;
    private final BenchmarkOptions options;
    private final IResultsConsumer [] consumers;
    private final Statement base;


    /* */
    public BenchmarkStatement(Statement base, Description description,
            IResultsConsumer[] consumers) {
        this.base = base;
        this.description = description;
        this.consumers = consumers;

        this.options = resolveOptions(description);
    }

    /* Provide the default options from the annotation. */
    @BenchmarkOptions
    private void defaultOptions()
    {
    }

    /* */
    private BenchmarkOptions resolveOptions(Description description) {
        // Method-level
        BenchmarkOptions options = description.getAnnotation(BenchmarkOptions.class);
        if (options != null) return options;
        
        // Class-level
        Class<?> clz = description.getTestClass();
        while (clz != null)
        {
            options = clz.getAnnotation(BenchmarkOptions.class);
            if (options != null) return options;

            clz = clz.getSuperclass();
        }

        // Defaults.
        try
        {
            return getClass()
                .getDeclaredMethod("defaultOptions")
                .getAnnotation(BenchmarkOptions.class);
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

        final BenchmarkEvaluator evaluator;
        final BenchmarkInvokeMethod test = TestRepository.getTest(new TestId(description.getTestClass(), description.getMethodName()));
        if (concurrency == BenchmarkOptions.CONCURRENCY_SEQUENTIAL)
        {
           if(test == null){
              evaluator = new LegacySequentialEvaluator(this, warmupRounds, benchmarkRounds, totalRounds, options.clock());
           }
           else{
              final SequentialEvaluator sequentialEvaluator = new SequentialEvaluator(this, warmupRounds, benchmarkRounds, totalRounds, options.clock());
              test.setTimeRecorder(sequentialEvaluator);
              evaluator = sequentialEvaluator;
           }  
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

            if(test == null){
               evaluator = new LegacyConcurrentEvaluator(this, warmupRounds, benchmarkRounds, totalRounds, threads, options.clock());
            }
            else{
               final ConcurrentEvaluator concurrentEvaluator = new ConcurrentEvaluator(this, warmupRounds, benchmarkRounds, totalRounds, threads, options.clock());
               test.setTimeRecorder(concurrentEvaluator);
               evaluator = concurrentEvaluator;
            }
            
        }

        final Result result = evaluator.evaluate();

        for (IResultsConsumer consumer : consumers)
            consumer.accept(result);
    }

    /**
     * Best effort attempt to clean up the memory if {@link BenchmarkOptions#callgc()} is
     * enabled.
     */
    void cleanupMemory()
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
    
    /**
     * execute the underlying test
     */
    void executeTest() throws Throwable{
       this.base.evaluate();
    }
    
}