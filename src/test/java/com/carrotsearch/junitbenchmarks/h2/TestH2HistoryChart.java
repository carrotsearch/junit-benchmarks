package com.carrotsearch.junitbenchmarks.h2;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.codehaus.jackson.map.ObjectMapper;
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

        RepeatedTestSlave.cleanup = false;
        try
        {
            Result result = JUnitCore.runClasses(
                RepeatedTestSlave.class,
                RepeatedTestSlave.class,
                RepeatedTestSlave.class);
            assertEquals(0, result.getFailureCount());
        }
        finally
        {
            RepeatedTestSlave.cleanup = true;            
        }

        Common.existsAndDelete(RepeatedTestSlave.class.getName() + ".html");
        Common.existsAndDelete(RepeatedTestSlave.class.getName() + ".json");
        RepeatedTestSlave.dbFileFull.delete();
    }
    
    // http://issues.carrot2.org/browse/JUNITBENCH-39
    @Test
    public void testMissingComma_JUNITBENCH39() throws Exception
    {
        SingleMethodSlave.dbFileFull.delete();
        SingleMethodSlave.cleanup = false;
        try
        {
            Result result = JUnitCore.runClasses(
                SingleMethodSlave.class,
                SingleMethodSlave.class,
                SingleMethodSlave.class);
            assertEquals(0, result.getFailureCount());
            
            File expectedJson = new File(SingleMethodSlave.class.getName() + ".json");
            
            ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(expectedJson);
        }
        finally
        {
            RepeatedTestSlave.cleanup = true;            
        }

        Common.existsAndDelete(SingleMethodSlave.class.getName() + ".html");
        Common.existsAndDelete(SingleMethodSlave.class.getName() + ".json");
        SingleMethodSlave.dbFileFull.delete();
    }    
}
