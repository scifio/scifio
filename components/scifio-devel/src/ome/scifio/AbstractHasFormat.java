package ome.scifio;

import org.scijava.plugin.SortablePlugin;

/**
 * Abstract super class for HasFormat implementations
 * 
 * @author Mark Hiner
 *
 */
public abstract class AbstractHasFormat extends SortablePlugin implements HasFormat {
  
  // -- HasFormat API --

  public Format getFormat() {
    return getContext().getService(SCIFIO.class).formats().getFormatFromComponent(getClass());
  }
}
