/**
 * Provides {@link Format} implementations for all file formats that are 
 * supported by default in SCIFIO.
 * <p>
 * The set of default formats are all open formats that can be freely read and
 * written, to facilitate the inclusion of SCIFIO as an image IO library in
 * external projects.
 * </p>
 * 
 * <h3>Changes since Bio-Formats</h3>
 * <ul>
 *  <li>
 *  In Bio-Formats, image input and output was separated by Reader and Writer
 *  classes. Although these classes still exist in SCIFIO, some operations have
 *  been split out (especially from the Reader) to other components. All of these
 *  classes are collected under a single {@link io.scif.Format} as nested
 *  classes.
 *  </li>
 * </ul>
 * 
 * <h3>Future plans</h3>
 * NB: items with an asterisk (*) are of unknown merit and may or may not
 * be implemented, pending discussion.
 * <ul>
 *  <li>
 *  Convert the remaining open source formats (in the scifio component, 
 *  loci.formats.in  and loci.formats.out) to SCIFIO Formats.
 *  </li>
 * </ul>
 * 
 * @author Mark Hiner
 * @see io.scif.Format
 */
package io.scif.formats;