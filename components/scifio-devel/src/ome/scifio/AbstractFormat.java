package ome.scifio;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;
import ome.scifio.discovery.Discoverer;
import ome.scifio.discovery.SCIFIOTranslator;
import ome.scifio.discovery.TranslatorDiscoverer;

/**
 * Abstract superclass of all SCIFIO components that implement
 * ome.scifio.Format.
 * 
 */
public abstract class AbstractFormat<M extends Metadata, C extends Checker<M>, P extends Parser<M>, R extends Reader<M>, W extends Writer<M>>
    extends AbstractHasContext implements Format<M, C, P, R, W> {
  
  // -- Constants --

  protected static final Logger LOGGER =
    LoggerFactory.getLogger(Format.class);
  
  // -- Fields --

  private Class<M> metadataClass;
  private Class<C> checkerClass;
  private Class<P> parserClass;
  private Class<R> readerClass;
  private Class<W> writerClass;

  private final List<Class<Translator<?, ?>>> translatorClassList = new ArrayList<Class<Translator<?, ?>>>();

  private final Double priority = 0.0;

  // -- Constructor --

  public AbstractFormat(final SCIFIO ctx, Class<M> mClass, Class<C> cClass, Class<P> pClass, Class<R> rClass, Class<W> wClass ) throws FormatException {
    super(ctx);
    this.metadataClass = mClass;
    this.checkerClass = cClass;
    this.parserClass = pClass;
    this.readerClass = rClass;
    this.writerClass = wClass;
    findTranslatorClassList();
  }

  // -- Format API Methods --

  /* @see Format#getPriority() */
  public Double getPriority() {
    return this.priority;
  }

  /* @see Format#createMetadata() */
  public M createMetadata() throws FormatException {
    return createContextualObject(this.getMetadataClass());
  }

  /* @see Format#createChecker() */
  public C createChecker() throws FormatException {
    return createContextualObject(this.getCheckerClass());
  }

  /* @see Format#createParser() */
  public P createParser() throws FormatException {
    return createContextualObject(this.getParserClass());
  }

  /* @see Format#createReader() */
  public R createReader() throws FormatException {
    return createContextualObject(this.getReaderClass());
  }

  /* @see Format#createWriter() */
  public W createWriter() throws FormatException {
    return createContextualObject(this.getWriterClass());
  }

  /* @see Format#getMetadataClass() */
  public Class<M> getMetadataClass() {
    return metadataClass;
  }

  /* @see Format#getCheckerClass() */
  public Class<C> getCheckerClass() {
    return checkerClass;
  }

  /* @see Format#getParserClass() */
  public Class<P> getParserClass() {
    return parserClass;
  }

  /* @see Format#getReaderClass() */
  public Class<R> getReaderClass() {
    return readerClass;
  }

  /* @see Format#getWriteerClass() */
  public Class<W> getWriterClass() {
    return writerClass;
  }

  /* @see Format#findSourceTranslator() */
  public <N extends Metadata> Translator<M, N> findSourceTranslator(
      final Class<N> targetMeta) throws FormatException {
      return findTranslator(metadataClass, targetMeta);
  }

  /* @see Format#findDestTranslator() */
  public <N extends Metadata> Translator<N, M> findDestTranslator(
      final Class<N> targetMeta) throws FormatException {
    return findTranslator(targetMeta, metadataClass);
  }

  /* @see Format#getTranslatorclassList() */
  public List<Class<Translator<?, ?>>> getTranslatorClassList() {
    return translatorClassList;
  }

  // -- Helper Methods --

  /**
   * Populates the list of Translators associated with this Format
   */
  @SuppressWarnings("unchecked")
  private List<Class<Translator<?, ?>>> findTranslatorClassList()
      throws FormatException {
    final List<Class<Translator<?, ?>>> translatorList = new ArrayList<Class<Translator<?, ?>>>();
    for (@SuppressWarnings("rawtypes")
    final IndexItem<SCIFIOTranslator, Translator> item : Index.load(
        SCIFIOTranslator.class, Translator.class)) {
      if (metadataClass == item.annotation().metaIn()
          || metadataClass == item.annotation().metaOut()) {
        Class<Translator<?, ?>> trans;
        try {
          trans = (Class<Translator<?, ?>>) Class.forName(item
              .className());
          translatorList.add(trans);
        } catch (final ClassNotFoundException e) {
          throw new FormatException(e);
        }
      }
    }
    return translatorList;
  }

  /**
   * Returns a SCIFIO component from its object. Also sets its context based
   * on the current context.
   * 
   * @param <T>
   * @param c
   * @return
   * @throws FormatException
   */
  private <T extends HasContext> T createContextualObject(final Class<T> c)
      throws FormatException {
    final T t = createObject(c);
    t.setContext(this.getContext());
    return t;
  }

  /**
   * Returns an instance of an object from its Class
   * 
   * @param <T>
   * @param c
   * @return
   * @throws FormatException
   */
  private <T> T createObject(final Class<T> c) throws FormatException {
    try {
      return c.newInstance();
    } catch (InstantiationException e) {
      throw new FormatException(e);
    } catch (IllegalAccessException e) {
      throw new FormatException(e);
    }
  }
  
  /*
   * Returns a translator object translating from metaIn to metaOut
   */
  private <S extends Metadata, T extends Metadata> Translator<S, T> 
  findTranslator(final Class<S> metaIn, final Class<T> metaOut) throws FormatException {
    Discoverer<SCIFIOTranslator, Translator<S, T>> disc = 
        new TranslatorDiscoverer<S, T>(metaIn, metaOut);
    Translator<S, T> translator = disc.discover().get(0);
    translator.setContext(this.getContext());
    return translator;
  }

  // -- Comparable API Methods --
  
  public int compareTo(final Format<?, ?, ?, ?, ?> format) {
    return getPriority().compareTo(format.getPriority());
  }
}
