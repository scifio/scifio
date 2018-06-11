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

/**
 * Provides the interfaces and default implementations for the components of
 * SCIFIO, and the context for instantiating these components.
 * <p>
 * Please refer to the <a href="http://loci.wisc.edu/scifio/faq">FAQ</a> for
 * answers to commonly asked questions.
 * </p>
 * <p>
 * Support for a given image format in SCIFIO is established by implementing the
 * {@link io.scif.Format} interface. Each Format consists of six types of
 * components:
 * </p>
 * <ul>
 * <li>{@link io.scif.Checker} - determines if the {@code Format} can read/write
 * a given image.</li>
 * <li>{@link io.scif.Metadata} - data structure for all metadata that can be
 * found in images of the associated {@code Format}.</li>
 * <li>{@link io.scif.Parser} - builds {@code Metadata} objects, without reading
 * pixel information from the image.</li>
 * <li>{@link io.scif.Reader} - uses corresponding {@code Metadata} to produce a
 * standard pixel representation, e.g. encapsulated in a {@link io.scif.Plane}.
 * </li>
 * <li>{@link io.scif.Writer} - uses corresponding {@code Metadata} to save
 * pixel data to an output source of this format's type, e.g. writing to a file
 * on disk.</li>
 * <li>{@link io.scif.Translator} - converts between two types of Metadata.
 * {@code Translator} converts in a single direction. To avoid an NxN
 * translation problem, typically {@code Translators} are only defined to and
 * from format-agnostic {@code Metadata} representations. Furthermore, only
 * writable Formats require translators to their Metadata type.</li>
 * </ul>
 * <p>
 * The intended workflow in SCIFIO takes an image source through the following
 * steps:
 * </p>
 * <ol>
 * <li>Use each discovered Format's Checker to determine if that Format is
 * compatible with the image source.</li>
 * <li>If a match is found, use a Parser to extract the format-specific Metadata
 * from the image source. Format-specific information is also used at this point
 * to populate the associated ImageMetadata information.</li>
 * <li>Now that we have a Metadata object, it can be attached to a Reader and
 * used to open image Planes.</li>
 * <li>If we want to save these planes to a different Format, we need to use
 * Translators to convert our parsed Metadata to the destination Metadata. This
 * could be done by translating to and from an intermediary - e.g. an open
 * exchange format, like OME-XML. Or, it could be done by simply translating
 * from the source's ImageMetadata to the destination.</li>
 * <li>The translated Metadata is then attached to an appropriate Writer, and
 * Planes opened by the Reader are passed, as desired, to the Writer for saving
 * to an output source.</li>
 * </ol>
 * <p>
 * The {@link io.scif.SCIFIO} class wraps {@link org.scijava.Context} instances
 * and provides convenient access to many services.
 * </p>
 * <p>
 * This package also contains an {@link io.scif.ImageMetadata} interface and
 * default implementation. ImageMetadata is use as a format-agnostic
 * representation of common image attributes. SCIFIO follows the OME syntax of
 * what is an "image" - in that top-level is really a dataset, which are
 * arbitrary lists of one or more images. Images are the pixel containers, and
 * metadata can exist at both the image and dataset level.
 * </p>
 * <h3>Changes since Bio-Formats</h3>
 * <ul>
 * <li>Converting to {@code Format} representations from the Bio-Formats
 * {@code Reader}. Both encapsulate the functions to read and write images of a
 * given format, but in SCIFIO the {@code Format} consists of multiple
 * single-purpose components to address each part of image IO atomically.
 * <p>
 * The intent of breaking this functionality down to components is two-fold:
 * first, to ease the process of creating new formats by external developers, by
 * designing each component to operate without consideration for how the others
 * work. This modularity then facilitates extensibility. For example, SCIFIO
 * comes with a simple {@code ImageMetadata} class that encapsulates a baseline
 * of image attributes. To replace this generation with OME-XML could be as
 * simple as adding a new {@code OMEXML-Translator} class to the classpath.
 * </p>
 * <p>
 * Note that components are all aware of their {@code Format}, and thus capable
 * of accessing related components, via the {@link io.scif.HasFormat} interface.
 * </p>
 * </li>
 * <li>Leveraging the scijava-common Context. In Bio-Formats, readers held state
 * (e.g. the current image index). In SCIFIO, as much as possible, state is
 * removed from all components and stored in one central location (the context).
 * This makes SCIFIO much more friendly for using in a multi-threaded
 * environment, allowing broader incorporation in external software.
 * <p>
 * Note that all potentially context-sensitive components can access the context
 * in which they were created, via the {@link org.scijava.Contextual} interface.
 * </p>
 * <p>
 * Note also that the scijava-common Context provides a standard notion of
 * services. Thus the {@link io.scif.services} package has been heavily
 * reworked.
 * </p>
 * </li>
 * <li>Dataset and Image terminology. In Bio-Formats, a file was a collection of
 * {@code series} each containing multiple {@code images}. In SCIFIO, we adopt
 * the OME terminology that you open a {@code dataset} that contains one or more
 * {@code images}. Each {@code image} has a number of {@code planes}.
 * <table summary="Bio-Formats vs. SCIFIO terminology comparison">
 * <tr>
 * <th>Bio-Formats</th>
 * <th>SCIFIO</th>
 * </tr>
 * <tr>
 * <td>File</td>
 * <td>Dataset</td>
 * </tr>
 * <tr>
 * <td>Series</td>
 * <td>Image</td>
 * </tr>
 * <tr>
 * <td>Image</td>
 * <td>Plane</td>
 * </tr>
 * </table>
 * </li>
 * <li>Addition of generic parameterization. Because of the prevalence of
 * related components in individual classes, SCIFIO makes extensive use of
 * generics to capture these relationships.
 * <p>
 * Most of these parameters disappear at the concrete implementation level, but
 * are present in the interfaces and abstract layers (which is representative of
 * the desire for SCIFIO to generically define the image IO process - with the
 * specifications of Bio-Formats being just one possible implementation).
 * </p>
 * </li>
 * <li>
 * <p>
 * Serialization. The {@link io.scif.Metadata} class implements
 * {@link java.io.Serializable}. The idea behind metadata serialization is that
 * parsing the metadata is one of the most expensive processes of image IO. If
 * metadata can be saved for future sessions, then repeated analysis of the
 * associated dataset can be, potentially quite significantly, sped up.
 * </p>
 * </li>
 * <li>Dimensionality changes. Bio-Formats was hard-coded to be 5D (some
 * permutation of XYZCT). To be fully extensible SCIFIO should be N-D. The
 * current SCIFIO implementation uses ImgLib2 {@link net.imagej.axis.Axes} to
 * get away from the strict 5D representation.
 * <p>
 * However, many methods in SCIFIO (especially utility methods) are still
 * effectively 5D, as they were strictly ported from Bio-Formats code. Thus this
 * is still an area for refinement. Especially given that the parallel axis
 * length and type arrays are somewhat confusing, when they could be replaced by
 * a single object.
 * </p>
 * </li>
 * <li>{@link io.scif.Plane}s. In Bio-Formats, readers always returned byte[],
 * or maybe Objects which were assumed to be BufferedImages. This left very few
 * hooks for manipulation of the resultant data. In SCIFIO we define this
 * relationship via the Plane hierarchy, which ties together the concept of an
 * image plane + ColorTable, and facilitates future extensibility, e.g. via new
 * Plane types.
 * <p>
 * Currently SCIFIO provides implementations for ByteArray and BufferedImage
 * planes, to mirror the behavior of Bio-Formats.
 * </p>
 * </li>
 * </ul>
 * <h3>Future plans</h3> NB: items with an asterisk (*) are of unknown merit and
 * may or may not be implemented, pending discussion.
 * <ul>
 * <li>Translate more formats! The goal is to translate all the open source
 * formats that were supported by Bio-Formats by the end of May.</li>
 * <li>Add default components. Currently implementing a new {@code Format}
 * mandates the implementation of all components. However, some formats will not
 * use all components (e.g. proprietary formats that won't have a
 * {@code Writer}) or do not need to go beyond the abstract implementations (for
 * many {@code Checkers}).
 * <p>
 * To solve this and to make it easier for new developers to pick and choose
 * what they implement, there should be a true "Default" implementation for each
 * component. This could potentially just be the current abstract layer. The end
 * goal is simply that one could simply create a {@code Format} class and it
 * would compile. At that point components could be added as needed.
 * </p>
 * </li>
 * <li>
 * <p>
 * Add sub-resolution API to {@code ImageMetadata}. This feature was added after
 * the split of SCIFIO off Bio-Formats and has not been reconciled yet. It will
 * likely involve turning {@code ImageMetadata} into a list as well, and
 * possibly adding a third index to many methods (imageIndex within the dataset,
 * planeIndex within the image, sub-resolution index within the plane).
 * </p>
 * </li>
 * <li>
 * <p>
 * Cancel early in {@link io.scif.Parser#parse}. There should be an
 * {@code active} flag that can be set on a {@code Parser} instance, with
 * regular polls set up through runtime of the {@code parse} method. If the flag
 * ever returns false, the current parsing operation is aborted.
 * </p>
 * </li>
 * <li>Create a {@code HasSource} style interface. {@code Metadata} objects
 * should have some sort of mapping back to their
 * {@link io.scif.io.RandomAccessInputStream} object(s).
 * <p>
 * Default implementation could be a 2D array, as a list of companion files for
 * each image in the dataset.
 * </p>
 * <p>
 * Ultimately this interface will be used to generate the information required
 * for the {@code getUsedSource} equivalent methods from Bio-Formats.
 * </p>
 * </li>
 * <li>*Use a bit mask for recovering used sources. Related to the
 * {@code HasSource} interface, when the sources are queried there should be
 * some standard way to do so, without using an assortment of overridden
 * methods.
 * <p>
 * A bit mask is a simple, logical way to do so, where the bits correspond to
 * various optional flags. For example, the {@code noPixels} flag that currently
 * is used in Bio-Formats.
 * </p>
 * <p>
 * Note that bit masks may not be the ideal implementation of this
 * functionality. It may be better to have some sort of object that wraps an
 * arbitrary number of flags with a similar effect. The goal is just to create a
 * standard that will allow for extensibility without breaking API.</li>
 * <li>Create {@code OriginalMetadata} class. Currently both {@code Metadata}
 * and {@code ImageMetadata} maintain {@code HashTables} for general metadata
 * storage. This should be standardized into a new class that works more
 * intelligently in the SCIFIO framework.
 * <p>
 * The foundation of this {@code OriginalMetadata} class will be in annotating
 * using the {@link io.scif.Field} annotation and labeling with the index for
 * each metadata field. This annotation allows original names to be preserved
 * easily and in a standard way.
 * </p>
 * <p>
 * By making the {@code OriginalMetadata} class implement the {@code Metadata}
 * interface, we can add translators and set up methods to automatically
 * translate from common metadata forms to {@code OriginalMetadata}.
 * </p>
 * </li>
 * <li>*Refine filtering logic. Filtering should only be a factor during the
 * parsing process. In Bio-Formats there was a filterMetadata flag, but in
 * SCIFIO this is replaced by the act of translating or not. Right now that's
 * not very clear, but may be aided by implementing the {@code OriginalMetadata}
 * class.
 * <p>
 * For backwards compatibility we need to ensure that {@code setId} in
 * Bio-Formats generates both original and OME-XML metadata.
 * </p>
 * </li>
 * <li>*Generic state cache in the context. Many Bio-Formats classes cached
 * items such as the last plane read. This behavior may still be useful. To keep
 * SCIFIO component instances free of state, it would make sense to allow the
 * context to cache information in a generic way. Then different instances of a
 * given class created under the same context would behave similarly, and
 * potentially allow for speed improvements over a completely stateless
 * environment.</li>
 * </ul>
 *
 * @author Mark Hiner
 * @see io.scif.Format
 * @see io.scif.SCIFIO
 */

package io.scif;
