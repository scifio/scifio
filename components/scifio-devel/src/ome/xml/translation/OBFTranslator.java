package ome.xml.translation;

import io.scif.ImageMetadata;
import io.scif.formats.OBFFormat;

import java.util.List;

import net.imglib2.meta.Axes;
import ome.xml.meta.OMEMetadata;
import ome.xml.model.primitives.PositiveFloat;

import org.scijava.Priority;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Plugin;

/**
 * Container class for translators between OME and OBF formats.
 * 
 * @author Mark Hiner hinerm at gmail.com
 *
 */
public class OBFTranslator {

  /**
   * Translator class from {@link io.scif.formats.OBFFormat.Metadata} to
   * {@link ome.xml.meta.OMEMetadata}
   * <p>
   * NB: Plugin priority is set to high to be selected over the base
   * {@link io.scif.Metadata} translator.
   * </p>
   * 
   * @author Mark Hiner
   */
  @Plugin(type = ToOMETranslator.class, priority = Priority.HIGH_PRIORITY,
      attrs = {
    @Attr(name = OBFOMETranslator.SOURCE, value = OBFFormat.Metadata.CNAME),
    @Attr(name = OBFOMETranslator.DEST, value = OMEMetadata.CNAME)
  })
  public static class OBFOMETranslator extends ToOMETranslator<OBFFormat.Metadata> {

    // -- Translator API methods --

    @Override
    protected void typedTranslate(OBFFormat.Metadata source, OMEMetadata dest) {
      for (int image = 0 ; image != source.getImageCount() ; ++ image)
      {
        ImageMetadata obf = source.get(image) ;

        final String name = obf.getTable().get("Name").toString() ;
        dest.getRoot().setImageName(name, image) ;

        @SuppressWarnings("unchecked")
        final List<Double> lengths = (List<Double>) obf.getTable().get("Lengths") ;

        final double lengthX = Math.abs(lengths.get(0)) ;
        if (lengthX > 0)
        {
          final PositiveFloat physicalSizeX = new PositiveFloat( lengthX / obf.getAxisLength(Axes.X) ) ;
          dest.getRoot().setPixelsPhysicalSizeX(physicalSizeX, image) ;
        }
        final double lengthY = Math.abs(lengths.get(1)) ;
        if (lengthY > 0)
        {
          final PositiveFloat physicalSizeY = new PositiveFloat( lengthY / obf.getAxisLength(Axes.Y) ) ;
          dest.getRoot().setPixelsPhysicalSizeY(physicalSizeY, image) ;
        }
        final double lengthZ = Math.abs(lengths.get(2)) ;
        if (lengthZ > 0)
        {
          final PositiveFloat physicalSizeZ = new PositiveFloat( lengthZ / obf.getAxisLength(Axes.Z) ) ;
          dest.getRoot().setPixelsPhysicalSizeZ(physicalSizeZ, image) ;
        }
      }
    }
  }
}