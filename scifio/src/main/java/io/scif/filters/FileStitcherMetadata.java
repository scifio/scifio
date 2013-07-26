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

package io.scif.filters;

import io.scif.Metadata;
import io.scif.util.FormatTools;
import net.imglib2.meta.AxisType;

import org.scijava.plugin.Attr;
import org.scijava.plugin.Plugin;

/**
 * {@link io.scif.filters.MetadataWrapper} implementation specifically for use
 * with the {@link io.scif.filters.FileStitcher}.
 * 
 * @see io.scif.filters.MetadataWrapper
 * @see io.scif.filters.FileStitcher
 * @author Mark Hiner
 */
@Plugin(type = MetadataWrapper.class, attrs = { @Attr(
	name = FileStitcherMetadata.METADATA_KEY,
	value = FileStitcherMetadata.METADATA_VALUE) })
public class FileStitcherMetadata extends AbstractMetadataWrapper {

	// -- Constants --

	public static final String METADATA_VALUE = "io.scif.filters.FileStitcher";
	// -- Fields --

	boolean noStitch = true;

	// -- Constructors --

	public FileStitcherMetadata() {
		this(null);
	}

	public FileStitcherMetadata(final Metadata metadata) {
		super(metadata);
	}

	// -- ChannelFillerMetadata API Methods --

	/**
	 * @param stitch - Whether stitching is enabled
	 */
	public void setStitching(final boolean stitch) {
		noStitch = !stitch;
	}

	// -- Metadata API Methods --

	/*
	 * @see io.scif.AbstractMetadata#isRGB(int)
	 */
	@Override
	public boolean isRGB(final int imageIndex) {
		return noStitch ? super.isRGB(imageIndex) : unwrap().isRGB(imageIndex);
	}

	/*
	 * @see io.scif.AbstractMetadata#getAxisLength(int, net.imglib2.meta.AxisType)
	 */
	@Override
	public int getAxisLength(final int imageIndex, final AxisType t) {
		return noStitch ? super.getAxisLength(imageIndex, t) : unwrap()
			.getAxisLength(imageIndex, t);
	}

	/*
	 * @see io.scif.AbstractMetadata#getPixelType(int)
	 */
	@Override
	public int getPixelType(final int imageIndex) {
		return noStitch ? super.getPixelType(imageIndex) : unwrap().getPixelType(
			imageIndex);
	}

	/*
	 * @see io.scif.AbstractMetadata#getBitsPerPixel(int)
	 */
	@Override
	public int getBitsPerPixel(final int imageIndex) {
		return noStitch ? super.getBitsPerPixel(imageIndex) : unwrap()
			.getBitsPerPixel(imageIndex);
	}

	/*
	 * @see io.scif.AbstractMetadata#isIndexed(int)
	 */
	@Override
	public boolean isIndexed(final int imageIndex) {
		return noStitch ? super.isIndexed(imageIndex) : unwrap().isIndexed(
			imageIndex);
	}

	/*
	 * @see io.scif.AbstractMetadata#isFalseColor(int)
	 */
	@Override
	public boolean isFalseColor(final int imageIndex) {
		return noStitch ? super.isFalseColor(imageIndex) : unwrap().isFalseColor(
			imageIndex);
	}

	/*
	 * @see io.scif.AbstractMetadata#getThumbSizeX(int)
	 */
	@Override
	public int getThumbSizeX(final int imageIndex) {
		return noStitch ? super.getThumbSizeX(imageIndex) : unwrap().getThumbSizeX(
			imageIndex);
	}

	/*
	 * @see io.scif.AbstractMetadata#getThumbSizeY(int)
	 */
	@Override
	public int getThumbSizeY(final int imageIndex) {
		return noStitch ? super.getThumbSizeY(imageIndex) : unwrap().getThumbSizeY(
			imageIndex);
	}

	/*
	 * @see io.scif.AbstractMetadata#isLittleEndian(int)
	 */
	@Override
	public boolean isLittleEndian(final int imageIndex) {
		return noStitch ? super.isLittleEndian(imageIndex) : unwrap()
			.isLittleEndian(imageIndex);
	}

	/*
	 * @see io.scif.AbstractMetadata#isOrderCertain(int)
	 */
	@Override
	public boolean isOrderCertain(final int imageIndex) {
		return noStitch ? super.isOrderCertain(imageIndex) : unwrap()
			.isOrderCertain(imageIndex);
	}

	/*
	 * @see io.scif.AbstractMetadata#isThumbnailImage(int)
	 */
	@Override
	public boolean isThumbnailImage(final int imageIndex) {
		return noStitch ? super.isThumbnailImage(imageIndex) : unwrap()
			.isThumbnailImage(imageIndex);
	}

	/*
	 * @see io.scif.AbstractMetadata#isInterleaved(int)
	 */
	@Override
	public boolean isInterleaved(final int imageIndex) {
		return noStitch ? super.isInterleaved(imageIndex) : unwrap().isInterleaved(
			imageIndex);
	}

	/**
	 * @param imageIndex
	 * @return
	 */
	public String getDimensionOrder(final int imageIndex) {
		return FormatTools.findDimensionOrder((noStitch ? this : unwrap()),
			imageIndex);
	}

	/*
	 * @see io.scif.AbstractMetadata#getImageCount()
	 */
	@Override
	public int getImageCount() {
		return noStitch ? super.getImageCount() : unwrap().getImageCount();
	}

	// TODO with new group files API
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
