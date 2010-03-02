package com.carrotsearch.junitbenchmarks;

import org.junit.Rule;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * A benchmark rule (causes tests to be repeated and measured). Benchmark rule should be
 * placed in the test class as a field, with annotation. Example:
 * 
 * <pre>
 * {@link Rule}
 * public {@link MethodRule} runBenchmarks = new BenchmarkRule();
 * </pre>
 */
public final class BenchmarkRule implements MethodRule
{
    private final IResultsConsumer [] consumers;

    /**
     * Creates a benchmark rule with the default sink for benchmark results (the default
     * sink is taken from global properties).
     */
    public BenchmarkRule()
    {
        this(Globals.getDefaultConsumers());
    }

    /**
     * Creates a benchmark rule with a given sink for benchmark results.
     */
    public BenchmarkRule(IResultsConsumer... consumers)
    {
        if (consumers == null || consumers.length == 0)
            throw new IllegalArgumentException("There needs to be at least one consumer.");

        this.consumers = consumers;
    }

    /**
     * Apply benchmarking to the given method and target.
     */
    public Statement apply(Statement base, FrameworkMethod method, Object target)
    {
        return new BenchmarkStatement(base, method, target, consumers);
    }
}