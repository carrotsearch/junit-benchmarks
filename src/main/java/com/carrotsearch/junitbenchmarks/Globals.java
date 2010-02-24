package com.carrotsearch.junitbenchmarks;

import java.io.*;
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
    public final static String WARMUP_ROUNDS_PROPERTY = "rounds.warmup";

    /**
     * Override property setting the default number of benchmark rounds.
     */
    public final static String BENCHMARK_ROUNDS_PROPERTY = "rounds.benchmark";

    /**
     * If set to <code>true</code>, the defaults (or property values) take precedence over
     * {@link BenchmarkOptions} annotations.
     */
    public final static String IGNORE_ANNOTATION_OPTIONS_PROPERTY = "ignore.annotation.options";

    /**
     * Do not call {@link System#gc()} between rounds. Speeds up tests a lot, but renders
     * GC statistics useless.
     */
    public final static String IGNORE_CALLGC_PROPERTY = "ignore.callgc";

    /**
     * If set, an {@link XMLConsumer} is added to the consumers list.
     */
    public final static String BENCHMARKS_RESULTS_XML_PROPERTY = "benchmarks.xml.file";

    /**
     * If set, an {@link H2Consumer} is added to the consumers list
     * and benchmark results are saved to a database.
     */
    public final static String BENCHMARKS_RESULTS_DB_PROPERTY = "benchmarks.db.file";

    /**
     * Benchmarks output directory if {@link H2Consumer} is active and charts are generated.
     */
    public final static String CHARTS_DIR_PROPERTY = "charts.dir";

    /**
     * Custom key to attach to the run.
     */
    public final static String CUSTOM_KEY_PROPERTY = "benchmarks.key";

    /**
     * The default consumer of benchmark results.
     */
    private static IResultsConsumer [] consumers;

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
        assert consumers == null;

        final ArrayList<IResultsConsumer> result = new ArrayList<IResultsConsumer>();
        
        String path = System.getProperty(BENCHMARKS_RESULTS_DB_PROPERTY);
        String chartsDir = System.getProperty(CHARTS_DIR_PROPERTY, ".");
        String customKey = System.getProperty(CUSTOM_KEY_PROPERTY);
        if (path != null && !path.trim().equals(""))
        {
            result.add(new H2Consumer(new File(path), new File(chartsDir), customKey));
        }

        path = System.getProperty(BENCHMARKS_RESULTS_XML_PROPERTY);
        if (path != null && !path.trim().equals(""))
        {
            result.add(new XMLConsumer(new File(path)));
        }

        result.add(new WriterConsumer(new OutputStreamWriter(System.out)
        {
            public void close() throws IOException
            {
                // Don't close the superstream.
            }
        }));

        return result.toArray(new IResultsConsumer [result.size()]);
    }
}
