package com.carrotsearch.junitbenchmarks;

import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Global settings for benchmarks.
 */
public final class Globals
{
    /**
     * @return Return the default {@link IResultsConsumer}.
     */
    public static IResultsConsumer getDefaultConsumer()
    {
        return new WriterConsumer(
            new OutputStreamWriter(System.out) {
                public void close() throws IOException
                {
                    // Don't close the superstream.
                }
            });
    }
}
