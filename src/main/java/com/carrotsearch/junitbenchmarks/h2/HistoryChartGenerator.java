package com.carrotsearch.junitbenchmarks.h2;

import java.io.File;
import java.sql.*;
import java.text.NumberFormat;
import java.util.*;

import com.carrotsearch.junitbenchmarks.Escape;

/**
 * Generate historical view of a given test class (one or more methods). 
 */
public final class HistoryChartGenerator
{
    private Connection connection;
    private String clazzName;
    private File parentDir;

    /**
     * A list of methods included in the chart.
     */
    private ArrayList<String> methods = new ArrayList<String>();

    /**
     * Maximum number of history steps to fetch.
     */
    private int maxRuns = Integer.MIN_VALUE;

    /**
     * Prefix for output files.
     */
    private String filePrefix;

    /**
     * Min/ max.
     */
    private double min = Double.NaN, max = Double.NaN;

    /**
     * Value holder for row aggregation.
     */
    private static final class StringHolder {
        public String value;
        
        public StringHolder(String value)
        {
            this.value = value;
        }
    };

    /**
     * @param connection H2 database connection. 
     * @param parentDir Parent directory where charts should be dumped.
     * @param filePrefix Prefix for output files.
     * @param clazzName The target test class (fully qualified name).
     */
    public HistoryChartGenerator(Connection connection, File parentDir, 
        String filePrefix, 
        String clazzName)
    {
        this.connection = connection;
        this.clazzName = clazzName;
        this.parentDir = parentDir;
        this.filePrefix = filePrefix;
    }

    /**
     * Generate the chart's HTML.
     */
    public void generate() throws Exception
    {
        final String jsonFileName = filePrefix + ".json";
        final String htmlFileName = filePrefix + ".html";

        String template = H2Consumer.getResource("HistoryChartGenerator.html");
        template = GeneratorUtils.replaceToken(template, "CLASSNAME", clazzName);
        template = GeneratorUtils.replaceToken(template, "JSONDATA.json", jsonFileName);
        template = GeneratorUtils.replaceToken(template, "/*MINMAX*/", 
            GeneratorUtils.getMinMax(min, max));
        template = GeneratorUtils.replaceToken(template, "PROPERTIES", getProperties());

        GeneratorUtils.save(parentDir, htmlFileName, template);
        GeneratorUtils.save(parentDir, jsonFileName, getData());
    }

    private String getProperties()
    {
        return "Shows historical runs: " + (maxRuns == Integer.MAX_VALUE ? "all" : maxRuns);
    }

    /**
     * Get chart data as JSON string.
     */
    private String getData() throws SQLException
    {
        String methodsRestrictionClause = "";
        if (methods.size() > 0)
        {
            StringBuilder b = new StringBuilder();
            b.append(" AND NAME IN (");
            for (int i = 0; i < methods.size(); i++)
            {
                if (i > 0) b.append(", ");
                b.append("'");
                b.append(Escape.sqlEscape(methods.get(i)));
                b.append("'");
            }
            b.append(")");
            methodsRestrictionClause = b.toString();
        }

        PreparedStatement s;
        ResultSet rs;
        int minRunId = 0;
        if (maxRuns != Integer.MAX_VALUE)
        {
            // Get min. runId to start from.
            s = connection.prepareStatement(
                "SELECT DISTINCT RUN_ID FROM TESTS t, RUNS r " + 
                " WHERE t.classname = ? " +
                " AND t.run_id = r.id " +
                methodsRestrictionClause +
                " ORDER BY RUN_ID DESC " +
                " LIMIT ?");
            s.setString(1, clazzName);
            s.setInt(2, maxRuns);
            rs = s.executeQuery();
            if (rs.last())
            {
                minRunId = rs.getInt(1);
            }
            s.close();
        }

        // Get all the method names within the runs range.
        s = connection.prepareStatement(
            "SELECT DISTINCT NAME FROM TESTS t, RUNS r " +
            " WHERE t.classname = ? " +
            " AND t.run_id = r.id " +
            methodsRestrictionClause +
            " AND r.id >= ? " +
            " ORDER BY NAME ");
        s.setString(1, clazzName);
        s.setInt(2, minRunId);

        final ArrayList<String> columnNames = new ArrayList<String>(); 
        rs = s.executeQuery();
        while (rs.next())
        {
            columnNames.add(rs.getString(1));
        }

        // Emit columns.
        StringBuilder buf = new StringBuilder();
        buf.append("{\n");

        buf.append("\"cols\": [\n");
        buf.append("{\"label\": \"");
        buf.append("Run");
        buf.append("\", \"type\": \"string\"}");

        for (int i = 0; i < columnNames.size(); i++)
        {
            buf.append(",\n");
            buf.append("{\"label\": \"");
            buf.append(Escape.jsonEscape(columnNames.get(i)));
            buf.append("\", \"type\": \"string\"} ");
        }
        buf.append("],\n");

        // Emit data.
        s = connection.prepareStatement(
            "SELECT RUN_ID, NAME, ROUND_AVG " + 
            "FROM TESTS t, RUNS r " + 
            "WHERE t.classname = ? " +
            " AND t.run_id = r.id " +
            methodsRestrictionClause +
            " AND r.id >= ? " +
            "ORDER BY r.id ASC, NAME ASC");
        s.setString(1, clazzName);
        s.setInt(2, minRunId);
        rs = s.executeQuery();

        /*
         * We need to emit a value for every column, possibly missing, so prepare a
         * full row.
         */
        final NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
        nf.setMaximumFractionDigits(3);
        nf.setGroupingUsed(false);

        final HashMap<String, StringHolder> byColumn = new HashMap<String, StringHolder>();
        final ArrayList<StringHolder> row = new ArrayList<StringHolder>();
        row.add(new StringHolder(null));
        for (String name : columnNames)
        {
            StringHolder nv = new StringHolder(null);
            row.add(nv);
            byColumn.put(name, nv);
        }

        int previousRowId = -1;
        buf.append("\"rows\": [\n");
        while (rs.next())
        {
            int rowId = rs.getInt(1);
            String name = rs.getString(2);
            double avg = rs.getDouble(3);

            if (rowId != previousRowId || rs.isLast())
            {
                if (rs.isLast())
                {
                    byColumn.get(name).value = nf.format(avg);
                    previousRowId = rowId;
                }

                if (previousRowId >= 0)
                {
                    // Emit the last row. Clear row data.
                    row.get(0).value = '"' + Integer.toString(previousRowId) + '"';

                    buf.append("{\"c\": [");
                    for (StringHolder nv : row)
                    {
                        buf.append("{\"v\": ");
                        buf.append(nv.value);
                        buf.append("}, ");
                    }
                    buf.append("]},");
                    buf.append('\n');

                    for (StringHolder nv : row)
                        nv.value = null;
                }

                previousRowId = rowId;
            }

            final StringHolder nv = byColumn.get(name);
            if (nv == null) 
                throw new RuntimeException("Missing column: " + name);
            nv.value = nf.format(avg);
        }
        buf.append("]}\n");

        return buf.toString();
    }

    /**
     * Include a given method in the chart. 
     */
    public void includeMethod(String methodName)
    {
        methods.add(methodName);
    }

    /**
     * Update max history steps.
     */
    public void updateMaxRuns(int newMax)
    {
        this.maxRuns = Math.max(newMax, maxRuns);
    }

    /**
     * Update min/max fields.
     */
    public void updateMinMax(AxisRange r)
    {
        if (Double.isNaN(this.min))
            this.min = r.min();

        if (!Double.isNaN(r.min()))
        {
            this.min = Math.min(r.min(), this.min);
        }

        if (Double.isNaN(this.max))
            this.max = r.max();

        if (!Double.isNaN(r.max()))
        {
            this.max = Math.max(r.max(), this.max);
        }
    }
}
