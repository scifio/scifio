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

package io.scif;

import io.scif.codec.CodecOptions;
import io.scif.common.DataTools;
import io.scif.io.RandomAccessOutputStream;
import io.scif.util.FormatTools;
import io.scif.util.SCIFIOMetadataTools;

import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;

import net.imglib2.meta.Axes;

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
	protected M metadata;

	/** Frame rate to use when writing in frames per second, if applicable. */
	protected int fps = 10;

	/** Default color model. */
	protected ColorModel cm;

	/** Available compression types. */
	protected String[] compressionTypes;

	/** Current compression type. */
	protected String compression;

	/** The options if required. */
	protected CodecOptions options;

	/**
	 * Whether each plane in each image of the current file has been prepped for
	 * writing.
	 */
	protected boolean[][] initialized;

	/** Whether the channels in an RGB image are interleaved. */
	protected boolean interleaved;

	/** The number of valid bits per pixel. */
	protected int validBits;

	/** Whether or not we are writing planes sequentially. */
	protected boolean sequential;

	/** Where the image should be written. */
	protected RandomAccessOutputStream out;

	// -- Writer API Methods --

	@Override
	public void savePlane(final int imageIndex, final long planeIndex,
		final Plane plane) throws FormatException, IOException
	{
		final long[] planeMax = metadata.get(imageIndex).getAxesLengthsPlanar();
		final long[] planeMin = new long[planeMax.length];
		savePlane(imageIndex, planeIndex, plane, planeMin, planeMax);
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
		getMetadata().setDatasetName(fileName);
		setDest(new RandomAccessOutputStream(getContext(), fileName));
	}


	@Override
	public void setDest(final File file, final int imageIndex)
		throws FormatException, IOException
	{
		setDest(file.getName());
	}

	@Override
	public void setDest(final RandomAccessOutputStream out, final int imageIndex)
		throws FormatException, IOException
	{
		if (metadata == null) throw new FormatException(
			"Can not set Destination without setting Metadata first.");

		// FIXME
		// set metadata.datasetName here when RAOS has better id handling

		this.out = out;
		initialize(imageIndex);
	}

	@Override
	public RandomAccessOutputStream getStream() {
		return out;
	}

	@Override
	public void setColorModel(final ColorModel cm) {
		this.cm = cm;
	}

	@Override
	public ColorModel getColorModel() {
		return cm;
	}

	@Override
	public void setFramesPerSecond(final int rate) {
		fps = rate;
	}

	@Override
	public int getFramesPerSecond() {
		return fps;
	}

	@Override
	public String[] getCompressionTypes() {
		return compressionTypes;
	}

	@Override
	public int[] getPixelTypes() {
		return getPixelTypes(getCompression());
	}

	@Override
	public int[] getPixelTypes(final String codec) {
		return new int[] { FormatTools.INT8, FormatTools.UINT8, FormatTools.INT16,
			FormatTools.UINT16, FormatTools.INT32, FormatTools.UINT32,
			FormatTools.FLOAT };
	}

	@Override
	public boolean isSupportedType(final int type) {
		final int[] types = getPixelTypes();
		for (int i = 0; i < types.length; i++) {
			if (type == types[i]) return true;
		}
		return false;
	}

	@Override
	public void setCompression(final String compress) throws FormatException {
		for (int i = 0; i < compressionTypes.length; i++) {
			if (compressionTypes[i].equals(compress)) {
				compression = compress;
				return;
			}
		}
		throw new FormatException("Invalid compression type: " + compress);
	}

	@Override
	public String getCompression() {
		return compression;
	}

	@Override
	public void setCodecOptions(final CodecOptions options) {
		this.options = options;
	}

	@Override
	public void changeOutputFile(final String id) throws FormatException,
		IOException
	{
		setDest(id);
	}

	@Override
	public void setWriteSequentially(final boolean sequential) {
		this.sequential = sequential;
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
	}

	// -- HasSource API Methods --

	@Override
	public void close(final boolean fileOnly) throws IOException {
		if (out != null) out.close();
		if (metadata != null) metadata.close(fileOnly);
		out = null;
		initialized = null;
	}

	// -- Helper methods --

	/** Sets up the initialized array and ensures this Writer is ready for writing */
	private void initialize(final int imageIndex) throws FormatException
	{
		SCIFIOMetadataTools.verifyMinimumPopulated(metadata, out);
		initialized = new boolean[metadata.getImageCount()][];
		for (int i = 0; i < metadata.getImageCount(); i++) {
			initialized[i] = new boolean[(int) metadata.get(imageIndex).getPlaneCount()];
		}
	}

	/**
	 * Ensure that the arguments that are being passed to saveBytes(...) are
	 * valid.
	 * 
	 * @throws FormatException if any of the arguments is invalid.
	 */
	protected void checkParams(final int imageIndex, final long planeIndex,
		final byte[] buf, final long[] planeMin, final long[] planeMax)
		throws FormatException
	{
		SCIFIOMetadataTools.verifyMinimumPopulated(metadata, out, imageIndex);

		if (buf == null) throw new FormatException("Buffer cannot be null.");
		long planes = metadata.get(imageIndex).getPlaneCount();

		if (metadata.get(imageIndex).isMultichannel()) planes *=
			metadata.get(imageIndex).getAxisLength(Axes.CHANNEL);

		if (planeIndex < 0) throw new FormatException(String.format(
			"Plane index:%d must be >= 0", planeIndex));
		if (planeIndex >= planes) {
			throw new FormatException(String.format("Plane index:%d must be < %d",
				planeIndex, planes));
		}

		FormatTools.checkPlaneForWriting(getMetadata(), imageIndex, planeIndex,
			buf.length, planeMin, planeMax);

		final int pixelType = metadata.get(imageIndex).getPixelType();

		if (!DataTools.containsValue(getPixelTypes(compression), pixelType)) {
			throw new FormatException("Unsupported image type '" +
				FormatTools.getPixelTypeString(pixelType) + "'.");
		}
	}
}
