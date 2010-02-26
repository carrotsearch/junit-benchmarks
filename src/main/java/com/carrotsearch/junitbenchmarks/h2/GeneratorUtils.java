package com.carrotsearch.junitbenchmarks.h2;

import java.io.*;
import java.sql.*;
import java.util.regex.Pattern;

import com.carrotsearch.junitbenchmarks.Escape;

/**
 * Report generator utilities.
 */
final class GeneratorUtils
{
    /**
     * Literal 'CLASSNAME'.
     */
    private final static Pattern CLASSNAME_PATTERN = 
        Pattern.compile("CLASSNAME", Pattern.LITERAL);

    /**
     * Get extra properties associated with the given run. 
     */
    static String getProperties(Connection connection, int runId) throws SQLException
    {
        final StringBuilder buf = new StringBuilder();

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

        rs.close();
        s.close();

        return Escape.htmlEscape(buf.toString());
    }

    /**
     * Format a given SQL value to be placed in JSON script (add quotes as needed). 
     */
    static Object formatValue(int sqlColumnType, Object val)
    {
        switch (sqlColumnType)
        {
            case java.sql.Types.VARCHAR:
                return "\"" + Escape.jsonEscape(val.toString()) + "\"";

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

    /**
     * Get Google Chart API type for a given SQL type. 
     */
    static String getMappedType(int sqlColumnType)
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
     * Preprocess a given template and substitute a fixed token.
     */
    static String replaceToken(String template, String key, String replacement)
    {
        Pattern p = Pattern.compile(key, Pattern.LITERAL);
        return p.matcher(template).replaceAll(replacement); 
    }

    /**
     * Save an output resource to a given file. 
     */
    static void save(String fileName, String content) throws IOException
    {
        final FileOutputStream fos = new FileOutputStream(new File(fileName));
        fos.write(content.getBytes("UTF-8"));
        fos.close();
    }

    static String getMinMax(double min, double max)
    {
        StringBuilder b = new StringBuilder();
        if (!Double.isNaN(min))
        {
            b.append("min: " + min + ",");
        }
    
        if (!Double.isNaN(max))
        {
            b.append("max: " + max + ",");
        }
    
        return b.toString();
    }

    /**
     * Process file prefix for charts.
     * 
     * @param clazz Chart's class.
     * @param filePrefix File prefix annotation's value (may be empty).
     * @param chartsDir Parent directory for chart output files.
     * @return Fully qualified file name prefix (absolute).
     */
    public static String getFilePrefix(Class<?> clazz, String filePrefix, File chartsDir)
    {
        if (filePrefix == null || filePrefix.trim().equals(""))
        {
            filePrefix = clazz.getName();
        }

        filePrefix = CLASSNAME_PATTERN.matcher(filePrefix).replaceAll(clazz.getName());

        if (!new File(filePrefix).isAbsolute())
        {
            // For relative prefixes, attach parent directory.
            filePrefix = new File(chartsDir, filePrefix).getAbsolutePath();
        }

        return filePrefix;
    }
}
