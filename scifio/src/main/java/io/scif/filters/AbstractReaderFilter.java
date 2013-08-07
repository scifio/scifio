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

import io.scif.Format;
import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.Metadata;
import io.scif.Plane;
import io.scif.Reader;
import io.scif.io.RandomAccessInputStream;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;

/**
 * Abstract superclass for all {@link io.scif.filters.Filter} that delegate to
 * {@link io.scif.Reader} instances.
 * <p>
 * NB: All concrete implementations of this interface should be annotated as
 * {@link Plugin} for automatic discovery.
 * </p>
 * <p>
 * NB: This class attempts to locate a type-matching MetadataWrapper to
 * protectively wrap the wrapped {@code Reader}'s Metadata. If none is found, a
 * reference to the {@code Reader's} Metadata itself is used.
 * </p>
 * 
 * @author Mark Hiner
 * @see io.scif.Reader
 * @see io.scif.filters.Filter
 * @see io.scif.filters.AbstractMetadataWrapper
 */
public abstract class AbstractReaderFilter extends AbstractFilter<Reader>
	implements Reader
{

	// -- Fields --

	/* Need to wrap each Reader's Metadata separately */
	private Metadata wrappedMeta = null;

	private final Class<? extends Metadata> metaClass;

	@Parameter
	private PluginService pluginService;

	// -- Constructor --

	public AbstractReaderFilter() {
		this(null);
	}

	public AbstractReaderFilter(final Class<? extends Metadata> metaClass) {
		super(Reader.class);
		this.metaClass = metaClass;
	}

	// -- AbstractReaderFilter API Methods --

	/**
	 * Allows code to be executed regardless of which {@link #setSource()}
	 * signature is called.
	 * 
	 * @param source - Lowest common denominator of arguments in the
	 *          {@code setSource} series.
	 */
	protected void setSourceHelper(final String source) {

	}

	/**
	 * Allows code to be executed regardless of which {@link #openPlane()}
	 * signature is called.
	 */
	protected void openPlaneHelper() {

	}

	/**
	 * Allows code to be executed regardless of which {@link #readPlane()}
	 * signature is called.
	 */
	protected void readPlaneHelper() {}

	/**
	 * Convenience accessor for the parent's Metadata
	 */
	protected Metadata getParentMeta() {
		return getParent().getMetadata();
	}

	// -- Filter API Methods --

	/*
	 * @see io.scif.filters.AbstractFilter#setParent(java.lang.Object)
	 */
	@Override
	public void setParent(final Object parent) {
		super.setParent(parent);

		final Reader r = (Reader) parent;

		// TODO Maybe cache this result so we don't have to discover every time
		// setparent is called
		// because it will be called frequently, given how MasterFilterHelper is
		// implemented

		final List<PluginInfo<MetadataWrapper>> wrapperInfos =
			getContext().getPluginIndex().getPlugins(MetadataWrapper.class);

		// look for a compatible MetadataWrapper class
		for (final PluginInfo<MetadataWrapper> info : wrapperInfos) {
			final String wrapperClassName = info.get(MetadataWrapper.METADATA_KEY);

			if (wrapperClassName != null) {
				Class<?> wrapperClass;
				try {
					wrapperClass = Class.forName(wrapperClassName);
					if (wrapperClass.isAssignableFrom(getClass())) {
						final MetadataWrapper metaWrapper =
							pluginService.createInstance(info);
						metaWrapper.wrap(r.getMetadata());
						wrappedMeta = metaWrapper;
						return;
					}
				}
				catch (final ClassNotFoundException e) {
					log().error("Failed to find class: " + wrapperClassName);
				}
			}
		}

		// No Filter-specific wrapper found
		wrappedMeta = r.getMetadata();
	}

	/*
	 * @see io.scif.filters.Filter#isCompatible(java.lang.Class)
	 */
	@Override
	public boolean isCompatible(final Class<?> c) {
		return Reader.class.isAssignableFrom(c);
	}

	// -- Reader API Methods --

	/*
	 * @see io.scif.Reader#openPlane(int, int)
	 */
	public Plane openPlane(final int imageIndex, final int planeIndex)
		throws FormatException, IOException
	{
		openPlaneHelper();
		return getParent().openPlane(imageIndex, planeIndex);
	}

	/*
	 * @see io.scif.Reader#openPlane(int, int, int, int, int, int)
	 */
	public Plane openPlane(final int imageIndex, final int planeIndex,
		final int x, final int y, final int w, final int h) throws FormatException,
		IOException
	{
		openPlaneHelper();
		return getParent().openPlane(imageIndex, planeIndex, x, y, w, h);
	}

	/*
	 * @see io.scif.Reader#openPlane(int, int, io.scif.Plane)
	 */
	public Plane openPlane(final int imageIndex, final int planeIndex,
		final Plane plane) throws FormatException, IOException
	{
		openPlaneHelper();
		return getParent().openPlane(imageIndex, planeIndex, plane);
	}

	/*
	 * @see io.scif.Reader#openPlane(int, int, io.scif.Plane, int, int, int, int)
	 */
	public Plane openPlane(final int imageIndex, final int planeIndex,
		final Plane plane, final int x, final int y, final int w, final int h)
		throws FormatException, IOException
	{
		openPlaneHelper();
		return getParent().openPlane(imageIndex, planeIndex, plane, x, y, w, h);
	}

	/*
	 * @see io.scif.Reader#openThumbPlane(int, int)
	 */
	public Plane openThumbPlane(final int imageIndex, final int planeIndex)
		throws FormatException, IOException
	{
		return getParent().openThumbPlane(imageIndex, planeIndex);
	}

	/*
	 * @see io.scif.Reader#setGroupFiles(boolean)
	 */
	public void setGroupFiles(final boolean group) {
		getParent().setGroupFiles(group);
	}

	/*
	 * @see io.scif.Reader#isGroupFiles()
	 */
	public boolean isGroupFiles() {
		return getParent().isGroupFiles();
	}

	/*
	 * @see io.scif.Reader#fileGroupOption(java.lang.String)
	 */
	public int fileGroupOption(final String id) throws FormatException,
		IOException
	{
		return getParent().fileGroupOption(id);
	}

	/*
	 * @see io.scif.Reader#getCurrentFile()
	 */
	public String getCurrentFile() {
		return getParent().getCurrentFile();
	}

	/*
	 * @see io.scif.Reader#getDomains()
	 */
	public String[] getDomains() {
		return getParent().getDomains();
	}

	/*
	 * @see io.scif.Reader#getStream()
	 */
	public RandomAccessInputStream getStream() {
		return getParent().getStream();
	}

	/*
	 * @see io.scif.Reader#getUnderlyingReaders()
	 */
	public Reader[] getUnderlyingReaders() {
		return getParent().getUnderlyingReaders();
	}

	/*
	 * @see io.scif.Reader#getOptimalTileWidth(int)
	 */
	public int getOptimalTileWidth(final int imageIndex) {
		return getParent().getOptimalTileWidth(imageIndex);
	}

	/*
	 * @see io.scif.Reader#getOptimalTileHeight(int)
	 */
	public int getOptimalTileHeight(final int imageIndex) {
		return getParent().getOptimalTileHeight(imageIndex);
	}

	/*
	 * @see io.scif.Reader#setMetadata(io.scif.Metadata)
	 */
	public void setMetadata(final Metadata meta) throws IOException {
		getParent().setMetadata(meta);

		if (wrappedMeta instanceof MetadataWrapper) ((MetadataWrapper) wrappedMeta)
			.wrap(meta);
		else wrappedMeta = meta;
	}

	/*
	 * @see io.scif.Reader#getMetadata()
	 */
	public Metadata getMetadata() {
		return wrappedMeta;
	}

	/*
	 * @see io.scif.Reader#setNormalized(boolean)
	 */
	public void setNormalized(final boolean normalize) {
		getParent().setNormalized(normalize);
	}

	/*
	 * @see io.scif.Reader#isNormalized()
	 */
	public boolean isNormalized() {
		return getParent().isNormalized();
	}

	/*
	 * @see io.scif.Reader#hasCompanionFiles()
	 */
	public boolean hasCompanionFiles() {
		return getParent().hasCompanionFiles();
	}

	/*
	 * @see io.scif.Reader#setSource(java.lang.String)
	 */
	public void setSource(final String fileName) throws IOException {
		setSourceHelper(fileName);
		getParent().setSource(fileName);
	}

	/*
	 * @see io.scif.Reader#setSource(java.io.File)
	 */
	public void setSource(final File file) throws IOException {
		setSourceHelper(file.getAbsolutePath());
		getParent().setSource(file);
	}

	/*
	 * @see io.scif.Reader#setSource(io.scif.io.RandomAccessInputStream)
	 */
	public void setSource(final RandomAccessInputStream stream)
		throws IOException
	{
		setSourceHelper(stream.getFileName());
		getParent().setSource(stream);
	}

	/*
	 * @see io.scif.Reader#close(boolean)
	 */
	public void close(final boolean fileOnly) throws IOException {
		getParent().close(fileOnly);
	}

	/*
	 * @see io.scif.Reader#close()
	 */
	public void close() throws IOException {
		getParent().close();
	}

	/*
	 * @see io.scif.Reader#readPlane(io.scif.io.RandomAccessInputStream, int, int, int, int, int, io.scif.Plane)
	 */
	public Plane readPlane(final RandomAccessInputStream s, final int imageIndex,
		final int x, final int y, final int w, final int h, final Plane plane)
		throws IOException
	{
		readPlaneHelper();
		return getParent().readPlane(s, imageIndex, x, y, w, h, plane);
	}

	/*
	 * @see io.scif.Reader#readPlane(io.scif.io.RandomAccessInputStream, int, int, int, int, int, int, io.scif.Plane)
	 */
	public Plane readPlane(final RandomAccessInputStream s, final int imageIndex,
		final int x, final int y, final int w, final int h, final int scanlinePad,
		final Plane plane) throws IOException
	{
		readPlaneHelper();
		return getParent().readPlane(s, imageIndex, x, y, w, h, scanlinePad, plane);
	}

	/*
	 * @see io.scif.Reader#getPlaneCount(int)
	 */
	public int getPlaneCount(final int imageIndex) {
		return getParent().getPlaneCount(imageIndex);
	}

	/*
	 * @see io.scif.Reader#getImageCount()
	 */
	public int getImageCount() {
		return getParent().getImageCount();
	}

	/*
	 * @see io.scif.Reader#createPlane(int, int, int, int)
	 */
	public Plane createPlane(final int xOffset, final int yOffset,
		final int xLength, final int yLength)
	{
		return getParent().createPlane(xOffset, yOffset, xLength, yLength);
	}

	/*
	 * @see io.scif.Reader#createPlane(io.scif.ImageMetadata, int, int, int, int)
	 */
	public Plane createPlane(final ImageMetadata meta, final int xOffset,
		final int yOffset, final int xLength, final int yLength)
	{
		return getParent().createPlane(meta, xOffset, yOffset, xLength, yLength);
	}

	/*
	 * @see io.scif.Reader#castToTypedPlane(io.scif.Plane)
	 */
	public <P extends Plane> P castToTypedPlane(final Plane plane) {
		return getParent().<P> castToTypedPlane(plane);
	}

	// -- Groupable API Methods --

	/*
	 * @see io.scif.Groupable#isSingleFile(java.lang.String)
	 */
	public boolean isSingleFile(final String id) throws FormatException,
		IOException
	{
		return getParent().isSingleFile(id);
	}

	// -- HasFormat API Methods --

	/*
	 * @see io.scif.HasFormat#getFormat()
	 */
	public Format getFormat() {
		return getParent().getFormat();
	}

	// -- HasContext API Methods --

	/*
	 * @see io.scif.HasContext#getContext()
	 */
	@Override
	public Context getContext() {
		return getParent().getContext();
	}

	/*
	 * @see io.scif.HasContext#setContext(io.scif.SCIFIO)
	 */
	@Override
	public void setContext(final Context ctx) {
		getParent().setContext(ctx);
	}

	// -- Helper methods --

	/* Returns true if this filter's metdata can be cast to ChannelFillerMetadata */
	protected boolean metaCheck() {
		final Metadata meta = getMetadata();

		return metaClass.isAssignableFrom(meta.getClass());
	}
}
