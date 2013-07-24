/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2013 Open Microscopy Environment:
 *   - Board of Regents of the University of Wisconsin-Madison
 *   - Glencoe Software, Inc.
 *   - University of Dundee
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */
package io.scif.formats;

import io.scif.AbstractFormat;
import io.scif.AbstractWriter;
import io.scif.DefaultMetadata;
import io.scif.FormatException;
import io.scif.Metadata;
import io.scif.Plane;
import io.scif.common.DataTools;
import io.scif.io.RandomAccessOutputStream;
import io.scif.util.FormatTools;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.img.basictypeaccess.array.LongArray;
import net.imglib2.img.basictypeaccess.array.ShortArray;

import org.scijava.plugin.Plugin;

/**
 * Format for Java source code.
 * At the moment, this code just writes a very simple container for pixel data.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/out/JavaWriter.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/out/JavaWriter.java;hb=HEAD">Gitweb</a></dd></dl>
 */
@Plugin(type = JavaFormat.class)
public class JavaFormat extends AbstractFormat {
  
  // -- Format API Methods --

  /*
   * @see io.scif.Format#getFormatName()
   */
  public String getFormatName() {
    return "Java source code";
  }

  /*
   * @see io.scif.Format#getSuffixes()
   */
  public String[] getSuffixes() {
    return new String[]{"java"};
  }

  // -- Nested classes --
  
  /**
   * @author Mark Hiner hinerm at gmail.com
   *
   */
  public static class Writer extends AbstractWriter<DefaultMetadata> {

    // -- Writer API methods --
    
    public void setDest(RandomAccessOutputStream stream, int imageIndex)
      throws FormatException, IOException
    {
      super.setDest(stream, imageIndex);
      if (out.length() == 0) writeHeader(); 
    }
    
    /*
     * @see io.scif.Writer#savePlane(int, int, io.scif.Plane, int, int, int, int)
     */
    public void savePlane(int imageIndex, int planeIndex, Plane plane, int x,
      int y, int w, int h) throws FormatException, IOException
    {
      byte[] buf = plane.getBytes();
      Metadata meta = getMetadata();
      
      checkParams(imageIndex, planeIndex, buf, x, y, w, h);
      if (!isFullPlane(imageIndex, x, y, w, h)) {
        throw new FormatException(
          "JavaWriter does not yet support saving image tiles.");
      }

      // check pixel type
      String pixelType = FormatTools.getPixelTypeString(meta.getPixelType(imageIndex));
      int type = FormatTools.pixelTypeFromString(pixelType);
      if (!DataTools.containsValue(getPixelTypes(), type)) {
        throw new FormatException("Unsupported image type '" + pixelType + "'.");
      }
      int bpp = FormatTools.getBytesPerPixel(type);
      boolean fp = FormatTools.isFloatingPoint(type);
      boolean little =
        Boolean.FALSE.equals(!meta.isLittleEndian(imageIndex));

      // write array
      String varName = "image" + imageIndex + "Plane" + planeIndex;
      Object array = DataTools.makeDataArray(buf, bpp, fp, little);

      out.seek(out.length());
      writePlane(varName, getType(array), w, h);

    }
    
    @Override
    public boolean canDoStacks() { return true; }

    @Override
    public int[] getPixelTypes(String codec) {
      return new int[] {
        FormatTools.INT8,
        FormatTools.UINT8,
        FormatTools.UINT16,
        FormatTools.UINT32,
        FormatTools.INT32,
        FormatTools.FLOAT,
        FormatTools.DOUBLE
      };
    }

    @Override
    public void close() throws IOException {
      if (out != null) writeFooter();
      super.close();
    }
    
    // -- Helper methods --

    protected void writeHeader() throws IOException {
      String className = metadata.getDatasetName().substring(0, metadata.getDatasetName().length() - 5);
      className = className.substring(className.lastIndexOf(File.separator) + 1);

      out.writeLine("//");
      out.writeLine("// " + className + ".java");
      out.writeLine("//");
      out.writeLine("");
      out.writeLine("// Generated by SCIFIO v" + scifio().getVersion());
      out.writeLine("// Generated on " + new Date());
      out.writeLine("");
      out.writeLine("public class " + className + " {");
      out.writeLine("");
    }
    
    protected void writePlane(String varName, ArrayPlus type, int w, int h)
      throws IOException
    {
      int i = 0;
      out.writeLine("  public " + type.label() + "[][] " + varName + " = {");
      for (int y=0; y<h; y++) {
        out.writeBytes("    {");
        for (int x=0; x<w; x++) {
          out.writeBytes(type.value(i++));
          if (x < w - 1) out.writeBytes(", ");
          else out.writeBytes("}");
        }
        if (y < h - 1) out.writeLine(",");
        else out.writeLine("");
      }
      out.writeLine("  };");
      out.writeLine("");
    }

    protected void writeFooter() throws IOException {
      out.writeLine("}");
    }
    
    private ArrayPlus getType(Object array) {
      ArrayPlus type = null;
      if (array instanceof byte[]) {
        type = new ByteArrayPlus((byte[])array);
      }
      else if (array instanceof short[]) {
        type = new ShortArrayPlus((short[])array);
      }
      else if (array instanceof int[]) {
        type = new IntArrayPlus((int[])array);
      }
      else if (array instanceof long[]) {
        type = new LongArrayPlus((long[])array);
      }
      else if (array instanceof float[]) {
        type = new FloatArrayPlus((float[])array);
      }
      else if (array instanceof double[]) {
        type = new DoubleArrayPlus((double[])array);
      }
      return type;
    }
  }
  
  // -- Helper classes --

  private interface ArrayPlus {
    String label();
    String value(int index);
  }
  
  private static class ByteArrayPlus extends ByteArray implements ArrayPlus {
    public ByteArrayPlus(byte[] data) { super(data); }
    public String label() { return "byte"; }
    public String value(int index) { return String.valueOf(getValue(index)); }
  }
  
  private static class IntArrayPlus extends IntArray implements ArrayPlus {
    public IntArrayPlus(int[] data) { super(data); }
    public String label() { return "int"; }
    public String value(int index) { return String.valueOf(getValue(index)); }
  }
  
  private static class ShortArrayPlus extends ShortArray implements ArrayPlus {
    public ShortArrayPlus(short[] data) { super(data); }
    public String label() { return "short"; }
    public String value(int index) { return String.valueOf(getValue(index)); }
  }
  
  private static class LongArrayPlus extends LongArray implements ArrayPlus {
    public LongArrayPlus(long[] data) { super(data); }
    public String label() { return "long"; }
    public String value(int index) { return String.valueOf(getValue(index)); }
  }
  
  private static class FloatArrayPlus extends FloatArray implements ArrayPlus {
    public FloatArrayPlus(float[] data) { super(data); }
    public String label() { return "float"; }
    public String value(int index) { return String.valueOf(getValue(index)); }
  }
  
  private static class DoubleArrayPlus extends DoubleArray implements ArrayPlus {
    public DoubleArrayPlus(double[] data) { super(data); };
    public String label() { return "double"; }
    public String value(int index) { return String.valueOf(getValue(index)); }
  }
}
