package ome.scifio.discovery;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;
import ome.scifio.FormatException;
import ome.scifio.Metadata;
import ome.scifio.Translator;

public class TranslatorDiscoverer<M extends Metadata, N extends Metadata>
    implements Discoverer<SCIFIOTranslator, Translator<M, N>> {

  // -- Constants --
  
  protected static final Logger LOGGER =
      LoggerFactory.getLogger(Discoverer.class);
          
  // -- Fields --
          
  private Class<M> metaInClass;
  private Class<N> metaOutClass;
  
  // -- Constructor --

  public TranslatorDiscoverer(Class<M> metaIn, Class<N> metaOut) {
    this.metaInClass = metaIn;
    this.metaOutClass = metaOut;
  }
  
  // -- Discoverer API Methods --
  
  public List<Translator<M, N>> discover() throws FormatException {
    List<Translator<M, N>> transList = new ArrayList<Translator<M, N>>();
    for (@SuppressWarnings("rawtypes")
    final IndexItem<SCIFIOTranslator, Translator> item : Index.load(
        SCIFIOTranslator.class, Translator.class)) {
      if (metaInClass == item.annotation().metaIn()
          && metaOutClass == item.annotation().metaOut()) {
        Translator<M, N> trans = null;
        try {
          trans = getInstance(item);
        }
        catch (InstantiationException e) {
           LOGGER.debug("Failed to instantiate: " + item, e);
        }
        transList.add(trans);
        return transList;
      }
    }
    return null;
  }

  // -- Helper Methods --
  
  @SuppressWarnings("unchecked")
  private Translator<M, N> getInstance(
      @SuppressWarnings("rawtypes") IndexItem<SCIFIOTranslator, Translator> item) throws InstantiationException {
    return item.instance();
  }
}
