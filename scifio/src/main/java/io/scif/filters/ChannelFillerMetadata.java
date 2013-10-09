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

import java.io.IOException;

import io.scif.DefaultImageMetadata;
import io.scif.FormatException;
import io.scif.HasColorTable;
import io.scif.ImageMetadata;
import io.scif.Metadata;
import io.scif.Reader;
import io.scif.util.FormatTools;
import net.imglib2.display.ArrayColorTable;
import net.imglib2.display.ColorTable;
import net.imglib2.meta.Axes;

import org.scijava.plugin.Attr;
import org.scijava.plugin.Plugin;

/**
 * {@link io.scif.filters.MetadataWrapper} implementation specifically for use
 * with the {@link io.scif.filters.ChannelFiller}.
 * 
 * @see io.scif.filters.MetadataWrapper
 * @see io.scif.filters.ChannelFiller
 * @author Mark Hiner
 */
@Plugin(type = MetadataWrapper.class, attrs = { @Attr(
	name = ChannelFillerMetadata.METADATA_KEY,
	value = ChannelFillerMetadata.METADATA_VALUE) })
public class ChannelFillerMetadata extends AbstractMetadataWrapper {

	// -- Constants --

	public static final String METADATA_VALUE = "io.scif.filters.ChannelFiller";

	// -- Fields --

	/**
	 * Number of components in the wrapped color table
	 */
	private int lutLength;

	// -- Constructors --

	public ChannelFillerMetadata() {
		this(null);
	}

	public ChannelFillerMetadata(final Metadata metadata) {
		super(metadata);
	}

	// -- ChannelFiller API methods --

	public int getLutLength() {
		return lutLength;
	}

	// -- Metadata API methods --
	
	@Override
	public void populateImageMetadata() {
		final Metadata m = unwrap();
		createImageMetadata(0);

		for (int i = 0; i < m.getImageCount(); i++) {
			final ImageMetadata iMeta = new DefaultImageMetadata(m.get(i));

			if (m.isIndexed(i)) {
				iMeta.setIndexed(false);
				ColorTable cTable = null;

				// Extract the color table. If the Metadata has one attached we
				// can access it directly
				if (HasColorTable.class.isAssignableFrom(m.getClass())) {
					cTable = ((HasColorTable) m).getColorTable(i, 0);
				}
				// Otherwise we have to open a plane
				else {
					Reader r = null;
					try {
						r =
							scifio().initializer().initializeReader(
								m.getSource().getFileName(), true);
						cTable = r.openPlane(0, 0).getColorTable();
						r.close();
					}
					catch (FormatException e) {
						throw new IllegalArgumentException(
							"ChannelFiller failed, could not open ColorTable for an indexed dataset",
							e);
					}
					catch (IOException e) {
						throw new IllegalArgumentException(
							"ChannelFiller failed, could not open ColorTable for an indexed dataset",
							e);
					}
				}
				lutLength = cTable.getComponentCount();

				// Attempt to update the pixel type based on the color table type
				if (ArrayColorTable.class.isAssignableFrom(cTable.getClass())) {
					int bitsPerElement = ((ArrayColorTable<?>) cTable).getBits();
					boolean signed = FormatTools.isSigned(iMeta.getPixelType());
					boolean floating = FormatTools.isFloatingPoint(iMeta.getPixelType());

					try {
						iMeta.setPixelType(FormatTools.pixelTypeFromBytes(
							bitsPerElement / 8, signed, floating));
					}
					catch (FormatException e) {
						log().warn("Could not update pixel type of ChannelFiller metadata.");
					}
					
				}
				
				iMeta.setAxisLength(Axes.CHANNEL, iMeta.getAxisLength(Axes.CHANNEL) *
					lutLength);
			}

			add(iMeta, false);
		}
	}
}
