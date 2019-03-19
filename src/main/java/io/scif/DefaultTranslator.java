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

import java.util.List;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

/**
 * Basic {@link io.scif.Translator} implementation. Can convert between any two
 * Metadata implementations because only {@link io.scif.ImageMetadata} is used.
 *
 * @see io.scif.Translator
 * @see io.scif.DefaultMetadata
 * @see io.scif.ImageMetadata
 * @author Mark Hiner
 */
@Plugin(type = Translator.class, priority = Priority.VERY_LOW)
public class DefaultTranslator extends AbstractTranslator<Metadata, Metadata> {

	// -- Translater API Methods --

	@Override
	public Class<? extends io.scif.Metadata> source() {
		return io.scif.Metadata.class;
	}

	@Override
	public Class<? extends io.scif.Metadata> dest() {
		return io.scif.Metadata.class;
	}

	@Override
	protected void translateImageMetadata(final List<ImageMetadata> source,
		final Metadata dest)
	{
		dest.createImageMetadata(source.size());

		for (int i = 0; i < source.size(); i++) {
			final ImageMetadata sourceMeta = source.get(i);

			// Need to add a new ImageMetadata
			if (i >= dest.getImageCount()) {
				dest.add(new DefaultImageMetadata(sourceMeta));
			}
			// Update the existing ImageMetadata using sourceMeta
			else {
				dest.get(i).copy(sourceMeta);
			}
		}
	}
}
