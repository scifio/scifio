package ome.scifio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract superclass of all SCIFIO Translator components.
 *
 */
public abstract class AbstractTranslator<M extends Metadata, N extends Metadata>
  extends AbstractHasContext implements Translator<M, N> {

  public AbstractTranslator(final SCIFIO ctx) {
    super(ctx);
  }

  // -- Constants --

  protected static final Logger LOGGER =
    LoggerFactory.getLogger(Translator.class);

  // -- Constructors --

  // -- HasFormat API Methods --

  @Override
  @SuppressWarnings("unchecked")
  public Format<M, ?, ?, ?, ?> getFormat() {
    return getContext().getFormatFromTranslator(getClass());
  }
}
