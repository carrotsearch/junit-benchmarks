package com.carrotsearch.junitbenchmarks.mysql;

import com.carrotsearch.junitbenchmarks.BenchmarkOptionsSystemProperties;
import com.carrotsearch.junitbenchmarks.db.DbConsumer;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * {@link DbConsumer} implementation for MySQL.
 */
public final class MySQLConsumer extends DbConsumer
{

    /**
     * The server location
     */
    private String dbUrl;

    /**
     * Creates a consumer with the default file name.
     */
    public MySQLConsumer()
    {
        this(getDefaultServerLocation());
    }

    /**
     * Creates a consumer with the default charts and custom key dirs.
     *
     * @param dbFileName the database file name
     */
    public MySQLConsumer(String dbUrl)
    {
        this(dbUrl, getDefaultChartsDir(), getDefaultCustomKey());
    }

    /**
     * Creates a consumer with the specified database file, charts directory,
     * and custom key value.
     *
     * @param dbUrl the database url
     * @param chartsDir the charts directory
     * @param customKeyValue the custom key value
     */
    public MySQLConsumer(String dbUrl, File chartsDir, String customKeyValue)
    {
        super(chartsDir, customKeyValue);
        this.dbUrl = dbUrl;
        try
        {
            checkSchema();
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Cannot initialize MySQL database.", e);
        }
    }

    /**
     * Return the global default DB name.
     */
    private static String getDefaultServerLocation()
    {
        final String dbPath = System.getProperty(BenchmarkOptionsSystemProperties.MYSQL_URL_PROPERTY);
        if (dbPath != null && !dbPath.trim().equals(""))
        {
            return dbPath;
        }
        throw new IllegalArgumentException("Missing global property: "
                + BenchmarkOptionsSystemProperties.MYSQL_URL_PROPERTY);
    }

    @Override
    protected Connection createConnection() throws SQLException
    {
        final MysqlDataSource ds = new MysqlDataSource();
        ds.setURL(dbUrl);
        Connection results = ds.getConnection();
        results.setAutoCommit(false);
        return results;
    }

    @Override
    public String getMethodChartResultsQuery()
    {
        return getResource(MySQLConsumer.class, "method-chart-results.sql");
    }

    @Override
    public String getMethodChartPropertiesQuery()
    {
        return getResource(MySQLConsumer.class, "method-chart-properties.sql");
    }

    @Override
    protected String getCreateRunsSql()
    {
        return getResource(MySQLConsumer.class, "000-create-runs.sql");
    }

    @Override
    protected String getCreateTestsSql()
    {
        return getResource(MySQLConsumer.class, "001-create-tests.sql");
    }

    @Override
    protected String getNewRunSql()
    {
        return getResource(MySQLConsumer.class, "002-new-run.sql");
    }

    @Override
    protected String getTestInsertSql()
    {
        return getResource(MySQLConsumer.class, "003-new-result.sql");
    }

    @Override
    protected String getCreateDbVersionSql()
    {
        return getResource(MySQLConsumer.class, "004-create-dbversion.sql");
    }

    @Override
    protected String getAddCustomKeySql()
    {
        return getResource(MySQLConsumer.class, "005-add-custom-key.sql");
    }
}
