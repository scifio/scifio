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

package io.scif.filters;

import io.scif.DefaultImageMetadata;
import io.scif.ImageMetadata;
import io.scif.Metadata;
import io.scif.util.FormatTools;
import net.imglib2.meta.Axes;
import net.imglib2.meta.CalibratedAxis;

import org.scijava.plugin.Attr;
import org.scijava.plugin.Plugin;

/**
 * {@link io.scif.filters.MetadataWrapper} implementation specifically for use
 * with the {@link io.scif.filters.ChannelSeparator}.
 * 
 * @see io.scif.filters.MetadataWrapper
 * @see io.scif.filters.ChannelSeparator
 * @author Mark Hiner
 */
@Plugin(type = MetadataWrapper.class, attrs = { @Attr(
	name = ChannelSeparatorMetadata.METADATA_KEY,
	value = ChannelSeparatorMetadata.METADATA_VALUE) })
public class ChannelSeparatorMetadata extends AbstractMetadataWrapper {

	// -- Constants --

	public static final String METADATA_VALUE =
		"io.scif.filters.ChannelSeparator";

	// -- Fields --

	private final CalibratedAxis[] xyczt = FormatTools.createAxes(Axes.X, Axes.Y,
		Axes.CHANNEL, Axes.Z, Axes.TIME);
	private final CalibratedAxis[] xyctz = FormatTools.createAxes(Axes.X, Axes.Y,
		Axes.CHANNEL, Axes.TIME, Axes.Z);

	// -- Constructors --

	public ChannelSeparatorMetadata() {
		this(null);
	}

	public ChannelSeparatorMetadata(final Metadata metadata) {
		super(metadata);
	}

	// -- Metadata API Methods --

	@Override
	public void populateImageMetadata() {
		final Metadata m = unwrap();
		createImageMetadata(0);

		for (int i = 0; i < m.getImageCount(); i++) {
			final ImageMetadata iMeta = new DefaultImageMetadata(m.get(i));
			if (iMeta.isRGB() && !iMeta.isIndexed()) iMeta.setPlaneCount(iMeta
				.getPlaneCount() *
				iMeta.getRGBChannelCount());

			add(iMeta, false);
		}
	}

	@Override
	public boolean isRGB(final int imageIndex) {
		return isIndexed(imageIndex) && !isFalseColor(imageIndex) &&
			getAxisLength(imageIndex, Axes.CHANNEL) > 1;
	}

	@Override
	public CalibratedAxis[] getAxes(final int imageIndex) {
		if (unwrap().isRGB(imageIndex) && !unwrap().isIndexed(imageIndex)) {
			final int timeIndex = unwrap().getAxisIndex(imageIndex, Axes.TIME);
			final int zIndex = unwrap().getAxisIndex(imageIndex, Axes.Z);
			return timeIndex > zIndex ? xyczt : xyctz;
		}
		return unwrap().getAxes(imageIndex);
	}
}
