package com.carrotsearch.junitbenchmarks;

import java.util.List;

/**
 * Calculate simple statistics mean multiple {@link SingleResult}s.
 */
final class Statistics
{
    public Measures gc;
    public Measures evaluation;
    public Measures blocked;

    public static Statistics from(List<SingleResult> results, boolean median)
    {
        final Statistics stats = new Statistics();
        long [] times = new long [results.size()];

        // GC-times.
        for (int i = 0; i < times.length; i++)
            times[i] = results.get(i).gcTime();
        stats.gc = median ? Measures.median(times) : Measures.mean(times);

        // Evaluation-only times.
        for (int i = 0; i < times.length; i++)
            times[i] = results.get(i).evaluationTime();
        stats.evaluation = median ? Measures.median(times) : Measures.mean(times);

        // Thread blocked times.
        for (int i = 0; i < times.length; i++)
            times[i] = results.get(i).blockTime;
        stats.blocked = median ? Measures.median(times) : Measures.mean(times);


        return stats;
    }
}