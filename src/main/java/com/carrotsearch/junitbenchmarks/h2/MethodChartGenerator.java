package com.carrotsearch.junitbenchmarks.h2;

import java.io.*;
import java.sql.*;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

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
        template = replaceToken(template, "CLASSNAME", clazzName);
        template = replaceToken(template, "JSONDATA.json", jsonFileName);
        template = replaceToken(template, "PROPERTIES", getProperties());

        save(parentDir, htmlFileName, template);
        save(parentDir, jsonFileName, getData());
        return null;
    }

    /**
     * Get extra properties associated with the chart's test class/ run. 
     */
    private String getProperties() throws SQLException
    {
        StringBuilder buf = new StringBuilder();

        final PreparedStatement s = 
            connection.prepareStatement(H2Consumer.getResource("method-chart-properties.sql"));
        s.setInt(1, runId);

        ResultSet rs = s.executeQuery();
        ResultSetMetaData metaData = rs.getMetaData();
        while (rs.next())
        {   
            for (int i = 1; i <= metaData.getColumnCount(); i++)
            {
                final Object obj = rs.getObject(i);
                if (obj == null)
                    continue;

                buf.append(metaData.getColumnLabel(i));
                buf.append(": ");
                buf.append(obj);
                buf.append("\n");
            }
        }

        // TODO: buf HTML-escaping here?
        return buf.toString();
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
            final String type = getMappedType(metaData.getColumnType(i));

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
                final Object value = formatValue(metaData.getColumnType(i), rs.getObject(i)); 
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

    private Object formatValue(int sqlColumnType, Object val)
    {
        switch (sqlColumnType)
        {
            // TODO: add escaping here? Seems to be of little practical use in this scenario.
            case java.sql.Types.VARCHAR:
                return "\"" + val + "\"";

            case java.sql.Types.NUMERIC:
            case java.sql.Types.DOUBLE:
            case java.sql.Types.FLOAT:
            case java.sql.Types.INTEGER:
            case java.sql.Types.SMALLINT:
            case java.sql.Types.TINYINT:
                return val;
        }
        throw new RuntimeException("Unsupported column type: " + sqlColumnType);
    }

    private String getMappedType(int sqlColumnType)
    {
        switch (sqlColumnType)
        {
            case java.sql.Types.VARCHAR:
                return "string";

            case java.sql.Types.NUMERIC:
            case java.sql.Types.DOUBLE:
            case java.sql.Types.FLOAT:
            case java.sql.Types.INTEGER:
            case java.sql.Types.SMALLINT:
            case java.sql.Types.TINYINT:
                return "number";
        }
        throw new RuntimeException("Unsupported column type: " + sqlColumnType);
    }

    /**
     * Process the template and substitute a fixed token.
     */
    private String replaceToken(String template, String key, String replacement)
    {
        Pattern p = Pattern.compile(key, Pattern.LITERAL);
        return p.matcher(template).replaceAll(replacement); 
    }

    /**
     * Save an output resource. 
     */
    private void save(File parentDir, String fileName, String content) throws IOException
    {
        final FileOutputStream fos = new FileOutputStream(fileName);
        fos.write(content.getBytes("UTF-8"));
        fos.close();
    }
}
