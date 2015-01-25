package io.scif;

public class TicToc {
	public static long startTime;

	public static void tic() {
		startTime = System.currentTimeMillis();
	}

	public static void toc() {
		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.println(String.format("Elapsed time: %d.%03dsec",
					elapsedTime / 1000, elapsedTime % 1000));
		startTime = 1;
	}
}
