/**
 * Provides the flattened OME-XML schema structures for storing and
 * retrieving metadata, and a wrapping {@link ome.scifio.Metadata}
 * implementation.
 * 
 * <h3>Changes since Bio-Formats</h3>
 * <ul>
 *  <li>
 *  Some of the classes in this package used to exist in a 
 *  {@link loci.formats.ome} package. They have been extracted into an
 *  {@code ome.xml} package name to facilitate the eventual split of OME-XML
 *  and SCIFIO. SCIFIO is not bound to the OME-XML model, so instead OME-XML
 *  will be a SCIFIO extension which defines a new {@link Metadata} 
 *  implementation and the corresponding {@link Translator} classes for the 
 *  base SCIFIO formats.
 *  <p>
 *  By extension, Bio-Formats will become a SCIFIO plug-in
 *  on top of the OME-XML and SCIFIO libraries, defining proprietary format
 *  translators and metadata to OME-XML.
 *  </p>
 *  </li>
 *  <li>
 *  Because of the split between SCIFIO and OME-XML classes, the Bio-Formats
 *  tools class was split into {@link ome.xml.meta.OMEXMLMetadataTools} and
 *  {@link ome.scifio.util.SCIFIOMetadataTools}.
 *  </li>
 * </ul>
 * 
 * <h3>Future plans</h3>
 * NB: items with an asterisk (*) are of unknown merit and may or may not
 * be implemented, pending discussion.
 * <ul>
 *  <li>
 *  </li>
 * </ul>
 * 
 * @author Mark Hiner
 * @see ome.xml.meta.IMetadata
 * @see ome.xml.meta.OMEXMLMetadata
 */
package ome.xml.meta;