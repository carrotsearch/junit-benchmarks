package com.carrotsearch.junitbenchmarks;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * A benchmark rule (causes tests to be repeated and measured). Benchmark rule should be
 * placed in the test class as a field, with annotation. Example:
 * 
 * <pre>
 * @{@link Rule}
 * public {@link MethodRule} runBenchmarks = new BenchmarkRule();
 * </pre>
 */
public final class BenchmarkRule implements MethodRule
{
    private final IResultsConsumer consumer;

    /**
     * Creates a benchmark rule with the default sink for benchmark results (the default
     * sink is taken from global properties).
     */
    public BenchmarkRule()
    {
        this(Globals.getDefaultConsumer());
    }

    /**
     * Creates a benchmark rule with a given sink for benchmark results.
     */
    public BenchmarkRule(IResultsConsumer consumer)
    {
        if (consumer == null)
            throw new IllegalArgumentException("Consumer must not be null.");

        this.consumer = consumer;
    }

    /**
     * Apply benchmarking to the given method and target.
     */
    public Statement apply(Statement base, FrameworkMethod method, Object target)
    {
        return new BenchmarkStatement(base, method, target, consumer);
    }
}