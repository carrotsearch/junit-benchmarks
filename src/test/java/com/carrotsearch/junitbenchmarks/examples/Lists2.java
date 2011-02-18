package com.carrotsearch.junitbenchmarks.examples;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.annotation.*;

// [[[start:lists-annot]]]
@BenchmarkOptions(callgc = false, benchmarkRounds = 20, warmupRounds = 3)
// [[[end:lists-annot]]]

// [[[start:lists-chart-methods]]]
@AxisRange(min = 0, max = 1)
@BenchmarkMethodChart(filePrefix = "benchmark-lists")
// [[[end:lists-chart-methods]]]

// [[[start:lists-chart-history]]]
@BenchmarkHistoryChart(labelWith = LabelType.CUSTOM_KEY, maxRuns = 20)
// [[[end:lists-chart-history]]]
public class Lists2
{
    private static Object singleton = new Object();
    private static int COUNT = 50000;
    private static int [] rnd;

    @Rule
    public MethodRule benchmarkRun = new BenchmarkRule();    

    /** Prepare random numbers for tests. */
    @BeforeClass
    public static void prepare()
    {
        rnd = new int [COUNT];

        final Random random = new Random();
        for (int i = 0; i < COUNT; i++)
        {
            rnd[i] = Math.abs(random.nextInt());
        }
    }

    @Test
    public void arrayList() throws Exception
    {
        runTest(new ArrayList<Object>());
    }

    @Test
    public void linkedList() throws Exception
    {
        runTest(new LinkedList<Object>());
    }

    @Test
    public void vector() throws Exception
    {
        runTest(new Vector<Object>());
    }

    private void runTest(List<Object> list)
    {
        assert list.isEmpty();
        
        // First, add a number of objects to the list.
        for (int i = 0; i < COUNT; i++)
            list.add(singleton);
        
        // Randomly delete objects from the list.
        for (int i = 0; i < rnd.length; i++)
            list.remove(rnd[i] % list.size());
    }
}