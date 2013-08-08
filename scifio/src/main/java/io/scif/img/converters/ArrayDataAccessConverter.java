/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2013 Open Microscopy Environment:
 *   - Board of Regents of the University of Wisconsin-Madison
 *   - Glencoe Software, Inc.
 *   - University of Dundee
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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package io.scif.img.converters;

import io.scif.Reader;
import io.scif.img.ImgOptions;
import io.scif.img.cell.loaders.BitArrayLoader;
import io.scif.img.cell.loaders.ByteArrayLoader;
import io.scif.img.cell.loaders.CharArrayLoader;
import io.scif.img.cell.loaders.DoubleArrayLoader;
import io.scif.img.cell.loaders.FloatArrayLoader;
import io.scif.img.cell.loaders.IntArrayLoader;
import io.scif.img.cell.loaders.LongArrayLoader;
import io.scif.img.cell.loaders.ShortArrayLoader;
import net.imglib2.img.ImgPlus;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.basictypeaccess.array.BitArray;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.basictypeaccess.array.CharArray;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.img.basictypeaccess.array.LongArray;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.type.numeric.RealType;

import org.scijava.plugin.Plugin;

/**
 * {@link PlaneConverter} implementation specialized for populating
 * {@link ArrayImg} instances.
 * 
 * @author Mark Hiner
 */
@Plugin(type = PlaneConverter.class, name = "ArrayDataAccess")
public class ArrayDataAccessConverter extends AbstractPlaneConverter {

	public <T extends RealType<T>> void populatePlane(final Reader reader,
		final int imageIndex, final int planeIndex, final byte[] source,
		final ImgPlus<T> dest, final ImgOptions imgOptions)
	{
		final ArrayImg<?, ?> arrayImg = (ArrayImg<?, ?>) dest.getImg();

		final Object store = arrayImg.update(null);

		// FIXME actually do need to pass a reader w/ metadata to the loader

		// FIXME loaders are faster than byte buffers but of course slower than a
		// direct system.arraycopy call, and slower still than passing the array
		// directly.
		// however that simply may not be feasible given the variety of data types.

		if (store instanceof BitArray) {
			final BitArrayLoader loader = new BitArrayLoader(reader, imgOptions.getRegion());
			loader.convertBytes((BitArray) store, source, planeIndex);
		}
		else if (store instanceof ByteArray) {
			final ByteArrayLoader loader = new ByteArrayLoader(reader, imgOptions.getRegion());
			loader.convertBytes((ByteArray) store, source, planeIndex);
		}
		else if (store instanceof ShortArray) {
			final ShortArrayLoader loader = new ShortArrayLoader(reader, imgOptions.getRegion());
			loader.convertBytes((ShortArray) store, source, planeIndex);
		}
		else if (store instanceof LongArray) {
			final LongArrayLoader loader = new LongArrayLoader(reader, imgOptions.getRegion());
			loader.convertBytes((LongArray) store, source, planeIndex);
		}
		else if (store instanceof CharArray) {
			final CharArrayLoader loader = new CharArrayLoader(reader, imgOptions.getRegion());
			loader.convertBytes((CharArray) store, source, planeIndex);
		}
		else if (store instanceof DoubleArray) {
			final DoubleArrayLoader loader = new DoubleArrayLoader(reader, imgOptions.getRegion());
			loader.convertBytes((DoubleArray) store, source, planeIndex);
		}
		else if (store instanceof FloatArray) {
			final FloatArrayLoader loader = new FloatArrayLoader(reader, imgOptions.getRegion());
			loader.convertBytes((FloatArray) store, source, planeIndex);
		}
		else if (store instanceof IntArray) {
			final IntArrayLoader loader = new IntArrayLoader(reader, imgOptions.getRegion());
			loader.convertBytes((IntArray) store, source, planeIndex);
		}

	}

}
