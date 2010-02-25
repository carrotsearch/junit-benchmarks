package com.carrotsearch.junitbenchmarks.h2;

import java.lang.reflect.Method;
import java.util.HashSet;

import com.carrotsearch.junitbenchmarks.Result;

/**
 * Collector of {@link BenchmarkMethodChart} annotations.
 */
class MethodChartVisitor implements IChartAnnotationVisitor
{
    /**
     * Types for which method-level chart should be generated.
     */
    private HashSet<Class<?>> types = new HashSet<Class<?>>();

    /*
     * 
     */
    public void generate(H2Consumer c) throws Exception
    {
        for (Class<?> clazz : types)
        {
            new MethodChartGenerator(
                c.getConnection(), 
                c.chartsDir,
                getFilePrefix(clazz),
                c.runId, 
                clazz.getName()).generate();
        }
    }

    /*
     * 
     */
    public void visit(Class<?> clazz, Method method, Result result)
    {
        if (clazz.isAnnotationPresent(BenchmarkMethodChart.class)
            && !types.contains(clazz))
        {
            types.add(clazz);
        }
    }
    
    /*
     * 
     */
    private String getFilePrefix(Class<?> clazz)
    {
        BenchmarkMethodChart ann = clazz.getAnnotation(BenchmarkMethodChart.class);
        String filePrefix = ann.filePrefix();
        if (filePrefix.length() == 0)
        {
            filePrefix = clazz.getName();
        }
        return filePrefix;
    }
}
