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

package io.scif.formats;

import io.scif.AbstractFormat;
import io.scif.AbstractWriter;
import io.scif.DefaultMetadata;
import io.scif.Format;
import io.scif.FormatException;
import io.scif.Metadata;
import io.scif.Plane;
import io.scif.common.DataTools;
import io.scif.io.RandomAccessOutputStream;
import io.scif.util.FormatTools;
import io.scif.util.SCIFIOMetadataTools;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.img.basictypeaccess.array.LongArray;
import net.imglib2.img.basictypeaccess.array.ShortArray;

import org.scijava.plugin.Plugin;

/**
 * Format for Java source code. At the moment, this code just writes a very
 * simple container for pixel data.
 */
@Plugin(type = Format.class)
public class JavaFormat extends AbstractFormat {

	// -- Format API Methods --

	@Override
	public String getFormatName() {
		return "Java source code";
	}

	@Override
	public String[] getSuffixes() {
		return new String[] { "java" };
	}

	// -- Nested classes --

	/**
	 * @author Mark Hiner
	 */
	public static class Writer extends AbstractWriter<DefaultMetadata> {

		// -- Writer API methods --

		@Override
		public void setDest(final RandomAccessOutputStream stream,
			final int imageIndex) throws FormatException, IOException
		{
			super.setDest(stream, imageIndex);
			if (out.length() == 0) writeHeader();
		}

		@Override
		public void savePlane(final int imageIndex, final int planeIndex,
			final Plane plane, final long[] planeMin, final long[] planeMax)
			throws FormatException, IOException
		{
			final byte[] buf = plane.getBytes();
			final Metadata meta = getMetadata();

			checkParams(imageIndex, planeIndex, buf, planeMin, planeMax);
			if (!SCIFIOMetadataTools.wholePlane(imageIndex, meta, planeMin, planeMax))
			{
				throw new FormatException(
					"JavaWriter does not yet support saving image tiles.");
			}

			// check pixel type
			final String pixelType =
				FormatTools.getPixelTypeString(meta.getPixelType(imageIndex));
			final int type = FormatTools.pixelTypeFromString(pixelType);
			if (!DataTools.containsValue(getPixelTypes(), type)) {
				throw new FormatException("Unsupported image type '" + pixelType + "'.");
			}
			final int bpp = FormatTools.getBytesPerPixel(type);
			final boolean fp = FormatTools.isFloatingPoint(type);
			final boolean little =
				Boolean.FALSE.equals(!meta.isLittleEndian(imageIndex));

			// write array
			final String varName = "image" + imageIndex + "Plane" + planeIndex;
			final Object array = DataTools.makeDataArray(buf, bpp, fp, little);

			out.seek(out.length());
			writePlane(varName, getType(array), (int)planeMax[0], (int)planeMax[1]);

		}

		@Override
		public boolean canDoStacks() {
			return true;
		}

		@Override
		public int[] getPixelTypes(final String codec) {
			return new int[] { FormatTools.INT8, FormatTools.UINT8,
				FormatTools.UINT16, FormatTools.UINT32, FormatTools.INT32,
				FormatTools.FLOAT, FormatTools.DOUBLE };
		}

		@Override
		public void close() throws IOException {
			if (out != null) writeFooter();
			super.close();
		}

		// -- Helper methods --

		protected void writeHeader() throws IOException {
			String className =
				metadata.getDatasetName().substring(0,
					metadata.getDatasetName().length() - 5);
			className =
				className.substring(className.lastIndexOf(File.separator) + 1);

			out.writeLine("//");
			out.writeLine("// " + className + ".java");
			out.writeLine("//");
			out.writeLine("");
			out.writeLine("// Generated by SCIFIO v" + scifio().getVersion());
			out.writeLine("// Generated on " + new Date());
			out.writeLine("");
			out.writeLine("public class " + className + " {");
			out.writeLine("");
		}

		protected void writePlane(final String varName, final ArrayPlus type,
			final int w, final int h) throws IOException
		{
			int i = 0;
			out.writeLine("  public " + type.label() + "[][] " + varName + " = {");
			for (int y = 0; y < h; y++) {
				out.writeBytes("    {");
				for (int x = 0; x < w; x++) {
					out.writeBytes(type.value(i++));
					if (x < w - 1) out.writeBytes(", ");
					else out.writeBytes("}");
				}
				if (y < h - 1) out.writeLine(",");
				else out.writeLine("");
			}
			out.writeLine("  };");
			out.writeLine("");
		}

		protected void writeFooter() throws IOException {
			out.writeLine("}");
		}

		private ArrayPlus getType(final Object array) {
			ArrayPlus type = null;
			if (array instanceof byte[]) {
				type = new ByteArrayPlus((byte[]) array);
			}
			else if (array instanceof short[]) {
				type = new ShortArrayPlus((short[]) array);
			}
			else if (array instanceof int[]) {
				type = new IntArrayPlus((int[]) array);
			}
			else if (array instanceof long[]) {
				type = new LongArrayPlus((long[]) array);
			}
			else if (array instanceof float[]) {
				type = new FloatArrayPlus((float[]) array);
			}
			else if (array instanceof double[]) {
				type = new DoubleArrayPlus((double[]) array);
			}
			return type;
		}
	}

	// -- Helper classes --

	private interface ArrayPlus {

		String label();

		String value(int index);
	}

	private static class ByteArrayPlus extends ByteArray implements ArrayPlus {

		public ByteArrayPlus(final byte[] data) {
			super(data);
		}

		@Override
		public String label() {
			return "byte";
		}

		@Override
		public String value(final int index) {
			return String.valueOf(getValue(index));
		}
	}

	private static class IntArrayPlus extends IntArray implements ArrayPlus {

		public IntArrayPlus(final int[] data) {
			super(data);
		}

		@Override
		public String label() {
			return "int";
		}

		@Override
		public String value(final int index) {
			return String.valueOf(getValue(index));
		}
	}

	private static class ShortArrayPlus extends ShortArray implements ArrayPlus {

		public ShortArrayPlus(final short[] data) {
			super(data);
		}

		@Override
		public String label() {
			return "short";
		}

		@Override
		public String value(final int index) {
			return String.valueOf(getValue(index));
		}
	}

	private static class LongArrayPlus extends LongArray implements ArrayPlus {

		public LongArrayPlus(final long[] data) {
			super(data);
		}

		@Override
		public String label() {
			return "long";
		}

		@Override
		public String value(final int index) {
			return String.valueOf(getValue(index));
		}
	}

	private static class FloatArrayPlus extends FloatArray implements ArrayPlus {

		public FloatArrayPlus(final float[] data) {
			super(data);
		}

		@Override
		public String label() {
			return "float";
		}

		@Override
		public String value(final int index) {
			return String.valueOf(getValue(index));
		}
	}

	private static class DoubleArrayPlus extends DoubleArray implements ArrayPlus
	{

		public DoubleArrayPlus(final double[] data) {
			super(data);
		}

		@Override
		public String label() {
			return "double";
		}

		@Override
		public String value(final int index) {
			return String.valueOf(getValue(index));
		}
	}
}
