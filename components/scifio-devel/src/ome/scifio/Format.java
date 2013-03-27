package ome.scifio;

import java.util.List;

/**
 * Interface for all SciFIO formats.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="">Trac</a>,
 * <a href="">Gitweb</a></dd></dl>
 */
public interface Format<M extends Metadata, C extends Checker<M>, P extends Parser<M>, R extends Reader<M>, W extends Writer<M>>
  extends HasContext, Comparable<Format<?, ?, ?, ?, ?>> {

  // -- Format API methods --

  /** Returns the priority of this format.  Used for comparison to other formats. */
  Double getPriority();

  /**
   * Create the Metadata object associated with this format.
   * @return
   * @throws FormatException
   */
  M createMetadata() throws FormatException;

  /**
   * Create the Checker object associated with this format.
   * @return
   * @throws FormatException
   */
  C createChecker() throws FormatException;

  /**
   * Create the Parser object associated with this format.
   * @return
   * @throws FormatException
   */
  P createParser() throws FormatException;

  /**
   * Creates the Reader object associated with this format.
   * @return
   * @throws FormatException
   */
  R createReader() throws FormatException;

  /**
   * Creates the Writer object associated with this format.
   * @return
   * @throws FormatException
   */
  W createWriter() throws FormatException;

  /**
   * Returns a translator capable of translating from this format's Metadata
   * to the target Metadata class. (this format's Metadata is the source)
   * 
   * @param <N>
   * @param targetMeta
   * @return
   */
  <N extends Metadata> Translator<M, N> findSourceTranslator(Class<N> targetMeta)
    throws FormatException;

  /**
   * Returns a translator capable of translating from the target Metadata to this
   * format's Metadata (this Format's metadata is the destination).
   * 
   * @param <N>
   * @param targetMeta
   * @return
   */
  <N extends Metadata> Translator<N, M> findDestTranslator(Class<N> targetMeta)
    throws FormatException;

  /**
   * Returns a list of all known Translator classes that could potentially
   * translate to or from this Format's Metadata object (e.g., Format to Core
   * and Core to Format translators)
   * 
   * @return
   * @throws FormatException
   */
  List<Class<Translator<?, ?>>> getTranslatorClassList();

  /**
   * Returns the class of the Metadata associated with this format
   * @return
   */
  Class<M> getMetadataClass();

  /**
   * Returns the class of the Checker associated with this format
   * @return
   */
  Class<C> getCheckerClass();

  /**
   * Returns the class of the Parser associated with this format
   * @return
   */
  Class<P> getParserClass();

  /**
   * Returns the class of the Reader associated with this format
   * @return
   */
  Class<R> getReaderClass();

  /**
   * Returns the class of the Writer associated with this format
   * @return
   */
  Class<W> getWriterClass();

}