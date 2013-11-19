package com.carrotsearch.junitbenchmarks;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class BenchmarkInvokeMethod extends Statement{
   
   private final FrameworkMethod fTestMethod;
   private Object fTarget;
   private TimeRecorder timeRecorder;

   public BenchmarkInvokeMethod(FrameworkMethod testMethod, Object target) {
       fTestMethod = testMethod;
       fTarget = target;
   }
   
   public void setTimeRecorder(TimeRecorder timeRecorder){
      this.timeRecorder = timeRecorder;
   }

   @Override
   public void evaluate() throws Throwable {
       final int id = timeRecorder.startTestIteration();
       fTestMethod.invokeExplosively(fTarget);
       timeRecorder.stopTestIteration(id);
   }
}
