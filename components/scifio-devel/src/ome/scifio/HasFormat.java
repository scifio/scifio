package ome.scifio;

/**
 * Super-interface for SCIFIO components relating to a particular format.
 *
 * @author Mark Hiner
 *
 */
public interface HasFormat {

  /**
   * Provides a link back to the format associated with this component.
   */
  Format<?, ?, ?, ?, ?> getFormat();

}
