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
import org.scijava.util.ClassUtils;

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
	 * @throws IOException 
	 */
  protected void setSourceHelper(final String source) throws IOException {
    final String filterSource = getMetadata() == null ? null : getMetadata()
        .getSource().getFileName();

    if (filterSource == null || !filterSource.equals(source)) {
      setMetadata(getParent().getMetadata());
    }
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
				final Class<?> wrapperClass = ClassUtils.loadClass(wrapperClassName);
				if (wrapperClass == null) {
					log().error("Failed to find class: " + wrapperClassName);
					continue;
				}
				if (wrapperClass.isAssignableFrom(getClass())) {
					final MetadataWrapper metaWrapper =
							getContext().getService(PluginService.class).createInstance(info);
					metaWrapper.wrap(r.getMetadata());
					wrappedMeta = metaWrapper;
					return;
				}
			}
		}

		// No Filter-specific wrapper found
		wrappedMeta = r.getMetadata();
	}

	@Override
	public boolean isCompatible(final Class<?> c) {
		return Reader.class.isAssignableFrom(c);
	}

	// -- Reader API Methods --

	@Override
	public Plane openPlane(final int imageIndex, final int planeIndex)
		throws FormatException, IOException
	{
		openPlaneHelper();
		return getParent().openPlane(imageIndex, planeIndex);
	}

	@Override
	public Plane openPlane(final int imageIndex, final int planeIndex,
		final long[] planeMin, final long[] planeMax) throws FormatException,
		IOException
	{
		openPlaneHelper();
		return getParent().openPlane(imageIndex, planeIndex, planeMin, planeMax);
	}

	@Override
	public Plane openPlane(final int imageIndex, final int planeIndex,
		final Plane plane) throws FormatException, IOException
	{
		openPlaneHelper();
		return getParent().openPlane(imageIndex, planeIndex, plane);
	}

	@Override
	public Plane openPlane(final int imageIndex, final int planeIndex,
		final Plane plane, final long[] planeMin, final long[] planeMax)
		throws FormatException, IOException
	{
		openPlaneHelper();
		return getParent().openPlane(imageIndex, planeIndex, plane, planeMin,
			planeMax);
	}

	@Override
	public Plane openThumbPlane(final int imageIndex, final int planeIndex)
		throws FormatException, IOException
	{
		return getParent().openThumbPlane(imageIndex, planeIndex);
	}

	@Override
	public void setGroupFiles(final boolean group) {
		getParent().setGroupFiles(group);
	}

	@Override
	public boolean isGroupFiles() {
		return getParent().isGroupFiles();
	}

	@Override
	public int fileGroupOption(final String id) throws FormatException,
		IOException
	{
		return getParent().fileGroupOption(id);
	}

	@Override
	public String getCurrentFile() {
		return getParent().getCurrentFile();
	}

	@Override
	public String[] getDomains() {
		return getParent().getDomains();
	}

	@Override
	public RandomAccessInputStream getStream() {
		return getParent().getStream();
	}

	@Override
	public Reader[] getUnderlyingReaders() {
		return getParent().getUnderlyingReaders();
	}

	@Override
	public long getOptimalTileWidth(final int imageIndex) {
		return getParent().getOptimalTileWidth(imageIndex);
	}

	@Override
	public long getOptimalTileHeight(final int imageIndex) {
		return getParent().getOptimalTileHeight(imageIndex);
	}

	@Override
	public void setMetadata(final Metadata meta) throws IOException {
		getParent().setMetadata(meta);

		if (wrappedMeta instanceof MetadataWrapper) ((MetadataWrapper) wrappedMeta)
			.wrap(meta);
		else wrappedMeta = meta;
	}

	@Override
	public Metadata getMetadata() {
		return wrappedMeta;
	}

	@Override
	public void setNormalized(final boolean normalize) {
		getParent().setNormalized(normalize);
	}

	@Override
	public boolean isNormalized() {
		return getParent().isNormalized();
	}

	@Override
	public boolean hasCompanionFiles() {
		return getParent().hasCompanionFiles();
	}

	@Override
	public void setSource(final String fileName) throws IOException {
		getParent().setSource(fileName);
		setSourceHelper(fileName);
	}

	@Override
	public void setSource(final File file) throws IOException {
		getParent().setSource(file);
		setSourceHelper(file.getAbsolutePath());
	}

	@Override
	public void setSource(final RandomAccessInputStream stream)
		throws IOException
	{
		getParent().setSource(stream);
		setSourceHelper(stream.getFileName());
	}

	@Override
	public void close(final boolean fileOnly) throws IOException {
		getParent().close(fileOnly);
	}

	@Override
	public void close() throws IOException {
		getParent().close();
	}

	@Override
	public Plane readPlane(final RandomAccessInputStream s, final int imageIndex,
		final long[] planeMin, final long[] planeMax, final Plane plane)
		throws IOException
	{
		readPlaneHelper();
		return getParent().readPlane(s, imageIndex, planeMin, planeMax, plane);
	}

	@Override
	public Plane readPlane(final RandomAccessInputStream s, final int imageIndex,
		final long[] planeMin, final long[] planeMax, final int scanlinePad,
		final Plane plane) throws IOException
	{
		readPlaneHelper();
		return getParent().readPlane(s, imageIndex, planeMin, planeMax,
			scanlinePad, plane);
	}

	@Override
	public int getPlaneCount(final int imageIndex) {
		return getParent().getPlaneCount(imageIndex);
	}

	@Override
	public int getImageCount() {
		return getParent().getImageCount();
	}

	@Override
	public Plane createPlane(final long[] planeMin, final long[] planeMax)
	{
		return getParent().createPlane(planeMin, planeMax);
	}

	@Override
	public Plane createPlane(final ImageMetadata meta, final long[] planeMin,
		final long[] planeMax)
	{
		return getParent().createPlane(meta, planeMin, planeMax);
	}

	@Override
	public <P extends Plane> P castToTypedPlane(final Plane plane) {
		return getParent().<P> castToTypedPlane(plane);
	}

	// -- Groupable API Methods --

	@Override
	public boolean isSingleFile(final String id) throws FormatException,
		IOException
	{
		return getParent().isSingleFile(id);
	}

	// -- HasFormat API Methods --

	@Override
	public Format getFormat() {
		return getParent().getFormat();
	}

	// -- Contextual API Methods --

	@Override
	public Context getContext() {
		return getParent().getContext();
	}

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
