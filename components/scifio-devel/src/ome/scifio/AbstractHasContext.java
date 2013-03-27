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

  @Override
  public SCIFIO getContext() {
    return context;
  }

  @Override
  public void setContext(final SCIFIO ctx) {
    if (this.context == null) {
      this.context = ctx;
    }
    else {
      throw new UnsupportedOperationException("Context can only be set once.");
    }
  }

  // -- Default toString implementation --

  @Override
  public String toString() {
    return new FieldPrinter(this).toString();
  }

}
