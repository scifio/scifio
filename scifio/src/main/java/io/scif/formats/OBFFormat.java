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

import io.scif.AbstractChecker;
import io.scif.AbstractFormat;
import io.scif.AbstractMetadata;
import io.scif.AbstractParser;
import io.scif.ByteArrayPlane;
import io.scif.ByteArrayReader;
import io.scif.Format;
import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.io.RandomAccessInputStream;
import io.scif.util.FormatTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import net.imglib2.meta.Axes;

import org.scijava.plugin.Plugin;

/**
 * OBFReader is the file format reader for Imspector OBF files.
 * 
 * @author Bjoern Thiel
 */
@Plugin(type = Format.class)
public class OBFFormat extends AbstractFormat {

	// -- Format API Methods --

	/*
	 * @see io.scif.Format#getFormatName()
	 */
	public String getFormatName() {
		return "OBF";
	}

	/*
	 * @see io.scif.Format#getSuffixes()
	 */
	public String[] getSuffixes() {
		return new String[] { "obf", "msr" };
	}

	// -- Nested classes --

	/**
	 * @author Mark Hiner
	 */
	public static class Metadata extends AbstractMetadata {

		// -- Constants --

		public static final String CNAME = "io.scif.formats.OBFFormat$Metadata";

		// -- Fields --

		private Frame currentInflatedFrame = new Frame();
		private Inflater inflater = new Inflater();
		private List<Stack> stacks = new ArrayList<Stack>();

		// -- OBFMetadata getters and setters --

		public Frame getCurrentInflatedFrame() {
			return currentInflatedFrame;
		}

		public void setCurrentInflatedFrame(final Frame currentInflatedFrame) {
			this.currentInflatedFrame = currentInflatedFrame;
		}

		public Inflater getInflater() {
			return inflater;
		}

		public void setInflater(final Inflater inflater) {
			this.inflater = inflater;
		}

		public List<Stack> getStacks() {
			return stacks;
		}

		public void setStacks(final List<Stack> stacks) {
			this.stacks = stacks;
		}

		// -- Metadata API Methods --

		/*
		 * @see io.scif.Metadata#populateImageMetadata()
		 */
		public void populateImageMetadata() {
			final ImageMetadata iMeta = get(0);

			iMeta.setIndexed(false);
			iMeta.setRGB(false);
			iMeta.setInterleaved(false);
			iMeta.setPlaneCount(iMeta.getAxisLength(Axes.Z) *
				iMeta.getAxisLength(Axes.TIME) * iMeta.getAxisLength(Axes.CHANNEL));
			iMeta.setOrderCertain(false);
		}

		@Override
		public void close(final boolean fileOnly) throws IOException {
			stacks = new ArrayList<Stack>();
			currentInflatedFrame = new Frame();
			inflater = new Inflater();

			super.close(fileOnly);
		}
	}

	public static class Checker extends AbstractChecker {

		// -- Constants --

		private static final int FILE_VERSION = 1;

		// -- Constructor --

		public Checker() {
			suffixNecessary = false;
			suffixSufficient = false;
		}

		// -- Checker API Methods --

		@Override
		public boolean isFormat(final RandomAccessInputStream stream)
			throws IOException
		{
			final int fileVersion = OBFUtilities.getFileVersion(stream);

			return fileVersion >= 0 && fileVersion <= FILE_VERSION;
		}
	}

	/**
	 * @author Mark Hiner
	 */
	public static class Parser extends AbstractParser<Metadata> {

		private static final int STACK_VERSION = 3;
		private static final String STACK_MAGIC_STRING = "OMAS_BF_STACK\n";
		private static final int MAXIMAL_NUMBER_OF_DIMENSIONS = 15;

		// -- AbstractParser API Methods --

		@Override
		protected void typedParse(final RandomAccessInputStream stream,
			final Metadata meta) throws IOException, FormatException
		{
			meta.getCurrentInflatedFrame().setImageIndex(-1);
			meta.getCurrentInflatedFrame().setNumber(-1);

			final int fileVersion = OBFUtilities.getFileVersion(stream);

			long stackPosition = stream.readLong();

			final int lengthOfDescription = stream.readInt();
			final String description = stream.readString(lengthOfDescription);
			this.addGlobalMeta("Description", description);

			if (stackPosition != 0) {
				meta.createImageMetadata(1);
				do {
					stackPosition = initStack(stackPosition, fileVersion);
				}
				while (stackPosition != 0);
			}
		}

		// -- Helper Methods --

		private long initStack(final long current, final int fileVersion)
			throws FormatException, IOException
		{
			in.seek(current);

			final String magicString = in.readString(STACK_MAGIC_STRING.length());
			final short magicNumber = in.readShort();
			final int version = in.readInt();

			if (magicString.equals(STACK_MAGIC_STRING) &&
				magicNumber == OBFUtilities.MAGIC_NUMBER && version <= STACK_VERSION)
			{
				final ImageMetadata iMeta = metadata.get(0);

				iMeta.setLittleEndian(OBFUtilities.LITTLE_ENDIAN);
				iMeta.setThumbnail(false);

				final int numberOfDimensions = in.readInt();
				if (numberOfDimensions > 5) {
					throw new FormatException("Unsupported number of " +
						numberOfDimensions + " dimensions");
				}

				final int[] sizes = new int[MAXIMAL_NUMBER_OF_DIMENSIONS];
				for (int dimension = 0; dimension != MAXIMAL_NUMBER_OF_DIMENSIONS; ++dimension)
				{
					final int size = in.readInt();
					sizes[dimension] = dimension < numberOfDimensions ? size : 1;
				}

				iMeta.setAxisLength(Axes.X, sizes[0]);
				iMeta.setAxisLength(Axes.Y, sizes[1]);
				iMeta.setAxisLength(Axes.Z, sizes[2]);
				iMeta.setAxisLength(Axes.CHANNEL, sizes[3]);
				iMeta.setAxisLength(Axes.TIME, sizes[4]);

				final List<Double> lengths = new ArrayList<Double>();
				for (int dimension = 0; dimension != MAXIMAL_NUMBER_OF_DIMENSIONS; ++dimension)
				{
					final double length = in.readDouble();
					if (dimension < numberOfDimensions) {
						lengths.add(new Double(length));
					}
				}
				iMeta.getTable().put("Lengths", lengths);

				final List<Double> offsets = new ArrayList<Double>();
				for (int dimension = 0; dimension != MAXIMAL_NUMBER_OF_DIMENSIONS; ++dimension)
				{
					final double offset = in.readDouble();
					if (dimension < numberOfDimensions) {
						offsets.add(new Double(offset));
					}
				}
				iMeta.getTable().put("Offsets", offsets);

				final int type = in.readInt();
				iMeta.setPixelType(OBFUtilities.getPixelType(type));
				iMeta.setPixelType(OBFUtilities.getBitsPerPixel(type));

				final Stack stack = new Stack();

				final int compression = in.readInt();
				stack.setCompression(getCompression(compression));

				in.skipBytes(4);

				final int lengthOfName = in.readInt();
				final int lengthOfDescription = in.readInt();

				in.skipBytes(8);

				final long lengthOfData = in.readLong();
				stack.setLength(getLength(lengthOfData));

				final long next = in.readLong();

				final String name = in.readString(lengthOfName);
				iMeta.getTable().put("Name", name);
				final String description = in.readString(lengthOfDescription);
				iMeta.getTable().put("Description", description);

				stack.setPosition(in.getFilePointer());

				metadata.getStacks().add(stack);

				if (fileVersion >= 1) {
					in.skip(lengthOfData);

					final long footer = in.getFilePointer();
					final int offset = in.readInt();

					final List<Boolean> stepsPresent = new ArrayList<Boolean>();
					for (int dimension = 0; dimension != MAXIMAL_NUMBER_OF_DIMENSIONS; ++dimension)
					{
						final int present = in.readInt();
						if (dimension < numberOfDimensions) {
							stepsPresent.add(new Boolean(present != 0));
						}
					}
					final List<Boolean> stepLabelsPresent = new ArrayList<Boolean>();
					for (int dimension = 0; dimension != MAXIMAL_NUMBER_OF_DIMENSIONS; ++dimension)
					{
						final int present = in.readInt();
						if (dimension < numberOfDimensions) {
							stepLabelsPresent.add(new Boolean(present != 0));
						}
					}

					in.seek(footer + offset);

					final List<String> labels = new ArrayList<String>();
					for (int dimension = 0; dimension != numberOfDimensions; ++dimension)
					{
						final int length = in.readInt();
						final String label = in.readString(length);
						labels.add(label);
					}
					iMeta.getTable().put("Labels", labels);

					final List<List<Double>> steps = new ArrayList<List<Double>>();
					for (int dimension = 0; dimension != numberOfDimensions; ++dimension)
					{
						final List<Double> list = new ArrayList<Double>();
						if (stepsPresent.get(dimension)) {
							for (int position = 0; position != sizes[dimension]; ++position) {
								final double step = in.readDouble();
								list.add(new Double(step));
							}
						}
						steps.add(list);
					}
					iMeta.getTable().put("Steps", steps);

					final List<List<String>> stepLabels = new ArrayList<List<String>>();
					for (int dimension = 0; dimension != numberOfDimensions; ++dimension)
					{
						final List<String> list = new ArrayList<String>();
						if (stepLabelsPresent.get(dimension)) {
							for (int position = 0; position != sizes[dimension]; ++position) {
								final int length = in.readInt();
								final String label = in.readString(length);
								list.add(label);
							}
						}
						stepLabels.add(list);
					}
					iMeta.getTable().put("StepLabels", stepLabels);
				}
				return next;
			}
			throw new FormatException("Unsupported stack format");
		}

		private long getLength(final long length) throws FormatException {
			if (length >= 0) return length;
			throw new FormatException("Negative stack length on disk");
		}

		private boolean getCompression(final int compression)
			throws FormatException
		{
			switch (compression) {
				case 0:
					return false;
				case 1:
					return true;
				default:
					throw new FormatException("Unsupported compression " + compression);
			}
		}
	}

	/**
	 * @author Mark Hiner
	 */
	public static class Reader extends ByteArrayReader<Metadata> {

		// -- Reader API Methods --

		public ByteArrayPlane openPlane(final int imageIndex, final int planeIndex,
			final ByteArrayPlane plane, final int x, final int y, final int w,
			final int h) throws FormatException, IOException
		{
			final Metadata meta = getMetadata();
			final byte[] buffer = plane.getBytes();

			final int rows = meta.getAxisLength(imageIndex, Axes.Y);
			final int columns = meta.getAxisLength(imageIndex, Axes.X);
			final int bytesPerPixel = meta.getBitsPerPixel(imageIndex) / 8;

			final Stack stack = meta.getStacks().get(imageIndex);
			if (stack.isCompression()) {
				final Frame cInflatedFrame = meta.getCurrentInflatedFrame();

				if (imageIndex != cInflatedFrame.getImageIndex()) {
					cInflatedFrame.setBytes(new byte[rows * columns * bytesPerPixel]);
					cInflatedFrame.setImageIndex(imageIndex);
					cInflatedFrame.setNumber(-1);
				}

				final byte[] bytes = cInflatedFrame.getBytes();
				if (planeIndex != cInflatedFrame.getNumber()) {
					if (planeIndex < cInflatedFrame.getNumber()) {
						cInflatedFrame.setNumber(-1);
					}
					if (cInflatedFrame.getNumber() == -1) {
						getStream().seek(stack.getPosition());
						meta.getInflater().reset();
					}

					final byte[] input = new byte[8192];
					while (planeIndex != cInflatedFrame.getNumber()) {
						int offset = 0;
						while (offset != bytes.length) {
							if (meta.getInflater().needsInput()) {
								final long remainder =
									stack.getPosition() + stack.getLength() -
										getStream().getFilePointer();
								if (remainder > 0) {
									final int length =
										remainder > input.length ? input.length : (int) remainder;

									getStream().read(input, 0, length);
									meta.getInflater().setInput(input, 0, length);
								}
								else {
									throw new FormatException("Corrupted zlib compression");
								}
							}
							else if (meta.getInflater().needsDictionary()) {
								throw new FormatException("Unsupported zlib compression");
							}
							try {
								offset +=
									meta.getInflater().inflate(bytes, offset,
										bytes.length - offset);
							}
							catch (final DataFormatException exception) {
								throw new FormatException(exception.getMessage());
							}
						}
						cInflatedFrame.setNumber(cInflatedFrame.getNumber() + 1);
					}
				}
				for (int row = 0; row != h; ++row) {
					System.arraycopy(bytes, ((row + y) * columns + x) * bytesPerPixel,
						buffer, row * w * bytesPerPixel, w * bytesPerPixel);
				}
			}
			else {
				for (int row = 0; row != h; ++row) {
					getStream().seek(
						stack.getPosition() +
							((planeIndex * rows + row + y) * columns + x) * bytesPerPixel);
					getStream().read(buffer, row * w * bytesPerPixel, w * bytesPerPixel);
				}
			}

			return plane;
		}
	}

	// -- Helper Classes --

	private static class OBFUtilities {

		// -- Constants --

		private static final short MAGIC_NUMBER = (short) 0xFFFF;
		private static final boolean LITTLE_ENDIAN = true;
		private static final String FILE_MAGIC_STRING = "OMAS_BF\n";

		// -- Utility methods --

		public static int getPixelType(final int type) throws FormatException {
			switch (type) {
				case 0x01:
					return FormatTools.UINT8;
				case 0x02:
					return FormatTools.INT8;
				case 0x04:
					return FormatTools.UINT16;
				case 0x08:
					return FormatTools.INT16;
				case 0x10:
					return FormatTools.UINT32;
				case 0x20:
					return FormatTools.INT32;
				case 0x40:
					return FormatTools.FLOAT;
				case 0x80:
					return FormatTools.DOUBLE;
				default:
					throw new FormatException("Unsupported data type " + type);
			}
		}

		public static int getBitsPerPixel(final int type) throws FormatException {
			switch (type) {
				case 0x01:
				case 0x02:
					return 8;
				case 0x04:
				case 0x08:
					return 16;
				case 0x10:
				case 0x20:
					return 32;
				case 0x40:
					return 32;
				case 0x80:
					return 64;
				default:
					throw new FormatException("Unsupported data type " + type);
			}
		}

		public static int getFileVersion(final RandomAccessInputStream stream)
			throws IOException
		{
			stream.seek(0);

			stream.order(OBFUtilities.LITTLE_ENDIAN);

			try {
				final String magicString =
					stream.readString(FILE_MAGIC_STRING.length());
				final short magicNumber = stream.readShort();
				final int version = stream.readInt();

				if (magicString.equals(FILE_MAGIC_STRING) &&
					magicNumber == OBFUtilities.MAGIC_NUMBER)
				{
					return version;
				}
			}
			catch (final IOException exception) {}

			return -1;
		}
	}

	private static class Stack {

		// -- Fields --

		private long position;
		private long length;
		private boolean compression;

		// -- Getters and Setters --

		public long getPosition() {
			return position;
		}

		public void setPosition(final long position) {
			this.position = position;
		}

		public long getLength() {
			return length;
		}

		public void setLength(final long length) {
			this.length = length;
		}

		public boolean isCompression() {
			return compression;
		}

		public void setCompression(final boolean compression) {
			this.compression = compression;
		}
	}

	private static class Frame {

		// -- Fields --

		private byte[] bytes;
		private int imageIndex;
		private int number;

		// -- Getters and Setters --

		public byte[] getBytes() {
			return bytes;
		}

		public void setBytes(final byte[] bytes) {
			this.bytes = bytes;
		}

		public int getImageIndex() {
			return imageIndex;
		}

		public void setImageIndex(final int series) {
			this.imageIndex = series;
		}

		public int getNumber() {
			return number;
		}

		public void setNumber(final int number) {
			this.number = number;
		}
	}
}
