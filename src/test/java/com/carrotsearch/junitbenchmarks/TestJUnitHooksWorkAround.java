package com.carrotsearch.junitbenchmarks;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runners.model.Statement;

public class TestJUnitHooksWorkAround
{
    public static class BeforeAfterRules {
        public static int beforeCalls;
        public static int afterCalls;
        public static int methodRuleCalls;

        @Rule
        public TestRule benchmarkRun = RuleChain
            .outerRule(new TestRule() {
                public Statement apply(final Statement base, Description description)
                {
                    return new Statement()
                    {
                        public void evaluate() throws Throwable
                        {
                            before();
                            try {
                                methodRuleCalls++;
                                base.evaluate();
                            } finally {
                                after();
                            }
                        }
                    };
                }
            })
            .around(new BenchmarkRule());

        // DO NOT USE @Before, call from the RuleChain
        // @Before
        public void before() {
            beforeCalls++;
        }

        // DO NOT USE @After, call from the RuleChain
        // @After
        public void after() {
            afterCalls++;
        }

        @BenchmarkOptions(warmupRounds = 10, benchmarkRounds = 10)
        @Test
        public void testFoo() throws Exception
        {
            Thread.sleep(10);
        }

        @BeforeClass
        public static void reset() {
            methodRuleCalls = 
                beforeCalls = 
                afterCalls  = 0;
        }
    }
    
    @Test
    public void testBefore() {
        JUnitCore.runClasses(BeforeAfterRules.class);
        Assert.assertEquals(1, BeforeAfterRules.beforeCalls);
    }
    
    @Test
    public void testAfter() {
        JUnitCore.runClasses(BeforeAfterRules.class);
        Assert.assertEquals(1, BeforeAfterRules.afterCalls);
    }


    @Test
    public void testMethodRuleCalls() {
        JUnitCore.runClasses(BeforeAfterRules.class);
        Assert.assertEquals(1, BeforeAfterRules.methodRuleCalls);
    }
}
