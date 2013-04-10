/*
 * #%L
 * OME SCIFIO package for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2005 - 2013 Open Microscopy Environment:
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
package ome.scifio.filters;

import java.util.Hashtable;

import org.scijava.plugin.Attr;
import org.scijava.plugin.Plugin;

import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import ome.scifio.Metadata;
import ome.scifio.util.FormatTools;

/**
 * {@link ome.scifio.filters.MetadataWrapper} implementation specifically
 * for use with the {@link ome.scifio.filters.FileStitcher}.
 * 
 * @see ome.scifio.filters.MetadataWrapper
 * @see ome.scifio.filters.FileStitcher
 * 
 * @author Mark Hiner
 */
@Plugin(type=MetadataWrapper.class, attrs={
  @Attr(name=FileStitcherMetadata.METADATA_KEY, value=FileStitcherMetadata.METADATA_VALUE)
  })
public class FileStitcherMetadata extends AbstractMetadataWrapper {
  
  // -- Constants --
  
  public static final String METADATA_VALUE = "ome.scifio.filters.FileStitcher";
  // -- Fields --
  
  boolean noStitch = true;
  
  // -- Constructors --
  
  public FileStitcherMetadata() {
    this(null);
  }
  
  public FileStitcherMetadata(Metadata metadata) {
    super(metadata);
  }
  
  // -- ChannelFillerMetadata API Methods --
  
  /**
   * @param stitch - Whether stitching is enabled
   */
  public void setStitching(boolean stitch) {
    noStitch = !stitch;
  }
  
  // -- Metadata API Methods --
  
  /*
   * @see ome.scifio.AbstractMetadata#isRGB(int)
   */
  public boolean isRGB(int imageIndex) {
    return noStitch ? super.isRGB(imageIndex) : unwrap().isRGB(imageIndex);
  }
  
  /*
   * @see ome.scifio.AbstractMetadata#getAxisLength(int, net.imglib2.meta.AxisType)
   */
  public int getAxisLength(int imageIndex, AxisType t) {
    return noStitch ? super.getAxisLength(imageIndex, t) :
      unwrap().getAxisLength(imageIndex, t);
  }

  /*
   * @see ome.scifio.AbstractMetadata#getPixelType(int)
   */
  public int getPixelType(int imageIndex) {
    return noStitch ? super.getPixelType(imageIndex) : unwrap().getPixelType(imageIndex);
  }

  /*
   * @see ome.scifio.AbstractMetadata#getBitsPerPixel(int)
   */
  public int getBitsPerPixel(int imageIndex) {
    return noStitch ? super.getBitsPerPixel(imageIndex) : unwrap().getBitsPerPixel(imageIndex);
  }

  /*
   * @see ome.scifio.AbstractMetadata#isIndexed(int)
   */
  public boolean isIndexed(int imageIndex) {
    return noStitch ? super.isIndexed(imageIndex) : unwrap().isIndexed(imageIndex);
  }

  /*
   * @see ome.scifio.AbstractMetadata#isFalseColor(int)
   */
  public boolean isFalseColor(int imageIndex) {
    return noStitch ? super.isFalseColor(imageIndex) : unwrap().isFalseColor(imageIndex);
  }

  /*
   * @see ome.scifio.AbstractMetadata#getChannelDimLengths(int)
   */
  public int[] getChannelDimLengths(int imageIndex) {
    if (noStitch) return super.getChannelDimLengths(imageIndex);
    if (unwrap().getChannelDimLengths(imageIndex) == null) {
      return new int[] {unwrap().getAxisLength(imageIndex, Axes.CHANNEL)};
    }
    return unwrap().getChannelDimLengths(imageIndex);
  }

  /*
   * @see ome.scifio.AbstractMetadata#getChannelDimTypes(int)
   */
  public String[] getChannelDimTypes(int imageIndex) {
    if (noStitch) return super.getChannelDimTypes(imageIndex);
    if (unwrap().getChannelDimTypes(imageIndex) == null) {
      return new String[] {FormatTools.CHANNEL};
    }
    return unwrap().getChannelDimTypes(imageIndex);
  }

  /*
   * @see ome.scifio.AbstractMetadata#getThumbSizeX(int)
   */
  public int getThumbSizeX(int imageIndex) {
    return noStitch ? super.getThumbSizeX(imageIndex) :
      unwrap().getThumbSizeX(imageIndex);
  }

  /*
   * @see ome.scifio.AbstractMetadata#getThumbSizeY(int)
   */
  public int getThumbSizeY(int imageIndex) {
    return noStitch ? super.getThumbSizeY(imageIndex) :
      unwrap().getThumbSizeY(imageIndex);
  }

  /*
   * @see ome.scifio.AbstractMetadata#isLittleEndian(int)
   */
  public boolean isLittleEndian(int imageIndex) {
    return noStitch ? super.isLittleEndian(imageIndex) :
      unwrap().isLittleEndian(imageIndex);
  }

  /*
   * @see ome.scifio.AbstractMetadata#isOrderCertain(int)
   */
  public boolean isOrderCertain(int imageIndex) {
    return noStitch ? super.isOrderCertain(imageIndex) : unwrap().isOrderCertain(imageIndex);
  }

  /*
   * @see ome.scifio.AbstractMetadata#isThumbnailImage(int)
   */
  public boolean isThumbnailImage(int imageIndex) {
    return noStitch ? super.isThumbnailImage(imageIndex) : unwrap().isThumbnailImage(imageIndex);
  }

  /*
   * @see ome.scifio.AbstractMetadata#isInterleaved(int)
   */
  public boolean isInterleaved(int imageIndex) {
    return noStitch ? super.isInterleaved(imageIndex) :
      unwrap().isInterleaved(imageIndex);
  }

  /**
   * @param imageIndex
   * @return
   */
  public String getDimensionOrder(int imageIndex) {
    return FormatTools.findDimensionOrder((noStitch ? this : unwrap()), imageIndex);
  }

  /*
   * @see ome.scifio.AbstractMetadata#getImageCount()
   */
  public int getImageCount() {
    return noStitch ? super.getImageCount() : unwrap().getImageCount();
  }

  //TODO with new group files API
//  public void setNormalized(boolean normalize) {
//    FormatTools.assertId(getCurrentFile(), false, 2);
//    if (externals == null) reader.setNormalized(normalize);
//    else {
//      for (ExternalSeries s : externals) {
//        for (DimensionSwapper r : s.getReaders()) {
//          r.setNormalized(normalize);
//        }
//      }
//    }
//  }
//    
//    public void setGroupFiles(boolean group) {
//    this.group = group;
//  }
//
//  public boolean isGroupFiles() {
//    return group;
//  }
//
//  public String[] getUsedFiles() {
//    FormatTools.assertId(getCurrentFile(), true, 2);
//
//    if (noStitch) return reader.getUsedFiles();
//
//    // returning the files list directly here is fast, since we do not
//    // have to call initFile on each constituent file; but we can only do so
//    // when each constituent file does not itself have multiple used files
//
//    Vector<String> files = new Vector<String>();
//    for (ExternalSeries s : externals) {
//      String[] f = s.getFiles();
//      for (String file : f) {
//        if (!files.contains(file)) files.add(file);
//      }
//
//      DimensionSwapper[] readers = s.getReaders();
//      for (int i=0; i<readers.length; i++) {
//        try {
//          readers[i].setId(f[i]);
//          String[] used = readers[i].getUsedFiles();
//          for (String file : used) {
//            if (!files.contains(file)) files.add(file);
//          }
//        }
//        catch (FormatException e) {
//          LOGGER.debug("", e);
//        }
//        catch (IOException e) {
//          LOGGER.debug("", e);
//        }
//      }
//    }
//    return files.toArray(new String[files.size()]);
//  }
//
//  public String[] getUsedFiles(boolean noPixels) {
//    return noPixels && noStitch ?
//      reader.getUsedFiles(noPixels) : getUsedFiles();
//  }
//
//  public String[] getSeriesUsedFiles() {
//    return getUsedFiles();
//  }
//
//  public String[] getSeriesUsedFiles(boolean noPixels) {
//    return getUsedFiles(noPixels);
//  }
//
//  public FileInfo[] getAdvancedUsedFiles(boolean noPixels) {
//    if (noStitch) return reader.getAdvancedUsedFiles(noPixels);
//    String[] files = getUsedFiles(noPixels);
//    if (files == null) return null;
//    FileInfo[] infos = new FileInfo[files.length];
//    for (int i=0; i<infos.length; i++) {
//      infos[i] = new FileInfo();
//      infos[i].filename = files[i];
//      try {
//        infos[i].reader = ((DimensionSwapper) reader).unwrap().getClass();
//      }
//      catch (FormatException e) {
//        LOGGER.debug("", e);
//      }
//      catch (IOException e) {
//        LOGGER.debug("", e);
//      }
//      infos[i].usedToInitialize = files[i].endsWith(getCurrentFile());
//    }
//    return infos;
//  }
//
//  public FileInfo[] getAdvancedSeriesUsedFiles(boolean noPixels) {
//    if (noStitch) return reader.getAdvancedSeriesUsedFiles(noPixels);
//    String[] files = getSeriesUsedFiles(noPixels);
//    if (files == null) return null;
//    FileInfo[] infos = new FileInfo[files.length];
//    for (int i=0; i<infos.length; i++) {
//      infos[i] = new FileInfo();
//      infos[i].filename = files[i];
//      try {
//        infos[i].reader = ((DimensionSwapper) reader).unwrap().getClass();
//      }
//      catch (FormatException e) {
//        LOGGER.debug("", e);
//      }
//      catch (IOException e) {
//        LOGGER.debug("", e);
//      }
//      infos[i].usedToInitialize = files[i].endsWith(getCurrentFile());
//    }
//    return infos;
//  }
}
