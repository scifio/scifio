/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2016 Board of Regents of the University of
 * Wisconsin-Madison
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

package io.scif.img.cell.loaders;

import io.scif.ImageMetadata;
import io.scif.Reader;
import io.scif.img.ImageRegion;
import io.scif.util.FormatTools;

import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.type.numeric.integer.GenericByteType;

/**
 * {@link SCIFIOArrayLoader} implementation for {@link ByteArray} types.
 *
 * @author Mark Hiner
 */
public class ByteArrayLoader extends AbstractArrayLoader<ByteArray> {

	public ByteArrayLoader(final Reader reader, final ImageRegion subRegion) {
		super(reader, subRegion);
	}

	@Override
	public void convertBytes(final ByteArray data, final byte[] bytes,
		final int planesRead)
	{
		if (isCompatible()) {
			final int offset = planesRead * bytes.length;

			System.arraycopy(bytes, 0, data.getCurrentStorageArray(), offset,
				bytes.length);
		}
		else {
			final ImageMetadata iMeta = reader().getMetadata().get(0);
			final int pixelType = iMeta.getPixelType();
			final int bpp = FormatTools.getBytesPerPixel(pixelType);
			final int offset = planesRead * (bytes.length / bpp);

			for (int index = 0; index < bytes.length / bpp; index++) {
				final byte value =
					(byte) utils().decodeWord(bytes, index * bpp, pixelType,
						iMeta.isLittleEndian());
				data.setValue(offset + index, value);
			}
		}
	}

	@Override
	public ByteArray emptyArray(final int entities) {
		return new ByteArray(entities);
	}

	@Override
	public int getBitsPerElement() {
		return 8;
	}

	@Override
	public Class<?> outputClass() {
		return GenericByteType.class;
	}
}
