package ome.scifio.io.img.cell.loaders;

public interface SCIFIOArrayLoader< A >
{
  int getBitsPerElement();

  A loadArray( int[] dimensions, long[] min );

  A emptyArray( final int[] dimensions );
}
