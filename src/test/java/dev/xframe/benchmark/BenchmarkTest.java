package dev.xframe.benchmark;

import org.openjdk.jmh.annotations.Benchmark;

import dev.xframe.benchmark.BenchmarkUtil;

public class BenchmarkTest {
	
	@Benchmark
	public int test1() {
		return Math.max(1, 2);
	}
	
	@Benchmark
	public int test2() {
		return Math.min(1, 2);
	}
	
	public static void main(String[] args) throws Throwable {
		BenchmarkUtil.process();
	}

}
