package com.carrotsearch.junitbenchmarks.h2;

import java.lang.reflect.Method;
import java.util.HashSet;

import com.carrotsearch.junitbenchmarks.Result;
import com.carrotsearch.junitbenchmarks.annotation.AxisRange;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;

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
            MethodChartGenerator g = new MethodChartGenerator(
                c.getConnection(), 
                GeneratorUtils.getFilePrefix(
                    clazz, 
                    clazz.getAnnotation(BenchmarkMethodChart.class).filePrefix(),
                    c.chartsDir),
                c.runId, 
                clazz.getName());

            AxisRange ann = clazz.getAnnotation(AxisRange.class);
            if (ann != null)
            {
                g.min = ann.min();
                g.max = ann.max();
            }

            g.generate();
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
}
