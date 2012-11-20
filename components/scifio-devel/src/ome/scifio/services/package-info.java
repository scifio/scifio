/**
 * Provides base interface and factory for creating services that may be needed
 * when working with external libraries (e.g. NetCDF, OME-XML, etc...).
 * 
 * <h3>Changes since Bio-Formats</h3>
 * <ul>
 *  <li>
 *  Removed {@code services.txt}. A text file maintaining the current services 
 *  is prohibitive to external development, as per {@code readers.txt} in 
 *  {@link ome.scifio}. Instead, all services are now discoverable via
 *  <a href="http://sezpoz.java.net/">SezPoz</a>. New services should be
 *  annotated using {@link ome.scifio.discovery.SCIFIOService}.
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
 * @see ome.scifio.discovery.SCIFIOService
 */
package ome.scifio.services;