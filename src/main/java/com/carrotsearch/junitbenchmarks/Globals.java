package com.carrotsearch.junitbenchmarks;

import java.util.ArrayList;

import com.carrotsearch.junitbenchmarks.h2.H2Consumer;

/**
 * Global settings for benchmarks.
 */
public final class Globals
{
    /**
     * Override property setting the default number of warmup rounds.
     */
    public final static String WARMUP_ROUNDS_PROPERTY = "jub.rounds.warmup";

    /**
     * Override property setting the default number of benchmark rounds.
     */
    public final static String BENCHMARK_ROUNDS_PROPERTY = "jub.rounds.benchmark";

    /**
     * If set to <code>true</code>, the defaults (or property values) take precedence over
     * {@link BenchmarkOptions} annotations.
     */
    public final static String IGNORE_ANNOTATION_OPTIONS_PROPERTY = "jub.ignore.annotations";

    /**
     * Do not call {@link System#gc()} between rounds. Speeds up tests a lot, but renders
     * GC statistics useless.
     */
    public final static String IGNORE_CALLGC_PROPERTY = "jub.ignore.callgc";

    /**
     * If set, an {@link XMLConsumer} is added to the consumers list.
     */
    public final static String XML_FILE_PROPERTY = "jub.xml.file";

    /**
     * If set, an {@link H2Consumer} is added to the consumers list
     * and benchmark results are saved to a database. This property must point to the
     * H2 database file's location (without <code>.h2.db</code> suffix). If the database does
     * not exist, it will be created. 
     */
    public final static String DB_FILE_PROPERTY = "jub.db.file";

    /**
     * Output directory for benchmark charts if {@link H2Consumer} is active 
     * and chart annotations are present. If not present, the current working
     * directory is used.
     */
    public final static String CHARTS_DIR_PROPERTY = "jub.charts.dir";

    /**
     * Custom key to attach to the run.
     */
    public final static String CUSTOMKEY_PROPERTY = "jub.customkey";

    /**
     * Which consumers to instantiate and add to the benchmark results feed? This property
     * takes a comma-separated list of values from {@link ConsumerName}.
     */
    public final static String CONSUMERS_PROPERTY = "jub.consumers";

    /**
     * The default consumer of benchmark results.
     */
    static IResultsConsumer [] consumers;

    /**
     * @return Return the default {@link IResultsConsumer}.
     */
    public synchronized static IResultsConsumer [] getDefaultConsumers()
    {
        if (consumers == null)
        {
            consumers = initializeDefault();
        }

        return consumers;
    }

    /**
     * Initialize the default consumers.
     */
    private static IResultsConsumer [] initializeDefault()
    {
        if (consumers != null)
        {
            throw new RuntimeException("Consumers list already initialized.");
        }

        /* Get the requested consumer list. */
        String [] consumers = 
            System.getProperty(CONSUMERS_PROPERTY, ConsumerName.CONSOLE.toString()).split("\\s*[,]\\s*");

        final ArrayList<IResultsConsumer> result = new ArrayList<IResultsConsumer>();

        for (String consumerName : consumers)
        {
            // For now only allow consumers from the consumer list.
            ConsumerName c = ConsumerName.valueOf(consumerName.toUpperCase());
            try
            {
                result.add(c.clazz.newInstance());
            }
            catch (Throwable e)
            {
                if (e instanceof Error)
                    throw (Error) e;
                if (e instanceof RuntimeException)
                    throw (RuntimeException) e;
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        return result.toArray(new IResultsConsumer [result.size()]);
    }
}
