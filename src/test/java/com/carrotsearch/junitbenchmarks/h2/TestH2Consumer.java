package com.carrotsearch.junitbenchmarks.h2;

import static org.junit.Assert.*;

import java.io.File;
import java.sql.*;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.*;
import org.junit.rules.MethodRule;

import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.h2.H2Consumer;

/**
 * Test H2 consumer. 
 */
public class TestH2Consumer
{
    private static final File dbFile = new File("test-benchmarks");
    private static final File dbFileFull = new File(dbFile.getName() + ".h2.db");
    private static final String CUSTOM_KEY_VALUE = "xyz";

    private static H2Consumer h2consumer;

    @BeforeClass
    public static void checkFile() throws SQLException
    {
        if (dbFileFull.exists())
            assertTrue(dbFileFull.delete());

        h2consumer = new H2Consumer(dbFile, new File("."), CUSTOM_KEY_VALUE);
    }

    @Rule
    public MethodRule benchmarkRun = new BenchmarkRule(h2consumer);

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
        // Check DB upgrade process.
        DbVersions ver = h2consumer.getDbVersion();
        int maxVersion = DbVersions.UNINITIALIZED.version;
        for (DbVersions v : DbVersions.values())
            maxVersion = Math.max(maxVersion, v.version);
        assertEquals(maxVersion, ver.version);

        h2consumer.close();
        assertTrue(dbFileFull.exists());

        // Check if rows have been added.
        final JdbcDataSource ds = new org.h2.jdbcx.JdbcDataSource();
        ds.setURL("jdbc:h2:" + dbFile.getAbsolutePath());
        ds.setUser("sa");

        final Connection connection = ds.getConnection();

        ResultSet rs = connection.createStatement().executeQuery("SELECT COUNT(*) FROM TESTS");
        assertTrue(rs.next());
        assertEquals(2, rs.getInt(1));

        rs = connection.createStatement().executeQuery("SELECT COUNT(*) FROM RUNS");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt(1));

        rs = connection.createStatement().executeQuery("SELECT CUSTOM_KEY FROM RUNS");
        assertTrue(rs.next());
        assertEquals(CUSTOM_KEY_VALUE, rs.getString(1));

        connection.close();
        
        assertTrue(dbFileFull.delete());
    }
}
