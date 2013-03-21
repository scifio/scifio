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
package main.java;

import java.io.IOException;

import org.scijava.Context;

import ome.scifio.FormatException;
import ome.scifio.Reader;
import ome.scifio.SCIFIO;
import ome.scifio.Writer;

/**
 * Tutorial demonstrating use of the Writer component.
 * 
 * @author Mark Hiner
 *
 */
public class SavingImagePlanes {

  public static void main(final String... args) throws FormatException, IOException {
    // In this tutorial, we're going to make our little .fake sample image
    // real. If you look at the FakeFormat source code, you'll notice that
    // it doesn't have a functional Writer, so we'll have to translate
    // to a different Format that can write our fake planes to disk.
    
    Context context = new Context();
    String sampleImage = "8bit-signed&pixelType=int8&sizeZ=3&sizeC=5&sizeT=7&sizeY=50&sizeX=50.fake";

    // We'll need a path to write to
    String outPath = "SCIFIOTutorial.png";
    
    // We'll need a reader for the input image
    Reader reader = context.getService(SCIFIO.class).initializer().initializeReader(sampleImage);
    
    // .. and a writer for the output path
    Writer writer = context.getService(SCIFIO.class).initializer().initializeWriter(sampleImage, outPath);
    
    // Note that these initialize methods are used for convenience.
    // Initializing a reader and a writer requires that you set the source
    // and metadata properly. Also note that the Metadata attached to a writer
    // describes how to interpret the incoming Planes, but may not reflect
    // the image on disk - e.g. if planes were saved in a different order
    // than on the input image. For accurate Metadata describing the saved
    // image, you should re-parse.
    
    // Anyway, now that we have a reader and a writer, we can save all the planes:
    
    for (int i=0; i<reader.getImageCount(); i++) {
      for (int j=0; j<reader.getPlaneCount(i); j++) {
        writer.savePlane(i, j, reader.openPlane(i, j));
      }
    }
    // Note that this code is for illustration purposes only. 
    // A more general solution would need higher level API that could account for larger planes, etc..
    
    // close our components now that we're done
    reader.close();
    writer.close();
    
    // That's it! There should be a new SCIFIOTutorial image in whichever
    // directory you ran this tutorial from.
  }
}
