package com.carrotsearch.junitbenchmarks;

interface SingleResult {

   public long gcTime();

   public long evaluationTime();

   public long blockTime();

}