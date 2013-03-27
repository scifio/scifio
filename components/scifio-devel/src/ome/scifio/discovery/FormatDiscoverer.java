package ome.scifio.discovery;

import java.util.ArrayList;
import java.util.List;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;
import ome.scifio.Checker;
import ome.scifio.Format;
import ome.scifio.FormatException;
import ome.scifio.Metadata;
import ome.scifio.Parser;
import ome.scifio.Reader;
import ome.scifio.Writer;

public class FormatDiscoverer implements
    Discoverer<SCIFIOFormat, Format<? extends Metadata, ? extends Checker<? extends Metadata>, 
        ? extends Parser<? extends Metadata>, ? extends Reader<? extends Metadata>, 
        ? extends Writer<? extends Metadata>>> {

  // -- Discoverer API Methods --
  
  /* Builds a list of all discovered Formats */
  public List<Format<? extends Metadata, ? extends Checker<? extends Metadata>, 
      ? extends Parser<? extends Metadata>, ? extends Reader<? extends Metadata>, 
      ? extends Writer<? extends Metadata>>> discover() throws FormatException {
    
    final List<Format<? extends Metadata, ? extends Checker<? extends Metadata>, 
        ? extends Parser<? extends Metadata>, ? extends Reader<? extends Metadata>, 
        ? extends Writer<? extends Metadata>>> formats = 
        new ArrayList<Format<? extends Metadata, ? extends Checker<? extends Metadata>, 
            ? extends Parser<? extends Metadata>, ? extends Reader<? extends Metadata>, 
            ? extends Writer<? extends Metadata>>>();

    for (final IndexItem<SCIFIOFormat, Format> item : Index
        .load(SCIFIOFormat.class, Format.class)) {
      try {
        @SuppressWarnings("unchecked")
        final Format<? extends Metadata, ? extends Checker<? extends Metadata>, 
            ? extends Parser<? extends Metadata>, ? extends Reader<? extends Metadata>, 
            ? extends Writer<? extends Metadata>> format = item.instance();
        formats.add(format);
      } catch (final InstantiationException e) {
        throw new FormatException(e);
      }
    }

    return formats;
  }
}
