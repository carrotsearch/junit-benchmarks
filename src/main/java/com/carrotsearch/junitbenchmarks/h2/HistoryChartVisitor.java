package com.carrotsearch.junitbenchmarks.h2;

import java.lang.reflect.Method;
import java.util.*;

import com.carrotsearch.junitbenchmarks.Result;

class HistoryChartVisitor implements IChartAnnotationVisitor
{
    /**
     * Cache of all types and methods for which 
     * {@link HistoryChartGenerator} should be invoked.
     */
    private HashMap<Class<?>, List<Method>> types = new HashMap<Class<?>, List<Method>>();

    /*
     * 
     */
    public void visit(Class<?> clazz, Method method, Result result)
    {
        boolean onMethod = method.isAnnotationPresent(BenchmarkHistoryChart.class);
        boolean onClass = clazz.isAnnotationPresent(BenchmarkHistoryChart.class);

        if (onMethod || onClass)
        {
            if (!types.containsKey(clazz))
            {
                types.put(clazz, new ArrayList<Method>());
            }
         
            if (onMethod)
            {
                types.get(clazz).add(method);
            }
        }
    }

    /*
     * 
     */
    public void generate(H2Consumer c) throws Exception
    {
        for (Map.Entry<Class<?>, List<Method>> e : types.entrySet())
        {
            final Class<?> clazz = e.getKey();
            final List<Method> methods = e.getValue();

            /*
             * Check if the annotation is present on the class. If so, generate a chart
             * for all methods of this class. 
             */
            final BenchmarkHistoryChart ann = clazz.getAnnotation(BenchmarkHistoryChart.class);
            if (ann != null)
            {
                HistoryChartGenerator gen = new HistoryChartGenerator(
                    c.getConnection(), c.chartsDir, getFilePrefix(ann, clazz), clazz.getName());
                gen.updateMax(ann.maxRuns());
                gen.generate();
            }
            
            /*
             * Now check per-method annotations. Partition by file prefix first.
             */
            HashMap<String, List<Method>> byPrefix = new HashMap<String, List<Method>>();
            for (Method m : methods)
            {
                BenchmarkHistoryChart methodAnn = m.getAnnotation(BenchmarkHistoryChart.class);
                String prefix = getFilePrefix(methodAnn, clazz);
                if (!byPrefix.containsKey(prefix))
                {
                    byPrefix.put(prefix, new ArrayList<Method>());
                }
                byPrefix.get(prefix).add(m);
            }
            
            /*
             * Remove method-level entries which are already covered by class-level annotation.
             */
            if (ann != null)
            {
                byPrefix.remove(getFilePrefix(ann, clazz));
            }
            
            for (Map.Entry<String, List<Method>> e2 : byPrefix.entrySet())
            {
                HistoryChartGenerator gen = new HistoryChartGenerator(
                    c.getConnection(), c.chartsDir, e2.getKey(), clazz.getName());
                
                for (Method m : e2.getValue())
                {
                    gen.updateMax(m.getAnnotation(BenchmarkHistoryChart.class).maxRuns());
                    gen.includeMethod(m.getName());
                }
                gen.generate();
            }
        }
    }

    /*
     * 
     */
    private String getFilePrefix(BenchmarkHistoryChart ann, Class<?> clazz)
    {
        if (ann.filePrefix().length() > 0)
            return ann.filePrefix();

        return clazz.getName();
    }
}
