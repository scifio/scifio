package ome.scifio.apng;

import java.awt.image.IndexColorModel;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.DeflaterOutputStream;

import net.imglib2.meta.Axes;
import ome.scifio.AbstractWriter;
import ome.scifio.FormatException;
import ome.scifio.SCIFIO;
import ome.scifio.common.DataTools;
import ome.scifio.io.RandomAccessOutputStream;
import ome.scifio.util.FormatTools;

/**
 * The SCIFIO file format writer for PNG and APNG files.
 * 
 */
public class APNGWriter extends AbstractWriter<APNGMetadata> {

	// -- Constants --

	private static final byte[] PNG_SIG = new byte[] { (byte) 0x89, 0x50, 0x4e,
			0x47, 0x0d, 0x0a, 0x1a, 0x0a };

	// -- Fields --

	// Number of frames written
	private int numFrames = 0;

	// Pointer to position in acTL chunk to write the number of frames in this
	// image
	private long numFramesPointer = 0;

	// Current sequence number, shared by fcTL and fdAT frames to indicate
	// ordering
	private int nextSequenceNumber;
	private boolean littleEndian;

	// -- Constructor --

	public APNGWriter() {
		this(null);
	}

	public APNGWriter(final SCIFIO ctx) {
		super("Animated PNG", "png", ctx);
	}

	// -- Writer API Methods --

	/**
	 * @see ome.scifio.Writer#saveBytes(int, byte[], int, int, int, int)
	 */
	public void saveBytes(final int imageIndex, final int planeIndex,
			final byte[] buf, final int x, final int y, final int w, final int h)
			throws FormatException, IOException {
		checkParams(imageIndex, planeIndex, buf, x, y, w, h);
		if (!isFullPlane(imageIndex, x, y, w, h)) {
			throw new FormatException(
					"APNGWriter does not yet support saving image tiles.");
		}

		final int width = cMeta.getAxisLength(imageIndex, Axes.X);
		final int height = cMeta.getAxisLength(imageIndex, Axes.Y);

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
			writePixels(imageIndex, "IDAT", buf, x, y, w, h);
		} else {
			writeFCTL(width, height, planeIndex);
			writePixels(imageIndex, "fdAT", buf, x, y, w, h);
		}
		numFrames++;
	}

	/* @see ome.scifio.Writer#canDoStacks() */
	public boolean canDoStacks() {
		return true;
	}

	/* @see ome.scifio.Writer#getPixelTypes(String) */
	public int[] getPixelTypes(final String codec) {
		return new int[] { FormatTools.INT8, FormatTools.UINT8,
				FormatTools.INT16, FormatTools.UINT16 };
	}

	/* @see Writer#setMetadata(M) */
	public void setMetadata(final APNGMetadata meta) {
		super.setMetadata(meta, new APNGCoreTranslator());
	}

	// -- APNGWriter Methods --

	/* @see ome.scifio.Writer#close() */
	public void close() throws IOException {
		if (out != null) {
			writeFooter();
		}
		super.close();
		numFrames = 0;
		numFramesPointer = 0;
		nextSequenceNumber = 0;
		littleEndian = false;
	}

	/* @see ome.scifio.Writer#setDest(RandomAccessOutputStream, int) */
	public void setDest(final RandomAccessOutputStream out, final int imageIndex)
			throws FormatException, IOException {
		super.setDest(out, imageIndex);
		initialize(imageIndex);
	}

	// -- Helper Methods --

	private void initialize(final int imageIndex) throws FormatException,
			IOException {
		if (out.length() == 0) {
			final int width = cMeta.getAxisLength(imageIndex, Axes.X);
			final int height = cMeta.getAxisLength(imageIndex, Axes.Y);
			final int bytesPerPixel = FormatTools.getBytesPerPixel(cMeta
					.getPixelType(imageIndex));
			final int nChannels = cMeta.getAxisLength(imageIndex, Axes.CHANNEL);
			final boolean indexed = getColorModel() != null
					&& (getColorModel() instanceof IndexColorModel);
			littleEndian = cMeta.isLittleEndian(imageIndex);

			// write 8-byte PNG signature
			out.write(PNG_SIG);

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
			if (indexed)
				b[13] = (byte) 3;
			else if (nChannels == 1)
				b[13] = (byte) 0;
			else if (nChannels == 2)
				b[13] = (byte) 4;
			else if (nChannels == 3)
				b[13] = (byte) 2;
			else if (nChannels == 4)
				b[13] = (byte) 6;
			b[14] = metadata.getIhdr().getCompressionMethod();
			b[15] = metadata.getIhdr().getFilterMethod();
			b[16] = metadata.getIhdr().getInterlaceMethod();

			out.write(b);
			out.writeInt(crc(b));

			// write acTL chunk

			final APNGacTLChunk actl = metadata.getActl();

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
			final int planeIndex) throws IOException {
		out.writeInt(26);
		final APNGfcTLChunk fctl = metadata.getFctl().get(
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
		if (!(getColorModel() instanceof IndexColorModel))
			return;

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
			final byte[] stream, final int x, final int y, final int width,
			final int height) throws FormatException, IOException {
		final int sizeC = cMeta.getAxisLength(imageIndex, Axes.CHANNEL);
		final int pixelType = cMeta.getPixelType(imageIndex);
		final boolean signed = FormatTools.isSigned(pixelType);

		if (!isFullPlane(imageIndex, x, y, width, height)) {
			throw new FormatException(
					"APNGWriter does not support writing tiles.");
		}

		final ByteArrayOutputStream s = new ByteArrayOutputStream();
		s.write(chunk.getBytes());
		if (chunk.equals("fdAT")) {
			s.write(DataTools.intToBytes(nextSequenceNumber++, false));
		}
		final DeflaterOutputStream deflater = new DeflaterOutputStream(s);
		final int planeSize = stream.length / sizeC;
		final int rowLen = stream.length / height;
		final int bytesPerPixel = stream.length / (width * height * sizeC);
		final byte[] rowBuf = new byte[rowLen];
		for (int i = 0; i < height; i++) {
			deflater.write(0);
			if (interleaved) {
				if (littleEndian) {
					for (int col = 0; col < width * sizeC; col++) {
						final int offset = (i * sizeC * width + col)
								* bytesPerPixel;
						final int pixel = DataTools.bytesToInt(stream, offset,
								bytesPerPixel, littleEndian);
						DataTools.unpackBytes(pixel, rowBuf, col
								* bytesPerPixel, bytesPerPixel, false);
					}
				} else
					System.arraycopy(stream, i * rowLen, rowBuf, 0, rowLen);
			} else {
				final int max = (int) Math.pow(2, bytesPerPixel * 8 - 1);
				for (int col = 0; col < width; col++) {
					for (int c = 0; c < sizeC; c++) {
						final int offset = c * planeSize + (i * width + col)
								* bytesPerPixel;
						int pixel = DataTools.bytesToInt(stream, offset,
								bytesPerPixel, littleEndian);
						if (signed) {
							if (pixel < max)
								pixel += max;
							else
								pixel -= max;
						}
						final int output = (col * sizeC + c) * bytesPerPixel;
						DataTools.unpackBytes(pixel, rowBuf, output,
								bytesPerPixel, false);
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
		DataTools.unpackBytes(metadata.getActl() == null ? 0 : metadata.getActl().getNumPlays(), b, 8, 4, false);
		out.writeInt(crc(b));
	}
}
