package com.carrotsearch.junitbenchmarks;

import com.carrotsearch.junitbenchmarks.h2.H2Consumer;
import com.carrotsearch.junitbenchmarks.mysql.MySQLConsumer;

/**
 * Shortcuts for known {@link IResultsConsumer}.
 */
public enum ConsumerName
{
    XML(XMLConsumer.class),
    H2(H2Consumer.class),
    CONSOLE(WriterConsumer.class),
    MYSQL(MySQLConsumer.class);

    /** 
     * Consumer class.
     */
    public final Class<? extends IResultsConsumer> clazz;

    /*
     * 
     */
    private ConsumerName(Class<? extends IResultsConsumer> clazz)
    {
        this.clazz = clazz;
    }
}
