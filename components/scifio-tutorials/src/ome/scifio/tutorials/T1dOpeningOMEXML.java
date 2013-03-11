/*
 * #%L
 * Tutorials for SCIFIO API
 * %%
 * Copyright (C) 2013 Open Microscopy Environment:
 *   - Board of Regents of the University of Wisconsin-Madison
 *   - Glencoe Software, Inc.
 *   - University of Dundee
 * %%
 * To the extent possible under law, the SCIFIO developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 * 
 * See the CC0 1.0 Universal license for details:
 * http://creativecommons.org/publicdomain/zero/1.0/
 * #L%
 */
package ome.scifio.tutorials;

import java.io.IOException;

import ome.scifio.Format;
import ome.scifio.FormatException;
import ome.scifio.Metadata;
import ome.scifio.Parser;
import ome.scifio.SCIFIO;
import ome.scifio.xml.XMLTools;
import ome.xml.meta.OMEMetadata;

/**
 * Tutorial on obtaining OME-XML using SCIFIO.
 * 
 * @author Mark Hiner
 *
 */
public class T1dOpeningOMEXML {
  
  public static void main(final String... args) throws FormatException, IOException {
    // Creation of OME-XML metadata in SCIFIO is accomplished via translation.
    // The OME-XML component is essentially a collection of translators, from
    // specific formats to OME-XML, which define how to extract the OME-XML
    // schema.
    // So, we will need to work with a sample image that has a defined translator
    // to OME-XML. Luckily we already have a tutorial which creates a PNG image
    // for us:
    T1cSavingImagePlanes.main(new String[]{});
    
    // We'll need a context for discovering formats and translators
    SCIFIO scifio = new SCIFIO();
    
    // Now we can reference that image via the path:
    String outPath = "SCIFIOTutorial.png";
    
    // This is the Metadata object we will translate to OMEXML Metadata;
    Metadata meta = null;
    
    // NB: we could use the following line here:
    // Reader reader = context.getService(SCIFIO.class).initializer().initializeReader(sampleImage);
    // which would provide us with an initialized reader. We could then obtain its Metadata
    // for Translation:
    // meta = reader.getMetadata();
    // If we were going to continue to use that Reader for more operations this would
    // be quite reasonable. But if we ONLY want the Metadata, it's really doing more than
    // we need. We just need a Parser:
    
    Format format = scifio.formats().getFormat(outPath);
    Parser parser = format.createParser();
    
    // You can see a more in-depth treatment of individual components in T2a and T2b.
    // For now it's enough to know that Parsers parse metadata from a dataset:
    meta = parser.parse(outPath);
    
    // Now that we have our source Metadata, we will need OME-XML Metadata to translate
    // to:
    
    OMEMetadata omexml = new OMEMetadata();
    omexml.setContext(scifio.getContext());
    
    scifio.translators().translate(meta, omexml);
    
    // Now that we have our OME-XML we can print it:
    String xml = omexml.getRoot().dumpXML();
    System.out.println(XMLTools.indentXML(xml, 3, true));
  }
}
