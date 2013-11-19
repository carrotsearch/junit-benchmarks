package com.carrotsearch.junitbenchmarks;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

public class BenchmarkRunner extends BlockJUnit4ClassRunner {

   public BenchmarkRunner(Class<?> klass) throws InitializationError {
      super(klass);
   }
   
   protected Statement methodInvoker(FrameworkMethod method, Object test) {
      final BenchmarkInvokeMethod benchmarkInvokeMethod = new BenchmarkInvokeMethod(method, test);
      TestRepository.addTest(new TestId(test.getClass().getName(), method.getName()), benchmarkInvokeMethod);
      return benchmarkInvokeMethod;
  }

}
