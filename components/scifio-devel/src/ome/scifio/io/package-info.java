/**
 * Provides the classes for reading and writing information between
 * image sources.
 * 
 * <h3>Changes since Bio-Formats</h3>
 * <ul>
 *  <li>
 *  Added {@link IStreamAccess} interface. This interface is used as a middle
 *  ground for {@link IRandomAccess} implementations that will be stream based,
 *  and extracts many methods that were injected in an abstract layer in
 *  Bio-Formats.
 *  </li>
 * </ul>
 * 
 * <h3>Future plans</h3>
 * NB: items with an asterisk (*) are of unknown merit and may or may not
 * be implemented, pending discussion.
 * <ul>
 *  <li>
 *  {code IRandomAccess} naming. The {@code IRandomAccess} interface should
 *  define a {@code getName} and {@code setName} method, which will be
 *  implemented in the various handles.
 *  <p>
 *  This value should be chained up to the {@code RandomAccessInputStream} 
 *  {@code toString} method.
 *  </p>
 *  </li>
 *  <li>
 *  Remove id maps from {@link Location}. Should be able to construct with just
 *  the {@link RandomAccessInputStream}.
 *  </li>
 * </ul>
 * 
 * @author Mark Hiner
 *
 */
package ome.scifio.io;