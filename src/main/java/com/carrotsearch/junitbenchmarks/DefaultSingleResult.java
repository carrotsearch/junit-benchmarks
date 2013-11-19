package com.carrotsearch.junitbenchmarks;

/**
 * A result of a single test.
 */
class DefaultSingleResult implements SingleResult
{
    private final long startTime;
    private final long afterGC;
    private final long endTime;
    private final long blockTime;

    public DefaultSingleResult(long startTime, long afterGC, long endTime, long blockTime)
    {
        this.startTime = startTime;
        this.afterGC = afterGC;
        this.endTime = endTime;
        this.blockTime = blockTime;
    }

    /* (non-Javadoc)
    * @see com.carrotsearch.junitbenchmarks.SingleResult#gcTime()
    */
   @Override
   public long gcTime()
    {
        return afterGC - startTime;
    }

    /* (non-Javadoc)
    * @see com.carrotsearch.junitbenchmarks.SingleResult#evaluationTime()
    */
   @Override
   public long evaluationTime()
    {
        return endTime - afterGC;
    }
    
    /* (non-Javadoc)
    * @see com.carrotsearch.junitbenchmarks.SingleResult#blockTime()
    */
   @Override
   public long blockTime(){
       return blockTime;
    }
}