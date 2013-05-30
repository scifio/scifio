package ome.scifio.io.img.cell.loaders;

import net.imglib2.img.basictypeaccess.array.IntArray;
import ome.scifio.Metadata;
import ome.scifio.Reader;
import ome.scifio.common.DataTools;

public class IntArrayLoader extends AbstractArrayLoader< IntArray >
{
  public IntArrayLoader (Reader reader) {
    super(reader);
  }

  @Override
  protected void convertBytes(IntArray data, byte[] bytes, int planesRead) {
    Metadata meta = reader().getMetadata();
    
    int bpp = meta.getBitsPerPixel(0) / 8;
    int offset = planesRead * (bytes.length / bpp);
    int idx = 0;
    
    for (int i=0; i<bytes.length; i+=bpp) {
      data.setValue(offset + idx++, DataTools.bytesToInt(bytes, i, bpp, meta.isLittleEndian(0)));
    }
  }
  
  public IntArray emptyArray( final int[] dimensions )
  {
    return new IntArray( countEntities(dimensions) );
  }

  public int getBitsPerElement() {
    return 32;
  }
}
