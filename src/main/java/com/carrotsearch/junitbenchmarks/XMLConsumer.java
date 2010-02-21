package com.carrotsearch.junitbenchmarks;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * {@link IResultsConsumer} that writes XML files for each benchmark.
 */
public final class XMLConsumer implements IResultsConsumer, Closeable
{
    /**
     * Timestamp format.
     */
    public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    /**
     * Output XML writer.
     */
    private Writer writer;

    /**
     * A list of writers to close at shutdown (if not closed earlier).
     */
    private static List<Writer> autoclose = new ArrayList<Writer>();

    /**
     * A shutdown agent closing {@link #autoclose}.
     */
    private static Thread shutdownAgent;

    /*
     * 
     */
    public XMLConsumer(File fileName)
    {
        try
        {
            writer = new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8");
            begin(writer);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Could not open output writer.", e);
        }

        initShutdownAgent();
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
            attribute(b, "classname", result.target.getClass().getSimpleName());
            attribute(b, "name", result.method.getName());

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
                end(writer);
        }
        catch (IOException e)
        {
            // Ignore.
        }
    }

    private static synchronized void begin(Writer writer) throws IOException
    {
        autoclose.add(writer);
        writer.write("<benchmark-results tstamp=\"" + tstamp() + "\">\n\n");
    }

    private static synchronized void end(Writer writer) throws IOException
    {
        while (autoclose.remove(writer))
        {
            // repeat.
        }
    
        writer.write("</benchmark-results>");
        writer.close();
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

    /*
     * 
     */
    private static synchronized void initShutdownAgent()
    {
        if (shutdownAgent == null)
        {
            shutdownAgent = new Thread()
            {
                public void run()
                {
                    for (Writer w : new ArrayList<Writer>(autoclose))
                    {
                        try
                        {
                            end(w);
                        }
                        catch (IOException e)
                        {
                            // Ignore, not much to do.
                        }
                    }
                }
            };
            Runtime.getRuntime().addShutdownHook(shutdownAgent);
        }
    }
}
