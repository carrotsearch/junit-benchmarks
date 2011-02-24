package com.carrotsearch.junitbenchmarks;

import java.io.*;
import java.util.Locale;

/**
 * {@link IResultsConsumer} printing benchmark results to a given writer. 
 */
public final class WriterConsumer implements IResultsConsumer
{
    private final Writer w;

    public WriterConsumer()
    {
        this(getDefaultWriter());
    }

    public WriterConsumer(Writer w)
    {
        this.w = w;
    }

    public void accept(Result result) throws IOException
    {
        w.write(String.format(Locale.ENGLISH,
            "%s: [measured %d out of %d rounds, %s]\n" +
            " round: %s, round.gc: %s, GC.calls: %d, GC.time: %.2f," +
            " time.total: %.2f, time.warmup: %.2f, time.bench: %.2f\n",
            result.getShortTestClassName() + "." + result.getTestMethodName(),
            result.benchmarkRounds, 
            result.benchmarkRounds + result.warmupRounds, 
            concurrencyToText(result.options),
            result.roundAverage.toString(), 
            result.gcAverage.toString(), 
            result.gcInfo.accumulatedInvocations(), 
            result.gcInfo.accumulatedTime() / 1000.0,
            (result.warmupTime + result.benchmarkTime) * 0.001,
            result.warmupTime * 0.001, 
            result.benchmarkTime * 0.001
        ));
        w.flush();
    }

    private String concurrencyToText(BenchmarkOptions options)
    {
        switch (options.concurrency())
        {
            case BenchmarkOptions.CONCURRENCY_AVAILABLE_CORES:
                return "threads: " + Runtime.getRuntime().availableProcessors() +
                    " (all physical processors)";

            case BenchmarkOptions.CONCURRENCY_SEQUENTIAL:
                return "threads: 1 (sequential)";

            default:
                return "threads: " + options.concurrency()
                    + " (physical processors: " + Runtime.getRuntime().availableProcessors() + ")";
        }
    }

    /**
     * Return the default writer (console).
     */
    private static Writer getDefaultWriter()
    {
        return new OutputStreamWriter(System.out)
        {
            public void close() throws IOException
            {
                // Don't close the superstream.
            }
        };
    }
}
