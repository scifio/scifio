/**
 * Provides annotations for marking classes to be discovered via 
 * <a href="http://sezpoz.java.net/">SezPoz</a>, and
 * the classes for discovering them at runtime.
 * <p>
 * For a class to be discovered by SezPoz, it must be annotated. The default
 * annotations provided are:
 * <ul>
 *  <li>{@link SCIFIOFormat}: for annotating {@link ome.scifio.Format} concrete
 *  implementations.</li>
 *  <li>{@link SCIFIOService}: for annotating 
 *  {@link ome.scifio.services.Service} concrete implementations.</li>
 *  <li>{@link SCIFIOTranslator}: for annotating {@link ome.scifio.Translator}
 *  concrete implementations.</li>
 * </ul>
 * </p>
 * <p>
 * Each class of annotation requires a corresponding implementation of the
 * {@link ome.scifio.discovery.Discoverer} interface. Instances of these discoverers can then be
 * used to populate lists of the target type at runtime.
 * </p>
 * <p>
 * NB: for a class to be discoverable, it must have a zero-parameter
 * constructor, and implicitly should have no sense of state.
 * </p>
 * 
 * <h3>Changes since Bio-Formats</h3>
 * <ul>
 *  <li>
 *  Removal of readers.txt (and equivalent systems). SCIFIO does not require
 *  an explicit list of Readers (or services, etc...) as Bio-Formats did.
 *  Requiring an explicit list of classes represents a barrier to external
 *  development, as it's one more file to change and there may
 *  not be an explicit indication of how to find those files  (e.g. if a developer
 *  doesn't know readers.txt exists, it's not obvious they have to change it).
 *  <p>
 *  SCIFIO instead uses SezPoz to annotate and discover classes at runtime.
 *  Using SezPoz aids in extensibility by not requiring every class to be
 *  included in the SCIFIO distribution. This means that new file formats
 *  can be defined and distributed as their own projects or plug-ins, without
 *  worrying about getting their code into the SCIFIO project, and yet be 
 *  immediately available to any software using SCIFIO for image IO.
 *  </p>
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
package ome.scifio.discovery;