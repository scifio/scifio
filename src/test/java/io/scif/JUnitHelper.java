/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2017 SCIFIO developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package io.scif;

import org.junit.Assert;

public class JUnitHelper {

	private final static double DOUBLE_DELTA = 1.0 / (1l << 52);

	private final static float FLOAT_DELTA = 1.0f / (1l << 23);

	/**
	 * Assert that two floating point numbers are equal up to a very small delta.
	 * <p>
	 * This method helps with JUnit's removal of the
	 * {@link Assert#assertEquals(double, double)} functionality.
	 * </p>
	 *
	 * @param expected expected value
	 * @param actual the value to check against expected
	 */
	public static void assertCloseEnough(final double expected,
		final double actual)
	{
		Assert.assertEquals(expected, actual, DOUBLE_DELTA);
	}

	/**
	 * Assert that two floating point numbers are equal up to a very small delta.
	 * <p>
	 * This method helps with JUnit's removal of the
	 * {@link Assert#assertEquals(double, double)} functionality.
	 * </p>
	 *
	 * @param expected expected value
	 * @param actual the value to check against expected
	 */
	public static void assertCloseEnough(final float expected,
		final float actual)
	{
		Assert.assertEquals(expected, actual, FLOAT_DELTA);
	}

}
