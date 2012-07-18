package ome.xml.translation;

import ome.scifio.AbstractMetadata;
import ome.scifio.AbstractTranslator;
import ome.scifio.SCIFIO;
import ome.xml.meta.OMEMetadata;

/**
 * Abstract base class for all ome.scifio.Translators that produce an
 * {@link OMEMetadata} object.
 * 
 * Contains any operations common to all translations involving OMEMetadata
 * (e.g. operations on the root)
 * 
 * @author Mark Hiner
 *
 */
public abstract class OMETranslator<M extends AbstractMetadata>
  extends AbstractTranslator<M, OMEMetadata> {

  // -- Constructor --

  public OMETranslator(final SCIFIO ctx) {
    super(ctx);
  }

  // -- Translator API Methods --

  public void translate(final M source, final OMEMetadata destination) {

  }

}
