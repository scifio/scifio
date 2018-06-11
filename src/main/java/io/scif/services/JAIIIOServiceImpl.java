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

package io.scif.services;

import io.scif.codec.JPEG2000CodecOptions;
import io.scif.media.imageio.plugins.jpeg2000.J2KImageReadParam;
import io.scif.media.imageio.plugins.jpeg2000.J2KImageWriteParam;
import io.scif.media.imageioimpl.plugins.jpeg2000.J2KImageReader;
import io.scif.media.imageioimpl.plugins.jpeg2000.J2KImageReaderSpi;
import io.scif.media.imageioimpl.plugins.jpeg2000.J2KImageWriter;
import io.scif.media.imageioimpl.plugins.jpeg2000.J2KImageWriterSpi;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * Implementation of JAIIIOService for reading and writing JPEG-2000 data.
 */
@Plugin(type = Service.class)
public class JAIIIOServiceImpl extends AbstractService implements
	JAIIIOService
{

	// -- JAIIIOService API methods --

	@Override
	public void writeImage(final OutputStream out, final BufferedImage img,
		final JPEG2000CodecOptions options) throws IOException, ServiceException
	{
		final ImageOutputStream ios = ImageIO.createImageOutputStream(out);

		final IIORegistry registry = IIORegistry.getDefaultInstance();
		final Iterator<J2KImageWriterSpi> iter = ServiceRegistry.lookupProviders(
			J2KImageWriterSpi.class);
		registry.registerServiceProviders(iter);
		final J2KImageWriterSpi spi = registry.getServiceProviderByClass(
			J2KImageWriterSpi.class);
		final J2KImageWriter writer = new J2KImageWriter(spi);
		writer.setOutput(ios);

		final String filter = options.lossless ? J2KImageWriteParam.FILTER_53
			: J2KImageWriteParam.FILTER_97;

		final IIOImage iioImage = new IIOImage(img, null, null);
		final J2KImageWriteParam param = (J2KImageWriteParam) writer
			.getDefaultWriteParam();
		param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		param.setCompressionType("JPEG2000");
		param.setLossless(options.lossless);
		param.setFilter(filter);
		param.setCodeBlockSize(options.codeBlockSize);
		param.setEncodingRate(options.quality);
		if (options.tileWidth > 0 && options.tileHeight > 0) {
			param.setTiling(options.tileWidth, options.tileHeight,
				options.tileGridXOffset, options.tileGridYOffset);
		}
		if (options.numDecompositionLevels != null) {
			param.setNumDecompositionLevels(options.numDecompositionLevels
				.intValue());
		}
		writer.write(null, iioImage, param);
		ios.close();
	}

	/**
	 * @deprecated use JAIIIOService#writeImage(OutputStream, BufferedImage,
	 *             JPEG2000CodecOptions)
	 */
	@Override
	@Deprecated
	public void writeImage(final OutputStream out, final BufferedImage img,
		final boolean lossless, final int[] codeBlockSize, final double quality)
		throws IOException, ServiceException
	{
		final JPEG2000CodecOptions options = JPEG2000CodecOptions
			.getDefaultOptions();
		options.lossless = lossless;
		options.codeBlockSize = codeBlockSize;
		options.quality = quality;
		writeImage(out, img, options);
	}

	@Override
	public BufferedImage readImage(final InputStream in,
		final JPEG2000CodecOptions options) throws IOException, ServiceException
	{
		final J2KImageReader reader = getReader();
		final MemoryCacheImageInputStream mciis = new MemoryCacheImageInputStream(
			in);
		reader.setInput(mciis, false, true);
		final J2KImageReadParam param = (J2KImageReadParam) reader
			.getDefaultReadParam();
		if (options.resolution != null) {
			param.setResolution(options.resolution.intValue());
		}
		return reader.read(0, param);
	}

	@Override
	public BufferedImage readImage(final InputStream in) throws IOException,
		ServiceException
	{
		return readImage(in, JPEG2000CodecOptions.getDefaultOptions());
	}

	@Override
	public Raster readRaster(final InputStream in,
		final JPEG2000CodecOptions options) throws IOException, ServiceException
	{
		final J2KImageReader reader = getReader();
		final MemoryCacheImageInputStream mciis = new MemoryCacheImageInputStream(
			in);
		reader.setInput(mciis, false, true);
		final J2KImageReadParam param = (J2KImageReadParam) reader
			.getDefaultReadParam();
		if (options.resolution != null) {
			param.setResolution(options.resolution.intValue());
		}
		return reader.readRaster(0, param);
	}

	@Override
	public Raster readRaster(final InputStream in) throws IOException,
		ServiceException
	{
		return readRaster(in, JPEG2000CodecOptions.getDefaultOptions());
	}

	/** Set up the JPEG-2000 image reader. */
	private J2KImageReader getReader() {
		final IIORegistry registry = IIORegistry.getDefaultInstance();
		final Iterator<J2KImageReaderSpi> iter = ServiceRegistry.lookupProviders(
			J2KImageReaderSpi.class);
		registry.registerServiceProviders(iter);
		final J2KImageReaderSpi spi = registry.getServiceProviderByClass(
			J2KImageReaderSpi.class);
		return new J2KImageReader(spi);
	}

}
