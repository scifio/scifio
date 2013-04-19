package ome.xml.translation;

import java.util.List;

import net.imglib2.meta.Axes;
import ome.scifio.ImageMetadata;
import ome.scifio.formats.OBFFormat;
import ome.xml.meta.OMEMetadata;
import ome.xml.model.primitives.PositiveFloat;

import org.scijava.Priority;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Plugin;


/**
 * Translator class from {@link ome.scifio.formats.OBFFormat.Metadata} to
 * {@link ome.xml.meta.OMEMetadata}
 * <p>
 * NB: Plugin priority is set to high to be selected over the base
 * {@link ome.scifio.Metadata} translator.
 * </p>
 * 
 * @author Mark Hiner
 */
@Plugin(type = ToOMETranslator.class, priority = Priority.HIGH_PRIORITY,
attrs = {
  @Attr(name = OBFOMETranslator.SOURCE, value = OBFFormat.Metadata.CNAME),
  @Attr(name = OBFOMETranslator.DEST, value = OMEMetadata.CNAME)
})
public class OBFOMETranslator extends ToOMETranslator<OBFFormat.Metadata> {
  
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
