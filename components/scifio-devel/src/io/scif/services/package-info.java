/**
 * Provides a set of Services (singletons within a given context)
 * for working with SCIFIO objects.
 * 
 * <h3>Changes since Bio-Formats</h3>
 * <ul>
 *  <li>
 *  Removed {@code services.txt}. A text file maintaining the current services 
 *  is prohibitive to external development, as per {@code readers.txt} in 
 *  {@link io.scif}. Instead, all services are now managed through the
 *  scijava-common {@link org.scijava.Context}. New services should be
 *  annotated using {@link io.scif.services.TypedService}.
 *  </li>
 *  <li>
 *  As the old ServiceFactory paradigm has been removed, this package has
 *  been generalized to contain services that plug into the scijava-common context.
 *  New services should follow an interface/implementation paradigm,
 *  and use the {@link org.scijava.service.Service} annotation.
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
 */
package io.scif.services;