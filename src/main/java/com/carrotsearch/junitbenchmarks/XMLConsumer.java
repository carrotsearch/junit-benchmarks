package com.carrotsearch.junitbenchmarks;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * {@link IResultsConsumer} that writes XML files for each benchmark.
 */
public final class XMLConsumer extends AutocloseConsumer implements Closeable
{
    /**
     * Timestamp format.
     */
    public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    /**
     * Output XML writer.
     */
    private Writer writer;

    /*
     * 
     */
    public XMLConsumer(File fileName)
    {
        try
        {
            writer = new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8");
            addAutoclose(this);
            writer.write("<benchmark-results tstamp=\"" + tstamp() + "\">\n\n");
        }
        catch (IOException e)
        {
            throw new RuntimeException("Could not open output writer.", e);
        }
    }

    /**
     * Accept a single benchmark result.
     */
    public void accept(Result result)
    {
        try
        {
            // We emit XML by hand. If anything more difficult comes up, we can switch
            // to SimpleXML or some other XML binding solution.
            final NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
            nf.setMaximumFractionDigits(3);
            nf.setGroupingUsed(false);

            final StringBuilder b = new StringBuilder();

            b.append("\t<testname");
            attribute(b, "classname", result.getTestClassName());
            attribute(b, "name", result.getTestMethodName());

            b.append("\n\t\t");
            attribute(b, "benchmark-rounds", Integer.toString(result.benchmarkRounds));
            attribute(b, "warmup-rounds", Integer.toString(result.warmupRounds));

            b.append("\n\t\t");
            attribute(b, "round-avg", nf.format(result.roundAverage.avg));
            attribute(b, "round-stddev", nf.format(result.roundAverage.stddev));

            b.append("\n\t\t");
            attribute(b, "gc-avg", nf.format(result.gcAverage.avg));
            attribute(b, "gc-stddev", nf.format(result.gcAverage.stddev));

            b.append("\n\t\t");
            attribute(b, "gc-invocations", Long.toString(result.gcInfo.accumulatedInvocations()));
            attribute(b, "gc-time", nf.format(result.gcInfo.accumulatedTime() / 1000.0));

            b.append("\n\t\t");
            attribute(b, "benchmark-time-total", nf.format(result.warmupTime * 0.001));
            attribute(b, "warmup-time-total", nf.format(result.benchmarkTime * 0.001));

            b.append("/>\n\n");

            writer.write(b.toString());
            writer.flush();
        }
        catch (IOException e)
        {
            // Ignore.
        }
    }

    /** 
     * Close the output XML stream.
     */
    public void close()
    {
        try
        {
            if (this.writer != null)
            {
                writer.write("</benchmark-results>");
                writer.close();
                writer = null;
                removeAutoclose(this);
            }
        }
        catch (IOException e)
        {
            // Ignore.
        }
    }

    /**
     * Unique timestamp for this XML consumer. 
     */
    private static String tstamp()
    {
        SimpleDateFormat sdf = new SimpleDateFormat(TIMESTAMP_FORMAT);
        return sdf.format(new Date());
    }

    /**
     * Append an attribute to XML.
     */
    private void attribute(StringBuilder b, String attrName, String value)
    {
        b.append(' ');
        b.append(attrName);
        b.append("=\"");
        b.append(value);
        b.append('"');
    }
}
