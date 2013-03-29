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

import org.scijava.plugin.Plugin;

import ome.scifio.AbstractChecker;
import ome.scifio.AbstractFormat;
import ome.scifio.AbstractMetadata;
import ome.scifio.AbstractParser;
import ome.scifio.AbstractWriter;
import ome.scifio.ByteArrayPlane;
import ome.scifio.ByteArrayReader;
import ome.scifio.Field;
import ome.scifio.Format;
import ome.scifio.FormatException;
import ome.scifio.Plane;
import ome.scifio.SCIFIO;
import ome.scifio.io.RandomAccessInputStream;

/**
 * Tutorial demonstrating defining your own image Format, and how to
 * make that Format available in any context.
 * 
 * @author Mark Hiner
 *
 */
public class T3bCustomFormats {

  // Before looking at the main method, take a look at the SampleFormat defined
  // below.
  public static void main(final String... args) throws FormatException {
    // ------------------------------------------------------------------------
    // COMPARISON WITH BIO-FORMATS 4.X
    // In Bio-Formats 4.X, adding support for a new format required modifying
    // a hard-coded list of readers or writers. This could be a significant
    // barrier for including new formats.
    // In SCIFIO, we allow formats to be discovered automatically via Sezpoz
    // (sezpoz.java.net) or manually added to a context.
    // ------------------------------------------------------------------------
    
    // Let's start by creating a new context as we have in the other tutorials:
    SCIFIO scifio = new SCIFIO();
    
    // ... and a sample image path:
    String sampleImage = "notAnImage.scifiosmpl";
    
    // When the Context was created, it automatically used SezPoz to discover 
    // all available Formats. As SampleFormat below was annotated as a @Plugin
    // it should be available to our context, directly:
    
    Format format = scifio.formats().getFormat(sampleImage);
    System.out.println("SampleFormat found via FormatService: " + (format != null));
    
    // Using the FormatService provides access to a consistent singleton Format within
    // the context.
    
    // Next let's suspend our disbelief and imagine that the SampleFormat was
    // not annotated and thus not discovered automatically when constructing
    // a context:
    
    SampleFormat sFormat = new SampleFormat();
    
    // It may be tempting to call sFormat.setContext at this point to
    // populate its context. But what we really want to do is ensure the
    // context's FormatService knows about our sFormat:
    
    scifio.formats().addFormat(sFormat);
    
    // Now our SampleFormat will be properly contextualized, and
    // this particular instance will serve as a singleton within the
    // FormatService of this context.
    
    // In closing, notice that the SampleFormat we defined lacks any
    // Translator objects. Translators would be defined within a Format
    // and annotated with @DiscoverableTranslator annotations. Translators
    // encode a many : many relationship between Metadata classes, and thus
    // can not be captured in a generic parameter, nor made mandatory.
    // However, a format without translators is fairly useless as no common
    // information can be discerned (e.g. via DatasteMetadata, because there
    // is no format:DatasetMetadata translator).
    // See the TranslatingMetadata tutorial for more information.
  }
  
  /*
   * This is a non-functional Format which adds "support" for a fictional
   * ".scifiosmpl" image type.
   * 
   * Note the annotation: Plugin. All Formats are plugins for the scijava
   * Context, which allows them to be automatically discovered and instantiated
   * as singletons whenever a Context is created.
   * 
   * Contexts also use a special type of plugin, Service, for performing operations
   * within the scope of that context. The FormatService plugin deals with managing
   * Formats within the context.
   *
   * @author Mark Hiner
   */
  @Plugin(type = Format.class)
  public static class SampleFormat extends AbstractFormat {
    // Note that we had to define each class that would be used by this Format.
    // Eventually this process will be simplified, with default implementations
    // for each component, so only components which will be over-written will
    // need to be defined (e.g. there's no reason to define a Writer for a
    // proprietary image format).

    // -- Format API Methods --

    // A lot of work is done for you in the AbstractFormat and Abstact component
    // classes. But you will always need to implement these methods when defining
    // a new Format.
    
    // First we have to declare a name for our Format.
    public String getFormatName() {
      return "Sample data";
    }

    // Then we need to register what suffixes our Format is capable of opening.
    // Note that you shouldn't put a separator ('.') in the extension Strings.
    public String[] getSuffixes() {
      return new String[]{"scifiosmpl"};
    }

    // Finally we specify what classes will be associated with this Format.
    // SCIFIO convention is to implement these as nested classes within
    // the owning Format, so related components are physically stored
    // together. Any component type not declared here will revert to
    // a Default implementation (e.g. ome.scifio.DefaultWriter). You should
    // look at the Default implementations in ome.scifio and to determine
    // which of these are sufficient for your needs.
    // In this tutorial, we will provide examples of each component type,
    // but note that we have not included our Checker in this class list
    // - thus the DefaultChecker implementation will be used by this
    // format (which performas basic extension checking).
    public Class<?>[] getCustomClasses() {
      return new Class<?>[]{Metadata.class,
          Parser.class, Reader.class, Writer.class};
    }
    
    // -- Nested classes --

    // Metadata doesn't have any methods that need to be implemented, it
    // is simply a bag of information.
    public static class Metadata extends AbstractMetadata {

      // The ome.scifio.Field notation flags fields as significant for a
      // Metadata class, and is intended to represent the original state
      // of the metadata as it would be found in the image source.
      // The label tag allows preservation of naming schemes that would
      // be mangled by Java's variable naming practices.
      @Field(label = "Sky color")
      private String color;
      
      public void setColor(String c) { 
        color = c;
      }

      // This method must be implemented for each concrete Metadata class.
      // Essentially, format-specific metadata is assumed to be populated
      // during Parsing or Translation. From that metadata, ImageMetadata
      // information, common to all formats - such as height, width, etc -
      // can be populated here.
      public void populateImageMetadata() {
        color = "orange";
      }
    }

    // The Parser also doesn't have any methods that explicitly
    // need to be implemented. However, the Parser's job is to populate
    // the Metadata objects properly. If Parser#parse is not
    // overriddne, it will simply return an empty Metadata object.
    public static class Parser extends AbstractParser<Metadata> {

      // Here we can populate a metadata object. Note that #parse is overridden
      // with a 1-parameter and 2-parameter version. The latter allows for non-
      // destructive, chain parsing and re-use of a single Metadata object.
      // Also note that whenever you see a chain of overridden signatures,
      // e.g. String > File > RandomAccessInputStream, the RAIS is typically
      // the "authoritative" signature and last to execute. Thus it is the only
      // signature Overridden here.
      public void typedParse(final RandomAccessInputStream stream, final Metadata meta)
          throws IOException, FormatException 
      {
        meta.setColor("blue");
      }
    }

    // Checkers legitimately do not need any methods to be implemented. The
    // Format can answer what suffixes are associated with a given image
    // format, and since each component can reach its parent format, the
    // default Checker implementation can always match suffixes as long as
    // suffixSufficient == true (it is true by default).
    // If the suffix alone is insufficient for determining Format
    // compatibility, that can be set here.
    public static class Checker extends AbstractChecker {

      public Checker() {
        suffixSufficient = true;
        suffixNecessary = true;
      }
    }
    
    // Each reader MUST implement the openPlane method, and must choose
    // a Plane type to return (e.g. ByteArrayPlane or BufferedImagePlane)
    // by extending the appropriate abstract class, or providing its own
    // typed method definitions.
    // Here we extend ByteArrayReader, signifying that this reader will
    // return ByteArrayPlanes.
    public static class Reader extends ByteArrayReader<Metadata> {

      // Any openPlane signature that contains a Plane object should
      // attempt to update that plane (e.g. via plane.populate() calls)
      // and avoid instantiating new planes. This allows a single Plane
      // instance to be reused through many openPlane calls.
      public ByteArrayPlane openPlane(int imageIndex, int planeIndex,
          ByteArrayPlane plane, int x, int y, int w, int h)
          throws FormatException, IOException {
        // update the data by reference
        byte[] bytes = plane.getData();
        Arrays.fill(bytes, 0, bytes.length, (byte)0);
        
        return plane;
      }
      
    }
    
    // Like the Reader, a Writer must implement its savePlane method
    // which writes the provided Plane object to disk. However, the
    // type of Plane is irrelevant for Writers, thanks to the
    // Plane.getBytes() method.
    public static class Writer extends AbstractWriter<Metadata> {

      public void savePlane(int imageIndex, int planeIndex, Plane plane, int x,
          int y, int w, int h) throws FormatException, IOException {
        byte[] bytes = plane.getBytes();
        
        System.out.println(bytes.length);
      }
    }
  }
}
