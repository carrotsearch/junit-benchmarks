package com.carrotsearch.junitbenchmarks;

import java.util.ArrayList;

import static com.carrotsearch.junitbenchmarks.Globals.*;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * Benchmark evaluator statement.
 */
final class BenchmarkStatement extends Statement
{
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
    private final Statement base;
    private final BenchmarkOptions options;
    private final IResultsConsumer consumer;

    /* */
    public BenchmarkStatement(Statement base, FrameworkMethod method, Object target, 
        IResultsConsumer consumer)
    {
        this.base = base;
        this.method = method;
        this.target = target;
        this.consumer = consumer;

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

        final int totalRounds = warmupRounds + benchmarkRounds;

        final ArrayList<SingleResult> results = new ArrayList<SingleResult>(totalRounds);

        GCSnapshot gcSnapshot = null;
        long warmupTime = System.currentTimeMillis();
        long benchmarkTime = 0;
        for (int i = 0; i < totalRounds; i++)
        {
            // We assume no reordering will take place here.
            final long startTime = System.currentTimeMillis();
            cleanupMemory();
            final long afterGC = System.currentTimeMillis();

            if (i == warmupRounds)
            {
                gcSnapshot = new GCSnapshot();
                benchmarkTime = System.currentTimeMillis();
                warmupTime = benchmarkTime - warmupTime;
            }

            base.evaluate();
            final long endTime = System.currentTimeMillis();

            results.add(new SingleResult(startTime, afterGC, endTime));
        }
        benchmarkTime = System.currentTimeMillis() - benchmarkTime;

        final Statistics stats = Statistics.from(
            results.subList(warmupRounds, totalRounds));

        final Result result = new Result(
            target, method,
            benchmarkRounds,
            warmupRounds,
            warmupTime,
            benchmarkTime,
            stats.evaluation,
            stats.gc,
            gcSnapshot
        );

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