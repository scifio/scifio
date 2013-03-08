package ome.scifio;

import org.scijava.plugin.SortablePlugin;

/**
 * Abstract super class for HasFormat implementations
 * 
 * @author Mark Hiner
 *
 */
public abstract class AbstractHasFormat extends SortablePlugin implements HasFormat {
  
  private SCIFIO scifio;
  
  // -- HasFormat API --

  public Format getFormat() {
    if (scifio == null) scifio = new SCIFIO(getContext());
    
    return scifio.formats().getFormatFromComponent(getClass());
  }
}
