/**
 * Provides the interfaces and default implementations for the components of
 * SCIFIO, and the context for instantiating these components.
 * <p>
 * Support for a given image format in SCIFIO is established by implementing
 * the {@link ome.scifio.Format} interface. Each Format consists of six types of
 * components:
 * <ul>
 *  <li>
 *  {@link ome.scifio.Checker} - determines if the {@code Format} can read/write a 
 *  given image.
 *  </li>
 *  <li>
 *  {@link ome.scifio.Metadata} - data structure for all metadata that can be found in 
 *  images of the associated {@code Format}.
 *  </li>
 *  <li>
 *  {@link ome.scifio.Parser} - builds {@code Metadata} objects, without reading
 *  pixel information from the image.
 *  </li>
 *  <li>
 *  {@link ome.scifio.Reader} - uses corresponding {@code Metadata} to produce a 
 *  standard pixel representation, e.g. encapsulated in a {@link Plane}.
 *  </li>
 *  <li>
 *  {@link ome.scifio.Writer} - uses corresponding {@code Metadata} to save pixel
 *  data to an output source of this format's type, e.g. writing to a file on
 *  disk.
 *  </li>
 *  <li>
 *  {@link ome.scifio.Translator} - converts between this format's metadata and another
 *  {@code Metadata} representation. Each individual 
 *  {@code Translator} converts in a single direction. To avoid an NxN
 *  translation problem, typically {@code Translators} are only defined to
 *  and from format-agnostic {@code Metadata} representations.
 *  </li>
 * </ul>
 * </p>
 * <p>
 * The {@link ome.scifio.SCIFIO} class allows contextualization of {@code Format} 
 * (and thus component) creation. All {@code Formats} are discoverable 
 * (see ome.scifio.discovery) via <a href="http://sezpoz.java.net/">SezPoz</a>
 * and convenience methods for instantiating component using this automatic
 * discovery mechanism can also be found in the SCIFIO class. </p>
 * </p>
 * <p>
 * This package also contains {@link ome.scifio.DatasetMetadata} and {@link ome.scifio.ImageMetadata} 
 * interfaces and default implementations. These classes are used as 
 * format-agnostic representations of image metadata. SCIFIO follows the OME
 *  syntax of what is an "image" - in that top-level is really a dataset, which
 * are arbitrary lists of one or more images. Images are the pixel containers,
 * and metadata can exist at both the image and dataset level.
 * </p>
 * <p>
 * The {@code DatasetMetadata} and {@code ImageMetadata} classes
 * encode this hierarchy and provide basic information, such as image
 * dimensionality, color and index flags, etc. Translating format-specific
 * metadata to an agnostic representation creates a common ground for operating
 * on datasets.
 * </p>
 * 
 * <h3>Changes since Bio-Formats</h3>
 * <ul>
 *  <li>
 *  Converting to {@code Format} representations from the Bio-Formats 
 *  {@code Reader}. Both encapsulate the functions to read and write images of a
 *  given format, but in SCIFIO the {@code Format} consists of multiple
 *  single-purpose components to address each part of image IO atomically.
 *  <p>
 *  The intent of breaking this functionality down to components is two-fold:
 *  first, to ease the process of creating new formats by external developers,
 *  by designing each component to operate without consideration for how the
 *  others work. This modularity then facilitates extensibility. For example,
 *  SCIFIO comes with a simple {@code CoreMetadata} class that encapsulates a
 *  baseline of image attributes. To replace this generation with OME-XML could
 *  be as simple as adding a new {@code OMEXML-Translator} class to the
 *  classpath.
 *  </p>
 *  <p>
 *  Note that components are all aware of their {@code Format}, and thus
 *  capable of accessing related components, via the {@link ome.scifio.HasFormat}
 *  interface.
 *  </p>
 *  </li>
 *  <li>
 *  Adding a context ({@code SCIFIO}) object. In Bio-Formats, readers held
 *  state (e.g. the current image index). In SCIFIO, as much as possible,
 *  state is removed from all components and stored in one central location
 *  (the context). This makes SCIFIO much more friendly for using in a
 *  multi-threaded environment, allowing broader incorporation in external
 *  software.
 *  <p>
 *  Note that all potentially context-sensitive components can access the
 *  context in which they were created via the {@link ome.scifio.HasContext} interace.
 *  </li>
 *  <li>
 *  Dataset and Image terminology. In Bio-Formats, a file was a collection of
 *  {@code series} each containing multiple {@code images}. In SCIFIO, we adopt
 *  the OME terminology that you open a {@code dataset} that contains one or
 *  more {@code images}. Each {@code image} has a number of {@code planes}.
 *  <p>
 *  <table>
 *    <tr>
 *      <th>Bio-Formats</th>
 *      <th>SCIFIO</th>
 *    </tr>
 *    <tr>
 *      <td>File</td>
 *      <td>Dataset</td>
 *    </tr>
 *    <tr>
 *      <td>Series</td>
 *      <td>Image</td>
 *    </tr>
 *    <tr>
 *      <td>Image</td>
 *      <td>Plane</td>
 *    </tr>
 *  </table>
 *  </p>
 *  </li>
 *  <li>
 *  Addition of generic parameterization. Because of the prevalence of related
 *  components in individual classes, SCIFIO makes extensive use of generics
 *  to capture these relationships.
 *  <p>
 *  Most of these parameters disappear at the concrete implementation level,
 *  but are present in the interfaces and abstract layers (which is 
 *  representative of the desire for SCIFIO to generically define the image IO
 *  process - with the specifications of Bio-Formats being just one possible
 *  implementation).
 *  </p>
 *  </li>
 *  <li>
 *  <p>
 *  Serialization. The {@link ome.scifio.Metadata} class implements 
 *  {@link java.io.Serializble}. The idea behind metadata serialization is that
 *  parsing the metadata is one of the most expensive processes of image IO.
 *  If metadata can be saved for future sessions, then repeated analysis of
 *  the associated dataset can be, potentially quite significantly, sped up.
 *  </p>
 *  </li>
 *  <li>
 *  Dimensionality changes. Bio-Formats was hard-coded to be 5D (some
 *  permutation of XYZCT). To be fully extensible SCIFIO should be N-D.
 *  The current SCIFIO implementation uses ImgLib2 {@link net.imglib2.meta.Axes} to
 *  get away from the strict 5D representation.
 *  <p>
 *  However, many methods in SCIFIO (especially utility methods) are still
 *  effectively 5D, as they were strictly ported from Bio-Formats code. Thus
 *  this is still an area for refinement. Especially given that the parallel
 *  axis length and type arrays are somewhat confusing, when they could be replaced
 *  by a single object.
 *  </p>
 *  </li>
 * </ul>
 * 
 * <h3>Future plans</h3>
 * NB: items with an asterisk (*) are of unknown merit and may or may not
 * be implemented, pending discussion.
 * <ul>
 *  <li>
 *  Decouple {@link ome.scifio.Reader#openBytes} methods and byte arrays. This requires
 *  the creation of a new {@code Plane} interface that can be specialized
 *  (e.g. via {@code ByteArrayPlane} or {@code BufferedImagePlane}) while
 *  still providing a contractual translation to a byte array.
 *  <p>
 *  This will make these methods significantly more general and require little
 *  to no workaround for any future image format that doesn't explicitly use a
 *  byte[] to represent data.
 *  </p>
 *  <p>
 *  Furthermore, having a {@code Plane} object allows for more testing seams,
 *  and facilitates a tighter bind with color tables. In Bio-Formats, the
 *  color table accessors were in the reader but had no guarantee if a
 *  plane hadn't been read. In SCIFIO, we can eliminate the get8BitLookupTable
 *  and get16BitLookupTable methods completely and just return each plane
 *  as a set of pixel data + (generic) color table.
 *  </p>
 *  </li>
 *  <li>
 *  <p>
 *  ColorTable refactoring. SCIFIO will be converted to use the imglib2 
 *  {@link net.imglib2.display.ColorTable} to represent all ColorTables.
 *  In Bio-Formats there was always a 16-bit or 8-bit color table dichotomy.
 *  Eliminating such a dependency makes the software more flexible and
 *  extensible.
 *  </p>
 *  </li>
 *  <li>
 *  Generic hiding. The introduction of generics to the many components of
 *  SCIFIO is great for encoding their relationship, but at low levels when
 *  writing general image IO code, the final {@code Format} types shouldn't
 *  be required to be known. Thus wildcards are heavily relied upon, which can be
 *  confusing and frustrating - even prohibiting some syntax that one would
 *  reasonably expect to be valid.
 *  <p>
 *  Our intent is to hide the generics at the lowest interface levels, as was
 *  done in {@link net.imglib2.display.ColorTable}. This allows generics to be
 *  built into the middle layers of code and ensure compile-time safety, but
 *  without affecting the two most probable use cases when coding against SCIFIO:
 *  the interface and the concrete implementations.
 *  </p>
 *  </li>
 *  <li>
 *  Add default components. Currently implementing a new {@code Format}
 *  mandates the implementation of all components. However, some formats will
 *  not use all components (e.g. proprietary formats that won't have a 
 *  {@code Writer}) or do not need to go beyond the abstract implementations
 *  (for many {@code Checkers}).
 *  <p>
 *  To solve this and to make it easier for new developers to pick and choose
 *  what they implement, there should be a true "Default" implementation for
 *  each component. This could potentially just be the current abstract layer.
 *  The end goal is simply that one could simply create a {@code Format}
 *  class and it would compile. At that point components could be added as
 *  needed.
 *  </p>
 *  </li>
 *  <li>
 *  <p>
 *  Add sub-resolution API to {@code ImageMetadata}. This feature was added after the
 *  split of SCIFIO off Bio-Formats and has not been reconciled yet. It will
 *  likely involve turning {@code ImageMetadata} into a list as well, and possibly
 *  adding a third index to many methods (imageIndex within the dataset,
 *  planeIndex within the image, sub-resolution index within the plane).
 *  </p>
 *  </li>
 *  <li>
 *  <p>
 *  Cancel early in {@link ome.scifio.Parser#parse}. There should be an {@code active}
 *  flag that can be set on a {@code Parser} instance, with regular polls
 *  set up through runtime of the {@code parse} method. If the flag ever
 *  returns false, the current parsing operation is aborted.
 *  </p>
 *  </li>
 *  <li>
 *  Create a {@code HasSource} style interface. {@code Metadata} objects should
 *  have some sort of mapping back to their {@link RandomAccessInputStream}
 *  object(s).
 *  <p>
 *  Default implementation could be a 2D array, as a list of companion files
 *  for each image in the dataset.
 *  </p>
 *  <p>
 *  Ultimately this interface will be used to generate the information required
 *  for the {@code getUsedSource} equivalent methods from Bio-Formats.
 *  </p>
 *  </li>
 *  <li>
 *  *Use a bit mask for recovering used sources. Related to the
 *  {@code HasSource} interface, when the sources are queried there should be
 *  some standard way to do so, without using an assortment of overridden
 *  methods.
 *  <p>
 *  A bit mask is a simple, logical way to do so, where the bits
 *  correspond to various optional flags. For example, the {@code noPixels}
 *  flag that currently is used in Bio-Formats.
 *  </p>
 *  <p>
 *  Note that bit masks may not be the ideal implementation of this
 *  functionality. It may be better to have some sort of object that wraps
 *  an arbitrary number of flags with a similar effect. The goal is just to
 *  create a standard that will allow for extensibility without breaking API.
 *  </li>
 *  <li>
 *  Create {@code OriginalMetadata} class. Currently both 
 *  {@code DatasetMetadata} and {@code ImageMetadata} maintain
 *  {@code HashTables} for general metadata storage. This should be
 *  standardized into a new class that works more intelligently in the SCIFIO
 *  framework.
 *  <p>
 *  The foundation of this {@code OriginalMetadata} class will be in annotating
 *  using the {@link ome.scifio.Field} annotation and labeling with the index
 *  for each metadata field. This annotation allows original names to be
 *  preserved easily and in a standard way.
 *  </p>
 *  <p>
 *  By making the {@code OriginalMetadata} class implement the {@code Metadata}
 *  interface, we can add translators and set up methods to automatically
 *  translate from common metadata forms to {@code OriginalMetadata}.
 *  </p>
 *  </li>
 *  <li>
 *  *Refine filtering logic. Filtering should only be a factor during the
 *  parsing process. In Bio-Formats there was a filterMetadata flag, but in
 *  SCIFIO this is replaced by the act of translating or not. Right now that's
 *  not very clear, but may be aided by implementing the
 *  {@code OriginalMetadata} class.
 *  <p>
 *  For backwards compatibility we need to ensure that {@code setId} in
 *  Bio-Formats generates both original and OME-XML metadata.
 *  </p>
 *  </li>
 *  <li>
 *  *Generic state cache in the context. Many Bio-Formats classes cached items
 *  such as the last plane read. This behavior may still be useful. To keep
 *  SCIFIO component instances free of state, it would make sense to allow the context
 *  to cache information in a generic way. Then different instances of a given
 *  class created under the same context would behave similarly, and 
 *  potentially allow for speed improvements over a completely stateless
 *  environment.
 *  </li>
 * </ul>
 * 
 * @author Mark Hiner
 * @see ome.scifio.Format
 * @see ome.scifio.SCIFIO
 */
package ome.scifio;