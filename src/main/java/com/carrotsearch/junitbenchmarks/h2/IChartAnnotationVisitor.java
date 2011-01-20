package com.carrotsearch.junitbenchmarks.h2;

import java.lang.reflect.Method;

import com.carrotsearch.junitbenchmarks.Result;

/**
 * 
 */
interface IChartAnnotationVisitor
{
    void visit(Class<?> clazz, Method method, Result result);

    void generate(H2Consumer consumer)
        throws Exception;
}
