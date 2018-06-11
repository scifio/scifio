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

package io.scif.img.converters;

import io.scif.Reader;
import io.scif.config.SCIFIOConfig;
import io.scif.img.cell.loaders.ByteAccessLoader;
import io.scif.img.cell.loaders.ByteArrayLoader;
import io.scif.img.cell.loaders.CharAccessLoader;
import io.scif.img.cell.loaders.CharArrayLoader;
import io.scif.img.cell.loaders.DoubleAccessLoader;
import io.scif.img.cell.loaders.DoubleArrayLoader;
import io.scif.img.cell.loaders.FloatAccessLoader;
import io.scif.img.cell.loaders.FloatArrayLoader;
import io.scif.img.cell.loaders.IntAccessLoader;
import io.scif.img.cell.loaders.IntArrayLoader;
import io.scif.img.cell.loaders.LongAccessLoader;
import io.scif.img.cell.loaders.LongArrayLoader;
import io.scif.img.cell.loaders.ShortAccessLoader;
import io.scif.img.cell.loaders.ShortArrayLoader;

import net.imagej.ImgPlus;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.basictypeaccess.ByteAccess;
import net.imglib2.img.basictypeaccess.CharAccess;
import net.imglib2.img.basictypeaccess.DoubleAccess;
import net.imglib2.img.basictypeaccess.FloatAccess;
import net.imglib2.img.basictypeaccess.IntAccess;
import net.imglib2.img.basictypeaccess.LongAccess;
import net.imglib2.img.basictypeaccess.ShortAccess;
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
 * @author Philipp Hanslovsky
 */
@Plugin(type = PlaneConverter.class, name = "ArrayDataAccess")
public class ArrayDataAccessConverter extends AbstractPlaneConverter {

	@Override
	public <T extends RealType<T>> void populatePlane(final Reader reader,
		final int imageIndex, final int planeIndex, final byte[] source,
		final ImgPlus<T> dest, final SCIFIOConfig config)
	{
		final ArrayImg<?, ?> arrayImg = (ArrayImg<?, ?>) dest.getImg();

		final Object store = arrayImg.update(null);

		// FIXME actually do need to pass a reader w/ metadata to the loader

		// FIXME loaders are faster than byte buffers but of course slower than
		// a
		// direct system.arraycopy call, and slower still than passing the array
		// directly.
		// however that simply may not be feasible given the variety of data
		// types.

		if (store instanceof ByteArray) {
			final ByteArrayLoader loader = new ByteArrayLoader(reader, config
				.imgOpenerGetRegion());
			loader.convertBytes((ByteArray) store, source, planeIndex);
		}
		else if (store instanceof ShortArray) {
			final ShortArrayLoader loader = new ShortArrayLoader(reader, config
				.imgOpenerGetRegion());
			loader.convertBytes((ShortArray) store, source, planeIndex);
		}
		else if (store instanceof LongArray) {
			final LongArrayLoader loader = new LongArrayLoader(reader, config
				.imgOpenerGetRegion());
			loader.convertBytes((LongArray) store, source, planeIndex);
		}
		else if (store instanceof CharArray) {
			final CharArrayLoader loader = new CharArrayLoader(reader, config
				.imgOpenerGetRegion());
			loader.convertBytes((CharArray) store, source, planeIndex);
		}
		else if (store instanceof DoubleArray) {
			final DoubleArrayLoader loader = new DoubleArrayLoader(reader, config
				.imgOpenerGetRegion());
			loader.convertBytes((DoubleArray) store, source, planeIndex);
		}
		else if (store instanceof FloatArray) {
			final FloatArrayLoader loader = new FloatArrayLoader(reader, config
				.imgOpenerGetRegion());
			loader.convertBytes((FloatArray) store, source, planeIndex);
		}
		else if (store instanceof IntArray) {
			final IntArrayLoader loader = new IntArrayLoader(reader, config
				.imgOpenerGetRegion());
			loader.convertBytes((IntArray) store, source, planeIndex);
		}
		else if (store instanceof ByteAccess) {
			final ByteAccessLoader loader = new ByteAccessLoader(reader, config
				.imgOpenerGetRegion(), ByteArray::new);
			loader.convertBytes((ByteAccess) store, source, planeIndex);

		}
		else if (store instanceof ShortAccess) {
			final ShortAccessLoader loader = new ShortAccessLoader(reader, config
				.imgOpenerGetRegion(), ShortArray::new);
			loader.convertBytes((ShortAccess) store, source, planeIndex);
		}
		else if (store instanceof LongAccess) {
			final LongAccessLoader loader = new LongAccessLoader(reader, config
				.imgOpenerGetRegion(), LongArray::new);
			loader.convertBytes((LongAccess) store, source, planeIndex);
		}
		else if (store instanceof CharAccess) {
			final CharAccessLoader loader = new CharAccessLoader(reader, config
				.imgOpenerGetRegion(), CharArray::new);
			loader.convertBytes((CharAccess) store, source, planeIndex);
		}
		else if (store instanceof DoubleAccess) {
			final DoubleAccessLoader loader = new DoubleAccessLoader(reader, config
				.imgOpenerGetRegion(), DoubleArray::new);
			loader.convertBytes((DoubleAccess) store, source, planeIndex);
		}
		else if (store instanceof FloatAccess) {
			final FloatAccessLoader loader = new FloatAccessLoader(reader, config
				.imgOpenerGetRegion(), FloatArray::new);
			loader.convertBytes((FloatAccess) store, source, planeIndex);
		}
		else if (store instanceof IntAccess) {
			final IntAccessLoader loader = new IntAccessLoader(reader, config
				.imgOpenerGetRegion(), IntArray::new);
			loader.convertBytes((IntAccess) store, source, planeIndex);
		}

	}

}
