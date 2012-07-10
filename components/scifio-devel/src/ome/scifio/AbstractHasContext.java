package ome.scifio;

/**
 * Abstract superclass of all SCIFIO components that are children of
 * ome.scifio.HasContext.
 *
 */
public abstract class AbstractHasContext implements HasContext {

  private transient SCIFIO context;

  // -- Constructor --

  public AbstractHasContext(final SCIFIO ctx) {
    this.context = ctx;
  }

  // -- HasContext API --

  public SCIFIO getContext() {
    return context;
  }

  public void setContext(final SCIFIO ctx) {
    if (this.context == null) {
      this.context = ctx;
    }
    else {
      throw new UnsupportedOperationException("Context can only be set once.");
    }
  }

  // -- Default toString implementation --

  public String toString() {
    return new FieldPrinter(this).toString();
  }

}
