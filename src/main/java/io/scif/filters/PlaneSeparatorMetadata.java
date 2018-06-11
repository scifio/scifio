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

package io.scif.filters;

import io.scif.DefaultImageMetadata;
import io.scif.ImageMetadata;
import io.scif.Metadata;

import java.util.HashSet;
import java.util.Set;

import net.imagej.axis.AxisType;

import org.scijava.plugin.Plugin;

/**
 * {@link io.scif.filters.MetadataWrapper} implementation specifically for use
 * with the {@link io.scif.filters.PlaneSeparator}.
 *
 * @see io.scif.filters.MetadataWrapper
 * @see io.scif.filters.PlaneSeparator
 * @author Mark Hiner
 */
@Plugin(type = MetadataWrapper.class)
public class PlaneSeparatorMetadata extends AbstractMetadataWrapper {

	// -- Fields --

	/** List of Axes to separate. */
	private Set<AxisType> splitTypes = new HashSet<>();

	// -- PlanarAxisSeparatorMetadata API Methods --

	/** Returns the number of axes being separated. */
	public int offset() {
		return splitTypes.size();
	}

	/**
	 * Specify which AxisTypes should be separated.
	 */
	public void separate(final AxisType... types) {
		if (unwrap() != null) {
			matchTypes(types);
			populateImageMetadata();
		}
	}

	/**
	 * @return true iff the specified AxisType is currently being split on
	 */
	public boolean splitting(final AxisType type) {
		return splitTypes.contains(type);
	}

	// -- MetadataWrapper API Methods --

	@Override
	public void wrap(final Metadata meta) {
		splitTypes = new HashSet<>();
		for (int i = 0; i < meta.get(0).getInterleavedAxisCount(); i++) {
			splitTypes.add(meta.get(0).getAxis(i).type());
		}
		super.wrap(meta);
	}

	@Override
	public Class<? extends Filter> filterType() {
		return io.scif.filters.PlaneSeparator.class;
	}

	// -- Metadata API Methods --

	@Override
	public void populateImageMetadata() {
		final Metadata m = unwrap();
		createImageMetadata(0);

		for (int i = 0; i < m.getImageCount(); i++) {
			final ImageMetadata iMeta = new DefaultImageMetadata(m.get(i));

			// offset to the next axis position
			int offset = 0;
			for (final AxisType type : splitTypes) {
				// For each potentially split axis, if it's a planar axis, move
				// it to a
				// non-planar position
				if (iMeta.getAxisIndex(type) >= 0 && iMeta.getAxisIndex(type) < iMeta
					.getPlanarAxisCount())
				{
					iMeta.setAxis(iMeta.getPlanarAxisCount() + offset++ - 1, iMeta
						.getAxis(type));
					iMeta.setPlanarAxisCount(iMeta.getPlanarAxisCount() - 1);
				}
			}

			add(iMeta, false);
		}
	}

	// -- Helper Methods --

	/**
	 * Returns a list of all AxisTypes that are present in the wrapped Metadata.
	 */
	private void matchTypes(final AxisType... types) {
		splitTypes.clear();
		for (final AxisType t : types) {
			final int axisIndex = unwrap().get(0).getAxisIndex(t);
			// If the specified axis is present and a planar axis, we can
			// separate it
			if (axisIndex >= 0 && axisIndex < unwrap().get(0).getPlanarAxisCount()) {
				splitTypes.add(t);
			}
		}
	}
}
