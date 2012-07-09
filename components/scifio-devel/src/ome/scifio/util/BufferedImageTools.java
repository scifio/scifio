package ome.scifio.util;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;

public class BufferedImageTools {
  /**
   * Gets the pixel type of the given image.
   * @return One of the following types:<ul>
   *   <li>FormatReader.INT8</li>
   *   <li>FormatReader.UINT8</li>
   *   <li>FormatReader.INT16</li>
   *   <li>FormatReader.UINT16</li>
   *   <li>FormatReader.INT32</li>
   *   <li>FormatReader.UINT32</li>
   *   <li>FormatReader.FLOAT</li>
   *   <li>FormatReader.DOUBLE</li>
   *   <li>-1 (unknown type)</li>
   * </ul>
   */
  public static int getPixelType(BufferedImage image) {
    final Raster raster = image.getRaster();
    if (raster == null) return -1;
    final DataBuffer buffer = raster.getDataBuffer();
    if (buffer == null) return -1;

    if (buffer instanceof SignedByteBuffer) {
      return FormatTools.INT8;
    }
    else if (buffer instanceof SignedShortBuffer) {
      return FormatTools.INT16;
    }
    else if (buffer instanceof UnsignedIntBuffer) {
      return FormatTools.UINT32;
    }

    int type = buffer.getDataType();
    int imageType = image.getType();
    switch (type) {
      case DataBuffer.TYPE_BYTE:
        return FormatTools.UINT8;
      case DataBuffer.TYPE_DOUBLE:
        return FormatTools.DOUBLE;
      case DataBuffer.TYPE_FLOAT:
        return FormatTools.FLOAT;
      case DataBuffer.TYPE_INT:
        if (imageType == BufferedImage.TYPE_INT_RGB ||
          imageType == BufferedImage.TYPE_INT_BGR ||
          imageType == BufferedImage.TYPE_INT_ARGB)
        {
          return FormatTools.UINT8;
        }
        if (buffer instanceof UnsignedIntBuffer) {
          return FormatTools.UINT32;
        }
        return FormatTools.INT32;
      case DataBuffer.TYPE_SHORT:
        return FormatTools.INT16;
      case DataBuffer.TYPE_USHORT:
        if (imageType == BufferedImage.TYPE_USHORT_555_RGB ||
          imageType == BufferedImage.TYPE_USHORT_565_RGB)
        {
          return FormatTools.UINT8;
        }
        return FormatTools.UINT16;
      default:
        return -1;
    }
  }
}
