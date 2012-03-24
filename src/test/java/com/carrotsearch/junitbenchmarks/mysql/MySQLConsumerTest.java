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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author <a href="mailto:steven.swor@summitsystemsinc.com"
 * title="steven.swor@summitsystemsinc.com">Steven Swor</a>
 */
public class MySQLConsumerTest {

    private static final String MYSQL_URL = "jdbc:mysql://localhost:{0}/MySQLConsumerTest?user=test&password=test&createDatabaseIfNotExist=true";
    private static MysqldResource server = null;
    private static String mySQLPort = null;
    
    private MySQLConsumer testInstance = null;

    public MySQLConsumerTest() {
        testInstance = new MySQLConsumer(MessageFormat.format(MYSQL_URL,getMySQLPort("3306")));
    }

    private static String getMySQLPort(String defaultResult) {
        if (mySQLPort == null) {
            FileInputStream fileStream = null;
            try {
                Properties props = new Properties();
                fileStream = new FileInputStream(new File("target/mysql.properties"));
                props.load(fileStream);
                mySQLPort = props.getProperty("mysql.port", defaultResult);
            } catch (IOException ex) {
                mySQLPort= defaultResult;
            } finally { 
                if (fileStream != null) {
                    try {
                        fileStream.close();
                    } catch (IOException ex) {
                        //trap
                    }
                }
            }
        }
        return mySQLPort;
    }

    @BeforeClass
    public static void setUpClass() {
        File ourAppDir = new File(System.getProperty("java.io.tmpdir"));
        File databaseDir = new File(ourAppDir, "Test");        
        server = new MysqldResource(ourAppDir, databaseDir);
        Map database_options = new HashMap();
        database_options.put(MysqldResourceI.PORT, getMySQLPort("3306"));
        database_options.put(MysqldResourceI.INITIALIZE_USER, "true");
        database_options.put(MysqldResourceI.INITIALIZE_USER_NAME, "test");
        database_options.put(MysqldResourceI.INITIALIZE_PASSWORD, "test");
        server.start("mysqld", database_options);
        if (!server.isRunning()) {
            throw new RuntimeException("MySQL did not start.");
        }
    }

    @AfterClass
    public static void tearDownClass() {
        if (server != null) {
            if (server.isRunning()) {
                try {
                    server.shutdown();
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            }
            server = null;
        }
    }

    /**
     * Test of createConnection method, of class MySQLConsumer.
     */
    @Test
    //@Ignore("requires a running mysql server instance")
    public void testCreateConnection() throws Exception {
        assertNotNull(testInstance.createConnection());
    }

    /**
     * Test of getMethodChartResultsQuery method, of class MySQLConsumer.
     */
    @Test
    public void testGetMethodChartResultsQuery() {
        assertNotNull(testInstance.getMethodChartResultsQuery());
    }

    /**
     * Test of getMethodChartPropertiesQuery method, of class MySQLConsumer.
     */
    @Test
    public void testGetMethodChartPropertiesQuery() {
        assertNotNull(testInstance.getMethodChartPropertiesQuery());
    }

    /**
     * Test of getCreateRunsSql method, of class MySQLConsumer.
     */
    @Test
    public void testGetCreateRunsSql() {
        assertNotNull(testInstance.getCreateRunsSql());
    }

    /**
     * Test of getCreateTestsSql method, of class MySQLConsumer.
     */
    @Test
    public void testGetCreateTestsSql() {
        assertNotNull(testInstance.getCreateTestsSql());
    }

    /**
     * Test of getNewRunSql method, of class MySQLConsumer.
     */
    @Test
    public void testGetNewRunSql() {
        assertNotNull(testInstance.getNewRunSql());
    }

    /**
     * Test of getTestInsertSql method, of class MySQLConsumer.
     */
    @Test
    public void testGetTestInsertSql() {
        assertNotNull(testInstance.getTestInsertSql());
    }

    /**
     * Test of getCreateDbVersionSql method, of class MySQLConsumer.
     */
    @Test
    public void testGetCreateDbVersionSql() {
        assertNotNull(testInstance.getCreateDbVersionSql());
    }

    /**
     * Test of getAddCustomKeySql method, of class MySQLConsumer.
     */
    @Test
    public void testGetAddCustomKeySql() {
        assertNotNull(testInstance.getAddCustomKeySql());
    }
}
