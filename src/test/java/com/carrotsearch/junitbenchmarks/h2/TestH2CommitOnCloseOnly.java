package com.carrotsearch.junitbenchmarks.h2;

import static org.junit.Assert.*;

import java.io.File;
import java.sql.*;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.*;
import org.junit.rules.TestRule;

import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.h2.H2Consumer;

/**
 * Test H2 consumer's commit-on-close only (no commit if no close() was called). 
 */
public class TestH2CommitOnCloseOnly
{
    private static final File dbFile = new File(TestH2CommitOnCloseOnly.class.getName());
    private static final File dbFileFull = new File(dbFile.getName() + ".h2.db");

    private static H2Consumer h2consumer;

    @BeforeClass
    public static void checkFile() throws SQLException
    {
        if (dbFileFull.exists())
            assertTrue(dbFileFull.delete());

        h2consumer = new H2Consumer(dbFile);
    }

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule(h2consumer);

    @Test
    public void testMethodA()
    {
        // empty.
    }

    @Test
    public void testMethodB()
    {
        // empty.
    }

    @AfterClass
    public static void verify() throws Exception
    {
        h2consumer.rollback();
        h2consumer.close();
        assertTrue(dbFileFull.exists());

        // Check if rows have been added.
        final JdbcDataSource ds = new org.h2.jdbcx.JdbcDataSource();
        ds.setURL("jdbc:h2:" + dbFile.getAbsolutePath());
        ds.setUser("sa");

        final Connection connection = ds.getConnection();
        try
        {
            ResultSet rs = connection.createStatement().executeQuery("SELECT COUNT(*) FROM TESTS");
            assertTrue(rs.next());
            assertEquals(0, rs.getInt(1));
    
            rs = connection.createStatement().executeQuery("SELECT COUNT(*) FROM RUNS");
            assertTrue(rs.next());
            assertEquals(0, rs.getInt(1));
        }
        finally
        {
            connection.close();
            dbFileFull.delete();
        }
    }
}
