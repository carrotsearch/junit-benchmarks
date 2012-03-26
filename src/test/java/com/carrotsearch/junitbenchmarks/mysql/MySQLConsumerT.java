/*
 * Copyright 2012 Carrot Search s.c..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.carrotsearch.junitbenchmarks.mysql;

import com.mysql.management.MysqldResource;
import com.mysql.management.MysqldResourceI;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.junit.AfterClass;
import static org.junit.Assert.assertNotNull;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit tests for the MySQL Consumer.
 * 
 * @author <a href="mailto:steven.swor@summitsystemsinc.com"
 * title="steven.swor@summitsystemsinc.com">Steven Swor</a>
 */
public class MySQLConsumerT
{
    /**
     * The MySQL connection URL
     */
    private static final String MYSQL_URL = "jdbc:mysql://localhost:{0}/MySQLConsumerTest?user=test&password=test&createDatabaseIfNotExist=true";
    
    /**
     * The embedded server instance.
     */
    private static MysqldResource server = null;
    
    /**
     * The server port.
     */
    private static String mySQLPort = null;
    
    /**
     * The test consumer.
     */
    private MySQLConsumer testInstance = null;

    /**
     * Creates a new MySQLConsumerT with a new test consumer.
     */
    public MySQLConsumerT()
    {
        testInstance = new MySQLConsumer(MessageFormat.format(MYSQL_URL, getMySQLPort("3306")));
    }

    /**
     * Gets the MySQL port.
     * @param defaultResult the default port
     * @return the MySQL port from a mysql.properties file, or the default value
     */
    private static String getMySQLPort(String defaultResult)
    {
        if (mySQLPort == null)
        {
            FileInputStream fileStream = null;
            try
            {
                Properties props = new Properties();
                fileStream = new FileInputStream(new File("target/mysql.properties"));
                props.load(fileStream);
                mySQLPort = props.getProperty("mysql.port", defaultResult);
            } catch (IOException ex)
            {
                mySQLPort = defaultResult;
            } finally
            {
                if (fileStream != null)
                {
                    try
                    {
                        fileStream.close();
                    } catch (IOException ex)
                    {
                        //trap
                    }
                }
            }
        }
        return mySQLPort;
    }

    /**
     * Launches the server instance.
     */
    @BeforeClass
    public static void setUpClass()
    {
        File ourAppDir = new File(System.getProperty("java.io.tmpdir"));
        File databaseDir = new File(ourAppDir, "Test");
        server = new MysqldResource(ourAppDir, databaseDir);
        Map database_options = new HashMap();
        database_options.put(MysqldResourceI.PORT, getMySQLPort("3306"));
        database_options.put(MysqldResourceI.INITIALIZE_USER, "true");
        database_options.put(MysqldResourceI.INITIALIZE_USER_NAME, "test");
        database_options.put(MysqldResourceI.INITIALIZE_PASSWORD, "test");
        server.start("mysqld", database_options);
        if (!server.isRunning())
        {
            throw new RuntimeException("MySQL did not start.");
        }
    }

    /**
     * Shuts down the server instance.
     */
    @AfterClass
    public static void tearDownClass()
    {
        if (server != null)
        {
            if (server.isRunning())
            {
                try
                {
                    server.shutdown();
                } catch (Throwable ex)
                {
                    ex.printStackTrace();
                }
            }
            server = null;
        }
    }

    /**
     * Tests {@link MySQLConsumer#createConnection()}.
     */
    @Test
    public void testCreateConnection() throws Exception
    {
        assertNotNull(testInstance.createConnection());
    }

    /**
     * Tests {@link MySQLConsumer#getMethodChartResultsQuery()}.
     */
    @Test
    public void testGetMethodChartResultsQuery()
    {
        assertNotNull(testInstance.getMethodChartResultsQuery());
    }

    /**
     * Tests {@link MySQLConsumer#getMethodChartPropertiesQuery()}.
     */
    @Test
    public void testGetMethodChartPropertiesQuery()
    {
        assertNotNull(testInstance.getMethodChartPropertiesQuery());
    }

    /**
     * Tests {@link MySQLConsumer#getCreateRunsSql()}.
     */
    @Test
    public void testGetCreateRunsSql()
    {
        assertNotNull(testInstance.getCreateRunsSql());
    }

    /**
     * Tests {@link MySQLConsumer#getCreateTestsSql()}.
     */
    @Test
    public void testGetCreateTestsSql()
    {
        assertNotNull(testInstance.getCreateTestsSql());
    }

    /**
     * Tests {@link MySQLConsumer#getNewRunSql()}.
     */
    @Test
    public void testGetNewRunSql()
    {
        assertNotNull(testInstance.getNewRunSql());
    }

    /**
     * Tests {@link MySQLConsumer#getTestInsertSql()}.
     */
    @Test
    public void testGetTestInsertSql()
    {
        assertNotNull(testInstance.getTestInsertSql());
    }

    /**
     * Tests {@link MySQLConsumer#getCreateDbVersionSql()}.
     */
    @Test
    public void testGetCreateDbVersionSql()
    {
        assertNotNull(testInstance.getCreateDbVersionSql());
    }

    /**
     * Tests {@link MySQLConsumer#getAddCustomKeySql()}.
     */
    @Test
    public void testGetAddCustomKeySql()
    {
        assertNotNull(testInstance.getAddCustomKeySql());
    }
}
