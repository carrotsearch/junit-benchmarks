package com.carrotsearch.junitbenchmarks;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;

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
     * If set, the default {@link IResultsConsumer} is set to {@link XMLConsumer} and
     * benchmark results are saved to a path given in this property.
     */
    public final static String BENCHMARKS_RESULTS_XML_PROPERTY = "benchmarks.xml.file";

    /**
     * The default consumer of benchmark results.
     */
    private static IResultsConsumer consumer;

    /**
     * @return Return the default {@link IResultsConsumer}.
     */
    public synchronized static IResultsConsumer getDefaultConsumer()
    {
        if (consumer == null)
        {
            consumer = initializeDefault();
        }
        return consumer;
    }

    /**
     * Initialize the default consumer.
     */
    private static IResultsConsumer initializeDefault()
    {
        assert consumer == null;

        String path = System.getProperty(BENCHMARKS_RESULTS_XML_PROPERTY);
        if (path != null && !path.trim().equals(""))
        {
            return new XMLConsumer(new File(path));
        }

        return new WriterConsumer(new OutputStreamWriter(System.out)
        {
            public void close() throws IOException
            {
                // Don't close the superstream.
            }
        });
    }
}
