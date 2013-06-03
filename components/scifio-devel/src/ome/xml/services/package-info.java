/**
 * Contains services facilitating the conversion between Metadata
 * and OME-XML.
 * 
 * <h3>Changes since Bio-Formats</h3>
 * <ul>
 *  <li>
 *  The {@link ome.xml.services.OMEXMLService} is now a scijava-common
 *  {@link org.scijava.Context} plugin, and does not need to be instantiated
 *  through a factory implementation.
 *  </li>
 *  <li>
 *  Added an {@link ome.xml.services.OMEXMLMetadataService} to
 *  provide static utility methods for converting between
 *  {@link io.scif.Metadata} and the OME-XML schema.
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
 *
 */
package ome.xml.services;