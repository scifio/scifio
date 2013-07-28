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

import io.scif.util.SCIFIOMetadataTools;

/**
 * Abstract superclass of all SCIFIO {@link io.scif.Translator} components.
 * <p>
 * NB: this class provides a special abstract method:
 * {@code typedTranslate(Metadata, Metadata)}. That is the method which should
 * be overridden when extending this class, and not
 * {@link #translate(Metadata, Metadata)} - which has been set up to translate
 * the type-general Metadata information, delegate to {@code typedTranslate},
 * and then populate the ImageMetadata of the destination.
 * </p>
 * 
 * @see io.scif.Translator
 * @see io.scif.services.TranslatorService
 * @see io.scif.Metadata
 * @author Mark Hiner
 * @param <M> - The source Metadata type required by this Translator
 * @param <N> - The destination Metadata type required by this Translator
 */
public abstract class AbstractTranslator<M extends Metadata, N extends Metadata>
	extends AbstractSCIFIOComponent implements Translator
{

	// -- Translator API --

	/*
	 * @see io.scif.Translator#translate(io.scif.Metadata, io.scif.Metadata)
	 */
	public void translate(final Metadata source, final Metadata dest) {
		// Cast the parameters to typed Metadata
		final M typedSource = SCIFIOMetadataTools.<M> castMeta(source);
		final N typedDest = SCIFIOMetadataTools.<N> castMeta(dest);

		// Boilerplate for common Metadata fields
		dest.setSource(source.getSource());
		dest.setFiltered(source.isFiltered());
		dest.setMetadataOptions(source.getMetadataOptions());
		dest.setDatasetName(source.getDatasetName());

		// Type-dependent translation
		typedTranslate(typedSource, typedDest);

		// -- Post-translation hook --
		// Update the source's ImageMetadata based on the translation results
		dest.populateImageMetadata();
	}

	// -- AbstractTranslator API --

	/**
	 * This method should contain the actual logic for populating the
	 * type-specific fields of the destination Metadata.
	 * <p>
	 * This separation of logic allows the translate(Metadata, Metadata) method to
	 * perform "pre" and "post" translation activities, while facilitating
	 * type-specific Metadata operations.
	 * </p>
	 * 
	 * @param source - Source Metadata
	 * @param dest - Destination Metadata
	 */
	protected abstract void typedTranslate(M source, N dest);
}
