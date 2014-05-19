package com.carrotsearch.junitbenchmarks;

import java.util.Arrays;
import java.util.Locale;

/**
 * Captures statistical measures of sampled data.
 */
public final class Measures
{
    /**
     * Central tendency of the sample in milliseconds (mean, median, ...).
     */
    public final double location;

    /**
     * Measure of the dispersion in milliseconds
     * (standard deviation, median absolute deviation).
     */
    public final double dispersion;

    /**
     * Store statistical information about a sample of data.
     * 
     * @param location Central tendency of the sample.
     * @param dispersion Measure of the dispersion.
     */
    Measures(double location, double dispersion)
    {
        this.location = location;
        this.dispersion = dispersion;
    }

    @Override
    public String toString()
    {
        return String.format(Locale.ENGLISH, "%.2f [+- %.2f]", 
            location, dispersion);
    }

    /**
     * Mean value and standard deviation of a sample of values.
     */
    static Measures mean(long [] values)
    {
        long sum = 0;
        long sumSquares = 0;

        for (long l : values)
        {
            sum += l;
            sumSquares += l * l;
        }

        double avg = sum / (double) values.length;
        return new Measures(
            (sum / (double) values.length) / 1000.0, 
            Math.sqrt(sumSquares / (double) values.length - avg * avg) / 1000.0);
    }

    /**
     * Median and median absolute deviations of a sample.
     */
    static Measures median(long [] values)
    {
        Arrays.sort(values);
        long median = middle(values);
        
        for (int i = 0; i < values.length; i++) {
            values[i] = Math.abs(values[i] - median);
        }
        
        Arrays.sort(values);
        long mad = middle(values);
        
        return new Measures(median / 1000.0, mad / 1000.0);
    }
    
    /**
     * Picks the middle value mean an array.
     */
    private static long middle(long [] values)
    {
        if (values.length % 2 == 1) {
            return values[values.length / 2];
        }
        
        return ( values[values.length / 2]
               + values[values.length / 2 - 1] ) / 2;
    }
}