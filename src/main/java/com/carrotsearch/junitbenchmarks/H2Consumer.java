package com.carrotsearch.junitbenchmarks;

import java.io.Closeable;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import org.h2.jdbcx.JdbcDataSource;

/**
 * {@link IResultsConsumer} that appends records to a H2 database.
 */
public final class H2Consumer extends AutocloseConsumer implements Closeable
{
    /* */
    private Connection connection;

    /*
     * 
     */
    public H2Consumer(File dbFileName) throws SQLException
    {
        final JdbcDataSource ds = new org.h2.jdbcx.JdbcDataSource();

        ds.setURL("jdbc:h2:" + dbFileName.getAbsolutePath());
        ds.setUser("sa");

        this.connection = ds.getConnection();
        connection.setAutoCommit(false);

        checkSchema();

        // TODO: add suite header record and remember the pk.
    }

    /**
     * Accept a single benchmark result.
     */
    public void accept(Result result)
    {
        // TODO: add log entry. associate with header record's pk.
    }

    /**
     * Close the output XML stream.
     */
    public void close()
    {
        try
        {
            if (connection != null)
            {
                connection.close();
                connection = null;
            }
        }
        catch (SQLException e)
        {
            // Ignore?
        }
    }

    /**
     * Check database schema and create it if needed.
     */
    private void checkSchema() throws SQLException
    {
        // TODO: check db schema. add tables and indexes if necessary.
        // store: jvm/ os/ execution key (build key?)?
    }
}
