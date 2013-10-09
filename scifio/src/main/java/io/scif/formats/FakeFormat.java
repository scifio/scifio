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
import io.scif.AbstractMetadata;
import io.scif.AbstractParser;
import io.scif.AbstractTranslator;
import io.scif.ByteArrayPlane;
import io.scif.ByteArrayReader;
import io.scif.Format;
import io.scif.FormatException;
import io.scif.HasColorTable;
import io.scif.ImageMetadata;
import io.scif.MetadataService;
import io.scif.Translator;
import io.scif.common.DataTools;
import io.scif.io.IStreamAccess;
import io.scif.io.Location;
import io.scif.io.RandomAccessInputStream;
import io.scif.io.StreamHandle;
import io.scif.util.FormatTools;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Random;

import net.imglib2.display.ColorTable;
import net.imglib2.display.ColorTable16;
import net.imglib2.display.ColorTable8;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.meta.CalibratedAxis;

import org.scijava.Context;
import org.scijava.Priority;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Plugin;

/**
 * FakeFormat is the file format reader for faking input data. It is mainly
 * useful for testing, as image sources can be defined in memory through String
 * notation, without requiring an actual dataset to exist on disk.
 * <p>
 * Examples:
 * </p>
 * 
 * <pre>
 * 'multi-series&series=11&sizeZ=3&sizeC=5&sizeT=7&sizeY=50.fake'
 * '8bit-signed&pixelType=int8&sizeZ=3&sizeC=5&sizeT=7&sizeY=50.fake'
 * '8bit-unsigned&pixelType=uint8&sizeZ=3&sizeC=5&sizeT=7&sizeY=50.fake'
 * '16bit-signed&pixelType=int16&sizeZ=3&sizeC=5&sizeT=7&sizeY=50.fake'
 * '16bit-unsigned&pixelType=uint16&sizeZ=3&sizeC=5&sizeT=7&sizeY=50.fake'
 * '32bit-signed&pixelType=int32&sizeZ=3&sizeC=5&sizeT=7&sizeY=50.fake'
 * '32bit-unsigned&pixelType=uint32&sizeZ=3&sizeC=5&sizeT=7&sizeY=50.fake'
 * '32bit-floating&pixelType=float&sizeZ=3&sizeC=5&sizeT=7&sizeY=50.fake'
 * '64bit-floating&pixelType=double&sizeZ=3&sizeC=5&sizeT=7&sizeY=50.fake'
 * </pre>
 */
@Plugin(type = Format.class)
public class FakeFormat extends AbstractFormat {

	// -- Constants --

	public static final int BOX_SIZE = 10;
	public static final int DEFAULT_SIZE_X = 512;
	public static final int DEFAULT_SIZE_Y = 512;
	public static final int DEFAULT_SIZE_Z = 1;
	public static final int DEFAULT_SIZE_C = 1;
	public static final int DEFAULT_SIZE_T = 1;
	public static final double DEFAULT_CAL_X = 1.0;
	public static final double DEFAULT_CAL_Y = 1.0;
	public static final double DEFAULT_CAL_Z = 1.0;
	public static final double DEFAULT_CAL_C = 1.0;
	public static final double DEFAULT_CAL_T = 1.0;
	public static final int DEFAULT_THUMB_SIZE_X = 0;
	public static final int DEFAULT_THUMB_SIZE_Y = 0;
	public static final String DEFAULT_PIXEL_TYPE = FormatTools
		.getPixelTypeString(FormatTools.UINT8);
	public static final int DEFAULT_RGB_CHANNEL_COUNT = 1;
	public static final int DEFAULT_LUT_LENGTH = 3;
	public static final int DEFAULT_SCALE_FACTOR = 1;
	public static final String DEFAULT_DIMENSION_ORDER = "XYZCT";

	private static final long SEED = 0xcafebabe;

	private static final String DEFAULT_NAME = "Untitled";

	// -- Allowed keys --

	private static final String SIZE_X = "sizeX";
	private static final String SIZE_Y = "sizeY";
	private static final String SIZE_Z = "sizeZ";
	private static final String SIZE_C = "sizeC";
	private static final String SIZE_T = "sizeT";
	private static final String CAL_X = "calX";
	private static final String CAL_Y = "calY";
	private static final String CAL_Z = "calZ";
	private static final String CAL_C = "calC";
	private static final String CAL_T = "calT";
	private static final String THUMB_X = "thumbSizeX";
	private static final String THUMB_Y = "thumbSizeY";
	private static final String PIXEL_TYPE = "pixelType";
	private static final String BITS_PER_PIXEL = "bitsPerPixel";
	private static final String DIM_ORDER = "dimOrder";
	private static final String INDEXED = "indexed";
	private static final String FALSE_COLOR = "falseColor";
	private static final String LITTLE_ENDIAN = "little";
	private static final String INTERLEAVED = "interleaved";
	private static final String META_COMPLETE = "metadataComplete";
	private static final String THUMBNAIL = "thumbnail";
	private static final String ORDER_CERTAIN = "orderCertain";
	private static final String LUT_LENGTH = "lutLength";
	private static final String SCALE_FACTOR = "scaleFactor";
	private static final String SERIES = "series";
	private static final String RGB = "rgb";

	// -- Constructor --

	public FakeFormat() {
		super();
	}

	// -- Format API Methods --

	@Override
	public String getFormatName() {
		return "Simulated data";
	}

	@Override
	public String[] getSuffixes() {
		return new String[] { "fake" };
	}

	// -- Nested Classes --

	/**
	 * Metadata class for Fake format. Actually holds no information about the
	 * "image" as everything is stored in the attached RandomAccessInputStream.
	 * <p>
	 * Fake specification should be accessed by {@link Metadata#getSource()}
	 * </p>
	 * <p>
	 * NB: Because FakeFormat images can be dynamically constructed in memory,
	 * ColorTables can be generated before reading image planes.
	 * </p>
	 * 
	 * @see io.scif.HasColorTable
	 */
	public static class Metadata extends AbstractMetadata implements
		HasColorTable
	{

		// -- Static Constants --

		public static final String CNAME = "io.scif.formats.FakeFormat$Metadata";

		// -- Fields --

		/** Channel of last opened image plane. */
		private int ac = 0;

		private ColorTable[] lut;

		private int[][] valueToIndex;

		// -- FakeFormat.Metadata methods --

		/**
		 * Gets the last read channel index
		 * 
		 * @return The last read channel
		 */
		public int getLastChannel() {
			return ac;
		}

		/**
		 * Sets the last read channel index
		 * 
		 * @param c - Last read channel
		 */
		public void setLastChannel(final int c) {
			ac = c;
		}

		/**
		 * Gets the lookup table attached to this dataset
		 * 
		 * @return An array of RGB ColorTables. Indexed by plane number.
		 */
		public ColorTable[] getLut() {
			return lut;
		}

		/**
		 * Sets the lookup table for this dataset.
		 * 
		 * @param lut - An array of RGB ColorTables. Indexed by plane number.
		 */
		public void setLut(final ColorTable[] lut) {
			this.lut = lut;
		}

		/**
		 * Gets the pixel value to index maps for this dataset
		 */
		public int[][] getValueToIndex() {
			return valueToIndex;
		}

		/**
		 * Sets the pixel value to index maps for this dataset
		 */
		public void setValueToIndex(final int[][] valueToIndex) {
			this.valueToIndex = valueToIndex;
		}

		// -- HasColorTable Methods --

		/**
		 * Returns the current color table for this dataset
		 */
		@Override
		public ColorTable getColorTable(final int imageIndex, final int planeIndex)
		{
			return lut == null ? null : lut[ac];
		}

		// -- Metadata API Methods --

		/**
		 * Generates ImageMetadata based on the id of this dataset.
		 */
		@Override
		public void populateImageMetadata() {
			int sizeX = DEFAULT_SIZE_X;
			int sizeY = DEFAULT_SIZE_Y;
			int sizeZ = DEFAULT_SIZE_Z;
			int sizeC = DEFAULT_SIZE_C;
			int sizeT = DEFAULT_SIZE_T;
			double calX = DEFAULT_CAL_X;
			double calY = DEFAULT_CAL_Y;
			double calZ = DEFAULT_CAL_Z;
			double calC = DEFAULT_CAL_C;
			double calT = DEFAULT_CAL_T;
			int thumbSizeX = DEFAULT_THUMB_SIZE_X;
			int thumbSizeY = DEFAULT_THUMB_SIZE_Y;
			int rgb = DEFAULT_RGB_CHANNEL_COUNT;
			boolean indexed = false;
			boolean falseColor = false;
			int pixelType = FormatTools.pixelTypeFromString(DEFAULT_PIXEL_TYPE);

			int imageCount = 1;
			int lutLength = DEFAULT_LUT_LENGTH;

			final Map<String, String> fakeMap =
				FakeUtils.extractFakeInfo(getContext(), getDatasetName());

			sizeX = FakeUtils.getIntValue(fakeMap.get(SIZE_X), sizeX);
			sizeY = FakeUtils.getIntValue(fakeMap.get(SIZE_Y), sizeY);
			sizeZ = FakeUtils.getIntValue(fakeMap.get(SIZE_Z), sizeZ);
			sizeC = FakeUtils.getIntValue(fakeMap.get(SIZE_C), sizeC);
			sizeT = FakeUtils.getIntValue(fakeMap.get(SIZE_T), sizeT);

			calX = FakeUtils.getDoubleValue(fakeMap.get(CAL_X), calX);
			calY = FakeUtils.getDoubleValue(fakeMap.get(CAL_Y), calY);
			calZ = FakeUtils.getDoubleValue(fakeMap.get(CAL_Z), calZ);
			calC = FakeUtils.getDoubleValue(fakeMap.get(CAL_C), calC);
			calT = FakeUtils.getDoubleValue(fakeMap.get(CAL_T), calT);

			thumbSizeX = FakeUtils.getIntValue(fakeMap.get(THUMB_X), thumbSizeX);
			thumbSizeY = FakeUtils.getIntValue(fakeMap.get(THUMB_Y), thumbSizeY);
			rgb = FakeUtils.getIntValue(fakeMap.get(RGB), rgb);
			indexed = FakeUtils.getBoolValue(fakeMap.get(INDEXED), indexed);
			falseColor = FakeUtils.getBoolValue(fakeMap.get(FALSE_COLOR), falseColor);
			final String mappedPType = fakeMap.get(PIXEL_TYPE);
			pixelType =
				FormatTools.pixelTypeFromString(mappedPType == null
					? DEFAULT_PIXEL_TYPE : mappedPType);

			imageCount = FakeUtils.getIntValue(fakeMap.get(SERIES), imageCount);
			lutLength = FakeUtils.getIntValue(fakeMap.get(LUT_LENGTH), lutLength);

			// TODO not sure how to handle error handling here yet
//      // Sanity checking
//      if (sizeX < 1) throw new FormatException("Invalid sizeX: " + sizeX);
//      if (sizeY < 1) throw new FormatException("Invalid sizeY: " + sizeY);
//      if (sizeZ < 1) throw new FormatException("Invalid sizeZ: " + sizeZ);
//      if (sizeC < 1) throw new FormatException("Invalid sizeC: " + sizeC);
//      if (sizeT < 1) throw new FormatException("Invalid sizeT: " + sizeT);
//      if (thumbSizeX < 0) {
//        throw new FormatException("Invalid thumbSizeX: " + thumbSizeX);
//      }
//      if (thumbSizeY < 0) {
//        throw new FormatException("Invalid thumbSizeY: " + thumbSizeY);
//      }
//      if (rgb < 1 || rgb > sizeC || sizeC % rgb != 0) {
//        throw new FormatException("Invalid sizeC/rgb combination: " +
//          sizeC + "/" + rgb);
//      }
//      if (falseColor && !indexed) {
//        throw new FormatException("False color images must be indexed");
//      }
//      if (imageCount < 1) {
//        throw new FormatException("Invalid seriesCount: " + imageCount);
//      }
//      if (lutLength < 1) {
//        throw new FormatException("Invalid lutLength: " + lutLength);
//      }

			// for indexed color images, create lookup tables
			if (indexed) {
				int[][] indexToValue = null;
				int[][] valueToIndex = null;
				ColorTable[] luts = null;

				if (pixelType == FormatTools.UINT8) {
					// create 8-bit LUTs
					final int num = 256;
					indexToValue = new int[sizeC][num];
					valueToIndex = new int[sizeC][num];
					FakeUtils.createIndexValueMap(indexToValue);
					luts = new ColorTable8[sizeC];
					// linear ramp
					for (int c = 0; c < sizeC; c++) {
						final byte[][] lutBytes = new byte[lutLength][num];
						for (int i = 0; i < lutLength; i++) {
							for (int index = 0; index < num; index++) {
								lutBytes[i][index] = (byte) indexToValue[c][index];
							}
						}
						luts[c] = new ColorTable8(lutBytes);
					}
				}
				else if (pixelType == FormatTools.UINT16) {
					// create 16-bit LUTs
					final int num = 65536;
					indexToValue = new int[sizeC][num];
					valueToIndex = new int[sizeC][num];
					FakeUtils.createIndexValueMap(indexToValue);
					luts = new ColorTable16[sizeC];
					// linear ramp
					for (int c = 0; c < sizeC; c++) {
						final short[][] lutShorts = new short[lutLength][num];
						for (int i = 0; i < lutLength; i++) {
							for (int index = 0; index < num; index++) {
								lutShorts[i][index] = (short) indexToValue[c][index];
							}
						}
						luts[c] = new ColorTable16(lutShorts);
					}
				}

				setLut(luts);

				if (valueToIndex != null) {
					FakeUtils.createInverseIndexMap(indexToValue, valueToIndex);
					setValueToIndex(valueToIndex);
				}
				// NB: Other pixel types will have null LUTs.
			}

			// General metadata population

			int bitsPerPixel = 0; // default
			String dimOrder = DEFAULT_DIMENSION_ORDER;
			boolean orderCertain = true;
			boolean little = true;
			boolean interleaved = false;
			boolean metadataComplete = true;
			boolean thumbnail = false;
			double scaleFactor = DEFAULT_SCALE_FACTOR;

			bitsPerPixel = FormatTools.getBitsPerPixel(pixelType);
			bitsPerPixel =
				FakeUtils.getIntValue(fakeMap.get(BITS_PER_PIXEL), bitsPerPixel);
			dimOrder =
				fakeMap.get(DIM_ORDER) == null ? dimOrder : fakeMap.get(DIM_ORDER)
					.toUpperCase();

			little = FakeUtils.getBoolValue(fakeMap.get(LITTLE_ENDIAN), little);
			interleaved =
				FakeUtils.getBoolValue(fakeMap.get(INTERLEAVED), interleaved);
			metadataComplete =
				FakeUtils.getBoolValue(fakeMap.get(META_COMPLETE), metadataComplete);
			thumbnail = FakeUtils.getBoolValue(fakeMap.get(THUMBNAIL), thumbnail);
			orderCertain =
				FakeUtils.getBoolValue(fakeMap.get(ORDER_CERTAIN), orderCertain);

			scaleFactor =
				FakeUtils.getDoubleValue(fakeMap.get(SCALE_FACTOR), scaleFactor);

			final CalibratedAxis[] axes = FormatTools.findDimensionList(dimOrder);
			final int[] axisLengths = new int[axes.length];
			final double[] calibrations = new double[axes.length];

			// Create axes arrays
			for (int i = 0; i < axes.length; i++) {
				final AxisType t = axes[i].type();
				if (t.equals(Axes.X)) {
					axisLengths[i] = sizeX;
					calibrations[i] = calX;
				}
				else if (t.equals(Axes.Y)) {
					axisLengths[i] = sizeY;
					calibrations[i] = calY;
				}
				else if (t.equals(Axes.Z)) {
					axisLengths[i] = sizeZ;
					calibrations[i] = calZ;
				}
				else if (t.equals(Axes.CHANNEL)) {
					axisLengths[i] = sizeC;
					calibrations[i] = calC;
				}
				else if (t.equals(Axes.TIME)) { 
					axisLengths[i] = sizeT;
					calibrations[i] = calT;
				}
				else axisLengths[i] = -1; // Unknown axis
			}

			getTable().put(SCALE_FACTOR, scaleFactor);
			getTable().put(LUT_LENGTH, lutLength);

			int numImages = 1;
			numImages = FakeUtils.getIntValue(fakeMap.get(SERIES), numImages);

			final int effSizeC = sizeC / rgb;

			createImageMetadata(numImages);

			// set ImageMetadata
			for (int i = 0; i < numImages; i++) {
				final ImageMetadata imageMeta = get(i);

				imageMeta.setAxes(axes);
				imageMeta.setAxisLengths(axisLengths);
				FormatTools.calibrate(this, i, calibrations);
				imageMeta.setPixelType(pixelType);
				imageMeta.setThumbSizeX(thumbSizeX);
				imageMeta.setThumbSizeY(thumbSizeY);
				imageMeta.setIndexed(indexed);
				imageMeta.setFalseColor(falseColor);
				imageMeta.setRGB(rgb > 1);
				imageMeta.setLittleEndian(little);
				imageMeta.setInterleaved(interleaved);
				imageMeta.setMetadataComplete(metadataComplete);
				imageMeta.setThumbnail(thumbnail);
				imageMeta.setOrderCertain(orderCertain);
				imageMeta.setBitsPerPixel(bitsPerPixel);
				imageMeta.setPlaneCount(sizeZ * effSizeC * sizeT);
			}
		}
	}

	/**
	 * Parser for Fake file format. The file suffix is sufficient for detection -
	 * as the name is the only aspect of a Fake file that is guaranteed to exist.
	 */
	public static class Parser extends AbstractParser<Metadata> {

		// -- Parser API Methods --

		/* @See Parser#Parse(RandomAccessInputStream, M) */
		@Override
		protected void typedParse(final RandomAccessInputStream stream,
			final Metadata meta) throws IOException, FormatException
		{}
	}

	/**
	 * Reader for the Fake file format. Pixel values are simulated based on the
	 * specified dimensions and qualities of the "image."
	 */
	public static class Reader extends ByteArrayReader<Metadata> {

		// -- Reader API methods --

		@Override
		public ByteArrayPlane openPlane(final int imageIndex, final int planeIndex,
			final ByteArrayPlane plane, final int x, final int y, final int w,
			final int h) throws FormatException, IOException
		{
			FormatTools.checkPlaneParameters(this, imageIndex, planeIndex, plane
				.getData().length, x, y, w, h);

			final Metadata meta = getMetadata();
			plane.setImageMetadata(meta.get(imageIndex));

			final int pixelType = meta.getPixelType(imageIndex);
			final int bpp = FormatTools.getBytesPerPixel(pixelType);
			final boolean signed = FormatTools.isSigned(pixelType);
			final boolean floating = FormatTools.isFloatingPoint(pixelType);
			final int rgb = meta.getRGBChannelCount(imageIndex);
			final boolean indexed = meta.isIndexed(imageIndex);
			final boolean little = meta.isLittleEndian(imageIndex);
			final boolean interleaved = meta.isInterleaved(imageIndex);
			final int scaleFactor =
				((Double) meta.getTable().get(SCALE_FACTOR)).intValue();
			final ColorTable[] lut = getMetadata().getLut();
			final int[][] valueToIndex = getMetadata().getValueToIndex();

			final int[] zct = FormatTools.getZCTCoords(this, imageIndex, planeIndex);
			final int zIndex = zct[0], cIndex = zct[1], tIndex = zct[2];
			getMetadata().setLastChannel(cIndex);

			// integer types start gradient at the smallest value
			long min = signed ? (long) -Math.pow(2, 8 * bpp - 1) : 0;
			if (floating) min = 0; // floating point types always start at 0

			for (int cOffset = 0; cOffset < rgb; cOffset++) {
				final int channel = rgb * cIndex + cOffset;
				for (int row = 0; row < h; row++) {
					final int yy = y + row;
					for (int col = 0; col < w; col++) {
						final int xx = x + col;
						long pixel = min + xx;

						// encode various information into the image plane
						boolean specialPixel = false;
						if (yy < BOX_SIZE) {
							final int grid = xx / BOX_SIZE;
							specialPixel = true;
							switch (grid) {
								case 0:
									pixel = imageIndex;
									break;
								case 1:
									pixel = planeIndex;
									break;
								case 2:
									pixel = zIndex;
									break;
								case 3:
									pixel = channel;
									break;
								case 4:
									pixel = tIndex;
									break;
								default:
									// just a normal pixel in the gradient
									specialPixel = false;
							}
						}

						// if indexed color with non-null LUT, convert value to index
						if (indexed && lut != null) {
							final int modValue =
								lut[getMetadata().getLastChannel()].getLength();
							plane.setColorTable(lut[getMetadata().getLastChannel()]);

							if (valueToIndex != null) pixel =
								valueToIndex[getMetadata().getLastChannel()][(int) (pixel % modValue)];
						}

						// scale pixel value by the scale factor
						// if floating point, convert value to raw IEEE floating point bits
						switch (pixelType) {
							case FormatTools.FLOAT:
								float floatPixel;
								if (specialPixel) floatPixel = pixel;
								else floatPixel = scaleFactor * pixel;
								pixel = Float.floatToIntBits(floatPixel);
								break;
							case FormatTools.DOUBLE:
								double doublePixel;
								if (specialPixel) doublePixel = pixel;
								else doublePixel = scaleFactor * pixel;
								pixel = Double.doubleToLongBits(doublePixel);
								break;
							default:
								if (!specialPixel) pixel = scaleFactor * pixel;
						}

						// unpack pixel into byte buffer
						int index;
						if (interleaved) index = w * rgb * row + rgb * col + cOffset; // CXY
						else index = h * w * cOffset + w * row + col; // XYC
						index *= bpp;
						DataTools.unpackBytes(pixel, plane.getData(), index, bpp, little);
					}
				}
			}

			return plane;
		}
	}

	/**
	 * Translator from {@link io.scif.Metadata} to FakeFormat$Metadata.
	 */
	@Plugin(type = Translator.class, attrs = {
		@Attr(name = FakeTranslator.SOURCE, value = io.scif.Metadata.CNAME),
		@Attr(name = FakeTranslator.DEST, value = Metadata.CNAME) },
		priority = Priority.LOW_PRIORITY)
	public static class FakeTranslator extends
		AbstractTranslator<io.scif.Metadata, Metadata>
	{

		// -- Translator API Methods --

		@Override
		public void typedTranslate(final io.scif.Metadata source,
			final Metadata dest)
		{
			String fakeId = MetadataService.NAME_KEY + "=" + source.getDatasetName();

			fakeId =
				FakeUtils.appendToken(fakeId, SIZE_X, source.getAxisLength(0, Axes.X));
			fakeId =
				FakeUtils.appendToken(fakeId, SIZE_Y, source.getAxisLength(0, Axes.Y));
			fakeId =
				FakeUtils.appendToken(fakeId, SIZE_Z, source.getAxisLength(0, Axes.Z));
			fakeId =
				FakeUtils.appendToken(fakeId, SIZE_C, source.getAxisLength(0,
					Axes.CHANNEL));
			fakeId =
				FakeUtils.appendToken(fakeId, SIZE_T, source
					.getAxisLength(0, Axes.TIME));
			
			fakeId = FakeUtils.appendToken(fakeId,
				CAL_X, FormatTools.getScale(source, 0, Axes.X));
			fakeId = FakeUtils.appendToken(fakeId,
				CAL_Y, FormatTools.getScale(source, 0, Axes.Y));
			fakeId = FakeUtils.appendToken(fakeId,
				CAL_Z, FormatTools.getScale(source, 0, Axes.Z));
			fakeId = FakeUtils.appendToken(fakeId,
				CAL_C, FormatTools.getScale(source, 0, Axes.CHANNEL));
			fakeId = FakeUtils.appendToken(fakeId,
				CAL_T, FormatTools.getScale(source, 0, Axes.TIME));

			fakeId = FakeUtils.appendToken(fakeId, THUMB_X, source.getThumbSizeX(0));
			fakeId = FakeUtils.appendToken(fakeId, THUMB_Y, source.getThumbSizeY(0));

			fakeId =
				FakeUtils.appendToken(fakeId, PIXEL_TYPE, FormatTools
					.getPixelTypeString(source.getPixelType(0)));
			fakeId =
				FakeUtils
					.appendToken(fakeId, BITS_PER_PIXEL, source.getBitsPerPixel(0));
			fakeId =
				FakeUtils.appendToken(fakeId, DIM_ORDER, FormatTools
					.findDimensionOrder(source, 0));
			fakeId = FakeUtils.appendToken(fakeId, INDEXED, source.isIndexed(0));
			fakeId =
				FakeUtils.appendToken(fakeId, FALSE_COLOR, source.isFalseColor(0));
			fakeId =
				FakeUtils.appendToken(fakeId, LITTLE_ENDIAN, source.isLittleEndian(0));
			fakeId =
				FakeUtils.appendToken(fakeId, INTERLEAVED, source.isInterleaved(0));
			fakeId =
				FakeUtils.appendToken(fakeId, META_COMPLETE, source
					.isMetadataComplete(0));
			fakeId =
				FakeUtils.appendToken(fakeId, THUMBNAIL, source.isThumbnailImage(0));
			fakeId =
				FakeUtils.appendToken(fakeId, ORDER_CERTAIN, source.isOrderCertain(0));
			fakeId = FakeUtils.appendToken(fakeId, SERIES, source.getImageCount());
			fakeId = FakeUtils.appendToken(fakeId, RGB, source.getRGBChannelCount(0));

			if (source.getTable().get(SCALE_FACTOR) != null) {
				final double scaleFactor = (Double) source.getTable().get(SCALE_FACTOR);
				fakeId =
					FakeUtils.appendToken(fakeId, SCALE_FACTOR, Double
						.toString(scaleFactor));
			}

			if (source.getTable().get(LUT_LENGTH) != null) {
				final int lutLength = (Integer) source.getTable().get(LUT_LENGTH);
				fakeId =
					FakeUtils
						.appendToken(fakeId, LUT_LENGTH, Integer.toString(lutLength));
			}

			fakeId += ".fake";

			try {
				dest.close();
				dest.setSource(new RandomAccessInputStream(getContext(), fakeId));
			}
			catch (final IOException e) {
				log().debug("Failed to create RAIS: " + fakeId, e);
			}
		}
	}

	/**
	 * Helper methods for the Fake file format. Methods are provided for parsing
	 * the key:value pairs from the name of a Fake file.
	 */
	public static class FakeUtils {

		/**
		 * Parses the provided path and returns a mapping of all known key/value
		 * pairs that were discovered.
		 * 
		 * @param fakePath - A properly formatted .fake id
		 * @return A mapping of all discovered properties.
		 */
		public static Map<String, String> extractFakeInfo(
			final Context context, String fakePath)
		{
			final Location loc = new Location(context, fakePath);

			if (loc.exists()) {
				fakePath = loc.getAbsoluteFile().getName();
			}

			// strip extension from filename
			final String noExt = fakePath.substring(0, fakePath.lastIndexOf("."));

			// parse tokens from filename
			final MetadataService metadataService =
				context.getService(MetadataService.class);
			final Map<String, String> fakeMap = metadataService.parse(noExt);

			// provide a default name if none was given
			if (!fakeMap.containsKey(MetadataService.NAME_KEY)) {
				fakeMap.put(MetadataService.NAME_KEY, DEFAULT_NAME);
			}

			return fakeMap;
		}

		/**
		 * Appends the provided key:boolean pair to the provided base and returns
		 * the result.
		 * 
		 * @return A formatted FakeFormat key:value pair
		 */
		public static String appendToken(final String base, final String key,
			final boolean value)
		{
			return FakeUtils.appendToken(base, key, Boolean.toString(value));
		}

		/**
		 * Appends the provided key:double pair to the provided base and returns
		 * the result.
		 * 
		 * @return A formatted FakeFormat key:value pair
		 */
		public static String appendToken(final String base, final String key,
			final double value)
		{
			return FakeUtils.appendToken(base, key, Double.toString(value));
		}

		/**
		 * Appends the provided key:int pair to the provided base and returns the
		 * result.
		 * 
		 * @return A formatted FakeFormat key:value pair
		 */
		public static String appendToken(final String base, final String key,
			final int value)
		{
			return FakeUtils.appendToken(base, key, Integer.toString(value));
		}

		/**
		 * Appends the provided key:String pair to the provided base and returns the
		 * result.
		 * 
		 * @return A formatted FakeFormat key:value pair
		 */
		public static String appendToken(String base, final String key,
			final String value)
		{
			base += "&" + key + "=" + value;
			return base;
		}

		// -- Value extraction methods --

		/**
		 * Returns the integer value of the passed String, or the default int value
		 * if testValue is null.
		 * 
		 * @param testValue - Potential int value
		 * @param defaultValue - Value to use if testValue is null
		 * @return The int value parsed from testValue, or defaultValue
		 */
		public static int
			getIntValue(final String testValue, final int defaultValue)
		{
			if (testValue == null) return defaultValue;

			return Integer.parseInt(testValue);
		}

		/**
		 * Returns the double value of the passed String, or the default double
		 * value if testValue is null.
		 * 
		 * @param testValue - Potential double value
		 * @param defaultValue - Value to use if testValue is null
		 * @return The double value parsed from testValue, or defaultValue
		 */
		public static double getDoubleValue(final String testValue,
			final double defaultValue)
		{
			if (testValue == null) return defaultValue;

			return Double.parseDouble(testValue);
		}

		/**
		 * Returns the boolean value of the passed String, or the default boolean
		 * value if testValue is null.
		 * 
		 * @param testValue - Potential boolean value
		 * @param defaultValue - Value to use if testValue is null
		 * @return The boolean value parsed from testValue, or defaultValue
		 */
		public static boolean getBoolValue(final String testValue,
			final boolean oldValue)
		{
			if (testValue == null) return oldValue;

			return Boolean.parseBoolean(testValue);
		}

		/**
		 * Populates a mapping between indicies and color values, and the inverse
		 * mapping of color values to indicies.
		 * <p>
		 * NB: The array parameters will be modified by this method and should
		 * simply be empty and initialized to the appropriate dimensions.
		 * </p>
		 * 
		 * @param indexToValue - a channel size X num values array, mapping indicies
		 *          to color values.
		 * @param valueToIndex - a channel size X num values array, mapping color
		 *          values to indicies.
		 */
		public static void createIndexMaps(final int[][] indexToValue,
			final int[][] valueToIndex)
		{
			sizeCheck(indexToValue, valueToIndex);
			createIndexValueMap(indexToValue);
			createInverseIndexMap(indexToValue, valueToIndex);
		}

		/**
		 * Populates the given array with a random mapping of indices to values.
		 * 
		 * @param indexToValue - An empty array that will be populated with an
		 *          index:value mapping.
		 */
		public static void createIndexValueMap(final int[][] indexToValue) {
			for (int c = 0; c < indexToValue.length; c++) {
				for (int index = 0; index < indexToValue[0].length; index++)
					indexToValue[c][index] = index;
				shuffle(c, indexToValue[c]);
			}
		}

		/**
		 * Populates an array with inverse mapping of values and indices, drawn from
		 * a base index:value mapping.
		 * 
		 * @param indexToValue - A populated mapping of indicies to color values.
		 * @param valueToIndex - An empty array that will be populated with the
		 *          inverse of indexToValue.
		 */
		public static void createInverseIndexMap(final int[][] indexToValue,
			final int[][] valueToIndex)
		{
			sizeCheck(indexToValue, valueToIndex);

			for (int c = 0; c < indexToValue.length; c++) {
				for (int index = 0; index < indexToValue[0].length; index++) {
					final int value = indexToValue[c][index];
					valueToIndex[c][value] = index;
				}
			}
		}

		/** Fisher-Yates shuffle with constant seeds to ensure reproducibility. */
		public static void shuffle(final int c, final int[] array) {
			final Random r = new Random(SEED + c);
			for (int i = array.length; i > 1; i--) {
				final int j = r.nextInt(i);
				final int tmp = array[j];
				array[j] = array[i - 1];
				array[i - 1] = tmp;
			}
		}

		/* Verifies two arrays are of the same size. */
		private static void sizeCheck(final int[][] array1, final int[][] array2) {
			if (array1.length != array2.length ||
				array1[0].length != array2[0].length) throw new IllegalArgumentException(
				"Arrays must be of the same size.");
		}
	}
}
