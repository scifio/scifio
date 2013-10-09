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
import io.scif.AbstractTranslator;
import io.scif.AbstractWriter;
import io.scif.BufferedImagePlane;
import io.scif.Field;
import io.scif.FieldPrinter;
import io.scif.Format;
import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.Plane;
import io.scif.Translator;
import io.scif.common.DataTools;
import io.scif.gui.AWTImageTools;
import io.scif.gui.BufferedImageReader;
import io.scif.io.RandomAccessInputStream;
import io.scif.io.RandomAccessOutputStream;
import io.scif.io.StreamTools;
import io.scif.util.FormatTools;
import io.scif.util.SCIFIOMetadataTools;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.DeflaterOutputStream;

import javax.imageio.ImageIO;

import net.imglib2.display.ColorTable;
import net.imglib2.display.ColorTable8;
import net.imglib2.meta.Axes;

import org.scijava.Priority;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Plugin;

/**
 * SCIFIO Format supporting the <a
 * href="http://www.libpng.org/pub/png/spec/">PNG</a> and <a
 * href="https://wiki.mozilla.org/APNG_Specification">APNG</a> image formats.
 * 
 * @author Mark Hiner
 */
@Plugin(type = Format.class)
public class APNGFormat extends AbstractFormat {

	// -- Constants --

	public static final byte[] PNG_SIGNATURE = new byte[] { (byte) 0x89, 0x50,
		0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a };

	// -- Format API Methods --

	@Override
	public String getFormatName() {
		return "Animated PNG";
	}

	@Override
	public String[] getSuffixes() {
		return new String[] { "png" };
	}

	// -- Nested Classes --

	/**
	 * File format SCIFIO Metadata for Animated Portable Network Graphics (APNG)
	 * images.
	 */
	public static class Metadata extends AbstractMetadata {

		// -- Constants --

		public static final String DEFAULT_KEY = "separate default";
		public static final String CNAME = "io.scif.formats.APNGFormat$Metadata";

		// -- Fields --

		// APNG Chunks
		private List<IDATChunk> idat;
		private List<FCTLChunk> fctl;
		private ACTLChunk actl;
		private IHDRChunk ihdr;
		private PLTEChunk plte;
		private IENDChunk iend;

		// true if the default image is not part of the animation
		private boolean separateDefault;

		// Reproduces the imageMetadata littleEndian field..
		// because APNG spec doesn't have its own little endian notation
		// (all integers are supposed to be written big endian)
		// so as an ImageMetadata field it can't be populated
		// in a translate() method, which is for format-specific
		// metadata only.
		private boolean littleEndian = false;

		// true if the pixel bits are signed
		private boolean signed = false;

		// -- Constructor --

		public Metadata() {
			fctl = new ArrayList<FCTLChunk>();
			idat = new ArrayList<IDATChunk>();
		}

		// -- APNGMetadata API Methods --

		/**
		 * @return true iff this dataset's pixel type is signed
		 */
		public boolean isSigned() {
			return signed;
		}

		/**
		 * Sets whether this dataset's pixel type is signed or not.
		 */
		public void setSigned(final boolean signed) {
			this.signed = signed;
		}

		/**
		 * @return true iff the first frame is not part of the animation.
		 */
		public boolean isSeparateDefault() {
			return separateDefault;
		}

		/**
		 * Sets whether or not the first (default) frame is part of the animation or
		 * not.
		 */
		public void setSeparateDefault(final boolean separateDefault) {
			this.separateDefault = separateDefault;
		}

		// -- Metadata API Methods --

		@Override
		public boolean isLittleEndian(final int imageIndex) {
			return littleEndian;
		}

		@Override
		public void setLittleEndian(final int imageIndex, final boolean little) {
			littleEndian = little;
		}

		@Override
		public void populateImageMetadata() {
			createImageMetadata(1);

			final ImageMetadata imageMeta = get(0);

			// These fields are fixed
			imageMeta.setOrderCertain(true);
			imageMeta.setFalseColor(false);
			imageMeta.setThumbnail(false);

			// Determine color information
			boolean indexed = false;
			boolean rgb = true;
			int sizec = 1;

			int bpp = getIhdr().getBitDepth();

			// Check the color type, directly from the png spec
			switch (getIhdr().getColourType()) {
				case 0x0:
					rgb = false;
					break;
				case 0x2:
					sizec = 3;
					break;
				case 0x3:
					indexed = true;
					sizec = 1;
					break;
				case 0x4:
					rgb = false;
					sizec = 2;
					break;
				case 0x6:
					sizec = 4;
					break;
			}

			/*
			 * TODO: destination metadata doesn't care about the LUT
			if (indexed) {
			  final byte[][] lut = new byte[3][0];

			  lut[0] = source.getPlte().getRed();
			  lut[1] = source.getPlte().getGreen();
			  lut[2] = source.getPlte().getBlue();

			  imageMeta.setLut(lut);
			}
			*/

			// The IHDR contains frame height and width
			imageMeta.setAxisTypes(Axes.X, Axes.Y);
			imageMeta.setAxisLengths(new long[] { getIhdr().getWidth(),
				getIhdr().getHeight() });
			imageMeta.setPlanarAxisCount(2);

			// Set pixel information
			imageMeta.setBitsPerPixel(bpp);
			try {
				imageMeta.setPixelType(FormatTools.pixelTypeFromBytes(bpp / 8,
					isSigned(), false));
			}
			catch (final FormatException e) {
				log().error("Failed to find pixel type from bytes: " + (bpp / 8), e);
			}

			// RGB planes are always stored planar
			if (rgb) {
				imageMeta.addAxis(Axes.CHANNEL, sizec);
			imageMeta.setPlanarAxisCount(3);
			}

			final ACTLChunk actl = getActl();
			if (actl != null) {
				imageMeta.addAxis(Axes.TIME, actl.getNumFrames());
			}

			imageMeta.setIndexed(indexed);
			imageMeta.setLittleEndian(isLittleEndian(0));

			// Some anciliary chunks may not have been parsed
			imageMeta.setMetadataComplete(false);

			get(0).getTable().put(Metadata.DEFAULT_KEY, isSeparateDefault());
			// TODO
			// coreMeta.setThumbSizeX(source.thumbSizeX);
			// coreMeta.setThumbSizeY(source.thumbSizeY);

			// coreMeta.setcLengths(source.cLengths);
			// coreMeta.setcTypes(source.cTypes);
		}

		// -- Chunk Getters and Setters --

		public List<IDATChunk> getIdat() {
			return idat;
		}

		public void setIdat(final List<IDATChunk> idat) {
			this.idat = idat;
		}

		public void addIdat(final IDATChunk idat) {
			this.idat.add(idat);
		}

		public List<FCTLChunk> getFctl() {
			return fctl;
		}

		public void setFctl(final List<FCTLChunk> fctl) {
			this.fctl = fctl;
		}

		public ACTLChunk getActl() {
			return actl;
		}

		public void setActl(final ACTLChunk actl) {
			this.actl = actl;
		}

		public IHDRChunk getIhdr() {
			return ihdr;
		}

		public void setIhdr(final IHDRChunk ihdr) {
			this.ihdr = ihdr;
		}

		public PLTEChunk getPlte() {
			return plte;
		}

		public void setPlte(final PLTEChunk plte) {
			this.plte = plte;
		}

		public IENDChunk getIend() {
			return iend;
		}

		public void setIend(final IENDChunk iend) {
			this.iend = iend;
		}

		// -- HasSource API Methods --

		@Override
		public void close(final boolean fileOnly) throws IOException {
			super.close(fileOnly);

			if (!fileOnly) {
				fctl = new ArrayList<FCTLChunk>();
				idat = new ArrayList<IDATChunk>();
			}
		}
	}

	/**
	 * File format SCIFIO Checker for Animated Portable Network Graphics (APNG)
	 * images.
	 */
	public static class Checker extends AbstractChecker {

		// -- Constructor --

		/** Constructs a new APNGChecker */
		public Checker() {
			suffixNecessary = false;
		}

		// -- Checker API Methods --

		@Override
		public boolean isFormat(final RandomAccessInputStream stream)
			throws IOException
		{
			final int blockLen = 8;
			if (!StreamTools.validStream(stream, blockLen, false)) return false;

			final byte[] signature = new byte[blockLen];
			stream.read(signature);

			if (signature[0] != (byte) 0x89 || signature[1] != 0x50 ||
				signature[2] != 0x4e || signature[3] != 0x47 || signature[4] != 0x0d ||
				signature[5] != 0x0a || signature[6] != 0x1a || signature[7] != 0x0a)
			{
				return false;
			}
			return true;
		}
	}

	/**
	 * File format SCIFIO Parser for Animated Portable Network Graphics (APNG)
	 * images.
	 */
	public static class Parser extends AbstractParser<Metadata> {

		// -- Parser API Methods --

		@Override
		protected void typedParse(final RandomAccessInputStream stream,
			final Metadata meta) throws IOException, FormatException
		{
			// check that this is a valid PNG file
			final byte[] signature = new byte[8];
			stream.read(signature);

			if (signature[0] != (byte) 0x89 || signature[1] != 0x50 ||
				signature[2] != 0x4e || signature[3] != 0x47 || signature[4] != 0x0d ||
				signature[5] != 0x0a || signature[6] != 0x1a || signature[7] != 0x0a)
			{
				throw new FormatException("Invalid PNG signature.");
			}

			// For determining if the first frame is also the default image
			boolean sawFctl = false;

			// read data chunks - each chunk consists of the following:
			// 1) 32 bit length
			// 2) 4 char type
			// 3) 'length' bytes of data
			// 4) 32 bit CRC

			while (stream.getFilePointer() < stream.length()) {
				final int length = stream.readInt();
				final String type = stream.readString(4);
				final long offset = stream.getFilePointer();

				APNGChunk chunk = null;

				if (type.equals("acTL")) {
					chunk = new ACTLChunk();
					final ACTLChunk actl = (ACTLChunk) chunk;
					actl.setNumFrames(stream.readInt());
					actl.setNumPlays(stream.readInt());
					meta.setActl(actl);
				}
				else if (type.equals("fcTL")) {
					sawFctl = true;
					chunk = new FCTLChunk();
					final FCTLChunk fctl = (FCTLChunk) chunk;
					fctl.setSequenceNumber(stream.readInt());
					fctl.setWidth(stream.readInt());
					fctl.setHeight(stream.readInt());
					fctl.setxOffset(stream.readInt());
					fctl.setyOffset(stream.readInt());
					fctl.setDelayNum(stream.readShort());
					fctl.setDelayDen(stream.readShort());
					fctl.setDisposeOp(stream.readByte());
					fctl.setBlendOp(stream.readByte());
					meta.getFctl().add(fctl);
				}
				else if (type.equals("IDAT")) {
					meta.setSeparateDefault(!sawFctl);
					chunk = new IDATChunk();
					meta.addIdat((IDATChunk) chunk);
					stream.skipBytes(length);
				}
				else if (type.equals("fdAT")) {
					chunk = new FDATChunk();
					((FDATChunk) chunk).setSequenceNumber(stream.readInt());
					meta.getFctl().get(meta.getFctl().size() - 1).addChunk(
						((FDATChunk) chunk));
					stream.skipBytes(length - 4);
				}
				else if (type.equals("IHDR")) {
					chunk = new IHDRChunk();
					final IHDRChunk ihdr = (IHDRChunk) chunk;
					ihdr.setWidth(stream.readInt());
					ihdr.setHeight(stream.readInt());
					ihdr.setBitDepth(stream.readByte());
					ihdr.setColourType(stream.readByte());
					ihdr.setCompressionMethod(stream.readByte());
					ihdr.setFilterMethod(stream.readByte());
					ihdr.setInterlaceMethod(stream.readByte());
					meta.setIhdr(ihdr);
				}
				else if (type.equals("PLTE")) {
					chunk = new PLTEChunk();
					final PLTEChunk plte = (PLTEChunk) chunk;

					final byte[] red = new byte[length / 3];
					final byte[] blue = new byte[length / 3];
					final byte[] green = new byte[length / 3];

					for (int i = 0; i < length / 3; i++) {
						red[i] = stream.readByte();
						green[i] = stream.readByte();
						blue[i] = stream.readByte();
					}

					plte.setRed(red);
					plte.setGreen(green);
					plte.setBlue(blue);

					meta.setPlte(plte);
				}
				else if (type.equals("IEND")) {
					chunk = new IENDChunk();
					stream.skipBytes((int) (stream.length() - stream.getFilePointer()));
					meta.setIend((IENDChunk) chunk);
				}
				else stream.skipBytes(length);

				if (chunk != null) {
					chunk.setOffset(offset);
					chunk.setLength(length);
				}

				if (stream.getFilePointer() < stream.length() - 4) {
					stream.skipBytes(4); // skip the CRC
				}
			}
		}
	}

	/**
	 * File format SCIFIO Reader for Animated Portable Network Graphics (APNG)
	 * images.
	 */
	public static class Reader extends BufferedImageReader<Metadata> {

		// -- Fields --

		// Cached copy of the last plane that was returned.
		private BufferedImagePlane lastPlane;

		// Plane index of the last plane that was returned.
		private int lastPlaneIndex = -1;

		// -- Constructor --

		public Reader() {
			domains = new String[] { FormatTools.GRAPHICS_DOMAIN };
		}

		// -- Reader API Methods --

		@Override
		public void setMetadata(final Metadata meta) throws IOException {
			lastPlaneIndex = -1;
			lastPlane = null;
			super.setMetadata(meta);
		}

		@Override
		public BufferedImagePlane openPlane(final int imageIndex,
			final int planeIndex, final BufferedImagePlane plane,
			final long[] planeMin, final long[] planeMax) throws FormatException,
			IOException
		{
			final Metadata meta = getMetadata();
			FormatTools.checkPlaneParameters(meta, imageIndex, planeIndex, -1,
				planeMin, planeMax);

			// If the last processed (cached) plane is requested, return the
			// requested sub-image, but don't update the last plane (in case the
			// full plane was not requested)
			if (planeIndex == lastPlaneIndex && lastPlane != null) {

				final BufferedImage subImage =
					AWTImageTools.getSubimage(lastPlane.getData(), meta
						.isLittleEndian(imageIndex), planeMin, planeMax);
				plane.setData(subImage);
				return plane;
			}
			else if (lastPlane == null) {
				lastPlane = createPlane(planeMin, planeMax);
				if (getMetadata().isIndexed(imageIndex)) {
					final PLTEChunk plte = meta.getPlte();
					if (plte != null) {
						final ColorTable ct =
							new ColorTable8(plte.getRed(), plte.getGreen(), plte.getBlue());
						plane.setColorTable(ct);
					}
				}
			}

			// The default frame is requested and we can use the standard
			// Java ImageIO to extract it
			if (planeIndex == 0) {
				getStream().seek(0);
				final DataInputStream dis =
					new DataInputStream(new BufferedInputStream(getStream(), 4096));
				BufferedImage subImg = ImageIO.read(dis);
				lastPlane.populate(meta.get(imageIndex), subImg, planeMin,
					planeMax);

				lastPlaneIndex = 0;

				plane.setData(lastPlane.getData());

				if (!SCIFIOMetadataTools.wholePlane(imageIndex, meta, planeMin,
					planeMax))
				{
					// updates the data of the plane to a sub-image, by reference
					subImg =
						AWTImageTools.getSubimage(lastPlane.getData(), meta
							.isLittleEndian(imageIndex), planeMin, planeMax);
					plane.setData(subImg);
				}

				return plane;
			}

			// For a non-default frame, the appropriate chunks will be used to create
			// a new image,
			// which will be read with the standard Java ImageIO and pasted onto frame
			// 0.
			final ByteArrayOutputStream stream = new ByteArrayOutputStream();
			stream.write(APNGFormat.PNG_SIGNATURE);

			final int[] coords =
				metadata.getFctl().get(planeIndex).getFrameCoordinates();
			// process IHDR chunk
			final IHDRChunk ihdr = metadata.getIhdr();
			processChunk(imageIndex, ihdr.getLength(), ihdr.getOffset(), coords,
				stream, true);

			// process fcTL and fdAT chunks
			final FCTLChunk fctl =
				metadata.getFctl().get(
					metadata.isSeparateDefault() ? planeIndex - 1 : planeIndex);

			// fdAT chunks are converted to IDAT chunks, as we are essentially
			// building a standalone single-frame image
			for (final FDATChunk fdat : fctl.getFdatChunks()) {
				getStream().seek(fdat.getOffset() + 4);
				byte[] b = new byte[fdat.getLength() + 8];
				DataTools.unpackBytes(fdat.getLength() - 4, b, 0, 4, getMetadata()
					.isLittleEndian(imageIndex));
				b[4] = 'I';
				b[5] = 'D';
				b[6] = 'A';
				b[7] = 'T';
				getStream().read(b, 8, b.length - 12);
				final int crc = (int) computeCRC(b, b.length - 4);
				DataTools.unpackBytes(crc, b, b.length - 4, 4, getMetadata()
					.isLittleEndian(imageIndex));
				stream.write(b);
				b = null;
			}

			// process PLTE chunks
			final PLTEChunk plte = metadata.getPlte();
			if (plte != null) {
				processChunk(imageIndex, plte.getLength(), plte.getOffset(), coords,
					stream, false);
			}
			final RandomAccessInputStream s =
				new RandomAccessInputStream(getContext(), stream.toByteArray());
			final DataInputStream dis =
				new DataInputStream(new BufferedInputStream(s, 4096));
			final BufferedImage bi = ImageIO.read(dis);
			dis.close();

			// Recover first plane
			openPlane(imageIndex, 0, new long[] { 0l, 0l }, new long[] {
				getMetadata().getAxisLength(imageIndex, Axes.X),
				getMetadata().getAxisLength(imageIndex, Axes.Y) });

			// paste current image onto first plane
			// NB: last plane read was the first plane

			final WritableRaster firstRaster = lastPlane.getData().getRaster();
			final WritableRaster currentRaster = bi.getRaster();

			firstRaster.setDataElements(coords[0], coords[1], currentRaster);
			final BufferedImage bImg =
				new BufferedImage(lastPlane.getData().getColorModel(), firstRaster,
					false, null);

			lastPlane.populate(getMetadata().get(imageIndex), bImg, planeMin, planeMax);

			lastPlaneIndex = planeIndex;
			return plane.populate(lastPlane);
		}

		@Override
		public void close(final boolean fileOnly) throws IOException {
			super.close(fileOnly);

			if (!fileOnly) {
				lastPlane = null;
				lastPlaneIndex = -1;
			}
		}

		// -- Helper methods --

		private long computeCRC(final byte[] buf, final int len) {
			final CRC32 crc = new CRC32();
			crc.update(buf, 0, len);
			return crc.getValue();
		}

		private void processChunk(final int imageIndex, final int length,
			final long offset, final int[] coords,
			final ByteArrayOutputStream stream, final boolean isIHDR)
			throws IOException
		{
			byte[] b = new byte[length + 12];
			DataTools.unpackBytes(length, b, 0, 4, getMetadata().isLittleEndian(
				imageIndex));
			final byte[] typeBytes = (isIHDR ? "IHDR".getBytes() : "PLTE".getBytes());
			System.arraycopy(typeBytes, 0, b, 4, 4);
			getStream().seek(offset);
			getStream().read(b, 8, b.length - 12);
			if (isIHDR) {
				DataTools.unpackBytes(coords[2], b, 8, 4, getMetadata().isLittleEndian(
					imageIndex));
				DataTools.unpackBytes(coords[3], b, 12, 4, getMetadata()
					.isLittleEndian(imageIndex));
			}
			final int crc = (int) computeCRC(b, b.length - 4);
			DataTools.unpackBytes(crc, b, b.length - 4, 4, getMetadata()
				.isLittleEndian(imageIndex));
			stream.write(b);
			b = null;
		}
	}

	/**
	 * The SCIFIO file format writer for PNG and APNG files.
	 */
	public static class Writer extends AbstractWriter<Metadata> {

		// -- Fields --

		// Number of frames written
		private int numFrames = 0;

		// Pointer to position in acTL chunk to write the number of frames in this
		// image
		private long numFramesPointer = 0;

		// Current sequence number, shared by fcTL and fdAT frames to indicate
		// ordering
		private int nextSequenceNumber;

		// -- Writer API Methods --

		@Override
		public void savePlane(final int imageIndex, final int planeIndex,
			final Plane plane, final long[] planeMin, final long[] planeMax)
			throws FormatException, IOException
		{
			checkParams(imageIndex, planeIndex, plane.getBytes(), planeMin, planeMax);
			if (!SCIFIOMetadataTools.wholePlane(imageIndex, getMetadata(), planeMin, planeMax)) {
				throw new FormatException(
					"APNGWriter does not yet support saving image tiles.");
			}

			final int width = (int)getMetadata().getAxisLength(imageIndex, Axes.X);
			final int height = (int)getMetadata().getAxisLength(imageIndex, Axes.Y);

			if (!initialized[imageIndex][planeIndex]) {
				if (numFrames == 0) {
					if (!metadata.isSeparateDefault()) {
						// first frame is default image
						writeFCTL(width, height, planeIndex);
					}
					writePLTE();
				}
				initialized[imageIndex][planeIndex] = true;
			}

			// write the data for this frame

			if (numFrames == 0) {
				// This is the first frame, and also the default image
				writePixels(imageIndex, "IDAT", plane.getBytes(), planeMin, planeMax);
			}
			else {
				writeFCTL(width, height, planeIndex);
				writePixels(imageIndex, "fdAT", plane.getBytes(), planeMin, planeMax);
			}
			numFrames++;
		}

		@Override
		public boolean canDoStacks() {
			return true;
		}

		@Override
		public int[] getPixelTypes(final String codec) {
			return new int[] { FormatTools.INT8, FormatTools.UINT8,
				FormatTools.INT16, FormatTools.UINT16 };
		}

		// -- APNGWriter Methods --

		@Override
		public void
			setDest(final RandomAccessOutputStream out, final int imageIndex)
				throws FormatException, IOException
		{
			super.setDest(out, imageIndex);
			initialize(imageIndex);
		}

		// -- HasSource API Methods --

		@Override
		public void close(final boolean fileOnly) throws IOException {
			if (out != null) {
				writeFooter();
			}
			super.close(fileOnly);
			numFrames = 0;
			numFramesPointer = 0;
			nextSequenceNumber = 0;
		}

		// -- Helper Methods --

		private void initialize(final int imageIndex) throws FormatException,
			IOException
		{
			if (out.length() == 0) {
				final int width = (int)getMetadata().getAxisLength(imageIndex, Axes.X);
				final int height = (int)getMetadata().getAxisLength(imageIndex, Axes.Y);
				final int bytesPerPixel =
					FormatTools.getBytesPerPixel(getMetadata().getPixelType(imageIndex));
				final int nChannels =
					(int)getMetadata().getAxisLength(imageIndex, Axes.CHANNEL);
				final boolean indexed =
					getColorModel() != null &&
						(getColorModel() instanceof IndexColorModel);

				// write 8-byte PNG signature
				out.write(APNGFormat.PNG_SIGNATURE);

				// write IHDR chunk
				out.writeInt(13);
				final byte[] b = new byte[17];
				b[0] = 'I';
				b[1] = 'H';
				b[2] = 'D';
				b[3] = 'R';

				DataTools.unpackBytes(width, b, 4, 4, false);
				DataTools.unpackBytes(height, b, 8, 4, false);

				b[12] = (byte) (bytesPerPixel * 8);
				if (indexed) b[13] = (byte) 3;
				else if (nChannels == 1) b[13] = (byte) 0;
				else if (nChannels == 2) b[13] = (byte) 4;
				else if (nChannels == 3) b[13] = (byte) 2;
				else if (nChannels == 4) b[13] = (byte) 6;
				b[14] = metadata.getIhdr().getCompressionMethod();
				b[15] = metadata.getIhdr().getFilterMethod();
				b[16] = metadata.getIhdr().getInterlaceMethod();

				out.write(b);
				out.writeInt(crc(b));

				// write acTL chunk

				final ACTLChunk actl = metadata.getActl();

				out.writeInt(8);
				out.writeBytes("acTL");
				numFramesPointer = out.getFilePointer();
				out.writeInt(actl == null ? 0 : actl.getNumFrames());
				out.writeInt(actl == null ? 0 : actl.getNumPlays());
				out.writeInt(0); // save a place for the CRC
			}
		}

		private int crc(final byte[] buf) {
			return crc(buf, 0, buf.length);
		}

		private int crc(final byte[] buf, final int off, final int len) {
			final CRC32 crc = new CRC32();
			crc.update(buf, off, len);
			return (int) crc.getValue();
		}

		private void writeFCTL(final int width, final int height,
			final int planeIndex) throws IOException
		{
			out.writeInt(26);
			final FCTLChunk fctl =
				metadata.getFctl().get(
					metadata.isSeparateDefault() ? planeIndex - 1 : planeIndex);
			final byte[] b = new byte[30];

			DataTools.unpackBytes(22, b, 0, 4, false);
			b[0] = 'f';
			b[1] = 'c';
			b[2] = 'T';
			b[3] = 'L';

			DataTools.unpackBytes(nextSequenceNumber++, b, 4, 4, false);
			DataTools.unpackBytes(fctl.getWidth(), b, 8, 4, false);
			DataTools.unpackBytes(fctl.getHeight(), b, 12, 4, false);
			DataTools.unpackBytes(fctl.getxOffset(), b, 16, 4, false);
			DataTools.unpackBytes(fctl.getyOffset(), b, 20, 4, false);
			DataTools.unpackBytes(fctl.getDelayNum(), b, 24, 2, false);
			DataTools.unpackBytes(fctl.getDelayDen(), b, 26, 2, false);
			b[28] = fctl.getDisposeOp();
			b[29] = fctl.getBlendOp();

			out.write(b);
			out.writeInt(crc(b));
		}

		private void writePLTE() throws IOException {
			if (!(getColorModel() instanceof IndexColorModel)) return;

			final IndexColorModel model = (IndexColorModel) getColorModel();
			final byte[][] lut = new byte[3][256];
			model.getReds(lut[0]);
			model.getGreens(lut[1]);
			model.getBlues(lut[2]);

			out.writeInt(768);
			final byte[] b = new byte[772];
			b[0] = 'P';
			b[1] = 'L';
			b[2] = 'T';
			b[3] = 'E';

			for (int i = 0; i < lut[0].length; i++) {
				for (int j = 0; j < lut.length; j++) {
					b[i * lut.length + j + 4] = lut[j][i];
				}
			}

			out.write(b);
			out.writeInt(crc(b));
		}

		private void writePixels(final int imageIndex, final String chunk,
			final byte[] stream, final long[] planeMin, final long[] planeMax)
			throws FormatException, IOException
		{

			final long rgbCCount =
				getMetadata().getAxisLength(imageIndex, Axes.CHANNEL);

			final int pixelType = getMetadata().getPixelType(imageIndex);
			final boolean signed = FormatTools.isSigned(pixelType);

			if (!SCIFIOMetadataTools.wholePlane(imageIndex, getMetadata(), planeMin,
				planeMax))
			{
				throw new FormatException("APNGWriter does not support writing tiles.");
			}

			final int width = (int) getMetadata().getAxisLength(imageIndex, Axes.X);
			final int height = (int) getMetadata().getAxisLength(imageIndex, Axes.Y);

			final ByteArrayOutputStream s = new ByteArrayOutputStream();
			s.write(chunk.getBytes());
			if (chunk.equals("fdAT")) {
				s.write(DataTools.intToBytes(nextSequenceNumber++, false));
			}
			final DeflaterOutputStream deflater = new DeflaterOutputStream(s);
			final long planeSize = stream.length / rgbCCount;
			final int rowLen = stream.length / height;
			final int bytesPerPixel = stream.length / (int)(width * height * rgbCCount);
			final byte[] rowBuf = new byte[rowLen];
			for (int i = 0; i < height; i++) {
				deflater.write(0);
				if (interleaved) {
					if (getMetadata().isLittleEndian(0)) {
						for (int col = 0; col < width * rgbCCount; col++) {
							final int offset = (int)(i * rgbCCount * width + col) * bytesPerPixel;
							final int pixel =
								DataTools.bytesToInt(stream, offset, bytesPerPixel,
									getMetadata().isLittleEndian(0));
							DataTools.unpackBytes(pixel, rowBuf, col * bytesPerPixel,
								bytesPerPixel, false);
						}
					}
					else System.arraycopy(stream, i * rowLen, rowBuf, 0, rowLen);
				}
				else {
					final int max = (int) Math.pow(2, bytesPerPixel * 8 - 1);
					for (int col = 0; col < width; col++) {
						for (int c = 0; c < rgbCCount; c++) {
							final int offset = (int)
								(c * planeSize + (i * width + col) * bytesPerPixel);
							int pixel =
								DataTools.bytesToInt(stream, offset, bytesPerPixel,
									getMetadata().isLittleEndian(0));
							if (signed) {
								if (pixel < max) pixel += max;
								else pixel -= max;
							}
							final int output = (int)(col * rgbCCount + c) * bytesPerPixel;
							DataTools
								.unpackBytes(pixel, rowBuf, output, bytesPerPixel, false);
						}
					}
				}
				deflater.write(rowBuf);
			}
			deflater.finish();
			final byte[] b = s.toByteArray();

			// write chunk length
			out.writeInt(b.length - 4);
			out.write(b);

			// write checksum
			out.writeInt(crc(b));
		}

		private void writeFooter() throws IOException {
			// write IEND chunk
			out.writeInt(0);
			out.writeBytes("IEND");
			out.writeInt(crc("IEND".getBytes()));

			// update frame count
			out.seek(numFramesPointer);
			out.writeInt(numFrames);
			out.skipBytes(4);
			final byte[] b = new byte[12];
			b[0] = 'a';
			b[1] = 'c';
			b[2] = 'T';
			b[3] = 'L';
			DataTools.unpackBytes(numFrames, b, 4, 4, false);
			DataTools.unpackBytes(metadata.getActl() == null ? 0 : metadata.getActl()
				.getNumPlays(), b, 8, 4, false);
			out.writeInt(crc(b));
		}
	}

	/**
	 * This class can be used for translating any io.scif.Metadata to Metadata for
	 * writing Animated Portable Network Graphics (APNG) files.
	 * <p>
	 * Note that Metadata translated from Core is only write-safe.
	 * </p>
	 * <p>
	 * If trying to read, there should already exist an originally-parsed APNG
	 * Metadata object which can be used.
	 * </p>
	 * <p>
	 * Note also that any APNG image written must be reparsed, as the Metadata
	 * used to write it can not be guaranteed valid.
	 * </p>
	 */
	@Plugin(type = Translator.class, attrs = {
		@Attr(name = APNGTranslator.SOURCE, value = io.scif.Metadata.CNAME),
		@Attr(name = APNGTranslator.DEST, value = Metadata.CNAME) },
		priority = Priority.LOW_PRIORITY)
	public static class APNGTranslator extends
		AbstractTranslator<io.scif.Metadata, Metadata>
	{

		// -- Translator API Methods --

		@Override
		public void typedTranslate(final io.scif.Metadata source,
			final Metadata dest)
		{
			final IHDRChunk ihdr =
				dest.getIhdr() == null ? new IHDRChunk() : dest.getIhdr();
			final PLTEChunk plte =
				dest.getPlte() == null ? new PLTEChunk() : dest.getPlte();
			final ACTLChunk actl =
				dest.getActl() == null ? new ACTLChunk() : dest.getActl();
			final List<FCTLChunk> fctl = new ArrayList<FCTLChunk>();

			dest.setIhdr(ihdr);
			dest.setPlte(plte);
			dest.setActl(actl);
			dest.setFctl(fctl);

			ihdr.setWidth((int)source.getAxisLength(0, Axes.X));
			ihdr.setHeight((int)source.getAxisLength(0, Axes.Y));
			ihdr.setBitDepth((byte) source.getBitsPerPixel(0));
			ihdr.setFilterMethod((byte) 0);
			ihdr.setCompressionMethod((byte) 0);
			ihdr.setInterlaceMethod((byte) 0);

			final long sizec =
				source.isMultichannel(0) ? source.getAxisLength(0, Axes.CHANNEL) : 1;
			final boolean indexed = source.isIndexed(0);

			if (indexed) {
				ihdr.setColourType((byte) 0x3);

				/*
				 * NB: not necessary to preserve ColorTable when translating. If
				 * an image has a color table it will be parsed and included in
				 * whatever plane is returned by an openPlane call. So it doesn't
				 * also need to be preserved in the Metadata.
				byte[][] lut = null;
				try {
				  lut = source.get8BitLookupTable(0);
				  plte.setRed(lut[0]);
				  plte.setGreen(lut[1]);
				  plte.setBlue(lut[2]);
				}
				catch (final FormatException e) {
				  log().error("Format error when finding 8bit lookup table", e);
				}
				catch (final IOException e) {
				  log().error("IO error when finding 8bit lookup table", e);
				}
				*/
			}
			else if (sizec == 2) {
				// grayscale with alpha
				ihdr.setColourType((byte) 0x4);
				// Each pixel is 2 samples. Bit depth is bits per sample
				// and not per pixel. Thus we divide by 2.
				ihdr.setBitDepth((byte) (ihdr.getBitDepth() / 2));
			}
			else if (sizec == 4) {
				// each pixel is an rgb triple, plus alpha
				ihdr.setColourType((byte) 0x6);
				// Each pixel is 2 samples. Bit depth is bits per sample
				// and not per pixel. Thus we divide by 2.
				ihdr.setBitDepth((byte) (ihdr.getBitDepth() / 2));
			}
			else if (sizec != 3) {
				// grayscale image
				ihdr.setColourType((byte) 0x0);
			}
			else {
				// each pixel is an RGB triple
				ihdr.setColourType((byte) 0x2);
			}

			actl.setNumFrames(source.getPlaneCount(0));

			for (int i = 0; i < actl.getNumFrames(); i++) {
				final FCTLChunk frame = new FCTLChunk();
				frame.setHeight(ihdr.getHeight());
				frame.setWidth(ihdr.getWidth());
				frame.setxOffset(0);
				frame.setyOffset(0);
				frame.setSequenceNumber(i);
				frame.setDelayDen((short) 0);
				frame.setDelayNum((short) 0);
				frame.setBlendOp((byte) 0);
				frame.setDisposeOp((byte) 0);
				fctl.add(frame);
			}

			// FIXME: all integers in apng should be written big endian per spec
			// but for bio-formats endianness is supposed to be preserved... resolve?
			dest.setLittleEndian(0, source.isLittleEndian(0));

			final boolean signed = FormatTools.isSigned(source.getPixelType(0));
			dest.setSigned(signed);

			final Object separateDefault =
				source.get(0).getTable().get(Metadata.DEFAULT_KEY);
			dest.setSeparateDefault(separateDefault == null ? false
				: (Boolean) separateDefault);
		}
	}

	/**
	 * A parent class for all APNG Chunk classes.
	 * <p>
	 * Provides a length and offset (in the overall file stream) field.
	 * </p>
	 * <p>
	 * Each chunk should instantiate and define its own CHUNK_SIGNATURE.
	 * </p>
	 */
	public static class APNGChunk {

		// -- Fields --

		// Offset in the file data stream. Points to the start of the
		// data of the chunk, which comes after an entry for the length
		// and the chunk's signature.
		private long offset;

		// Length of the chunk
		private int length;

		// Unique chunk type signature (e.g. "IHDR")
		protected byte[] CHUNK_SIGNATURE;

		// -- Methods --

		public byte[] getCHUNK_SIGNATURE() {
			return CHUNK_SIGNATURE;
		}

		public int[] getFrameCoordinates() {
			return new int[0];
		}

		public void setOffset(final long offset) {
			this.offset = offset;
		}

		public long getOffset() {
			return offset;
		}

		public void setLength(final int length) {
			this.length = length;
		}

		public int getLength() {
			return length;
		}

		@Override
		public String toString() {
			return new FieldPrinter(this).toString();
		}

	}

	/**
	 * Represents the IHDR chunk of the APNG image format.
	 * <p>
	 * The IHDR chunk is a critical chunk for all APNG and PNG images. It contains
	 * basic information about the image.
	 * </p>
	 * <p>
	 * The IHDR is always the first chunk of a correct PNG or APNG image file.
	 * </p>
	 */
	public static class IHDRChunk extends APNGChunk {

		// -- Constructor --

		public IHDRChunk() {
			CHUNK_SIGNATURE = new byte[] { (byte) 0x49, 0x48, 0x44, 0x52 };
		}

		// -- Fields --

		@Field(label = "Width")
		private int width;

		@Field(label = "height")
		private int height;

		@Field(label = "Bit depth")
		private byte bitDepth;

		@Field(label = "Colour type")
		private byte colourType;

		@Field(label = "Compression Method")
		private byte compressionMethod;

		@Field(label = "Filter method")
		private byte filterMethod;

		@Field(label = "Interlace method")
		private byte interlaceMethod;

		// -- Methods --

		public int getWidth() {
			return width;
		}

		public void setWidth(final int width) {
			this.width = width;
		}

		public int getHeight() {
			return height;
		}

		public void setHeight(final int height) {
			this.height = height;
		}

		public byte getBitDepth() {
			return bitDepth;
		}

		public void setBitDepth(final byte bitDepth) {
			this.bitDepth = bitDepth;
		}

		public byte getColourType() {
			return colourType;
		}

		public void setColourType(final byte colourType) {
			this.colourType = colourType;
		}

		public byte getCompressionMethod() {
			return compressionMethod;
		}

		public void setCompressionMethod(final byte compressionMethod) {
			this.compressionMethod = compressionMethod;
		}

		public byte getFilterMethod() {
			return filterMethod;
		}

		public void setFilterMethod(final byte filterMethod) {
			this.filterMethod = filterMethod;
		}

		public byte getInterlaceMethod() {
			return interlaceMethod;
		}

		public void setInterlaceMethod(final byte interlaceMethod) {
			this.interlaceMethod = interlaceMethod;
		}
	}

	/**
	 * Represents the PLTE chunk of the APNG image format.
	 * <p>
	 * The PLTE chunk contains color palette data for the current image and is
	 * only present in certain ARGB color formats.
	 * </p>
	 */
	public static class PLTEChunk extends APNGChunk {

		// -- Constructor --

		public PLTEChunk() {
			CHUNK_SIGNATURE = new byte[] { (byte) 0x50, 0x4C, 0x54, 0x45 };
		}

		// -- Fields --

		// Red palette entries
		private byte[] red;

		// Green palette entries
		private byte[] green;

		// Blue palette entries
		private byte[] blue;

		// -- Methods --

		public byte[] getRed() {
			return red;
		}

		public void setRed(final byte[] red) {
			this.red = red;
		}

		public byte[] getGreen() {
			return green;
		}

		public void setGreen(final byte[] green) {
			this.green = green;
		}

		public byte[] getBlue() {
			return blue;
		}

		public void setBlue(final byte[] blue) {
			this.blue = blue;
		}

	}

	/**
	 * Represents the fcTL chunk of the APNG image format.
	 * <p>
	 * The fcTL chunk contains metadata for a matching fdAT chunk, or IDAT chunk
	 * (if the default image is also the first frame of the animation).
	 * </p>
	 */
	public static class FCTLChunk extends APNGChunk {

		// -- Fields --

		/* Sequence number of the animation chunk, starting from 0 */
		@Field(label = "sequence_number")
		private int sequenceNumber;

		/* Width of the following frame */
		@Field(label = "width")
		private int width;

		/* Height of the following frame */
		@Field(label = "height")
		private int height;

		/* X position at which to render the following frame */
		@Field(label = "x_offset")
		private int xOffset;

		/* Y position at which to render the following frame */
		@Field(label = "y_offset")
		private int yOffset;

		/* Frame delay fraction numerator */
		@Field(label = "delay_num")
		private short delayNum;

		/* Frame delay fraction denominator */
		@Field(label = "delay_den")
		private short delayDen;

		/* Type of frame area disposal to be done after rendering this frame */
		@Field(label = "dispose_op")
		private byte disposeOp;

		/* Type of frame area rendering for this frame */
		@Field(label = "blend_op")
		private byte blendOp;

		private final List<FDATChunk> fdatChunks;

		// -- Constructor --

		public FCTLChunk() {
			fdatChunks = new ArrayList<FDATChunk>();
			CHUNK_SIGNATURE = new byte[] { (byte) 0x66, 0x63, 0x54, 0x4C };
		}

		// -- Methods --

		public void addChunk(final FDATChunk chunk) {
			fdatChunks.add(chunk);
		}

		public int getSequenceNumber() {
			return sequenceNumber;
		}

		public void setSequenceNumber(final int sequenceNumber) {
			this.sequenceNumber = sequenceNumber;
		}

		public int getWidth() {
			return width;
		}

		public void setWidth(final int width) {
			this.width = width;
		}

		public int getHeight() {
			return height;
		}

		public void setHeight(final int height) {
			this.height = height;
		}

		public int getxOffset() {
			return xOffset;
		}

		public void setxOffset(final int xOffset) {
			this.xOffset = xOffset;
		}

		public int getyOffset() {
			return yOffset;
		}

		public void setyOffset(final int yOffset) {
			this.yOffset = yOffset;
		}

		public short getDelayNum() {
			return delayNum;
		}

		public void setDelayNum(final short delayNum) {
			this.delayNum = delayNum;
		}

		public short getDelayDen() {
			return delayDen;
		}

		public void setDelayDen(final short delayDen) {
			this.delayDen = delayDen;
		}

		public byte getDisposeOp() {
			return disposeOp;
		}

		public void setDisposeOp(final byte disposeOp) {
			this.disposeOp = disposeOp;
		}

		public byte getBlendOp() {
			return blendOp;
		}

		public void setBlendOp(final byte blendOp) {
			this.blendOp = blendOp;
		}

		public List<FDATChunk> getFdatChunks() {
			return fdatChunks;
		}

		// -- Helper Method --
		@Override
		public int[] getFrameCoordinates() {
			return new int[] { xOffset, yOffset, width, height };
		}
	}

	/**
	 * Represents the IDAT chunk of the APNG image format.
	 * <p>
	 * The IDAT chunk is simply a dump of compressed image data for a single plane
	 * (the default image for the file).
	 * </p>
	 */
	public static class IDATChunk extends APNGChunk {

		// -- Constructor --

		public IDATChunk() {
			CHUNK_SIGNATURE = new byte[] { (byte) 0x49, 0x44, 0x41, 0x54 };
		}

	}

	/**
	 * Represents the acTL chunk of the APNG image format.
	 * <p>
	 * There is one acTL chunk per APNG image, and is not present in PNG files.
	 * </p>
	 * <p>
	 * The acTL chunk contains metadata describing the number of frames in the
	 * image, and how many times the animation sequence should be played.
	 * </p>
	 */
	public static class ACTLChunk extends APNGChunk {

		// -- Constructor --

		public ACTLChunk() {
			CHUNK_SIGNATURE = new byte[] { (byte) 0x61, 0x63, 0x54, 0x4C };
		}

		// -- Fields --

		/* Sequence number of the animation chunk, starting from 0 */
		@Field(label = "sequence_number")
		private int sequenceNumber;

		/* Number of frames in this APNG file */
		@Field(label = "num_frames")
		private int numFrames;

		/* Times to play the animation sequence */
		@Field(label = "num_plays")
		private int numPlays;

		// -- Methods --

		public int getNumFrames() {
			return numFrames;
		}

		public void setNumFrames(final int numFrames) {
			this.numFrames = numFrames;
		}

		public int getNumPlays() {
			return numPlays;
		}

		public void setNumPlays(final int numPlays) {
			this.numPlays = numPlays;
		}

		public int getSequenceNumber() {
			return sequenceNumber;
		}

		public void setSequenceNumber(final int sequenceNumber) {
			this.sequenceNumber = sequenceNumber;
		}
	}

	/**
	 * Represents the fdAT chunk of the APNG image format.
	 * <p>
	 * The fdAT chunk is identical in concept to the IDAT chunk: a container for
	 * compressed image data for a single frame.
	 * </p>
	 * <p>
	 * In the case of fdAT chunks, the image is of a non-default frame.
	 * </p>
	 * <p>
	 * Each fdAT chunk is paired with an fcTL chunk.
	 * </p>
	 */
	public static class FDATChunk extends APNGChunk {

		// -- Constructor --

		public FDATChunk() {
			CHUNK_SIGNATURE = new byte[] { (byte) 0x66, 0x64, 0x41, 0x54 };
		}

		// -- Fields --

		/** Sequence number of the animation chunk, starting from 0 */
		@Field(label = "sequence_number")
		private int sequenceNumber;

		// -- Methods --

		public int getSequenceNumber() {
			return sequenceNumber;
		}

		public void setSequenceNumber(final int sequenceNumber) {
			this.sequenceNumber = sequenceNumber;
		}
	}

	/**
	 * This class represents the critical IEND chunk that signifies the end of a
	 * PNG stream.
	 * 
	 * @author Mark Hiner
	 */
	public static class IENDChunk extends APNGChunk {

		// -- Constructor --
		public IENDChunk() {
			CHUNK_SIGNATURE = new byte[] { (byte) 0x49, 0x45, 0x4E, 0x44 };
		}
	}
}
