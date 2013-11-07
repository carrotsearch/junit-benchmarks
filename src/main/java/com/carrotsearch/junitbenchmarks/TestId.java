package com.carrotsearch.junitbenchmarks;

public class TestId {
   
   private final Class<?> testClass;
   
   private final String testMethodName;

   public TestId(Class<?> testClass, String testMethodName) {
      this.testClass = testClass;
      this.testMethodName = testMethodName;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((testClass == null) ? 0 : testClass.hashCode());
      result = prime * result + ((testMethodName == null) ? 0 : testMethodName.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      TestId other = (TestId) obj;
      if (testClass == null) {
         if (other.testClass != null) {
            return false;
         }
      } else if (!testClass.equals(other.testClass)) {
         return false;
      }
      if (testMethodName == null) {
         if (other.testMethodName != null) {
            return false;
         }
      } else if (!testMethodName.equals(other.testMethodName)) {
         return false;
      }
      return true;
   }
   
   

}
