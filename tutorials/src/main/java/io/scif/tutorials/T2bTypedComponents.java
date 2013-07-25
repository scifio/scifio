/*
 * #%L
 * Tutorials for SCIFIO API
 * %%
 * Copyright (C) 2011 - 2013 Open Microscopy Environment:
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
package io.scif.tutorials;

import java.io.IOException;

import io.scif.ByteArrayPlane;
import io.scif.FormatException;
import io.scif.SCIFIO;
import io.scif.formats.FakeFormat;
import io.scif.formats.FakeFormat.Parser;
import io.scif.formats.FakeFormat.Reader;

/**
 * Demonstrates accessing type-specific SCIFIO components.
 * 
 * @author Mark Hiner
 *
 */
public class T2bTypedComponents {

  public static void main(final String... args) throws FormatException, IOException {
    // In IntroToSCIFIO we saw the general case of image opening, but what
    // if we know exactly what kind of image we're working with?

    SCIFIO scifio = new SCIFIO();
    String sampleImage = "8bit-unsigned&pixelType=uint8&sizeZ=3&sizeC=3&rgb=3&indexed=true&sizeT=7&sizeY=50.fake";

    // This time, since we know we have a .fake image, we'll get a handle to the Fake format.
    FakeFormat fakeFormat = scifio.format().getFormatFromClass(FakeFormat.class);

    // Two important points here:
    // 1 - getformatFromClass is overloaded. You can use any component's class
    //     and get back the corresponding Format.
    // 2 - we didn't invoke the FakeFormat's constructor.
    //     new FakeFormat() would have given us a Format instance with no context.
    //     new FakeFormat(scifio) would have given us a Format with the correct context,
    //     but wouldn't update the context's FakeFormat singleton.
    // Formats have no state, so as long as you want a Format that was discovered,
    // you should access it via the desired context. We will discuss manual Format
    // instantiation in the CustomFormats tutorial.

    // Formats provide access to all other components, and with a typed Format
    // you can create typed components:

    FakeFormat.Reader reader = (Reader) fakeFormat.createReader();
    FakeFormat.Parser parser = (Parser) fakeFormat.createParser();

    // Now that we have typed components, we can guarantee the return type
    // for many methods, and access type-specific API:

    FakeFormat.Metadata meta = parser.parse(sampleImage);

    System.out.println("Color table: " + meta.getColorTable(0, 0));

    reader.setMetadata(meta);

    ByteArrayPlane plane = reader.openPlane(0, 0);

    System.out.println("Byte array plane: " + plane.getBytes().length);
  }
}
