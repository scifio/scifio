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

package io.scif.formats;

import io.scif.AbstractFormat;
import io.scif.AbstractMetadata;
import io.scif.AbstractParser;
import io.scif.AbstractTranslator;
import io.scif.ByteArrayPlane;
import io.scif.ByteArrayReader;
import io.scif.Field;
import io.scif.Format;
import io.scif.FormatException;
import io.scif.HasColorTable;
import io.scif.ImageMetadata;
import io.scif.MetadataService;
import io.scif.Plane;
import io.scif.Translator;
import io.scif.config.SCIFIOConfig;
import io.scif.io.Location;
import io.scif.io.RandomAccessInputStream;
import io.scif.util.FormatTools;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imagej.axis.DefaultLinearAxis;
import net.imglib2.Interval;
import net.imglib2.display.ColorTable;
import net.imglib2.display.ColorTable16;
import net.imglib2.display.ColorTable8;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import org.scijava.util.Bytes;

/**
 * FakeFormat is the file format reader for faking input data. It is mainly
 * useful for testing, as image sources can be defined in memory through String
 * notation, without requiring an actual dataset to exist on disk.
 * <p>
 * Examples:
 * </p>
 * <ul>
 * <li>{@code 'multi-image&images=11&axes=X,Y,Z,Channel,Time&lengths=1,50,3,5,7.fake'}</li>
 * <li>{@code '8bit-signed&pixelType=int8&axes=X,Y,Z,Channel,Time&lengths=1,50,3,5,7.fake'}</li>
 * <li>{@code '8bit-unsigned&pixelType=uint8&axes=X,Y,Z,Channel&lengths=1,50,3,5.fake'}</li>
 * <li>{@code '16bit-signed&pixelType=int16&axes=X,Y,Z,Channel,Time&lengths=1,50,3,7.fake'}</li>
 * <li>{@code '16bit-unsigned&pixelType=uint16&sizeZ=3&axes=X,Y,&lengths=1,1.fake'}</li>
 * <li>{@code '32bit-signed&pixelType=int32&sizeZ=3&axes=X,Y,Z,,Time&lengths=50,50.fake'}</li>
 * <li>{@code '32bit-unsigned&pixelType=uint32&axes=X,Y,Z&lengths=50,50,3.fake'}</li>
 * <li>{@code '32bit-floating&pixelType=float&axes=X,Y&lengths=1,1.fake'}</li>
 * <li>{@code '64bit-floating&pixelType=double&axes=X,Y,Time&lengths=256,256,3.fake'}</li>
 * <li>{@code 'rgb-image&lengths=512,512,3&axes=X,Y,Channel&planarDims=3.fake'}</li>
 * <li>{@code 'rgb-interleaved&lengths=3,512,512&axes=Channel,X,Y&planarDims=3.fake'}</li>
 * </ul>
 *
 * @author Mark Hiner
 * @author Curtis Rueden
 */
@Plugin(type = Format.class, name = "Simulated data")
public class FakeFormat extends AbstractFormat {

	// -- Constants --

	private static final long SEED = 0xcafebabe;

	private static final String DEFAULT_NAME = "Untitled";

	// -- Constructor --

	public FakeFormat() {
		super();
	}

	// -- AbstractFormat Methods --

	@Override
	protected String[] makeSuffixArray() {
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

		// -- Fields --

		@Field
		private String[] axes;

		@Field
		private long[] lengths;

		@Field
		private double[] scales;

		@Field
		private String[] units;

		@Field
		private int planarDims;

		@Field
		private int interleavedDims;

		@Field
		private int thumbSizeX;

		@Field
		private int thumbSizeY;

		@Field
		private String pixelType;

		@Field
		private boolean indexed;

		@Field
		private boolean falseColor;

		@Field
		private boolean little;

		@Field
		private boolean metadataComplete;

		@Field
		private boolean thumbnail;

		@Field
		private boolean orderCertain;

		@Field
		private int lutLength;

		@Field
		private int scaleFactor;

		@Field
		private int images;

		private ColorTable[][] luts;

		private int[][] valueToIndex;

		// -- FakeFormat.Metadata methods --

		/**
		 * @return Scale factor for this image
		 */
		public int getScaleFactor() {
			return scaleFactor;
		}

		/**
		 * Gets the lookup table attached to this dataset
		 *
		 * @return A 2D array of ColorTables. Indexed by image index and plane plane
		 *         index.
		 */
		public ColorTable[][] getLuts() {
			return luts;
		}

		/**
		 * Sets the lookup table for this dataset.
		 *
		 * @param luts A 2D array of ColorTables. Indexed by image index and plane
		 *          plane index.
		 */
		public void setLuts(final ColorTable[][] luts) {
			this.luts = luts;
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
		public ColorTable getColorTable(final int imageIndex,
			final long planeIndex)
		{

			return luts == null ? null : luts[imageIndex][(int) planeIndex];
		}

		// -- Metadata API Methods --

		/**
		 * Generates ImageMetadata based on the id of this dataset.
		 */
		@Override
		public void populateImageMetadata() {
			final MetadataService metadataService = getContext().getService(
				MetadataService.class);

			setDefaults();

			// parse key/value pairs from fake filename
			final Map<String, Object> fakeMap = FakeUtils.extractFakeInfo(
				metadataService, getDatasetName());

			metadataService.populate(this, fakeMap);

			if (axes.length != lengths.length) {
				throw new IllegalStateException("FakeFormat id: " + getDatasetName() +
					" is not valid. Can not have a differing number of axis types and axis lengths.");
			}

			final int pType = FormatTools.pixelTypeFromString(pixelType);
			final int bpp = FormatTools.getBitsPerPixel(pType);

			final CalibratedAxis[] calibratedAxes = new CalibratedAxis[axes.length];

			for (int i = 0; i < calibratedAxes.length; i++) {
				double scale = 1.0;
				String unit = "um";
				if (i < units.length) {
					unit = units[i];
				}
				if (i < scales.length) {
					scale = scales[i];
				}
				calibratedAxes[i] = new DefaultLinearAxis(Axes.get(axes[i]), unit,
					scale);
			}

			// Image metadata population
			createImageMetadata(images);

			// set ImageMetadata
			for (int i = 0; i < images; i++) {
				final ImageMetadata imageMeta = get(i);

				imageMeta.setAxes(calibratedAxes, lengths);
				imageMeta.setPlanarAxisCount(planarDims);
				imageMeta.setInterleavedAxisCount(interleavedDims);
				imageMeta.setPixelType(pType);
				imageMeta.setThumbSizeX(thumbSizeX);
				imageMeta.setThumbSizeY(thumbSizeY);
				imageMeta.setIndexed(indexed);
				imageMeta.setFalseColor(falseColor);
				imageMeta.setLittleEndian(little);
				imageMeta.setMetadataComplete(metadataComplete);
				imageMeta.setThumbnail(thumbnail);
				imageMeta.setOrderCertain(orderCertain);
				imageMeta.setBitsPerPixel(bpp);
			}

			// for indexed color images, create lookup tables
			if (indexed) {
				int[][] indexToValue = null;
				int[][] valueToIndex = null;
				ColorTable[][] luts = null;

				if (pType == FormatTools.UINT8) {
					// create 8-bit LUTs
					final int num = 256;
					luts = new ColorTable8[images][];
					for (int i = 0; i < images; i++) {
						final int planeCount = (int) get(i).getPlaneCount();
						luts[i] = new ColorTable8[planeCount];
						indexToValue = new int[planeCount][num];
						valueToIndex = new int[planeCount][num];
						FakeUtils.createIndexValueMap(indexToValue);
						// linear ramp
						for (int p = 0; p < planeCount; p++) {
							final byte[][] lutBytes = new byte[lutLength][num];
							for (int cmpIndex = 0; cmpIndex < lutLength; cmpIndex++) {
								for (int index = 0; index < num; index++) {
									lutBytes[cmpIndex][index] = (byte) indexToValue[p][index];
								}
							}
							luts[i][p] = new ColorTable8(lutBytes);
						}

					}
				}
				else if (pType == FormatTools.UINT16) {
					// create 16-bit LUTs
					final int num = 65536;
					luts = new ColorTable16[images][];
					for (int i = 0; i < images; i++) {
						final int planeCount = (int) get(i).getPlaneCount();
						luts[i] = new ColorTable16[planeCount];
						indexToValue = new int[planeCount][num];
						valueToIndex = new int[planeCount][num];
						FakeUtils.createIndexValueMap(indexToValue);
						// linear ramp
						for (int p = 0; p < planeCount; p++) {
							final short[][] lutShorts = new short[lutLength][num];
							for (int cmpIndex = 0; cmpIndex < lutLength; cmpIndex++) {
								for (int index = 0; index < num; index++) {
									lutShorts[cmpIndex][index] = (short) indexToValue[p][index];
								}
							}
							luts[i][p] = new ColorTable16(lutShorts);
						}
					}
				}

				setLuts(luts);

				if (valueToIndex != null) {
					FakeUtils.createInverseIndexMap(indexToValue, valueToIndex);
					setValueToIndex(valueToIndex);
				}
				// NB: Other pixel types will have null LUTs.
			}
		}

		// -- Helper methods --

		/**
		 * Sets default values for all fields. Necessary as field values may be
		 * erased post-initialization (e.g. by closing).
		 */
		private void setDefaults() {
			axes = new String[] { "X", "Y" };
			lengths = new long[] { 512, 512 };
			scales = new double[] { 1.0, 1.0 };
			units = new String[] { "um", "um" };
			planarDims = -1;
			interleavedDims = -1;
			thumbSizeX = 0;
			thumbSizeY = 0;
			pixelType = FormatTools.getPixelTypeString(FormatTools.UINT8);
			indexed = false;
			falseColor = false;
			little = true;
			metadataComplete = true;
			thumbnail = false;
			orderCertain = true;
			lutLength = 3;
			scaleFactor = 1;
			images = 1;
		}

		// -- Deprecated methods --

		/**
		 * Gets the lookup table for the first image of this dataset.
		 *
		 * @return An array of ColorTables. Indexed by plane number.
		 * @deprecated Use {@link #getLuts()}
		 */
		@Deprecated
		public ColorTable[] getLut() {
			return luts == null ? null : luts[0];
		}

		/**
		 * Sets the lookup table for the first image of this dataset.
		 *
		 * @param lut - An array of ColorTables. Indexed by plane number.
		 * @deprecated Use {@link #setLuts(ColorTable[][])}
		 */
		@Deprecated
		public void setLut(final ColorTable[] lut) {
			if (this.luts != null) {
				this.luts[0] = lut;
			}
			else {
				this.luts = new ColorTable[][] { lut };
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
			final Metadata meta, final SCIFIOConfig config) throws IOException,
			FormatException
		{
			// No operation. All work is done in the populateImageMetadata
			// method
			// of the metadata itself (the format-specific metadata is implied
			// purely by the file name).
		}
	}

	/**
	 * Reader for the Fake file format. Pixel values are simulated based on the
	 * specified dimensions and qualities of the "image."
	 */
	public static class Reader extends ByteArrayReader<Metadata> {

		// -- AbstractReader API Methods --

		@Override
		protected String[] createDomainArray() {
			return new String[0];
		}

		// -- Reader API methods --

		@Override
		public ByteArrayPlane openPlane(final int imageIndex, final long planeIndex,
			final ByteArrayPlane plane, final Interval bounds,
			final SCIFIOConfig config) throws FormatException, IOException
		{
			final Metadata meta = getMetadata();
			FormatTools.checkPlaneForReading(meta, imageIndex, planeIndex, plane
				.getData().length, bounds);
			plane.setImageMetadata(meta.get(imageIndex));

			final long[] pos = FormatTools.rasterToPosition(meta.get(imageIndex)
				.getAxesLengthsNonPlanar(), planeIndex);

			final long[] planarIndices = new long[bounds.numDimensions()];

			openPlaneHelper(imageIndex, planeIndex, meta, plane, bounds, pos,
				planarIndices, 0, -1, -1);

			return plane;
		}

		private void openPlaneHelper(final int imageIndex, final long planeIndex,
			final Metadata meta, final Plane plane, final Interval bounds,
			final long[] npIndices, final long[] planeIndices, final int planarPos,
			long xPos, long yPos)
		{
			if (planarPos < bounds.numDimensions()) {
				// Recursively descend along each planar axis
				for (int i = 0; i < bounds.dimension(planarPos); i++) {
					if (planarPos == meta.get(imageIndex).getAxisIndex(Axes.X)) xPos =
						bounds.min(planarPos) + i;
					if (planarPos == meta.get(imageIndex).getAxisIndex(Axes.Y)) yPos =
						bounds.min(planarPos) + i;
					planeIndices[planarPos] = bounds.min(planarPos) + i;
					openPlaneHelper(imageIndex, planeIndex, meta, plane, bounds,
						npIndices, planeIndices, planarPos + 1, xPos, yPos);
				}
			}
			else {
				final int pixelType = meta.get(imageIndex).getPixelType();
				final int bpp = FormatTools.getBytesPerPixel(pixelType);
				final boolean signed = FormatTools.isSigned(pixelType);
				final boolean floating = FormatTools.isFloatingPoint(pixelType);
				final boolean indexed = meta.get(imageIndex).isIndexed();
				final boolean little = meta.get(imageIndex).isLittleEndian();
				final int scaleFactor = meta.getScaleFactor();
				final ColorTable lut = meta.getColorTable(imageIndex, planeIndex);
				final int[][] valueToIndex = getMetadata().getValueToIndex();
				// integer types start gradient at the smallest value
				long min = signed ? (long) -Math.pow(2, 8 * bpp - 1) : 0;
				if (floating) min = 0;
				// floating point types always start at 0
				// to differentiate each plane, we create a box of imageIndex,
				// planeIndex and
				// the non-planar indices
				final int boxSize = 10;
				// Code for scaling pixel values (see default case in switch
				// below)
//				final double xMax = meta.getAxisLength(imageIndex, Axes.X);
//				int boxSize = 2 + meta.getAxesNonPlanar(imageIndex).size();
//				boxSize = (int) Math.min(boxSize, xMax / boxSize);

				// Generate a pixel
				long pixel = min + xPos;

				// encode various information into the image plane
				boolean specialPixel = false;
				if (yPos < boxSize) {
					int grid = (int) (xPos / boxSize);
					specialPixel = true;
					switch (grid) {
						case 0:
							pixel = imageIndex;
							// Code for scaling the pixel values (see default case)
//							pixel =
//								(long) Math.floor(imageIndex * xMax / meta.getImageCount());
							break;
						case 1:
							pixel = planeIndex;
							// Code for scaling the pixel values (see default case)
//							pixel =
//								(long) Math.floor(planeIndex * xMax /
//									meta.getPlaneCount(imageIndex));
							break;
						default:
							grid -= 2;
							if (grid < npIndices.length) {
								pixel = min + npIndices[grid];
								// The following code allows for scaling the box
								// pixels to the
								// max intensity of the image. This allows for much
								// easier
								// plane differentiation in manual testing, but
								// breaks or
								// complicates automated pixel verification, which
								// is the
								// primary purpose of these boxes. This code could
								// be
								// factored out into special "drawBox" methods, with
								// behavior
								// configurable by Fake parameters.
//								final double npMax =
//									meta.getAxesLengthsNonPlanar(imageIndex)[grid];
//								if (npMax < xMax) {
//									// create a gradient based on the current axis length
//									pixel =
//										(long) Math.floor(min + (npIndices[grid] * xMax / npMax));
//								}
//								else {
//									// create a repeating gradient based on max pixel intensity
//									pixel = (long) Math.floor(min + (npIndices[grid] % xMax));
//								}
							}
							else {
								specialPixel = false;
							}
							break;
					}
				}

				// if indexed color with non-null LUT, convert value to index
				if (indexed && lut != null) {
					final int modValue = lut.getLength();
					plane.setColorTable(lut);
					if (valueToIndex != null) pixel =
						valueToIndex[(int) planeIndex][(int) (pixel % modValue)];
				}

				// scale pixel value by the scale factor
				// if floating point, convert value to raw IEEE floating point
				// bits
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
				int index = 0;
				// Index is sum of each position * all previous axes lengths
				for (int i = planeIndices.length - 1; i >= 0; i--) {
					long partialIndex = planeIndices[i] - bounds.min(i);
					for (int j = 0; j < i; j++) {
						partialIndex *= bounds.dimension(j);
					}
					index += (int) partialIndex;
				}
				index *= bpp;
				Bytes.unpack(pixel, plane.getBytes(), index, bpp, little);
			}
		}
	}

	/**
	 * Translator from {@link io.scif.Metadata} to FakeFormat$Metadata.
	 */
	@Plugin(type = Translator.class, priority = Priority.LOW)
	public static class FakeTranslator extends
		AbstractTranslator<io.scif.Metadata, Metadata>
	{

		// -- Translator API Methods --

		@Override
		public Class<? extends io.scif.Metadata> source() {
			return io.scif.Metadata.class;
		}

		@Override
		public Class<? extends io.scif.Metadata> dest() {
			return Metadata.class;
		}

		@Override
		public void translateImageMetadata(final List<ImageMetadata> source,
			final Metadata dest)
		{
			final ImageMetadata iMeta = source.get(0);

			String fakeId = MetadataService.NAME_KEY + "=" + dest.getDatasetName();

			final String[] axes = new String[iMeta.getAxes().size()];
			final long[] lengths = new long[axes.length];
			final double[] scales = new double[axes.length];
			final String[] units = new String[axes.length];

			int index = 0;
			for (final CalibratedAxis axis : iMeta.getAxes()) {
				axes[index] = axis.type().getLabel();
				lengths[index] = iMeta.getAxisLength(axis);
				scales[index] = axis.averageScale(0, lengths[index]);
				units[index] = axis.unit();
				index++;
			}

			FakeUtils.appendToken(fakeId, "axes", (Object[]) axes);
			FakeUtils.appendToken(fakeId, "lengths", lengths);
			FakeUtils.appendToken(fakeId, "scales", scales);
			FakeUtils.appendToken(fakeId, "units", (Object[]) units);
			FakeUtils.appendToken(fakeId, "planarDims", iMeta.getPlanarAxisCount());
			FakeUtils.appendToken(fakeId, "interleavedDims", iMeta
				.getInterleavedAxisCount());
			FakeUtils.appendToken(fakeId, "thumbSizeX", iMeta.getThumbSizeX());
			FakeUtils.appendToken(fakeId, "thumbSizeY", iMeta.getThumbSizeY());
			FakeUtils.appendToken(fakeId, "pixelType", FormatTools.getPixelTypeString(
				iMeta.getPixelType()));
			FakeUtils.appendToken(fakeId, "falseColor", iMeta.isFalseColor());
			FakeUtils.appendToken(fakeId, "little", iMeta.isLittleEndian());
			FakeUtils.appendToken(fakeId, "metadataComplete", iMeta
				.isMetadataComplete());
			FakeUtils.appendToken(fakeId, "thumbnail", iMeta.isThumbnail());
			FakeUtils.appendToken(fakeId, "orderCertain", iMeta.isOrderCertain());
			FakeUtils.appendToken(fakeId, "images", source.size());

			if (iMeta.isIndexed()) {
				final int lutLength = ((HasColorTable) source).getColorTable(0, 0)
					.getComponentCount();
				FakeUtils.appendToken(fakeId, "indexed", iMeta.isIndexed());
				FakeUtils.appendToken(fakeId, "lutLength", lutLength);
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
		public static Map<String, Object> extractFakeInfo(
			final MetadataService metadataService, String fakePath)
		{
			final Location loc = new Location(metadataService.getContext(), fakePath);

			if (loc.exists()) {
				fakePath = loc.getAbsoluteFile().getName();
			}

			// strip extension from filename
			final String noExt = fakePath.substring(0, fakePath.lastIndexOf("."));

			// parse tokens from filename
			final Map<String, Object> fakeMap = metadataService.parse(noExt);

			// provide a default name if none was given
			if (!fakeMap.containsKey(MetadataService.NAME_KEY)) {
				fakeMap.put(MetadataService.NAME_KEY, DEFAULT_NAME);
			}

			return fakeMap;
		}

		// Fake name generation methods

		/**
		 * Appends the provided key:value pair to the provided base and returns the
		 * result.
		 *
		 * @return A formatted FakeFormat key:value pair
		 */
		public static String appendToken(String base, final String key,
			final Object... value)
		{
			String listValue = "" + value[0];

			// expand the array if necessary
			for (int i = 1; i < value.length; i++) {
				listValue += "," + value[i];
			}

			base += "&" + key + "=" + listValue;
			return base;
		}

		// -- Index : Value mapping methods

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
				array1[0].length != array2[0].length)
				throw new IllegalArgumentException("Arrays must be of the same size.");
		}
	}
}
