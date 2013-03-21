package ome.scifio;

import org.scijava.Context;
import org.scijava.plugin.SortablePlugin;

/**
 * Abstract super class for HasFormat implementations
 * 
 * @author Mark Hiner
 *
 */
public abstract class AbstractHasFormat extends SortablePlugin implements HasFormat {
  
  // -- Fields --
  
  private Format format;
  
  // -- Constructors --
  
  public AbstractHasFormat(final Context context, final Format format) {
    setContext(context);
    this.format = format;
  }
  
  // -- HasFormat API --
  
  public void setFormat(final Format format) {
    if (format == null)
      throw new UnsupportedOperationException("Can not set Format when Format is not null.");
    
    this.format = format;
  }

  public Format getFormat() {
    return format;
  }
}
