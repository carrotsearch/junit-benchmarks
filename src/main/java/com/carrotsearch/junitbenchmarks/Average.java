package com.carrotsearch.junitbenchmarks;

import java.util.Arrays;
import java.util.Locale;

/**
 * Average with standard deviation.
 */
public final class Average
{
    /**
     * Average (in milliseconds).
     */
    public final double avg;

    /**
     * Standard deviation (in milliseconds).
     */
    public final double stddev;

    /**
     * 
     */
    Average(double avg, double stddev)
    {
        this.avg = avg;
        this.stddev = stddev;
    }

    public String toString()
    {
        return String.format(Locale.ENGLISH, "%.2f [+- %.2f]", 
            avg, stddev);
    }

    static Average from(long [] values)
    {
        long sum = 0;
        long sumSquares = 0;

        for (long l : values)
        {
            sum += l;
            sumSquares += l * l;
        }

        double avg = sum / (double) values.length;
        return new Average(
            (sum / (double) values.length) / 1000.0, 
            Math.sqrt(sumSquares / (double) values.length - avg * avg) / 1000.0);
    }

    static Average median(long [] values)
    {
        Arrays.sort(values);
        long median = middle(values);
        
        for (int i = 0; i < values.length; i++) {
            values[i] = Math.abs(values[i] - median);
        }
        
        Arrays.sort(values);
        long mad = middle(values);
        
        return new Average(median / 1000.0, mad / 1000.0);
    }
    
    private static long middle(long [] values)
    {
        if (values.length % 2 == 1) {
            return values[values.length / 2];
        }
        
        return ( values[values.length / 2]
               + values[values.length / 2 - 1] ) / 2;
    }
}