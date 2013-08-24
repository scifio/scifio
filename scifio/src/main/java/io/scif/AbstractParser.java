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

import io.scif.common.DataTools;
import io.scif.io.RandomAccessInputStream;
import io.scif.util.FormatTools;
import io.scif.util.SCIFIOMetadataTools;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/**
 * Abstract superclass of all SCIFIO {@link io.scif.Parser} implementations.
 * 
 * @see io.scif.Parser
 * @see io.scif.Metadata
 * @see io.scif.HasFormat
 * @author Mark Hiner
 * @param <M> - The Metadata type returned by this Parser.
 */
public abstract class AbstractParser<M extends TypedMetadata> extends
	AbstractGroupable implements TypedParser<M>
{

	// -- Fields --

	/** Current image source. */
	protected RandomAccessInputStream in;

	/** Metadata for the current source. */
	protected M metadata;

	/** String id of current source. */
	protected String currentId;

	/** Whether or not to filter out invalid metadata. */
	protected boolean filterMetadata;

	/** Whether or not to save proprietary metadata in the MetadataStore. */
	protected boolean saveOriginalMetadata = false;

	/** Metadata parsing options. */
	protected MetadataOptions metadataOptions = new DefaultMetadataOptions();

	// -- Parser API Methods --

	public M parse(final String fileName) throws IOException, FormatException {
		@SuppressWarnings("unchecked")
		final M meta = (M) getFormat().createMetadata();
		return parse(fileName, meta);
	}

	public M parse(final File file) throws IOException, FormatException {
		@SuppressWarnings("unchecked")
		final M meta = (M) getFormat().createMetadata();
		return parse(file, meta);
	}

	public M parse(final RandomAccessInputStream stream) throws IOException,
		FormatException
	{
		@SuppressWarnings("unchecked")
		final M meta = (M) getFormat().createMetadata();
		return parse(stream, meta);
	}

	public M parse(final String fileName, final Metadata meta)
		throws IOException, FormatException
	{
		return parse(fileName, SCIFIOMetadataTools.<M> castMeta(meta));
	}

	public M parse(final File file, final Metadata meta) throws IOException,
		FormatException
	{
		return parse(file, SCIFIOMetadataTools.<M> castMeta(meta));
	}

	public M parse(final RandomAccessInputStream stream, final Metadata meta)
		throws IOException, FormatException
	{
		return parse(stream, SCIFIOMetadataTools.<M> castMeta(meta));
	}

	public void setOriginalMetadataPopulated(final boolean populate) {
		FormatTools.assertStream(in, false, 1);
		saveOriginalMetadata = populate;
	}

	public boolean isOriginalMetadataPopulated() {
		return saveOriginalMetadata;
	}

	public String[] getUsedFiles() {
		return getUsedFiles(false);
	}

	public String[] getUsedFiles(final boolean noPixels) {
		final Vector<String> files = new Vector<String>();
		for (int i = 0; i < metadata.getImageCount(); i++) {
			final String[] s = getImageUsedFiles(i, noPixels);
			if (s != null) {
				for (final String file : s) {
					if (!files.contains(file)) {
						files.add(file);
					}
				}
			}
		}
		return files.toArray(new String[files.size()]);
	}

	public void setMetadataFiltered(final boolean filter) {
		FormatTools.assertStream(in, false, 1);
		filterMetadata = filter;
	}

	public boolean isMetadataFiltered() {
		return filterMetadata;
	}

	public String[] getImageUsedFiles(final int imageIndex) {
		return getImageUsedFiles(imageIndex, false);
	}

	public String[]
		getImageUsedFiles(final int imageIndex, final boolean noPixels)
	{
		return noPixels ? null : new String[] { in.getFileName() };
	}

	public FileInfo[] getAdvancedUsedFiles(final boolean noPixels) {
		final String[] files = getUsedFiles(noPixels);
		if (files == null) return null;
		return getFileInfo(files);
	}

	public FileInfo[] getAdvancedImageUsedFiles(final int imageIndex,
		final boolean noPixels)
	{
		final String[] files = getImageUsedFiles(imageIndex, noPixels);
		if (files == null) return null;
		return getFileInfo(files);
	}

	public Set<MetadataLevel> getSupportedMetadataLevels() {
		final Set<MetadataLevel> supportedLevels = new HashSet<MetadataLevel>();
		supportedLevels.add(MetadataLevel.ALL);
		supportedLevels.add(MetadataLevel.NO_OVERLAYS);
		supportedLevels.add(MetadataLevel.MINIMUM);
		return supportedLevels;
	}

	public MetadataOptions getMetadataOptions() {
		return metadataOptions;
	}

	public void setMetadataOptions(final MetadataOptions options) {
		metadataOptions = options;
	}

	public void addMeta(String key, Object value, final MetaTable metaTable) {
		if (key == null || value == null /* || TODO !isMetadataCollected() */) {
			return;
		}

		key = key.trim();

		final boolean string =
			value instanceof String || value instanceof Character;
		final boolean simple =
			string || value instanceof Number || value instanceof Boolean;

		// string value, if passed in value is a string
		String val = string ? String.valueOf(value) : null;

		if (filterMetadata || (saveOriginalMetadata
		/* TODO: check if this Parser's metadata is OMEXML metadata &&
		 *  (getMetadataStore() instanceof OMEXMLMetadata)*/))
		{
			// filter out complex data types
			if (!simple) return;

			// verify key & value are reasonable length
			final int maxLen = 8192;
			if (key.length() > maxLen) return;
			if (string && val.length() > maxLen) return;

			// remove all non-printable characters
			key = DataTools.sanitize(key);
			if (string) val = DataTools.sanitize(val);

			// verify key contains at least one alphabetic character
			if (!key.matches(".*[a-zA-Z].*")) return;

			// remove &lt;, &gt; and &amp; to prevent XML parsing errors
			final String[] invalidSequences =
				new String[] { "&lt;", "&gt;", "&amp;", "<", ">", "&" };
			for (int i = 0; i < invalidSequences.length; i++) {
				key = key.replaceAll(invalidSequences[i], "");
				if (string) val = val.replaceAll(invalidSequences[i], "");
			}

			// verify key & value are not empty
			if (key.length() == 0) return;
			if (string && val.trim().length() == 0) return;

			if (string) value = val;
		}

		metaTable.put(key, val == null ? value : val);
	}

	// -- TypedParser API Methods --

	public M parse(final String fileName, final M meta) throws IOException,
		FormatException
	{
		RandomAccessInputStream stream = null;

		if (in != null) {
			if (in.getFileName().equals(fileName)) {
				in.seek(0);
				stream = in;
			}
			else {
				close();
			}
		}

		if (stream == null) stream =
			new RandomAccessInputStream(getContext(), fileName);

		return parse(stream, meta);
	}

	public M parse(final File file, final M meta) throws IOException,
		FormatException
	{
		return parse(file.getPath(), meta);
	}

	public M parse(final RandomAccessInputStream stream, final M meta)
		throws IOException, FormatException
	{
		metadata = meta;
		if (in == null || !in.getFileName().equals(stream.getFileName())) {
			init(stream);

			if (saveOriginalMetadata) {
				// TODO store all metadata in OMEXML store..
				// or equivalent function? as per setId.. or handle via annotations
			}
		}

		// TODO relying on Abstract-level API
		metadata.setFiltered(filterMetadata);
		metadata.setMetadataOptions(metadataOptions);
		if (metadata.getContext() == null) metadata.setContext(getContext());
		metadata.setSource(stream);
		metadata.setDatasetName(stream.getFileName());

		currentId = stream.getFileName();

		typedParse(stream, meta);

		metadata.populateImageMetadata();

		return metadata;
	}

	// -- HasSource API Methods --

	public void close(final boolean fileOnly) throws IOException {
		if (in != null) in.close();
		if (metadata != null) metadata.close(fileOnly);
		if (!fileOnly) {
			in = null;
			currentId = null;
		}
	}

	// -- AbstractParser Methods --

	/**
	 * A helper method, called by
	 * {@link #parse(RandomAccessInputStream, TypedMetadata)}. Allows for
	 * boilerplate code to come after parsing, specifically calls to
	 * {@link Metadata#populateImageMetadata()}.
	 * <p>
	 * This method should be implemented to populate any format-specific Metadata.
	 * </p>
	 * <p>
	 * NB: if a Format requires type-specific parsing to occur before the Abstract
	 * layer, Override {@code #parse(String, TypedMetadata)} instead.
	 * </p>
	 */
	protected abstract void typedParse(RandomAccessInputStream stream, M meta)
		throws IOException, FormatException;

	/** Adds an entry to the global metadata table. */
	public void addGlobalMeta(final String key, final Object value) {
		addMeta(key, value, metadata.getTable());
	}

	/** Adds an entry to the global metadata table. */
	public void addGlobalMeta(final String key, final boolean value) {
		addGlobalMeta(key, new Boolean(value));
	}

	/** Adds an entry to the global metadata table. */
	public void addGlobalMeta(final String key, final byte value) {
		addGlobalMeta(key, new Byte(value));
	}

	/** Adds an entry to the global metadata table. */
	public void addGlobalMeta(final String key, final short value) {
		addGlobalMeta(key, new Short(value));
	}

	/** Adds an entry to the global metadata table. */
	public void addGlobalMeta(final String key, final int value) {
		addGlobalMeta(key, new Integer(value));
	}

	/** Adds an entry to the global metadata table. */
	public void addGlobalMeta(final String key, final long value) {
		addGlobalMeta(key, new Long(value));
	}

	/** Adds an entry to the global metadata table. */
	public void addGlobalMeta(final String key, final float value) {
		addGlobalMeta(key, new Float(value));
	}

	/** Adds an entry to the global metadata table. */
	public void addGlobalMeta(final String key, final double value) {
		addGlobalMeta(key, new Double(value));
	}

	/** Adds an entry to the global metadata table. */
	public void addGlobalMeta(final String key, final char value) {
		addGlobalMeta(key, new Character(value));
	}

	/** Gets a value from the global metadata table. */
	public Object getGlobalMeta(final String key) {
		return metadata.getTable().get(key);
	}

	/* Sets the input stream for this parser if provided a new stream */
	private void init(final RandomAccessInputStream stream) throws IOException {

		// Check to see if the stream is already open
		if (in != null) {
			final String[] s = getUsedFiles();
			for (int i = 0; i < s.length; i++) {
				if (in.getFileName().equals(s[i])) return;
			}
		}

		close();
		in = stream;
	}

	/* Builds a FileInfo array around the provided array of file names */
	private FileInfo[] getFileInfo(final String[] files) {
		final FileInfo[] infos = new FileInfo[files.length];
		for (int i = 0; i < infos.length; i++) {
			infos[i] = new FileInfo();
			infos[i].filename = files[i];
			infos[i].reader = getFormat().getReaderClass();
			infos[i].usedToInitialize = files[i].endsWith(in.getFileName());
		}
		return infos;
	}
}
