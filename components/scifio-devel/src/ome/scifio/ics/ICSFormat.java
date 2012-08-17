package ome.scifio.ics;

import ome.scifio.AbstractFormat;
import ome.scifio.FormatException;
import ome.scifio.SCIFIO;
import ome.scifio.discovery.SCIFIOFormat;

@SCIFIOFormat
public class ICSFormat
  extends
  AbstractFormat<ICSMetadata, ICSChecker, ICSParser, ICSReader, ICSWriter> {

  // -- Constructor --

  public ICSFormat() throws FormatException {
	  this(null);
  }
	
  public ICSFormat(final SCIFIO ctx) throws FormatException {
    super(ctx, ICSMetadata.class, ICSChecker.class, ICSParser.class, ICSReader.class, ICSWriter.class);
  }
}
