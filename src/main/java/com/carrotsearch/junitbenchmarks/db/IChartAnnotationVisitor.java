package com.carrotsearch.junitbenchmarks.db;

import java.lang.reflect.Method;

import com.carrotsearch.junitbenchmarks.IResultsConsumer;
import com.carrotsearch.junitbenchmarks.Result;

/**
 * 
 */
public interface IChartAnnotationVisitor<C extends IResultsConsumer>
{
    void visit(Class<?> clazz, Method method, Result result);

    void generate(C consumer)
        throws Exception;
}
