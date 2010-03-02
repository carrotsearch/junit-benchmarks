package com.carrotsearch.junitbenchmarks.h2;

import java.io.*;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;

import org.h2.jdbcx.JdbcDataSource;

import com.carrotsearch.junitbenchmarks.*;

/**
 * {@link IResultsConsumer} that appends records to a H2 database.
 */
public final class H2Consumer extends AutocloseConsumer implements Closeable
{
    /*
     * Column indexes in the prepared insert statement.
     */
    private final static int RUN_ID, CLASSNAME, NAME, BENCHMARK_ROUNDS, WARMUP_ROUNDS,
        ROUND_AVG, ROUND_STDDEV, GC_AVG, GC_STDDEV, GC_INVOCATIONS, GC_TIME,
        TIME_BENCHMARK, TIME_WARMUP;

    static
    {
        int column = 1;
        RUN_ID = column++;
        CLASSNAME = column++;
        NAME = column++;
        BENCHMARK_ROUNDS = column++;
        WARMUP_ROUNDS = column++;
        ROUND_AVG = column++;
        ROUND_STDDEV = column++;
        GC_AVG = column++;
        GC_STDDEV = column++;
        GC_INVOCATIONS = column++;
        GC_TIME = column++;
        TIME_BENCHMARK = column++;
        TIME_WARMUP = column++;
    }

    /* */
    private Connection connection;

    /** Unique primary key for this consumer in the RUNS table. */
    int runId;

    /** Insert statement to the tests table. */
    private PreparedStatement newTest;

    /** Output directory for charts. */
    File chartsDir;

    /**
     * Charting visitors.  
     */
    private List<? extends IChartAnnotationVisitor> chartVisitors;

    /*
     * 
     */
    public H2Consumer()
    {
        this(getDefaultDbName());
    }

    /*
     * 
     */
    public H2Consumer(File dbFileName)
    {
        this(dbFileName, getDefaultChartsDir(), getDefaultCustomKey());
    }

    /*
     *
     */
    public H2Consumer(File dbFileName, File chartsDir, String customKeyValue)
    {
        try
        {
            final JdbcDataSource ds = new org.h2.jdbcx.JdbcDataSource();
            ds.setURL("jdbc:h2:" + dbFileName.getAbsolutePath() + ";DB_CLOSE_ON_EXIT=FALSE");
            ds.setUser("sa");

            this.chartsDir = chartsDir;
            this.chartVisitors = newChartVisitors();
    
            this.connection = ds.getConnection();
            connection.setAutoCommit(false);
            super.addAutoclose(this);

            checkSchema();

            runId = getRunID(customKeyValue);

            newTest = connection.prepareStatement(getResource("003-new-result.sql"));
            newTest.setInt(RUN_ID, runId);
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Cannot initialize H2 database.", e);
        }
    }

    /*
     * 
     */
    private List<? extends IChartAnnotationVisitor> newChartVisitors()
    {
        return Arrays.asList(
            new MethodChartVisitor(),
            new HistoryChartVisitor());
    }

    /**
     * Accept a single benchmark result.
     */
    public void accept(Result result)
    {
        // Visit chart collectors.
        final Class<?> clazz = result.getTestClass();
        final Method method = result.getTestMethod();
        for (IChartAnnotationVisitor v : chartVisitors)
            v.visit(clazz, method, result);

        try
        {
            newTest.setString(CLASSNAME, result.getTestClassName());
            newTest.setString(NAME, result.getTestMethodName());

            newTest.setInt(BENCHMARK_ROUNDS, result.benchmarkRounds);
            newTest.setInt(WARMUP_ROUNDS, result.warmupRounds);

            newTest.setDouble(ROUND_AVG, result.roundAverage.avg);
            newTest.setDouble(ROUND_STDDEV, result.roundAverage.stddev);

            newTest.setDouble(GC_AVG, result.gcAverage.avg);
            newTest.setDouble(GC_STDDEV, result.gcAverage.stddev);

            newTest.setInt(GC_INVOCATIONS, (int) result.gcInfo.accumulatedInvocations());
            newTest.setDouble(GC_TIME, result.gcInfo.accumulatedTime() / 1000.0);

            newTest.setDouble(TIME_WARMUP, result.warmupTime / 1000.0);
            newTest.setDouble(TIME_BENCHMARK, result.benchmarkTime / 1000.0);

            newTest.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(
                "Error while saving the benchmark result to H2.", e);
        }
    }

    /**
     * Close the database connection and finalize transaction.
     */
    public void close()
    {
        try
        {
            if (connection != null)
            {
                if (!connection.isClosed())
                {
                    doClose();
                }
                connection = null;
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to close H2 consumer.", e);
        }
    }

    /**
     * Rollback all performed operations on request.
     */
    public void rollback()
    {
        try
        {
            connection.rollback();
            this.chartVisitors = newChartVisitors();
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Could not rollback.", e);
        }
    }

    /**
     * Retrieve DB version. 
     */
    DbVersions getDbVersion() throws SQLException
    {
        Statement s = connection.createStatement();
        ResultSet rs = s.executeQuery("SHOW TABLES");
        Set<String> tables = new HashSet<String>();
        while (rs.next())
        {
            tables.add(rs.getString(1));
        }
    
        if (!tables.contains("DBVERSION"))
        {
            if (tables.contains("RUNS"))
            {
                return DbVersions.VERSION_1;
            }
    
            return DbVersions.UNINITIALIZED;
        }
    
        DbVersions version;
        rs = s.executeQuery("SELECT VERSION FROM DBVERSION");
        if (!rs.next())
        {
            throw new RuntimeException("Missing version row in DBVERSION table.");
        }
    
        version = DbVersions.fromInt(rs.getInt(1));
        if (rs.next()) {
            throw new RuntimeException("More than one row in DBVERSION table.");
        }
    
        return version;
    }

    /**
     * Read a given resource from classpath and return UTF-8 decoded string.
     */
    static String getResource(String resourceName)
    {
        try
        {
            InputStream is = H2Consumer.class.getResourceAsStream(resourceName);
            if (is == null) throw new IOException();
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final byte [] buffer = new byte [1024];
            int cnt;
            while ((cnt = is.read(buffer)) > 0) {
                baos.write(buffer, 0, cnt);
            }
            is.close();
            baos.close();
    
            return new String(baos.toByteArray(), "UTF-8");
        }
        catch (IOException e)
        {
            throw new RuntimeException("Required resource missing: "
                + resourceName);
        }
    }

    /**
     * Return the global default DB name.
     */
    private static File getDefaultDbName()
    {
        final String dbPath = System.getProperty(BenchmarkOptionsSystemProperties.DB_FILE_PROPERTY);
        if (dbPath != null && !dbPath.trim().equals(""))
        {
            return new File(dbPath);
        }
        
        throw new IllegalArgumentException("Missing global property: "
            + BenchmarkOptionsSystemProperties.DB_FILE_PROPERTY); 
    }

    private static String getDefaultCustomKey()
    {
        return System.getProperty(BenchmarkOptionsSystemProperties.CUSTOMKEY_PROPERTY);
    }

    private static File getDefaultChartsDir()
    {
        return new File(System.getProperty(BenchmarkOptionsSystemProperties.CHARTS_DIR_PROPERTY, "."));
    }

    /**
     * @return Create a row for this consumer's test run.
     */
    private int getRunID(String customKeyValue) throws SQLException
    {
        PreparedStatement s = connection.prepareStatement(
            getResource("002-new-run.sql"), Statement.RETURN_GENERATED_KEYS);
        s.setString(1, System.getProperty("java.runtime.version", "?"));
        s.setString(2, System.getProperty("os.arch", "?"));
        s.setString(3, customKeyValue);
        s.executeUpdate();
    
        ResultSet rs = s.getGeneratedKeys();
        if (!rs.next()) throw new SQLException("No autogenerated keys?");
        final int key = rs.getInt(1);
        if (rs.next()) throw new SQLException("More than one autogenerated key?");
    
        rs.close();
        s.close();
    
        return key;
    }

    /**
     * Do finalize the consumer; close db connection and emit reports.
     */
    private void doClose() throws Exception
    {
        try
        {
            for (IChartAnnotationVisitor v : chartVisitors)
            {
                v.generate(this);
            }
        }
        finally
        {
            if (!connection.isClosed())
            {
                connection.commit();
                connection.close();
            }
        }
    }

    /**
     * Check database schema and create it if needed.
     */
    private void checkSchema() throws SQLException
    {
        DbVersions dbVersion = getDbVersion();
        Statement s = connection.createStatement();
        switch (dbVersion)
        {
            case UNINITIALIZED:
                s.execute(getResource("000-create-runs.sql"));
                s.execute(getResource("001-create-tests.sql"));
                // fall-through.
            case VERSION_1:
                s.execute(getResource("004-create-dbversion.sql"));
                s.execute(getResource("005-add-custom-key.sql"));
                updateDbVersion(DbVersions.VERSION_2);
                // fall-through
            case VERSION_2:
                break;

            default:
                throw new RuntimeException("Unexpected database version: "
                    + dbVersion);
        }
        connection.commit();
    }

    /**
     * Update database version.
     */
    private void updateDbVersion(DbVersions newVersion) throws SQLException
    {
        Statement s = connection.createStatement();
        s.executeUpdate("DELETE FROM DBVERSION");
        s.executeUpdate("INSERT INTO DBVERSION (VERSION) VALUES (" + newVersion.version + ")");
    }

    /**
     * 
     */
    Connection getConnection()
    {
        return connection;
    }
}
