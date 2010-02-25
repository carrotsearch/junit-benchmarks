package com.carrotsearch.junitbenchmarks.h2;

import java.io.*;
import java.sql.*;
import java.util.concurrent.Callable;

/**
 * Generate a snippet of HTML code for a given class and all of its benchmarked methods. 
 */
public final class MethodChartGenerator implements Callable<Void>
{
    private Connection connection;
    private int runId;
    private String clazzName;
    private File parentDir;

    /**
     * @param connection H2 database connection. 
     * @param parentDir Parent directory where charts should be dumped.
     * @param runId The run from which to select data.
     * @param clazzName The target test class (fully qualified name).
     */
    public MethodChartGenerator(Connection connection, File parentDir, int runId, String clazzName)
    {
        this.connection = connection;
        this.runId = runId;
        this.clazzName = clazzName;
        this.parentDir = parentDir;
    }

    /**
     * Generate the chart's HTML.
     */
    public Void call() throws Exception
    {
        final String jsonFileName = clazzName + ".json";
        final String htmlFileName = clazzName + ".html";
        
        String template = H2Consumer.getResource("MethodChartGenerator.html");
        template = GeneratorUtils.replaceToken(template, "CLASSNAME", clazzName);
        template = GeneratorUtils.replaceToken(template, "JSONDATA.json", jsonFileName);
        template = GeneratorUtils.replaceToken(template, "PROPERTIES", 
            GeneratorUtils.getProperties(connection, runId));

        GeneratorUtils.save(parentDir, htmlFileName, template);
        GeneratorUtils.save(parentDir, jsonFileName, getData());
        return null;
    }

    /**
     * Get chart data as JSON string.
     */
    private String getData() throws SQLException
    {
        StringBuilder buf = new StringBuilder();
        buf.append("{\n");

        final PreparedStatement s = 
            connection.prepareStatement(H2Consumer.getResource("method-chart-results.sql"));
        s.setInt(1, runId);
        s.setString(2, clazzName);

        ResultSet rs = s.executeQuery();

        // Emit columns.
        buf.append("\"cols\": [\n");
        ResultSetMetaData metaData = rs.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++)
        {
            final String colLabel = metaData.getColumnLabel(i);
            final String type = GeneratorUtils.getMappedType(metaData.getColumnType(i));

            buf.append("{\"label\": \"");
            buf.append(colLabel);
            buf.append("\", \"type\": \"");
            buf.append(type);
            buf.append("\"}");
            if (i != metaData.getColumnCount()) buf.append(",");
            buf.append('\n');
        }
        buf.append("],\n");

        buf.append("\"rows\": [\n");
        while (rs.next())
        {
            buf.append("{\"c\": [");
            for (int i = 1; i <= metaData.getColumnCount(); i++)
            {
                if (i > 1) buf.append(", ");
                final Object value = GeneratorUtils.formatValue(metaData.getColumnType(i), rs.getObject(i)); 
                buf.append("{\"v\": ");
                buf.append(value.toString());
                buf.append("}");
            }
            buf.append("]}");
            if (!rs.isLast()) buf.append(",");
            buf.append('\n');
        }
        buf.append("]}\n");

        rs.close();
        return buf.toString();
    }
}
