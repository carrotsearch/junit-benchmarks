package com.carrotsearch.junitbenchmarks.h2;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface AxisRange
{
    double min() default Double.NaN;
    double max() default Double.NaN;
}