/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2018 SCIFIO developers.
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

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.type.numeric.RealType;

public class ImageHash {

	/**
	 * @param img iterable interval to hash
	 * @return the hexcode of SHA-1 hash over the interval's double values
	 */
	public static String hashImg(
		@SuppressWarnings("rawtypes") final IterableInterval img)
	{
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-1");
		}
		catch (NoSuchAlgorithmException exc) {
			throw new IllegalStateException();
		}
		final ByteBuffer buffer = ByteBuffer.allocate(5000);

		@SuppressWarnings("unchecked")
		final Cursor<? extends RealType<?>> c = img.cursor();
		while (c.hasNext()) {
			if (buffer.remaining() > 8) {
				buffer.putDouble(c.next().getRealDouble());
			}
			else {
				// flush
				digest.update(buffer.array());
				buffer.rewind();
			}
		}
		return convertByteArrayToHexString(digest.digest());
	}

	private static String convertByteArrayToHexString(final byte[] arrayBytes) {
		final StringBuilder builder = new StringBuilder();
		for (int i = 0; i < arrayBytes.length; i++) {
			builder.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16)
				.substring(1));
		}
		return builder.toString();
	}
}
