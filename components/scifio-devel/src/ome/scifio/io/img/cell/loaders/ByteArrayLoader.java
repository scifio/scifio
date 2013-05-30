package ome.scifio.io.img.cell.loaders;

import net.imglib2.img.basictypeaccess.array.ByteArray;
import ome.scifio.Reader;

public class ByteArrayLoader extends AbstractArrayLoader< ByteArray >
{
  public ByteArrayLoader (Reader reader) {
    super(reader);
  }

  @Override
  protected void convertBytes(ByteArray data, byte[] bytes, int planesRead) {
    int offset = planesRead * bytes.length;
    for (int i=offset; i < offset + bytes.length; i++) {
      data.setValue(i, bytes[i - offset]);
    }
  }
  
  public ByteArray emptyArray( final int[] dimensions )
  {
    return new ByteArray( countEntities(dimensions) );
  }

  public int getBitsPerElement() {
    return 8;
  }
}
