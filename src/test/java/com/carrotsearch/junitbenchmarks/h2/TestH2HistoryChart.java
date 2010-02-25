package com.carrotsearch.junitbenchmarks.h2;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import com.carrotsearch.junitbenchmarks.Common;

/**
 * Test H2 consumer's chart generation functionality. 
 */
public class TestH2HistoryChart
{
    @Test
    public void testHistoryChartGeneration() throws Exception
    {
        RepeatedTestSlave.dbFileFull.delete();

        Result result = JUnitCore.runClasses(
            RepeatedTestSlave.class,
            RepeatedTestSlave.class,
            RepeatedTestSlave.class);
        assertEquals(0, result.getFailureCount());

        Common.assertFileExists(RepeatedTestSlave.class.getName() + ".html");
    }
}
