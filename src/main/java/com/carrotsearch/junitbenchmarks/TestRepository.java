package com.carrotsearch.junitbenchmarks;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class TestRepository {
   
   private static Map<TestId,BenchmarkInvokeMethod> repository;
   
   static{
      repository = Collections.synchronizedMap(new HashMap<TestId,BenchmarkInvokeMethod>());
   }

   public static void addTest(TestId id, BenchmarkInvokeMethod test){
      repository.put(id, test);
   }
   
   public static BenchmarkInvokeMethod getTest(TestId id){
      return repository.get(id);
   }
   
}
