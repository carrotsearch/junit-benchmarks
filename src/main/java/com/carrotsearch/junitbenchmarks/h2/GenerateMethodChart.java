package com.carrotsearch.junitbenchmarks.h2;

import java.lang.annotation.*;

/**
 * Generate a graphical summary for all benchmarked methods of the annotated class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface GenerateMethodChart
{
}