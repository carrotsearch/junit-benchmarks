package com.carrotsearch.junitbenchmarks.examples;

import java.util.ArrayList;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;

public class BeforeAfterChaining
{
	public static abstract class BeforeAfterRule implements TestRule
	{
		@Override
		public final Statement apply(final Statement delegate, Description description)
		{
			return new Statement() {
				public void evaluate() throws Throwable
				{
					final ArrayList<Throwable> errors = new ArrayList<Throwable>();

					try
					{
						before();
						delegate.evaluate();
					}
					catch (Throwable t)
					{
						errors.add(t);
					}

					try
					{
						after();
					}
					catch (Throwable t)
					{
						errors.add(t);
					}

					MultipleFailureException.assertEmpty(errors);
				}
			};
		}

		protected void after() throws Exception {};
		protected void before() throws Exception {};		
	}

	/**
	 * Create a rule chain to have the setup/ cleanup code run exactly once per
	 * benchmarked test.
	 */
	@Rule
	public RuleChain chain = RuleChain
		.outerRule(new BeforeAfterRule() {
			protected void before() {
				System.out.println("Setup before benchmarked test.");
			};
			protected void after() {
				System.out.println("Teardown after benchmarked test.");
			};
		})
		.around(new BenchmarkRule());

	@BenchmarkOptions(benchmarkRounds = 20, warmupRounds = 0)
	@Test
	public void test100millis() throws Exception
	{
		Thread.sleep(100);
	}
	
	@BenchmarkOptions(benchmarkRounds = 20, warmupRounds = 0)
	@Test
	public void test200millis() throws Exception
	{
		Thread.sleep(200);
	}	
}