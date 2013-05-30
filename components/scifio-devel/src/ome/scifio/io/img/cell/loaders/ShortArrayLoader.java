package ome.scifio.io.img.cell.loaders;

import net.imglib2.img.basictypeaccess.array.ShortArray;
import ome.scifio.Metadata;
import ome.scifio.Reader;
import ome.scifio.common.DataTools;

public class ShortArrayLoader extends AbstractArrayLoader< ShortArray >
{
	public ShortArrayLoader(Reader reader) {
    super(reader);
  }

  @Override
  protected void convertBytes(ShortArray data, byte[] bytes, int planesRead) {
    Metadata meta = reader().getMetadata();
    
    int bpp = meta.getBitsPerPixel(0) / 8;
    int offset = planesRead * (bytes.length / bpp);
    
    int idx = 0;
    
    for (int i=0; i<bytes.length; i+=bpp) {
      data.setValue(offset + idx++, DataTools.bytesToShort(bytes, i, bpp, meta.isLittleEndian(0)));
    }
  }
  
	public ShortArray emptyArray( final int[] dimensions )
	{
		return new ShortArray( countEntities(dimensions) );
	}

	public int getBitsPerElement() {
		return 16;
	}
}
