/**
 * Provides wrapping classes for modifying the performance of one or more of
 * a {link ome.scifio.Format}'s components.
 * 
 * <h3>Changes since Bio-Formats</h3>
 * <ul>
 *  <li>
 *  </li>
 * </ul>
 * 
 * <h3>Future plans</h3>
 * NB: items with an asterisk (*) are of unknown merit and may or may not
 * be implemented, pending discussion.
 * <ul>
 *  <li>
 *  Define wrapping at the {@code Format} level. SCIFIO {@code Readers} are not
 *  equivalent to the reader classes in Bio-Formats; the true unit of wrapping
 *  should be the {@code Format} in SCIFIO.
 *  <p>
 *  A {@code FormatWrapper} would allow for individual component wrappers to be
 *  defined and logically tied together. For example, perhaps the
 *  {@code Reader} functionality is modified, and that requires a  parallel
 *  modification to the {@code Metadata}.
 *  </p>
 *  <p>
 *  When developing the Format wrappers, we must be careful not to interfere
 *  with the generic parameterization of the components, and their implicit
 *  contracts that come with having a context and a format.
 *  </p>
 *  </li>
 * </ul>
 * 
 * @author Mark Hiner
 *
 */
package ome.scifio.wrappers;