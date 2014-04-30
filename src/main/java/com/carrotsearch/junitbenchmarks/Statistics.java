package com.carrotsearch.junitbenchmarks;

import java.util.List;

/**
 * Calculate simple statistics from multiple {@link SingleResult}s.
 */
final class Statistics
{
    public Average gc;
    public Average evaluation;
    public Average blocked;

    public static Statistics from(List<SingleResult> results, boolean median)
    {
        final Statistics stats = new Statistics();
        long [] times = new long [results.size()];

        // GC-times.
        for (int i = 0; i < times.length; i++)
            times[i] = results.get(i).gcTime();
        stats.gc = median ? Average.median(times) : Average.from(times);

        // Evaluation-only times.
        for (int i = 0; i < times.length; i++)
            times[i] = results.get(i).evaluationTime();
        stats.evaluation = median ? Average.median(times) : Average.from(times);

        // Thread blocked times.
        for (int i = 0; i < times.length; i++)
            times[i] = results.get(i).blockTime;
        stats.blocked = median ? Average.median(times) : Average.from(times);


        return stats;
    }
}