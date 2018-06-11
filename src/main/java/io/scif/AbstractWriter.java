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

import io.scif.codec.CodecOptions;
import io.scif.config.SCIFIOConfig;
import io.scif.io.RandomAccessOutputStream;
import io.scif.util.FormatTools;
import io.scif.util.SCIFIOMetadataTools;

import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;

import net.imagej.axis.Axes;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;

import org.scijava.util.ArrayUtils;

/**
 * Abstract superclass of all SCIFIO {@link io.scif.Writer} implementations.
 *
 * @see io.scif.Writer
 * @see io.scif.HasFormat
 * @see io.scif.Metadata
 * @author Mark Hiner
 * @param <M> - The Metadata type required by this Writer.
 */
public abstract class AbstractWriter<M extends TypedMetadata> extends
	AbstractHasSource implements TypedWriter<M>
{

	// -- Fields --

	/** Metadata, of the output type, describing the input source. */
	private M metadata;

	/** Frame rate to use when writing in frames per second, if applicable. */
	private int fps;

	/** Available compression types. */
	private String[] compressionTypes;

	/** Current compression type. */
	private String compression;

	/** The options if required. */
	private CodecOptions options;

	/**
	 * Whether each plane in each image of the current file has been prepped for
	 * writing.
	 */
	private boolean[][] initialized;

	/** The number of valid bits per pixel. */
	private int validBits;

	/** Whether or not we are writing planes sequentially. */
	private boolean sequential;

	/** Where the image should be written. */
	private RandomAccessOutputStream out;

	/** ColorModel for this Writer. */
	private ColorModel model;

	// -- AbstractWriter API Methods --

	/**
	 * Ensure that the arguments that are being passed to saveBytes(...) are
	 * valid.
	 *
	 * @throws FormatException if any of the arguments is invalid.
	 */
	protected void checkParams(final int imageIndex, final long planeIndex,
		final byte[] buf, final Interval bounds) throws FormatException
	{
		SCIFIOMetadataTools.verifyMinimumPopulated(metadata, out, imageIndex);

		if (buf == null) throw new FormatException("Buffer cannot be null.");
		long planes = metadata.get(imageIndex).getPlaneCount();

		if (metadata.get(imageIndex).isMultichannel()) planes *= metadata.get(
			imageIndex).getAxisLength(Axes.CHANNEL);

		if (planeIndex < 0) throw new FormatException(String.format(
			"Plane index:%d must be >= 0", planeIndex));
		if (planeIndex >= planes) {
			throw new FormatException(String.format("Plane index:%d must be < %d",
				planeIndex, planes));
		}

		FormatTools.checkPlaneForWriting(getMetadata(), imageIndex, planeIndex,
			buf.length, bounds);
		FormatTools.assertId(out, true, 0);
	}

	/**
	 * Ensures this writer is prepared to write the given plane of the given
	 * image.
	 */
	protected void initialize(final int imageIndex, final long planeIndex,
		final Interval bounds) throws FormatException, IOException
	{
		initialized[imageIndex][(int) planeIndex] = true;
	}

	/**
	 * @return An array of compression types supported by this format. An empty
	 *         array indicates no compression.
	 */
	protected abstract String[] makeCompressionTypes();

	/**
	 * Helper method invoked by {@link #savePlane} to perform actual format-
	 * specific output.
	 */
	protected abstract void writePlane(final int imageIndex,
		final long planeIndex, final Plane plane, final Interval bounds)
		throws FormatException, IOException;

	// -- Writer API Methods --

	@Override
	public void savePlane(final int imageIndex, final long planeIndex,
		final Plane plane) throws FormatException, IOException
	{
		final Interval bounds = //
			new FinalInterval(metadata.get(imageIndex).getAxesLengthsPlanar());
		savePlane(imageIndex, planeIndex, plane, bounds);
	}

	@Override
	public void savePlane(final int imageIndex, final long planeIndex,
		final Plane plane, final Interval bounds) throws FormatException,
		IOException
	{
		initialize(imageIndex, planeIndex, bounds);
		writePlane(imageIndex, planeIndex, plane, bounds);
	}

	@Override
	public boolean canDoStacks() {
		return false;
	}

	@Override
	public void setMetadata(final Metadata meta) throws FormatException {
		setMetadata(SCIFIOMetadataTools.<M> castMeta(meta));
	}

	@Override
	public M getMetadata() {
		return metadata;
	}

	@Override
	public void setDest(final String fileName) throws FormatException,
		IOException
	{
		setDest(fileName, 0);
	}

	@Override
	public void setDest(final File file) throws FormatException, IOException {
		setDest(file.getName(), 0);
	}

	@Override
	public void setDest(final RandomAccessOutputStream out)
		throws FormatException, IOException
	{
		setDest(out, 0);
	}

	@Override
	public void setDest(final String fileName, final int imageIndex)
		throws FormatException, IOException
	{
		setDest(fileName, imageIndex, new SCIFIOConfig());
	}

	@Override
	public void setDest(final File file, final int imageIndex)
		throws FormatException, IOException
	{
		setDest(file.getName(), imageIndex, new SCIFIOConfig());
	}

	@Override
	public void setDest(final RandomAccessOutputStream out, final int imageIndex)
		throws FormatException, IOException
	{
		setDest(out, imageIndex, new SCIFIOConfig());
	}

	@Override
	public void setDest(final String fileName, final SCIFIOConfig config)
		throws FormatException, IOException
	{
		setDest(fileName, 0, config);
	}

	@Override
	public void setDest(final File file, final SCIFIOConfig config)
		throws FormatException, IOException
	{
		setDest(file.getName(), 0, config);
	}

	@Override
	public void setDest(final RandomAccessOutputStream out,
		final SCIFIOConfig config) throws FormatException, IOException
	{
		setDest(out, 0, config);
	}

	@Override
	public void setDest(final String fileName, final int imageIndex,
		final SCIFIOConfig config) throws FormatException, IOException
	{
		getMetadata().setDatasetName(fileName);
		setDest(new RandomAccessOutputStream(getContext(), fileName), imageIndex,
			config);
	}

	@Override
	public void setDest(final File file, final int imageIndex,
		final SCIFIOConfig config) throws FormatException, IOException
	{
		setDest(file.getName(), imageIndex, config);
	}

	@Override
	public void setDest(final RandomAccessOutputStream out, final int imageIndex,
		final SCIFIOConfig config) throws FormatException, IOException
	{
		if (metadata == null) throw new FormatException(
			"Can not set Destination without setting Metadata first.");

		// FIXME
		// set metadata.datasetName here when RAOS has better id handling

		this.out = out;
		fps = config.writerGetFramesPerSecond();
		options = config.writerGetCodecOptions();
		model = config.writerGetColorModel();
		compression = config.writerGetCompression();
		sequential = config.writerIsSequential();
		SCIFIOMetadataTools.verifyMinimumPopulated(metadata, out);
		initialized = new boolean[metadata.getImageCount()][];
		for (int i = 0; i < metadata.getImageCount(); i++) {
			initialized[i] = new boolean[(int) metadata.get(imageIndex)
				.getPlaneCount()];
		}
	}

	@Override
	public RandomAccessOutputStream getStream() {
		return out;
	}

	@Override
	public void setColorModel(final ColorModel cm) {
		model = cm;
	}

	@Override
	public ColorModel getColorModel() {
		return model;
	}

	@Override
	public String[] getCompressionTypes() {
		if (compressionTypes == null) {
			compressionTypes = makeCompressionTypes();
		}
		return compressionTypes;
	}

	@Override
	public int getFramesPerSecond() {
		return fps;
	}

	@Override
	public CodecOptions getCodecOptions() {
		return options;
	}

	@Override
	public boolean writeSequential() {
		return sequential;
	}

	@Override
	public int getValidBits() {
		return validBits;
	}

	@Override
	public int[] getPixelTypes(final String codec) {
		return new int[] { FormatTools.INT8, FormatTools.UINT8, FormatTools.INT16,
			FormatTools.UINT16, FormatTools.INT32, FormatTools.UINT32,
			FormatTools.FLOAT };
	}

	@Override
	public boolean isSupportedType(final int type, final String codec) {
		final int[] types = getPixelTypes(codec);
		for (final int otherType : types) {
			if (type == otherType) return true;
		}
		return false;
	}

	@Override
	public void isSupportedCompression(final String compression)
		throws FormatException
	{
		for (final String compressionType : compressionTypes) {
			if (compressionType.equals(compression)) {
				this.compression = compression;
				return;
			}
		}
		throw new FormatException("Invalid compression type: " + compression);
	}

	// -- TypedWriter API Methods --

	@Override
	public void setMetadata(final M meta) throws FormatException {
		if (metadata != null && metadata != meta) {
			try {
				metadata.close();
			}
			catch (final IOException e) {
				throw new FormatException(e);
			}
		}

		if (out != null) {
			try {
				close();
			}
			catch (final IOException e) {
				throw new FormatException(e);
			}
		}

		metadata = meta;

		// Check the first pixel type for compatibility with this writer
		for (int i = 0; i < metadata.getImageCount(); i++) {
			final int pixelType = metadata.get(i).getPixelType();

			if (!ArrayUtils.contains(getPixelTypes(compression), pixelType)) {
				throw new FormatException("Unsupported image type '" + FormatTools
					.getPixelTypeString(pixelType) + "'.");
			}
		}
	}

	@Override
	public boolean isInitialized(final int imageIndex, final long planeIndex) {
		return initialized[imageIndex][(int) planeIndex];
	}

	@Override
	public String getCompression() {
		return compression;
	}

	// -- HasSource API Methods --

	@Override
	public void close(final boolean fileOnly) throws IOException {
		if (out != null) out.close();
		if (metadata != null) metadata.close(fileOnly);
		out = null;
		initialized = null;
	}
}
