/**
 * Provides a {@link io.scif.Reader} backed {@link net.imglib2.img.cell.CellImg}
 * implementation and supporting classes.
 * <p>
 * These classes dynamically load tiles (Cells) of images as requested, allowing
 * exceedingly large images to be read immediately.
 * </p>
 * <p>
 * The {@link io.scif.io.img.cell.SCIFIOCellCache} is the entry point for loading
 * Cells. Cell positions are requested from the cache, which are then loaded if they
 * do not exist already. The cache can be memory or disk-based.
 * </p>
 * 
 * @author Mark Hiner hinerm at gmail.com
 *
 */
package io.scif.io.img.cell;