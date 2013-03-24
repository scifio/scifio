/**
 * Provides wrapping classes for modifying the performance of one or more of
 * a {link ome.scifio.Format}'s components.
 * 
 * <h3>Changes since Bio-Formats</h3>
 * <ul>
 *  <li>
 *  Implemented SCIFIO {@link ome.scifio.filters.Filter}s. Filters are designed
 *  to wrap arbitrary classes. An implementation is currently provided for 
 *  Readers: {@link ome.scifio.filters.ReaderFilter}.
 *  <p>
 *  Filters are analogous to the ReaderWrappers of Bio-Formats, but function
 *  a bit differently. In Bio-Formats, you would create a stack of ReaderWrappers
 *  based on what functionality you wanted to add to a given Reader instance.
 *  </p>
 *  <p>
 *  In SCIFIO, the Filters are discoverable plugins (via the scijava-common
 *  Context). When you create a ReaderFilter (e.g. via 
 *  {@link ome.scifio.services.InitializeService#initializeReader}) you get
 *  the whole stack of discovered plugins. Each can be individually configured
 *  to be enabled by default. This allows dynamic extensibility, as new 
 *  Filters can be added to the classpath and automatically discovered
 *  and enabled.
 *  </p>
 *  <p>
 *  The ReaderFilter is then passed around and functions like a normal Reader.
 *  But as plugins are enabled and disabled within the Filter, it automatically
 *  maintains the stack (based on each Filter's priority), obviating the need
 *  for user knowledge of a construction order.
 *  </p>
 *  </li>
 * </ul>
 * 
 * <h3>Future plans</h3>
 * NB: items with an asterisk (*) are of unknown merit and may or may not
 * be implemented, pending discussion.
 * <ul>
 *  <li>
 *  Implement Filters for the remaining component types.
 *  </li>
 * </ul>
 * 
 * @author Mark Hiner
 *
 */
package ome.scifio.filters;