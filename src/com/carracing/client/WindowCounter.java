package com.carracing.client;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Allows you to control the number of windows in the program.
 */
public final class WindowCounter {
	
	private static final AtomicInteger COUNT = new AtomicInteger(0);
	
	public static int getCount() {
		return COUNT.get();
	}
	
	public static void incement() {
		COUNT.incrementAndGet();
	}
	
	public static void decrement() {
		COUNT.decrementAndGet();
	}
	
	public static void executeIfZero(final Runnable task) {
		if (getCount() <= 0) task.run();
	}
}