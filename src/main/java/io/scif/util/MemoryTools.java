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

package io.scif.util;

/**
 * A utility class with convenience methods to obtain information about the
 * heap.
 *
 * @author Johannes Schindelin
 */
public class MemoryTools {

	/**
	 * Returns the total amount of remaining memory.
	 * <p>
	 * Slightly tricky: totalMemory() returns the amount of RAM claimed currently,
	 * not the maximum amount Java will claim when asked (that is maxMemory()
	 * instead). Likewise, freeMemory() returns the number of free bytes <i>in the
	 * currently claimed chunk of RAM</i>, not the number of bytes still available
	 * for Java.
	 * </p>
	 * <p>
	 * Therefore, a little arithmetic is required to obtain the real number of
	 * available bytes for us to use.
	 * </p>
	 *
	 * @return the amount of memory available for data
	 */
	public static long totalAvailableMemory() {
		final Runtime rt = Runtime.getRuntime();
		return rt.freeMemory() + rt.maxMemory() - rt.totalMemory();
	}

}
