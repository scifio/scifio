package ome.scifio.apng;

import ome.scifio.AbstractFormat;
import ome.scifio.FormatException;
import ome.scifio.SCIFIO;
import ome.scifio.discovery.SCIFIOFormat;

@SCIFIOFormat
public class APNGFormat
  extends
  AbstractFormat<APNGMetadata, APNGChecker, APNGParser, APNGReader, APNGWriter> {

  // -- Constructor --

  public APNGFormat() throws FormatException {
	  this(null);
  }
	
  public APNGFormat(final SCIFIO ctx) throws FormatException {
    super(ctx, APNGMetadata.class, APNGChecker.class, APNGParser.class, APNGReader.class, APNGWriter.class);
  }

}
