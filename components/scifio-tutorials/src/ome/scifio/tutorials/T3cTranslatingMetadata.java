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
import java.util.Arrays;

import org.scijava.plugin.Attr;
import org.scijava.plugin.Plugin;

import net.imglib2.display.ColorTable;
import net.imglib2.display.ColorTable8;

import ome.scifio.AbstractTranslator;
import ome.scifio.Format;
import ome.scifio.FormatException;
import ome.scifio.Metadata;
import ome.scifio.SCIFIO;
import ome.scifio.Translator;
import ome.scifio.fake.FakeFormat;

/**
 * Tutorial demonstrating translation between Metadata types.
 * 
 * @author Mark Hiner
 *
 */
public class T3cTranslatingMetadata { 
  
  public static void main(final String... args) throws FormatException, IOException {
    // In the CustomFormats tutorial we demonstrated the process of defining
    // a custom image format and making it available to the SCIFIO framework.
    // We didn't say much about the Translators then, but they are critically
    // important. A format can have any number of Translators, to and from
    // other Metadata types, and at a minimum must define a translator to
    // DatasetMetadata - which provides basic image information, such as
    // height, width, etc. Without translators, a Format is isolated and 
    // can not be converted to other (e.g. open-source) formats.
    
    // As usual, we start by creating a SCIFIO and our trusty sample image.
    SCIFIO scifio = new SCIFIO();
    String sampleImage = "8bit-signed&pixelType=uint8&indexed=true&rgb=3&sizeZ=3&sizeC=3&sizeT=7&sizeY=50.fake";
    
    // First let's get a handle on a compatible Format, and parse the sample
    // image's Metadata
    Format format = scifio.formats().getFormat(sampleImage);
    Metadata input = format.createParser().parse(sampleImage);
    
    // Now that we have some Metadata, let's find the MischeviousTranslator we defined
    // below.
    Translator t = null;
    
    // The translators() method in the SCIFIO service returns a TranslatorService
    // instance, which can be used to find compatible Translators between the
    // provided metadata types. In this case, since our sample translator 
    // goes to and from FakeFormat.Metadata, we provide this type as
    // both parameters to the findTranslator method.
    t = scifio.translators().findTranslator(input, input);
    
    // To try the MischeviousTranslator out, let's get another copy
    // of this image's Metadata.
    Metadata output = format.createParser().parse(sampleImage);
    
    // Then we translate...
    t.translate(input, output);
    
    // ... and observe the results
    System.out.println("100th element of input color table: " +
        ((FakeFormat.Metadata)input).getColorTable().get(0, 100));
    
    System.out.println("100th element of output color table: " +
        ((FakeFormat.Metadata)output).getColorTable().get(0, 100));
    
    // ------------------------------------------------------------------------
    // COMPARISON WITH BIO-FORMATS 4.X
    // In Bio-Formats 4.X, there was a single open-exchange format: OME-TIFF.
    // To convert between image formats, common metadata was stored in
    // loci.formats.CoreMetadata, and format-specific metadata was converted to
    // OME-XML which could then be used to write an OME-TIFF out.
    // In SCIFIO, we provide ome.scifio.DatasetMetadata to record certain
    // common image characteristics, but any number of open-exchange formats
    // could be devised. It would just be a matter of defining translators
    // for converting from other image formats to the open format.
    // OME-XML will still exist as a SCIFIO plug-in to capture the OME-XML
    // schema in SCIFIO Metadata, and Bio-Formats will become a collection of
    // SCIFIO Formats with translators to OME-XML. But now there is room for
    // plug-ins in for disciplines that don't fit the OME-XML schema.
    // ------------------------------------------------------------------------
  }
  
  /*
   * For translation to be as extensible as possible, individual Translators can
   * be discovered by SezPoz. When a Format invokes its findSource or findDest
   * Translator methods it triggers the SezPoz search. This allows individual
   * Formats to be customized via plug-in, and facilitates Translator-only
   * plug-ins that introduce new open-exchange formats.
   * 
   * Note the two annotation attributes: Translator.DEST is used to determine the 
   * format of the input Metadata of this Translator's translate() method,
   * and Translator.SOURCE is a key for the output type. Without these annotations,
   * this Translator could not be returned by Format#findSource or findDest translator.
   * 
   */
  @Plugin(type = Translator.class, attrs = {
    @Attr(name = Translator.DEST, value = FakeFormat.FORMAT_NAME),
    @Attr(name = Translator.SOURCE, value = FakeFormat.FORMAT_NAME)
  })
  public static class MischeviousTranslator
  extends AbstractTranslator<FakeFormat.Metadata, FakeFormat.Metadata>
  {
    
    // -- Translator API methods --
    
    // This method is the actual workhorse of the Translator. Objects are passed
    // by reference to allow non-destructive translation, so multiple
    // translation calls could be invoked in succession to collaboratively
    // populate a single Metadata object.
    // If you prefer to ensure your Metadata is fresh, call the destination's
    // reset() method.
    public void translate(final FakeFormat.Metadata source, final FakeFormat.Metadata destination) {
      super.translate(source, destination);
      
      // And now we're feeling particularly chaotic and decide to translate
      // only a ColorTable filled with useless values.
      ColorTable ct = source.getColorTable();
      byte[][] bytes = new byte[ct.getComponentCount()][ct.getLength()];
      
      for(byte[] b : bytes)
        Arrays.fill(b, (byte)0x2a);
      
      destination.setLut(new ColorTable[]{new ColorTable8(bytes)});
    }
  }
}
