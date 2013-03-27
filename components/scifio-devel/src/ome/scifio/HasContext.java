package ome.scifio;

/**
 * Super-interface for all SCIFIO components.
 *
 * @author Mark Hiner
 *
 */
public interface HasContext {

  /**
   * Provides a link back to the context in which this
   * component was created.
   *
   */
  SCIFIO getContext();

  /**
   * For use, once, when the zero-parameter constructor is called.
   * Calls attempting to overwrite a context will throw exceptions.
   */
  void setContext(SCIFIO ctx);

}
