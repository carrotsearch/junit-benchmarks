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
    private Class<?> clazz;
    private File parentDir;
    
    /**
     * @param connection H2 database connection. 
     * @param parentDir Parent directory where charts should be dumped.
     * @param runId The run from which to select data.
     * @param clazz The target test class.
     */
    public MethodChartGenerator(Connection connection, File parentDir, int runId, Class<?> clazz)
    {
        this.connection = connection;
        this.runId = runId;
        this.clazz = clazz;
        this.parentDir = parentDir;
    }

    /**
     * Generate the chart's HTML.
     */
    public Void call() throws Exception
    {
        String template = H2Consumer.getResource("MethodChartGenerator.html");
        template = replaceToken(template, "#CLASSNAME#", clazz.getSimpleName());
        template = replaceRegexp(template, 
            Pattern.compile("(#DATA:START#)(.+?)(#DATA:END#)", Pattern.DOTALL), getData());
        save(parentDir, clazz.getName() + ".html", template);
        return null;
    }

    /**
     * Get chart data.
     */
    private String getData() throws SQLException
    {
        StringBuilder buf = new StringBuilder();
        buf.append("\n");

        final PreparedStatement s = 
            connection.prepareStatement(H2Consumer.getResource("MethodChartGenerator.sql"));
        s.setInt(1, runId);
        s.setString(2, clazz.getSimpleName());
        
        ResultSet rs = s.executeQuery();

        // Emit columns.
        ResultSetMetaData metaData = rs.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++)
        {
            String colLabel = metaData.getColumnLabel(i);
            buf.append("data.addColumn(");
            buf.append("'" + getMappedType(metaData.getColumnType(i)) + "'");
            buf.append(", ");
            buf.append("'" + colLabel + "'");
            buf.append(");\n");
        }

        while (rs.next())
        {
            buf.append("data.addRow([");
            for (int i = 1; i <= metaData.getColumnCount(); i++)
            {
                if (i > 1) buf.append(", ");
                buf.append(formatValue(metaData.getColumnType(i), rs.getObject(i)));
            }
            buf.append("]);\n");
        }

        rs.close();
        return buf.toString();
    }

    private Object formatValue(int sqlColumnType, Object val)
    {
        switch (sqlColumnType)
        {
            // TODO: add escaping here? Seems to be of little practical use in this scenario.
            case java.sql.Types.VARCHAR:
                return "'" + val + "'";

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
     * Process the template and substitute a regexp.
     */
    private String replaceRegexp(String template, Pattern p, String replacement)
    {
        return p.matcher(template).replaceAll(replacement); 
    }

    /**
     * Save an output resource. 
     */
    private void save(File parentDir, String fileName, String content) throws IOException
    {
        FileOutputStream fos = new FileOutputStream(fileName);
        fos.write(content.getBytes("UTF-8"));
        fos.close();
    }
}
