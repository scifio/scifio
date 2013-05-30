package ome.scifio.io.img.cell.loaders;

import net.imglib2.img.basictypeaccess.array.CharArray;
import ome.scifio.Reader;

public class CharArrayLoader extends AbstractArrayLoader< CharArray >
{
  public CharArrayLoader (Reader reader) {
    super(reader);
  }

  @Override
  protected void convertBytes(CharArray data, byte[] bytes, int planesRead) {
    int offset = planesRead * bytes.length;
    
    for (int i=0; i<bytes.length; i++) {
      data.setValue(offset + i, (char)bytes[i]);
    }
  }
  
  public CharArray emptyArray( final int[] dimensions )
  {
    return new CharArray( countEntities(dimensions) );
  }

  public int getBitsPerElement() {
    return 8;
  }
}
