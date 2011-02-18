package com.carrotsearch.junitbenchmarks.examples;

import java.io.File;
import java.util.*;

import org.junit.*;
import org.junit.rules.MethodRule;

import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.annotation.*;
import com.carrotsearch.junitbenchmarks.h2.*;

/**
 *
 */
@BenchmarkHistoryChart()
@BenchmarkMethodChart()
@AxisRange(min = 0)
public class ArrayListIterationBenchmark
{
    private static final H2Consumer consumer = new H2Consumer(new File(
        TestH2MethodChart.class.getName()));

    @Rule
    public MethodRule benchmarkRun = new BenchmarkRule(consumer);

    private static ArrayList<Integer> arrayList = new ArrayList<Integer>();
    private static LinkedList<Integer> linkedList = new LinkedList<Integer>();

    static volatile Integer temp;

    @BeforeClass
    public static void prepareLists()
    {
        for (int i = 0; i < 5000000; i++)
        {
            linkedList.add(i + 128);
            arrayList.add(i + 128);
        }
    }

    @Test
    public void testArrayListGetByIndex()
    {
        getByIndex(arrayList);
    }
    
    @Test
    public void testArrayListIterator()
    {
        byIterator(arrayList);
    }
    
    @Test
    public void testLinkedListIterator()
    {
        byIterator(linkedList);
    }

    private void getByIndex(List<Integer> list)
    {
        for (int i = 0; i < list.size(); i++)
        {
            temp = list.get(i);
        }
    }

    private void byIterator(List<Integer> list)
    {
        for (Integer i : list)
        {
            temp = i;
        }
    }

}
