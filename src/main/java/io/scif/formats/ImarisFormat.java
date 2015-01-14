/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.scif.formats;

import io.scif.AbstractFormat;
import io.scif.AbstractWriter;
import io.scif.DefaultMetadata;
import io.scif.Format;
import io.scif.FormatException;
import io.scif.Metadata;
import io.scif.Plane;
import io.scif.Reader;
import io.scif.SCIFIO;
import io.scif.config.SCIFIOConfig;
import io.scif.formats.imaris.ImarisWriter;
import io.scif.io.Location;
import io.scif.io.RandomAccessOutputStream;
import io.scif.util.FormatTools;
import io.scif.util.SCIFIOMetadataTools;
import java.awt.image.ColorModel;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import net.imagej.axis.Axes;
import org.scijava.plugin.Plugin;

/**
 * SCIFIO Format supporting the writer for Imaris 5.5 files
 *
 * @author Henry Pinkard
 */
@Plugin(type = Format.class, name = "Imaris 5.5")
public class ImarisFormat extends AbstractFormat {

   //for testing
   public static void main(String[] args) throws IOException, FormatException {
      final SCIFIO scifio = new SCIFIO();
      String name = "mitosis";
      final String sampleImage = "/Users/henrypinkard/Desktop/" + name + ".tif";
      final String outPath = "/Users/henrypinkard/Desktop/" + name + ".ims";

      final Location l = new Location(scifio.getContext(), outPath);
      if (l.exists()) {
         l.delete();
      }
      final Reader reader = scifio.initializer().initializeReader(sampleImage);
      final io.scif.Writer writer
              = scifio.initializer().initializeWriter(sampleImage, outPath);
      for (int i = 0; i < reader.getImageCount(); i++) {
         for (int j = 0; j < reader.getPlaneCount(i); j++) {
            writer.savePlane(i, j, reader.openPlane(i, j));
         }
      }
      reader.close();
      writer.close();
   }

   @Override
   public String getFormatName() {
      return "Imaris 5.5 files";
   }

   @Override
   protected String[] makeSuffixArray() {
      return new String[]{"ims"};
   }

   public static class Writer extends AbstractWriter<DefaultMetadata> {

      private Metadata metadata;

      private ImarisWriter imsWriter;
      private String path;
      private int bitDepth;
      private long sizeX, sizeY, sizeZ, sizeC, sizeT;
      private double pixelSizeXY, pixelSizeZ;
      private ColorModel colorModel;

      //SCIFIO will internally (1) initialize, (2) save planes, and (3) close the writer
      //during initialization: destination metadata created, writer instance created,
      //writer.setMetadata(destMeta) called, and finally writer.setDest(destination, config);
      @Override
      public void setMetadata(final Metadata meta) throws FormatException {
         metadata = meta;
         //read metadata for writer initialization
         bitDepth = meta.get(0).getBitsPerPixel();
         sizeX = meta.get(0).getAxisLength(Axes.X);
         sizeY = meta.get(0).getAxisLength(Axes.Y);
         sizeZ = Math.max(1, meta.get(0).getAxisLength(Axes.Z));
         sizeC = Math.max(1, meta.get(0).getAxisLength(Axes.CHANNEL));
         sizeT = Math.max(1, meta.get(0).getAxisLength(Axes.TIME));
         if (meta.get(0).getPlaneCount() != sizeZ*sizeC*sizeT) {
            throw new FormatException("Channels * Frames * Z Slices must equal number of planes");
         }  
         //get pixel sizes
         pixelSizeXY = FormatTools.getScale(meta, 0, Axes.X);
         pixelSizeZ = FormatTools.getScale(meta, 0, Axes.Z);

//		if (metadata != null && metadata != meta) {
//			try {
//				metadata.close();
//			}
//			catch (final IOException e) {
//				throw new FormatException(e);
//			}
//		}
//
//		if (out != null) {
//			try {
//				close();
//			}
//			catch (final IOException e) {
//				throw new FormatException(e);
//			}
//		}
//
//		metadata = meta;
//
//		// Check the first pixel type for compatibility with this writer
//		for (int i = 0; i < metadata.getImageCount(); i++) {
//			final int pixelType = metadata.get(i).getPixelType();
//
//			if (!DataTools.containsValue(getPixelTypes(compression), pixelType)) {
//				throw new FormatException("Unsupported image type '" +
//					FormatTools.getPixelTypeString(pixelType) + "'.");
//			}
//		}
      }

      @Override
      public void setDest(final String fileName, final int imageIndex,
              final SCIFIOConfig config) throws FormatException, IOException {
         path = fileName;
         //Imaris writier uses JNI calls rather than output stream to write data,
         //override this method and don't unnecessarily create object
         setDest((RandomAccessOutputStream) null, imageIndex, config);
      }

      @Override
      public void setDest(final RandomAccessOutputStream out, final int imageIndex,
              final SCIFIOConfig config) throws FormatException, IOException {
         colorModel = config.writerGetColorModel();

//		if (metadata == null) throw new FormatException(
//			"Can not set Destination without setting Metadata first.");
//
//		// FIXME
//		// set metadata.datasetName here when RAOS has better id handling
//
//		this.out = out;
//		fps = config.writerGetFramesPerSecond();
//		options = config.writerGetCodecOptions();
//		model = config.writerGetColorModel();
//		compression = config.writerGetCompression();
//		sequential = config.writerIsSequential();
//		SCIFIOMetadataTools.verifyMinimumPopulated(metadata, out);
//		initialized = new boolean[metadata.getImageCount()][];
//		for (int i = 0; i < metadata.getImageCount(); i++) {
//			initialized[i] =
//				new boolean[(int) metadata.get(imageIndex).getPlaneCount()];
//		}
      }

      // If your writer supports a compression type, you can declare that here.
      // Otherwise it is sufficient to return an empty String[]
      @Override
      protected String[] makeCompressionTypes() {
         return new String[0];
      }

      /////////////////
      ///Override these methods to prevent exceptions because acces to fields in AbstractWriter is private rther than protected
      /////////////////
      protected void initialize(final int imageIndex, final long planeIndex,
              final long[] planeMin, final long[] planeMax) throws FormatException,
              IOException {
         //nothing to do
      }

      //have to override this method because access to metadata is private 
      @Override
      public void savePlane(final int imageIndex, final long planeIndex,
              final Plane plane) throws FormatException, IOException {
         final long[] planeMax = metadata.get(imageIndex).getAxesLengthsPlanar();
         final long[] planeMin = new long[planeMax.length];
         savePlane(imageIndex, planeIndex, plane, planeMin, planeMax);
      }

      @Override
      public boolean isInitialized(final int imageIndex, final long planeIndex) {
         return true;
      }
      /////////////////
      /////////////////
      /////////////////

      // Take a provided data plane, of specified dimensionality, and
      // write it to the given indices on disk.
      @Override
      public void writePlane(int imageIndex, long planeIndex, Plane plane,
              long[] planeMin, long[] planeMax) throws FormatException, IOException {
         if (!SCIFIOMetadataTools.wholePlane(imageIndex, metadata, planeMin, planeMax)) {
            throw new FormatException("Imaris writer does not support saving image tiles.");
         }    
         if (imsWriter == null) {
            imsWriter = new ImarisWriter(path, sizeX, sizeY, sizeZ, sizeC, sizeT, pixelSizeXY, pixelSizeZ, bitDepth, null);
         }
                
         int zAxis = plane.getImageMetadata().getAxisIndex(Axes.Z);
         int channelAxis = plane.getImageMetadata().getAxisIndex(Axes.CHANNEL);
         int frameAxis = plane.getImageMetadata().getAxisIndex(Axes.TIME);
         int t, z, c;
         if (frameAxis != -1 && (frameAxis < zAxis || frameAxis < channelAxis) ) {
            throw new FormatException("Unsupported plane ordering: Imaris writer can only write"
                    + "in CZT or ZCT sequence");
         } else {           
            t = (int) (planeIndex / (sizeC * sizeZ));
            if (channelAxis < zAxis) {
               //channels first or no channels
               z = (int) ((planeIndex / sizeC) % sizeZ);
               c = (int) (planeIndex % sizeC);       
            } else {
               //slices first or no slices                              
               c = (int) ((planeIndex / sizeZ) % sizeC);
               z = (int) (planeIndex % sizeZ);
            }
         }
         //Get date and time. Used for image timestamps,
         //which are important for calibration in ims files
         //Date + Time format       
         //"YYYY-MM-DD HH:MM:SS.XXX"
         //TODO: replace with reading from metadata
         String dnt = "2014-01-01 00:00:00.00" + t;        
         //Accepts byte[] or short[] pixels, convert as needed
         Object pixels;
         int pixelType = plane.getImageMetadata().getPixelType();
         if (pixelType == FormatTools.INT8 || pixelType == FormatTools.UINT8) {
            pixels = plane.getBytes();
         } else if (pixelType == FormatTools.INT16 || pixelType == FormatTools.UINT16) {
            byte[] bytes = plane.getBytes();
            short[] shorts = new short[bytes.length / 2];
            ByteBuffer.wrap(bytes).order(plane.getImageMetadata().isLittleEndian() ?
                    ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN).asShortBuffer().get(shorts);
            pixels = shorts;
         }else {
             throw new FormatException("Unsupported pixel type: Imaris writer only "
                     + "supports 8 or 16 bit pixels");
         }
         
         System.out.println(planeIndex + "\t" + c + "\t" + z + "\t" + t);
         imsWriter.addImage(pixels, z, c, t, dnt);
         
      }

      @Override
      public void close() throws IOException {
//         close(false);
         imsWriter.close();
      }
   }
}
