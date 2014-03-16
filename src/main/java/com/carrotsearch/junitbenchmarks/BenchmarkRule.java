package com.carrotsearch.junitbenchmarks;

import java.lang.reflect.Field;

import org.junit.Rule;
import org.junit.internal.runners.statements.ExpectException;
import org.junit.internal.runners.statements.FailOnTimeout;
import org.junit.internal.runners.statements.RunAfters;
import org.junit.internal.runners.statements.RunBefores;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * A benchmark rule (causes tests to be repeated and measured). Benchmark rule should be
 * placed in the test class as a field, with annotation. Example:
 * 
 * <pre>
 * {@link Rule}
 * public {@link TestRule} runBenchmarks = new BenchmarkRule();
 * </pre>
 */
public final class BenchmarkRule implements TestRule
{
    private final IResultsConsumer [] consumers;

    /**
     * Creates a benchmark rule with the default sink for benchmark results (the default
     * sink is taken from global properties).
     */
    public BenchmarkRule()
    {
        this(BenchmarkOptionsSystemProperties.getDefaultConsumers());
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
     * Apply benchmarking to the given test description.
     */
    @Override
    public Statement apply(Statement base, Description description) {
        try {
            if (workAroundWrappedStatements(base, description)) {
                return base;
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new BenchmarkStatement(base, description, consumers);
    }

    /**
     * Works around a JUnit limitation regarding &#x40;Before and &#x40;After.
     * <p>
     * When JUnit hands a {@link Statement} to the {@link BenchmarkRule}, it might
     * not be the original statement, but rather a statement that is wrapped in
     * statements ensuring that the methods annotated with &#x40;Before and/or
     * &#x40;After are run. This method works around that by unwrapping the
     * original statement, via reflection.
     * </p>
     *
     * @param statement the statement to benchmark, possibly wrapped
     * @param description the description
     * @return whether a workaround was necessary
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    private boolean workAroundWrappedStatements(Statement statement,
            Description description) throws SecurityException, NoSuchFieldException,
                IllegalArgumentException, IllegalAccessException
    {
        Field field = null;
        Object object = null;
        if (statement instanceof RunAfters) {
            object = statement;
            field = object.getClass().getDeclaredField("fNext");
            field.setAccessible(true);
            statement = (Statement) field.get(object);
        }
        if (statement instanceof RunBefores) {
            object = statement;
            field = object.getClass().getDeclaredField("fNext");
            field.setAccessible(true);
            statement = (Statement) field.get(object);
        }
        if (statement instanceof FailOnTimeout) {
            object = statement;
            field = object.getClass().getDeclaredField("fOriginalStatement");
            field.setAccessible(true);
            statement = (Statement) field.get(object);
        }
        if (statement instanceof ExpectException) {
            object = statement;
            field = object.getClass().getDeclaredField("fNext");
            field.setAccessible(true);
            statement = (Statement) field.get(object);
        }

        if (field == null) return false;
        field.set(object, new BenchmarkStatement(statement, description, consumers));
        return true;
    }
}
