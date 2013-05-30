package ome.scifio.io.img.cell.loaders;

import net.imglib2.img.basictypeaccess.array.BitArray;
import ome.scifio.Reader;

public class BitArrayLoader extends AbstractArrayLoader< BitArray >
{
  public BitArrayLoader (Reader reader) {
    super(reader);
  }

  @Override
  protected void convertBytes(BitArray data, byte[] bytes, int planesRead) {
    int offset = planesRead * bytes.length * 8;
    
    for (int i=0; i<bytes.length; i++) {
      byte b = bytes[i];
      
      for (int j=0; j<8; j++) {
        int idx = (i * 8) + j;
        data.setValue(offset + idx, (b & 0x01) == 1);
        b = (byte) (b >> 1);
      }
    }
  }
  
  public BitArray emptyArray( final int[] dimensions )
  {
    return new BitArray( countEntities(dimensions) );
  }

  public int getBitsPerElement() {
    return 1;
  }
}
