package com.carrotsearch.junitbenchmarks;

public interface TimeRecorder {

   void startTest();
   
   void stopTest();
   
   int startTestIteration();
   
   void stopTestIteration(int id);
}
