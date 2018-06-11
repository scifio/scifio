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

import io.scif.Format;
import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.Metadata;
import io.scif.Plane;
import io.scif.Reader;
import io.scif.config.SCIFIOConfig;
import io.scif.io.RandomAccessInputStream;

import java.io.File;
import java.io.IOException;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;

import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
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

	private final Class<? extends MetadataWrapper> metaClass;

	@Parameter
	private PluginService pluginService;

	// -- Constructor --

	public AbstractReaderFilter() {
		this(null);
	}

	public AbstractReaderFilter(
		final Class<? extends MetadataWrapper> metaClass)
	{
		super(Reader.class);
		this.metaClass = metaClass;
	}

	// -- AbstractReaderFilter API Methods --

	/**
	 * Allows code to be executed regardless of which {@link #setSource} signature
	 * is called.
	 *
	 * @param source - Lowest common denominator of arguments in the
	 *          {@code setSource} series.
	 * @throws IOException
	 */
	protected void setSourceHelper(final Location source,
		final SCIFIOConfig config) throws IOException
	{
		final Location filterSource = getMetadata() == null ? null : getMetadata()
			.getSourceLocation();

		if (filterSource == null || !filterSource.equals(source)) {
			setMetadata(getParent().getMetadata());
		}
	}

	/**
	 * Allows code to be executed regardless of which {@link #openPlane} signature
	 * is called.
	 */
	protected void openPlaneHelper() {

	}

	/**
	 * Allows code to be executed regardless of which {@link #readPlane} signature
	 * is called.
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
	public Class<?> target() {
		return io.scif.Reader.class;
	}

	@Override
	public void setParent(final Object parent) {
		super.setParent(parent);

		final Reader r = (Reader) parent;

		if (metaClass != null) {
			MetadataWrapper wrapper = null;
			try {
				wrappedMeta = wrapper = metaClass.newInstance();
				getContext().inject(wrapper);
				wrapper.wrap(r.getMetadata());
			}
			catch (final InstantiationException e) {
				log().error("Failed to create MetadataWrapper of type: " + metaClass,
					e);
			}
			catch (final IllegalAccessException e) {
				log().error("Failed to create MetadataWrapper of type: " + metaClass,
					e);
			}
		}
		else {
			// No Filter-specific wrapper found
			wrappedMeta = r.getMetadata();
		}
	}

	@Override
	public boolean isCompatible(final Class<?> c) {
		return Reader.class.isAssignableFrom(c);
	}

	// -- Reader API Methods --

	@Override
	public Plane openPlane(final int imageIndex, final long planeIndex)
		throws FormatException, IOException
	{
		return openPlane(imageIndex, planeIndex, new SCIFIOConfig());
	}

	@Override
	public Plane openPlane(final int imageIndex, final long planeIndex,
		final Interval bounds) throws FormatException, IOException
	{
		return openPlane(imageIndex, planeIndex, bounds, new SCIFIOConfig());
	}

	@Override
	public Plane openPlane(final int imageIndex, final long planeIndex,
		final Plane plane) throws FormatException, IOException
	{
		return openPlane(imageIndex, planeIndex, plane, new SCIFIOConfig());
	}

	@Override
	public Plane openPlane(final int imageIndex, final long planeIndex,
		final Plane plane, final Interval bounds) throws FormatException,
		IOException
	{
		return openPlane(imageIndex, planeIndex, plane, bounds, new SCIFIOConfig());
	}

	@Override
	public Plane openPlane(final int imageIndex, final long planeIndex,
		final SCIFIOConfig config) throws FormatException, IOException
	{
		openPlaneHelper();
		return getParent().openPlane(imageIndex, planeIndex, config);
	}

	@Override
	public Plane openPlane(final int imageIndex, final long planeIndex,
		final Interval bounds, final SCIFIOConfig config) throws FormatException,
		IOException
	{
		openPlaneHelper();
		return getParent().openPlane(imageIndex, planeIndex, bounds, config);
	}

	@Override
	public Plane openPlane(final int imageIndex, final long planeIndex,
		final Plane plane, final SCIFIOConfig config) throws FormatException,
		IOException
	{
		openPlaneHelper();
		return getParent().openPlane(imageIndex, planeIndex, plane, config);
	}

	@Override
	public Plane openPlane(final int imageIndex, final long planeIndex,
		final Plane plane, final Interval bounds, final SCIFIOConfig config)
		throws FormatException, IOException
	{
		openPlaneHelper();
		return getParent().openPlane(imageIndex, planeIndex, plane, bounds, config);
	}

	@Override
	public int fileGroupOption(final Location id) throws FormatException,
		IOException
	{
		return getParent().fileGroupOption(id);
	}

	@Override
	public Location getCurrentFile() {
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
	public void setSource(final Location loc) throws IOException {
		getParent().setSource(loc);
		setSourceHelper(loc, new SCIFIOConfig());
	}

	@Override
	public void setSource(final DataHandle<Location> handle) throws IOException {
		getParent().setSource(handle);
		setSourceHelper(handle.get(), new SCIFIOConfig());
	}

	@Override
	public void setSource(final Location loc, final SCIFIOConfig config)
		throws IOException
	{
		getParent().setSource(loc, config);
		setSourceHelper(loc, config);
	}

	@Override
	public void setSource(final DataHandle<Location> handle,
		final SCIFIOConfig config) throws IOException
	{
		getParent().setSource(handle, config);
		setSourceHelper(handle.get(), config);
	}

	@Override
	public void close(final boolean fileOnly) throws IOException {
		getParent().close(fileOnly);
		if (wrappedMeta != null) {
			wrappedMeta.close(fileOnly);
			wrappedMeta = null;
		}
		if (!fileOnly) cleanUp();
	}

	@Override
	public void close() throws IOException {
		close(false);
	}

	@Override
	public Plane readPlane(final DataHandle<Location> s, final int imageIndex,
		final long[] planeMin, final long[] planeMax, final Plane plane)
		throws IOException
	{
		readPlaneHelper();
		return getParent().readPlane(s, imageIndex, bounds, plane);
	}

	@Override
	public Plane readPlane(final DataHandle<Location> s, final int imageIndex,
		final Interval bounds, final int scanlinePad, final Plane plane)
		throws IOException
	{
		readPlaneHelper();
		return getParent().readPlane(s, imageIndex, bounds, scanlinePad, plane);
	}

	@Override
	public long getPlaneCount(final int imageIndex) {
		return getParent().getPlaneCount(imageIndex);
	}

	@Override
	public int getImageCount() {
		return getParent().getImageCount();
	}

	@Override
	public Plane createPlane(final Interval bounds) {
		return getParent().createPlane(bounds);
	}

	@Override
	public Plane createPlane(final ImageMetadata meta, final Interval bounds) {
		return getParent().createPlane(meta, bounds);
	}

	@Override
	public <P extends Plane> P castToTypedPlane(final Plane plane) {
		return getParent().<P> castToTypedPlane(plane);
	}

	// -- Groupable API Methods --

	@Override
	public boolean isSingleFile(final Location id) throws FormatException,
		IOException
	{
		return getParent().isSingleFile(id);
	}

	// -- HasFormat API Methods --

	@Override
	public Format getFormat() {
		return getParent().getFormat();
	}

	@Override
	public String getFormatName() {
		return getParent().getFormatName();
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

	/*
	 * Returns true if this filter's metdata can be cast to
	 * ChannelFillerMetadata
	 */
	protected boolean metaCheck() {
		final Metadata meta = getMetadata();

		return metaClass.isAssignableFrom(meta.getClass());
	}

	/**
	 * Helper method that is always called by the {@link #close} method, if the
	 * {@code fileOnly} flag is false.
	 */
	protected void cleanUp() throws IOException {
		// No-op
	}

	protected Interval planarBounds(final int imageIndex) {
		final Interval bounds = //
			new FinalInterval(getMetadata().get(imageIndex).getAxesLengthsPlanar());
		return bounds;
	}
}
